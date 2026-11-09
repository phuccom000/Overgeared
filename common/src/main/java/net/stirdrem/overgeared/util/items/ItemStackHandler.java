package net.stirdrem.overgeared.util.items;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.nbt.NbtOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * A simple {@link IItemHandlerModifiable} backed by a list of stacks.
 * <p>
 * The NBT layout ({@code Items} list with {@code Slot} ints, plus {@code Size}) matches the one
 * NeoForge's handler used, so inventories saved by earlier versions of the mod still load.
 */
public class ItemStackHandler implements IItemHandlerModifiable {
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public ItemStackHandler(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public void setSize(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);
        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            return existing.copy();
        }
        if (!simulate) {
            this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
            onContentsChanged(slot);
        }
        return existing.copyWithCount(toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        return DEFAULT_SLOT_LIMIT;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    /** Same layout as before 1.21.6: {"Items": [{"Slot": n, ...stack}], "Size": n}. */
    public void serialize(ValueOutput output) {
        ValueOutput.TypedOutputList<ItemStackWithSlot> items = output.list("Items", ItemStackWithSlot.CODEC);
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                items.add(new ItemStackWithSlot(i, stacks.get(i)));
            }
        }
        output.putInt("Size", stacks.size());
    }

    public void deserialize(ValueInput input) {
        setSize(input.getIntOr("Size", stacks.size()));
        input.listOrEmpty("Items", ItemStackWithSlot.CODEC).stream().forEach(entry -> {
            if (entry.isValidInContainer(stacks.size())) {
                stacks.set(entry.slot(), entry.stack());
            }
        });
        onLoad();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        ListTag items = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                ItemStackWithSlot.CODEC.encodeStart(ops, new ItemStackWithSlot(i, stacks.get(i))).ifSuccess(items::add);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", items);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        setSize(nbt.getIntOr("Size", stacks.size()));
        for (Tag tag : nbt.getListOrEmpty("Items")) {
            ItemStackWithSlot.CODEC.parse(ops, tag).ifSuccess(entry -> {
                if (entry.isValidInContainer(stacks.size())) {
                    stacks.set(entry.slot(), entry.stack());
                }
            });
        }
        onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }

    protected void onLoad() {
    }

    protected void onContentsChanged(int slot) {
    }

    private static final int DEFAULT_SLOT_LIMIT = 99;
}
