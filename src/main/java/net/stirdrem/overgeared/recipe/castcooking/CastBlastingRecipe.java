package net.stirdrem.overgeared.recipe.castcooking;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.recipe.ModRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class CastBlastingRecipe extends BlastingRecipe {

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastBlastingRecipe(ResourceLocation id, String group, CookingBookCategory category,
                               ItemStack result, float xp, int time,
                               Map<String, Double> reqMaterials, String toolType, boolean needPolishing) {
        super(id, group, category,
                Ingredient.of(ModItems.CLAY_TOOL_CAST, ModItems.NETHER_TOOL_CAST),
                result, xp, time);
        this.requiredMaterials = reqMaterials;
        this.toolType = toolType;
        this.needPolishing = needPolishing;
    }

    public static Map<String, Double> readMaterials(CompoundTag tag) {
        Map<String, Double> map = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_DOUBLE)) {
                map.put(key, tag.getDouble(key));
            } else if (tag.contains(key, Tag.TAG_INT)) {
                map.put(key, (double) tag.getInt(key));
            }
        }
        return map;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        // Build the same NBT tag JEI uses
        CompoundTag tag = new CompoundTag();
        tag.putString("ToolType", toolType);

        CompoundTag mats = new CompoundTag();
        double total = 0;
        for (var entry : requiredMaterials.entrySet()) {
            String mat = entry.getKey();
            double amt = entry.getValue();
            total += amt;
            mats.putDouble(mat, amt);
        }

        tag.put("Materials", mats);
        tag.putDouble("Amount", total);
        tag.putDouble("MaxAmount", total);

        // Create cast stacks with NBT
        ItemStack firedCast = new ItemStack(ModItems.CLAY_TOOL_CAST);
        firedCast.setTag(tag.copy());

        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST);
        netherCast.setTag(tag.copy());

        // Report them as the recipe input
        list.add(Ingredient.of(firedCast, netherCast));

        return list;
    }

    @Override
    public boolean matches(Container inv, Level world) {
        ItemStack input = inv.getItem(0);
        if (!(input.getItem() instanceof ToolCastItem)) return false;
        CompoundTag tag = input.getTag();
        if (tag == null || !tag.contains("Materials")) return false;
        if (!toolType.equals(tag.getString("ToolType").toLowerCase(Locale.ROOT))) return false;
        if (tag.contains("Amount") && tag.getFloat("Amount") <= 0) return false;
        if (!tag.contains("Amount")) return false;

        Map<String, Double> materials = readMaterials(tag.getCompound("Materials"));

        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase(Locale.ROOT);
            double needed = entry.getValue();
            double available = materials.getOrDefault(material, 0.0);
            if (available < needed) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        ItemStack input = inv.getItem(0);

        // Copy the cast itself
        ItemStack cast = input.copy();
        CompoundTag castTag = cast.getOrCreateTag();


        // Build the real result item
        ItemStack result = super.assemble(inv, registryAccess);

        CompoundTag resultTag = result.getOrCreateTag();

        // Transfer quality
        if (input.hasTag() && input.getTag().contains("Quality")) {
            String q = input.getTag().getString("Quality");
            if (!q.equals("none")) {
                resultTag.putString("ForgingQuality", q);
            }
        }

        // Polishing flag
        if (needPolishing) {
            resultTag.putBoolean("Polished", false);
        }

        // Heated result
        resultTag.putBoolean("Heated", true);

        // Creator
        if (input.hasCustomHoverName() && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            resultTag.putString("Creator", input.getHoverName().getString());
        }

        // Store output INSIDE the cast
        castTag.put("Output", result.save(new CompoundTag()));
        castTag.put("Materials", new CompoundTag());
        // Mark cast as filled / heated
        castTag.putBoolean("Heated", true);
        if (cast.isDamageableItem()) {
            if (cast.getDamageValue() + 1 >= cast.getMaxDamage()) {
                cast.shrink(1);
                return result;
            } else {
                cast.setDamageValue(cast.getDamageValue() + 1);
            }
        }
        return cast;
    }

    public Map<String, Double> getMaterialInputs() {
        return requiredMaterials;
    }

    public Map<String, Double> getRequiredMaterials() {
        return requiredMaterials;
    }

    public String getToolType() {
        return toolType;
    }

    public boolean requiresPolishing() {
        return needPolishing;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeType.BLASTING;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CAST_BLASTING;
    }

    public static class Serializer implements RecipeSerializer<CastBlastingRecipe> {
        public static final CastBlastingRecipe.Serializer INSTANCE = new CastBlastingRecipe.Serializer();

        @Override
        public CastBlastingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CookingBookCategory category = CookingBookCategory.MISC;

            JsonObject inputObj = GsonHelper.getAsJsonObject(json, "input");
            Map<String, Double> reqMaterials = new HashMap<>();
            inputObj.entrySet().forEach(e -> reqMaterials.put(e.getKey(), e.getValue().getAsDouble()));

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            float xp = GsonHelper.getAsFloat(json, "experience", 0f);
            int time = GsonHelper.getAsInt(json, "cookingtime", 200);

            String toolType = GsonHelper.getAsString(json, "tool_type").toLowerCase(Locale.ROOT);

            boolean needPolishing = GsonHelper.getAsBoolean(json, "need_polishing", false);

            return new CastBlastingRecipe(id, group, category, result, xp, time, reqMaterials, toolType, needPolishing);
        }

        @Override
        public CastBlastingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CookingBookCategory category = CookingBookCategory.MISC;
            int size = buf.readInt();
            Map<String, Double> reqMaterials = new HashMap<>();
            for (int i = 0; i < size; i++) {
                reqMaterials.put(buf.readUtf(), buf.readDouble());
            }
            ItemStack result = buf.readItem();
            float xp = buf.readFloat();
            int time = buf.readVarInt();
            String toolType = buf.readUtf();
            boolean needPolish = buf.readBoolean();

            return new CastBlastingRecipe(id, group, category, result, xp, time, reqMaterials, toolType, needPolish);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CastBlastingRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeInt(recipe.requiredMaterials.size());
            recipe.requiredMaterials.forEach((k, v) -> {
                buf.writeUtf(k);
                buf.writeDouble(v);
            });
            buf.writeItem(recipe.getResultItem(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookingTime());
            buf.writeUtf(recipe.toolType);
            buf.writeBoolean(recipe.needPolishing);
        }
    }
}
