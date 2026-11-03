package net.stirdrem.overgeared.util.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Exposes an {@link IItemHandler} as a {@link RecipeInput}.
 */
public class RecipeWrapper implements RecipeInput {
    protected final IItemHandler inv;

    public RecipeWrapper(IItemHandler inv) {
        this.inv = inv;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public int size() {
        return inv.getSlots();
    }
}
