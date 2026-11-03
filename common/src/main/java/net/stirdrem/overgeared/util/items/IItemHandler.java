package net.stirdrem.overgeared.util.items;

import net.minecraft.world.item.ItemStack;

/**
 * A slot-based item inventory. Mirrors the shape of NeoForge's interface of the same name so the
 * block entities and menus can be shared between loaders.
 */
public interface IItemHandler {
    int getSlots();

    /** The stack in the slot. Callers must not modify the returned stack. */
    ItemStack getStackInSlot(int slot);

    /**
     * Inserts a stack into the slot.
     *
     * @return the remainder that could not be inserted
     */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    /**
     * Extracts up to {@code amount} items from the slot.
     *
     * @return the extracted stack
     */
    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
