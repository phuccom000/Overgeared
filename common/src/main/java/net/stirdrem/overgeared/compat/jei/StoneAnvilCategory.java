package net.stirdrem.overgeared.compat.jei;
import net.minecraft.core.registries.BuiltInRegistries;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.util.ModTags;

public class StoneAnvilCategory extends ExplanationCategory {
    public static final RecipeType<ExplanationRecipe> STONE_ANVIL_GET =
            new RecipeType<>(OvergearedMod.loc("stone_anvil"), ExplanationRecipe.class);

    public StoneAnvilCategory(IGuiHelper guiHelper) {
        super(guiHelper, STONE_ANVIL_GET, new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()),
                "jei.overgeared.category.stone_anvil",
                "jei.overgeared.stone_anvil.description1");
    }

    @Override
    protected Ingredient getInput() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ModTags.Items.SMITHING_HAMMERS));
    }
}
