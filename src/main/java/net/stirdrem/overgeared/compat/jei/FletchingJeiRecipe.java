package net.stirdrem.overgeared.compat.jei;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.recipe.FletchingRecipe;

import java.util.List;
import java.util.Optional;

/**
 * JEI view of a fletching station recipe.
 * <p>
 * 26.3 port: vanilla {@link Ingredient}s can no longer carry item data (1.20.1 {@code Ingredient.of(ItemStack)} kept the
 * potion NBT for display), so the generated potion-tipping conversions list their potion stacks explicitly in
 * {@link #potionStacks()}. Real {@link FletchingRecipe}s use the ingredients directly.
 *
 * @param potionStacks when non-empty, shown in the potion slot instead of {@link #potion()}
 */
public record FletchingJeiRecipe(
        Identifier id,
        Optional<Ingredient> tip,
        Optional<Ingredient> shaft,
        Optional<Ingredient> feather,
        Optional<Ingredient> potion,
        List<ItemStack> potionStacks,
        ItemStack output
) {
    public static FletchingJeiRecipe of(RecipeHolder<FletchingRecipe> holder) {
        FletchingRecipe recipe = holder.value();
        return new FletchingJeiRecipe(
                holder.id().identifier(),
                recipe.getTip(),
                recipe.getShaft(),
                recipe.getFeather(),
                recipe.getPotion(),
                List.of(),
                recipe.getDefaultResult()
        );
    }
}
