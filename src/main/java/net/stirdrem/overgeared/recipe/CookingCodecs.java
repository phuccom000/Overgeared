package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;

/** Codec helpers for Overgeared recipes extending vanilla cooking recipes. */
public final class CookingCodecs {
    private CookingCodecs() {
    }

    /** Exposes the (protected in vanilla) result template of a cooking recipe. */
    public interface ResultAccess {
        ItemStackTemplate resultTemplate();
    }

    /**
     * Vanilla cooking JSON ({@code ingredient}, {@code result}, {@code experience}, {@code cookingtime},
     * {@code category}, {@code group}, {@code show_notification}) but with {@code cookingtime} optional (default
     * 200) and legacy 1.20.1 ingredient/result objects accepted, like the 1.20.1 Overgeared serializers.
     */
    public static <T extends AbstractCookingRecipe & ResultAccess> MapCodec<T> lenientCookingMapCodec(AbstractCookingRecipe.Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(o -> new Recipe.CommonInfo(o.showNotification())),
                AbstractCookingRecipe.CookingBookInfo.MAP_CODEC.forGetter(o -> new AbstractCookingRecipe.CookingBookInfo(o.category(), o.group())),
                RecipeCodecs.INGREDIENT.fieldOf("ingredient").forGetter(AbstractCookingRecipe::input),
                RecipeCodecs.RESULT.fieldOf("result").forGetter(ResultAccess::resultTemplate),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(AbstractCookingRecipe::experience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(AbstractCookingRecipe::cookingTime)
        ).apply(i, factory::create));
    }

    /** Result with the input's component patch applied on top (input wins), i.e. 1.20.1 "copy input NBT". */
    public static ItemStack keepComponents(ItemStackTemplate result, ItemStack input) {
        ItemStack output = result.create();
        if (!input.isEmpty()) {
            output.applyComponents(input.getComponentsPatch());
        }
        return output;
    }
}
