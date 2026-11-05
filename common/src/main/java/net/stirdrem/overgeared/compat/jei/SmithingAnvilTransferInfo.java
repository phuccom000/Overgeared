package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;

import java.util.List;
import java.util.Optional;

/**
 * Recipe transfer for smithing anvils. The grid's position depends on whether the anvil's tier has a blueprint slot,
 * so the slots are taken from the menu rather than fixed indices.
 */
public record SmithingAnvilTransferInfo<C extends AbstractSmithingAnvilMenu>(Class<C> containerClass, MenuType<C> menuType)
        implements IRecipeTransferInfo<C, ForgingRecipe> {

    @Override
    public Class<? extends C> getContainerClass() {
        return containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    public RecipeType<ForgingRecipe> getRecipeType() {
        return ForgingRecipeCategory.FORGING_RECIPE_TYPE;
    }

    @Override
    public boolean canHandle(C container, ForgingRecipe recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(C container, ForgingRecipe recipe) {
        return container.getInputSlots().stream().map(container::getSlot).toList();
    }

    @Override
    public List<Slot> getInventorySlots(C container, ForgingRecipe recipe) {
        return container.getPlayerSlots();
    }
}
