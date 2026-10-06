package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.Locale;
import java.util.Map;

/**
 * Cast furnace recipe. Input: {@link ItemListInput} with slot 0 = material input stack, slot 1 = tool cast.
 * <p>
 * JSON: {@code input} (material id -> amount, numbers > 0), {@code tool_type}, {@code result}, optional
 * {@code group}, {@code experience} (0), {@code cookingtime} (200), {@code need_polishing} (false).
 */
public class CastingRecipe implements Recipe<ItemListInput> {
    public static final int MATERIAL_SLOT = 0;
    public static final int CAST_SLOT = 1;

    private final String group;
    private final CookingBookCategory category;

    private final ItemStackTemplate result;
    private final float experience;
    private final int cookingTime;

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastingRecipe(String group, ItemStackTemplate result, float experience, int cookingTime,
                         Map<String, Double> requiredMaterials, String toolType, boolean needPolishing) {
        this.group = group;
        this.category = CookingBookCategory.MISC;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.requiredMaterials = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(requiredMaterials));
        this.toolType = toolType.toLowerCase(Locale.ROOT);
        this.needPolishing = needPolishing;
    }

    @Override
    public boolean matches(ItemListInput inv, Level world) {
        if (world.isClientSide()) return false;

        ItemStack cast = inv.getItem(CAST_SLOT);
        if (!(cast.getItem() instanceof ToolCastItem)) return false;
        if (!CastRecipeHelper.hasToolType(cast, toolType)) return false;

        ItemStack materialStack = inv.getItem(MATERIAL_SLOT);
        if (materialStack.isEmpty()) return false;
        if (!ConfigHelper.isValidMaterial(materialStack)) return false;

        Map<String, Integer> availableMaterials = ConfigHelper.getMaterialValuesForItem(materialStack);
        int count = materialStack.getCount();
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase(Locale.ROOT);
            double available = availableMaterials.getOrDefault(material, 0) * (double) count;
            if (available < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * The result with the cast's quality / polish / heated / creator applied. Unlike 1.20.1 this does NOT damage
     * the cast (the 1.20.1 version read a slot that never existed and returned EMPTY); the cast furnace block
     * entity handles cast wear itself.
     */
    @Override
    public ItemStack assemble(ItemListInput inv) {
        ItemStack cast = inv.getItem(CAST_SLOT);
        if (cast.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = result.create();
        CastRecipeHelper.applyCastToResult(out, cast, needPolishing);
        return out;
    }

    /** A cast stack showing the required materials, for recipe viewers (replaces 1.20.1 getIngredients()). */
    public ItemStack getDisplayCast() {
        return CastRecipeHelper.displayCast(ModItems.CLAY_TOOL_CAST, toolType, requiredMaterials);
    }

    /** A fresh copy of the plain result (no cast data applied). */
    public ItemStack getResultItem() {
        return result.create();
    }

    /** @deprecated use {@link #getResultItem()}. */
    @Deprecated
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    public ItemStackTemplate result() {
        return result;
    }

    @Override
    public String group() {
        return group;
    }

    public CookingBookCategory category() {
        return category;
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

    @Override
    public boolean isSpecial() {
        // no item ingredients -> not placeable; keeps it out of recipe book / property-set warnings
        return true;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public RecipeSerializer<CastingRecipe> getSerializer() {
        return ModRecipes.CASTING;
    }

    @Override
    public RecipeType<CastingRecipe> getType() {
        return ModRecipeTypes.CASTING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.CASTING;
    }

    public static class Type implements RecipeType<CastingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "casting";

        @Override
        public String toString() {
            return ID;
        }
    }

    public static final MapCodec<CastingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
            RecipeCodecs.RESULT.fieldOf("result").forGetter(r -> r.result),
            Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(r -> r.experience),
            Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(r -> r.cookingTime),
            RecipeCodecs.MATERIALS.fieldOf("input").forGetter(r -> r.requiredMaterials),
            Codec.STRING.fieldOf("tool_type").forGetter(r -> r.toolType),
            Codec.BOOL.optionalFieldOf("need_polishing", false).forGetter(r -> r.needPolishing)
    ).apply(i, CastingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, r -> r.group,
            ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.FLOAT, r -> r.experience,
            ByteBufCodecs.VAR_INT, r -> r.cookingTime,
            ByteBufCodecs.map(java.util.LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE), r -> r.requiredMaterials,
            ByteBufCodecs.STRING_UTF8, r -> r.toolType,
            ByteBufCodecs.BOOL, r -> r.needPolishing,
            CastingRecipe::new);

    public static final RecipeSerializer<CastingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
