package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilScreenHandler;

import java.util.List;
import java.util.Optional;

/**
 * JEI "move items" for the smithing anvils. The menu only has a blueprint slot on blueprint-capable
 * anvils with blueprint forging enabled, so the grid and inventory slot indexes are read from the
 * open menu instead of being fixed numbers.
 * Menu layout: hammer, [blueprint], 3x3 grid, result, player inventory (27), hotbar (9).
 */
public record AnvilRecipeTransferInfo<C extends AbstractSmithingAnvilScreenHandler>(
        Class<C> containerClass, MenuType<C> menuType) implements IRecipeTransferInfo<C, RecipeHolder<ForgingRecipe>> {

    @Override
    public Class<? extends C> getContainerClass() {
        return containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    public IRecipeType<RecipeHolder<ForgingRecipe>> getRecipeType() {
        return ForgingRecipeCategory.FORGING_RECIPE_TYPE;
    }

    @Override
    public boolean canHandle(C container, RecipeHolder<ForgingRecipe> recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(C container, RecipeHolder<ForgingRecipe> recipe) {
        int start = container.getGridSlotStart();
        return container.slots.subList(start, start + 9);
    }

    @Override
    public List<Slot> getInventorySlots(C container, RecipeHolder<ForgingRecipe> recipe) {
        int start = container.getGridSlotStart() + 10; // grid (9) + result (1)
        return container.slots.subList(start, start + 36);
    }
}
