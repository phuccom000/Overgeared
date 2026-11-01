package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;

/**
 * Explains the Drafting Table: empty blueprint -> smithing blueprint (JEI counterpart of DraftingTableEmiRecipe).
 */
public class DraftingTableCategory extends ExplanationCategory {
    public static final RecipeType<ExplanationRecipe> DRAFTING =
            new RecipeType<>(OvergearedMod.loc("drafting"), ExplanationRecipe.class);

    public DraftingTableCategory(IGuiHelper guiHelper) {
        super(guiHelper, DRAFTING, new ItemStack(ModBlocks.DRAFTING_TABLE.get()),
                "jei.overgeared.category.drafting_table",
                "jei.overgeared.drafting_table.description");
    }

    @Override
    protected Ingredient getInput() {
        return Ingredient.of(ModItems.EMPTY_BLUEPRINT.get());
    }
}
