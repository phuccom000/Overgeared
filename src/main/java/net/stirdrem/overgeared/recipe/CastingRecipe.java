package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import net.stirdrem.overgeared.util.ConfigHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CastingRecipe implements Recipe<Inventory> {

    private final Identifier id;
    private final String group;
    private final CookingRecipeCategory category;

    private final ItemStack result;
    private final float experience;
    private final int cookingTime;

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastingRecipe(
            Identifier id,
            String group,
            CookingRecipeCategory category,
            ItemStack result,
            float experience,
            int cookingTime,
            Map<String, Double> requiredMaterials,
            String toolType,
            boolean needPolishing
    ) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.requiredMaterials = requiredMaterials;
        this.toolType = toolType.toLowerCase(Locale.ROOT);
        this.needPolishing = needPolishing;
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        if (world.isClient) return false;

        // Tool cast (slot 1)
        ItemStack cast = inv.getStack(1);
        if (!(cast.getItem() instanceof ToolCastItem)) return false;

        NbtCompound castTag = cast.getNbt();
        if (castTag == null) return false;

        // Tool type check (FROM CAST)
        if (!castTag.contains("ToolType")) return false;
        if (!toolType.equals(castTag.getString("ToolType").toLowerCase(Locale.ROOT))) return false;

        // Material input slot (slot 0)
        ItemStack materialStack = inv.getStack(0);
        if (materialStack.isEmpty()) return false;

        // Must be a valid material
        if (!ConfigHelper.isValidMaterial(materialStack)) {
            return false;
        }

        // availableMaterials is derived ONLY from input slot
        Map<String, Integer> availableMaterials =
                ConfigHelper.getMaterialValuesForItem(materialStack);
        int count = materialStack.getCount();
        // Required material validation
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase(Locale.ROOT);
            double needed = entry.getValue();

            double available = availableMaterials
                    .getOrDefault(material, 0) * count;

            if (available < needed) {
                return false;
            }
        }

        return true;
    }


    @Override
    public ItemStack craft(Inventory inv, DynamicRegistryManager registryAccess) {
        ItemStack cast = inv.getStack(3);
        if (cast.isEmpty()) return ItemStack.EMPTY;

        NbtCompound castTag = cast.getOrCreateNbt();

        // Build result item
        ItemStack out = result.copy();
        NbtCompound outTag = out.getOrCreateNbt();

        // Transfer forging quality from cast
        if (castTag.contains("Quality")) {
            String q = castTag.getString("Quality");
            if (!q.equals("none")) {
                outTag.putString("ForgingQuality", q);
            }
        }

        // Polishing flag
        if (needPolishing) {
            outTag.putBoolean("Polished", false);
        }

        // Heated flag (used by your pipeline)
        outTag.putBoolean("Heated", true);

        // Creator tooltip
        if (cast.hasCustomName() && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            outTag.putString("Creator", cast.getName().getString());
        }

        /* -------------------------------------------------- */
        /* DAMAGE CAST — CAST STAYS IN SLOT                   */
        /* -------------------------------------------------- */

        if (cast.isDamageable()) {
            int newDamage = cast.getDamage() + 1;

            if (newDamage >= cast.getMaxDamage()) {
                // Cast breaks
                cast.decrement(1);
            } else {
                cast.setDamage(newDamage);
            }
        }

        // IMPORTANT: return the RESULT item
        return out;
    }

    /* ============================================================= */
    /* INGREDIENTS (JEI SUPPORT)                                     */
    /* ============================================================= */

    @Override
    public @NotNull DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> list = DefaultedList.of();

        NbtCompound tag = new NbtCompound();
        tag.putString("ToolType", toolType);

        NbtCompound mats = new NbtCompound();
        double total = 0;
        for (var e : requiredMaterials.entrySet()) {
            mats.putDouble(e.getKey(), e.getValue());
            total += e.getValue();
        }

        tag.put("Materials", mats);
        tag.putDouble("Amount", total);
        tag.putDouble("MaxAmount", total);

        ItemStack dummyCast = new ItemStack(ModItems.CLAY_TOOL_CAST);
        dummyCast.setNbt(tag);

        list.add(Ingredient.ofStacks(dummyCast));
        return list;
    }

    /* ============================================================= */
    /* BASIC META                                                    */
    /* ============================================================= */

    @Override
    public ItemStack getOutput(DynamicRegistryManager access) {
        return result;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public String getGroup() {
        return group;
    }


    @Override
    public boolean fits(int w, int h) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CASTING;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CASTING;
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

    public int getCookingTime() {
        return cookingTime;
    }

    public float getExperience() {
        return experience;
    }

    public boolean requiresPolishing() {
        return needPolishing;
    }

    public Map<String, Double> getRequiredMaterials() {
        return requiredMaterials;
    }

    public String getToolType() {
        return toolType;
    }

    public static class Type implements RecipeType<CastingRecipe> {
        public static final CastingRecipe.Type INSTANCE = new CastingRecipe.Type();
        public static final String ID = "casting";
    }

    public static class Serializer implements RecipeSerializer<CastingRecipe> {
        public static final CastingRecipe.Serializer INSTANCE = new CastingRecipe.Serializer();

        @Override
        public CastingRecipe read(Identifier id, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");
            CookingRecipeCategory category = CookingRecipeCategory.MISC;

            JsonObject input = JsonHelper.getObject(json, "input");

            Map<String, Double> mats = new HashMap<>();
            for (var entry : input.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                var value = entry.getValue();

                if (!value.isJsonPrimitive()) {
                    throw new JsonParseException(
                            "[Overgeared] Invalid casting recipe '" + id + "' -> material '" +
                                    key + "' must be a NUMBER, but got: " + value
                    );
                }

                if (!value.getAsJsonPrimitive().isNumber()) {
                    throw new JsonParseException(
                            "[Overgeared] Invalid casting recipe '" + id + "' -> material '" +
                                    key + "' must be numeric, but got: " + value
                    );
                }

                double amount = value.getAsDouble();

                if (amount <= 0) {
                    throw new JsonParseException(
                            "[Overgeared] Invalid casting recipe '" + id + "' -> material '" +
                                    key + "' must be > 0, got: " + amount
                    );
                }

                mats.put(key, amount);
            }

            ItemStack result = ShapedRecipe.outputFromJson(
                    JsonHelper.getObject(json, "result")
            );

            float xp = JsonHelper.getFloat(json, "experience", 0f);
            int time = JsonHelper.getInt(json, "cookingtime", 200);
            String toolType = JsonHelper.getString(json, "tool_type").toLowerCase(Locale.ROOT);
            boolean polish = JsonHelper.getBoolean(json, "need_polishing", false);

            return new CastingRecipe(
                    id, group, category,
                    result, xp, time,
                    mats, toolType, polish
            );
        }

        @Override
        public CastingRecipe read(Identifier id, PacketByteBuf buf) {
            String group = buf.readString();
            CookingRecipeCategory category = CookingRecipeCategory.MISC;

            int size = buf.readInt();
            Map<String, Double> mats = new HashMap<>();
            for (int i = 0; i < size; i++) {
                mats.put(buf.readString(), buf.readDouble());
            }

            ItemStack result = buf.readItemStack();
            float xp = buf.readFloat();
            int time = buf.readVarInt();
            String toolType = buf.readString();
            boolean polish = buf.readBoolean();

            return new CastingRecipe(
                    id, group, category,
                    result, xp, time,
                    mats, toolType, polish
            );
        }

        @Override
        public void write(PacketByteBuf buf, CastingRecipe recipe) {
            buf.writeString(recipe.group);

            buf.writeInt(recipe.requiredMaterials.size());
            recipe.requiredMaterials.forEach((k, v) -> {
                buf.writeString(k);
                buf.writeDouble(v);
            });

            buf.writeItemStack(recipe.result);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
            buf.writeString(recipe.toolType);
            buf.writeBoolean(recipe.needPolishing);
        }
    }
}
