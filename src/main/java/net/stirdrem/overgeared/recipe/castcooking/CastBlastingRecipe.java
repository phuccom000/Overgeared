package net.stirdrem.overgeared.recipe.castcooking;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.CastRecipeHelper;
import net.stirdrem.overgeared.recipe.ModRecipes;

import java.util.Map;

/**
 * Vanilla blast furnace recipe (type {@code minecraft:blasting}) that "fires" a filled tool cast: input is a clay or
 * nether tool cast whose CAST_DATA has the tool type and enough materials; output is the cast holding the
 * finished part in CAST_DATA.output (or the bare part if the cast breaks).
 * <p>
 * JSON ({@code overgeared:cast_blasting}): {@code input} (material -> amount), {@code tool_type}, {@code result},
 * optional {@code group}, {@code experience} (0), {@code cookingtime} (200), {@code need_polishing} (false).
 */
public class CastBlastingRecipe extends BlastingRecipe implements CastCookingLogic.Accessor {

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastBlastingRecipe(String group, ItemStackTemplate result, float xp, int time,
                              Map<String, Double> reqMaterials, String toolType, boolean needPolishing) {
        super(CastCookingLogic.commonInfo(), CastCookingLogic.bookInfo(group), CastCookingLogic.castIngredient(), result, xp, time);
        this.requiredMaterials = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(reqMaterials));
        this.toolType = toolType;
        this.needPolishing = needPolishing;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return CastCookingLogic.matches(input.item(), toolType, requiredMaterials);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return CastCookingLogic.assemble(input.item(), result(), needPolishing);
    }

    /** Display cast stacks (clay + nether) holding the required materials, for recipe viewers. */
    public java.util.List<ItemStack> getDisplayCasts() {
        return java.util.List.of(
                CastRecipeHelper.displayCast(ModItems.CLAY_TOOL_CAST, toolType, requiredMaterials),
                CastRecipeHelper.displayCast(ModItems.NETHER_TOOL_CAST, toolType, requiredMaterials));
    }

    /** A fresh copy of the plain result. */
    public ItemStack getResultItem() {
        return result().create();
    }

    @Override
    public ItemStackTemplate resultTemplate() {
        return result();
    }

    public Map<String, Double> getMaterialInputs() {
        return requiredMaterials;
    }

    @Override
    public Map<String, Double> getRequiredMaterials() {
        return requiredMaterials;
    }

    @Override
    public String getToolType() {
        return toolType;
    }

    @Override
    public boolean requiresPolishing() {
        return needPolishing;
    }

    @Override
    public RecipeSerializer<BlastingRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<BlastingRecipe> s = (RecipeSerializer) ModRecipes.CAST_BLASTING;
        return s;
    }

    public static final MapCodec<CastBlastingRecipe> MAP_CODEC = CastCookingLogic.mapCodec(CastBlastingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, CastBlastingRecipe> STREAM_CODEC = CastCookingLogic.streamCodec(CastBlastingRecipe::new);
    public static final RecipeSerializer<CastBlastingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
