package net.stirdrem.overgeared.recipe.castcooking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.recipe.CastRecipeHelper;
import net.stirdrem.overgeared.recipe.RecipeCodecs;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Logic and codecs shared by {@link CastSmeltingRecipe} and {@link CastBlastingRecipe}. */
final class CastCookingLogic {
    private CastCookingLogic() {
    }

    static Ingredient castIngredient() {
        return Ingredient.of(ModItems.CLAY_TOOL_CAST, ModItems.NETHER_TOOL_CAST);
    }

    static boolean matches(ItemStack input, String toolType, Map<String, Double> requiredMaterials) {
        if (!(input.getItem() instanceof ToolCastItem)) return false;
        CastData data = input.get(ModComponents.CAST_DATA);
        if (data == null || data.materials().isEmpty()) return false;
        if (!toolType.equals(data.toolType().toLowerCase(Locale.ROOT))) return false;
        if (data.amount() <= 0) return false;

        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase(Locale.ROOT);
            double available = data.materials().getOrDefault(material, 0);
            if (available < entry.getValue()) return false;
        }
        return true;
    }

    /**
     * Builds the cast result and stores it inside a copy of the cast (CAST_DATA output, materials cleared,
     * heated). Returns the filled cast, or the bare result if the cast breaks from wear.
     */
    static ItemStack assemble(ItemStack input, ItemStackTemplate resultTemplate, boolean needPolishing) {
        ItemStack result = resultTemplate.create();
        CastRecipeHelper.applyCastToResult(result, input, needPolishing);

        ItemStack cast = input.copy();
        CastData data = cast.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);
        cast.set(ModComponents.CAST_DATA, data.withOutput(result).withMaterials(Map.of()).withHeated(true));

        if (cast.isDamageableItem()) {
            if (cast.getDamageValue() + 1 >= cast.getMaxDamage()) {
                return result;
            }
            cast.setDamageValue(cast.getDamageValue() + 1);
        }
        return cast;
    }

    @FunctionalInterface
    interface Factory<T extends AbstractCookingRecipe> {
        T create(String group, ItemStackTemplate result, float xp, int time, Map<String, Double> materials, String toolType, boolean needPolishing);
    }

    interface Accessor {
        String group();

        ItemStackTemplate resultTemplate();

        float experience();

        int cookingTime();

        Map<String, Double> getRequiredMaterials();

        String getToolType();

        boolean requiresPolishing();
    }

    static Recipe.CommonInfo commonInfo() {
        return new Recipe.CommonInfo(true);
    }

    static AbstractCookingRecipe.CookingBookInfo bookInfo(String group) {
        return new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC, group);
    }

    static <T extends AbstractCookingRecipe & Accessor> MapCodec<T> mapCodec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(Accessor::group),
                RecipeCodecs.RESULT.fieldOf("result").forGetter(Accessor::resultTemplate),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(Accessor::experience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(Accessor::cookingTime),
                RecipeCodecs.MATERIALS.fieldOf("input").forGetter(Accessor::getRequiredMaterials),
                Codec.STRING.xmap(s -> s.toLowerCase(Locale.ROOT), s -> s).fieldOf("tool_type").forGetter(Accessor::getToolType),
                Codec.BOOL.optionalFieldOf("need_polishing", false).forGetter(Accessor::requiresPolishing)
        ).apply(i, factory::create));
    }

    static <T extends AbstractCookingRecipe & Accessor> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        StreamCodec<RegistryFriendlyByteBuf, Map<String, Double>> materials =
                ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE);
        return StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Accessor::group,
                ItemStackTemplate.STREAM_CODEC, Accessor::resultTemplate,
                ByteBufCodecs.FLOAT, Accessor::experience,
                ByteBufCodecs.VAR_INT, Accessor::cookingTime,
                materials, Accessor::getRequiredMaterials,
                ByteBufCodecs.STRING_UTF8, Accessor::getToolType,
                ByteBufCodecs.BOOL, Accessor::requiresPolishing,
                factory::create);
    }
}
