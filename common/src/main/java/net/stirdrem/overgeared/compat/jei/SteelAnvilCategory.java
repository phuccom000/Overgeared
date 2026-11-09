package net.stirdrem.overgeared.compat.jei;
import net.minecraft.core.registries.BuiltInRegistries;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.util.ModTags;

public class SteelAnvilCategory extends ExplanationCategory {
    public static final RecipeType<ExplanationRecipe> STEEL_ANVIL_GET =
            new RecipeType<>(OvergearedMod.loc("steel_anvil"), ExplanationRecipe.class);

    public SteelAnvilCategory(IGuiHelper guiHelper) {
        super(guiHelper, STEEL_ANVIL_GET, new ItemStack(ModBlocks.IRON_SMITHING_ANVIL.get()),
                "jei.overgeared.category.steel_anvil",
                "jei.overgeared.steel_anvil.description");
    }

    @Override
    protected Ingredient getInput() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.SMITHING_HAMMERS));
    }
}
