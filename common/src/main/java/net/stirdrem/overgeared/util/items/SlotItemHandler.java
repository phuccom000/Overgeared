package net.stirdrem.overgeared.util.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A menu slot backed by an {@link IItemHandler} instead of a vanilla {@link Container}.
 */
public class SlotItemHandler extends Slot {
    private static final Container EMPTY_CONTAINER = new SimpleContainer(0);

    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(EMPTY_CONTAINER, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return itemHandler.isItemValid(index, stack);
    }

    @Override
    public ItemStack getItem() {
        return this.getItemHandler().getStackInSlot(index);
    }

    @Override
    public void set(ItemStack stack) {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, stack);
        this.setChanged();
    }

    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, newStack);
        this.setChanged();
    }

    @Override
    public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {
    }

    @Override
    public int getMaxStackSize() {
        return this.itemHandler.getSlotLimit(this.index);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        ItemStack maxAdd = stack.copyWithCount(stack.getMaxStackSize());

        IItemHandler handler = this.getItemHandler();
        ItemStack currentStack = handler.getStackInSlot(index);
        if (handler instanceof IItemHandlerModifiable handlerModifiable) {
            handlerModifiable.setStackInSlot(index, ItemStack.EMPTY);
            ItemStack remainder = handlerModifiable.insertItem(index, maxAdd, true);
            handlerModifiable.setStackInSlot(index, currentStack);
            return maxAdd.getCount() - remainder.getCount();
        }
        ItemStack remainder = handler.insertItem(index, maxAdd, true);
        int current = currentStack.getCount();
        int added = maxAdd.getCount() - remainder.getCount();
        return current + added;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.getItemHandler().extractItem(index, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return this.getItemHandler().extractItem(index, amount, false);
    }

    @Override
    public void setChanged() {
        // The backing handler reports its own changes.
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }
}
