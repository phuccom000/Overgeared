package net.stirdrem.overgeared.recipe.castcooking;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.recipe.ModRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CastBlastingRecipe extends BlastingRecipe {

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastBlastingRecipe(Identifier id, String group, CookingRecipeCategory category,
                               ItemStack result, float xp, int time,
                               Map<String, Double> reqMaterials, String toolType, boolean needPolishing) {
        super(id, group, category,
                Ingredient.ofItems(ModItems.CLAY_TOOL_CAST, ModItems.NETHER_TOOL_CAST),
                result, xp, time);
        this.requiredMaterials = reqMaterials;
        this.toolType = toolType;
        this.needPolishing = needPolishing;
    }

    public static Map<String, Double> readMaterials(NbtCompound tag) {
        Map<String, Double> map = new HashMap<>();
        for (String key : tag.getKeys()) {
            if (tag.contains(key, NbtElement.DOUBLE_TYPE)) {
                map.put(key, tag.getDouble(key));
            } else if (tag.contains(key, NbtElement.INT_TYPE)) {
                map.put(key, (double) tag.getInt(key));
            }
        }
        return map;
    }

    @Override
    public @NotNull DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> list = DefaultedList.of();

        // Build the same NBT tag JEI uses
        NbtCompound tag = new NbtCompound();
        tag.putString("ToolType", toolType);

        NbtCompound mats = new NbtCompound();
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
        firedCast.setNbt(tag.copy());

        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST);
        netherCast.setNbt(tag.copy());

        // Report them as the recipe input
        list.add(Ingredient.ofStacks(firedCast, netherCast));

        return list;
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        ItemStack input = inv.getStack(0);
        if (!(input.getItem() instanceof ToolCastItem)) return false;
        NbtCompound tag = input.getNbt();
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
    public ItemStack craft(Inventory inv, DynamicRegistryManager registryAccess) {
        ItemStack input = inv.getStack(0);

        // Copy the cast itself
        ItemStack cast = input.copy();
        NbtCompound castTag = cast.getOrCreateNbt();


        // Build the real result item
        ItemStack result = super.craft(inv, registryAccess);

        NbtCompound resultTag = result.getOrCreateNbt();

        // Transfer quality
        if (input.hasNbt() && input.getNbt().contains("Quality")) {
            String q = input.getNbt().getString("Quality");
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
        if (input.hasCustomName() && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            resultTag.putString("Creator", input.getName().getString());
        }

        // Store output INSIDE the cast
        castTag.put("Output", result.writeNbt(new NbtCompound()));
        castTag.put("Materials", new NbtCompound());
        // Mark cast as filled / heated
        castTag.putBoolean("Heated", true);
        if (cast.isDamageable()) {
            if (cast.getDamage() + 1 >= cast.getMaxDamage()) {
                cast.decrement(1);
                return result;
            } else {
                cast.setDamage(cast.getDamage() + 1);
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
        public CastBlastingRecipe read(Identifier id, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");
            CookingRecipeCategory category = CookingRecipeCategory.MISC;

            JsonObject inputObj = JsonHelper.getObject(json, "input");
            Map<String, Double> reqMaterials = new HashMap<>();
            inputObj.entrySet().forEach(e -> reqMaterials.put(e.getKey(), e.getValue().getAsDouble()));

            ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
            float xp = JsonHelper.getFloat(json, "experience", 0f);
            int time = JsonHelper.getInt(json, "cookingtime", 200);

            String toolType = JsonHelper.getString(json, "tool_type").toLowerCase(Locale.ROOT);

            boolean needPolishing = JsonHelper.getBoolean(json, "need_polishing", false);

            return new CastBlastingRecipe(id, group, category, result, xp, time, reqMaterials, toolType, needPolishing);
        }

        @Override
        public CastBlastingRecipe read(Identifier id, PacketByteBuf buf) {
            String group = buf.readString();
            CookingRecipeCategory category = CookingRecipeCategory.MISC;
            int size = buf.readInt();
            Map<String, Double> reqMaterials = new HashMap<>();
            for (int i = 0; i < size; i++) {
                reqMaterials.put(buf.readString(), buf.readDouble());
            }
            ItemStack result = buf.readItemStack();
            float xp = buf.readFloat();
            int time = buf.readVarInt();
            String toolType = buf.readString();
            boolean needPolish = buf.readBoolean();

            return new CastBlastingRecipe(id, group, category, result, xp, time, reqMaterials, toolType, needPolish);
        }

        @Override
        public void write(PacketByteBuf buf, CastBlastingRecipe recipe) {
            buf.writeString(recipe.getGroup());
            buf.writeInt(recipe.requiredMaterials.size());
            recipe.requiredMaterials.forEach((k, v) -> {
                buf.writeString(k);
                buf.writeDouble(v);
            });
            buf.writeItemStack(recipe.getOutput(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookTime());
            buf.writeString(recipe.toolType);
            buf.writeBoolean(recipe.needPolishing);
        }
    }
}
