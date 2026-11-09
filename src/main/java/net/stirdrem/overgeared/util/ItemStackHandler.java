package net.stirdrem.overgeared.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric has no equivalent of Forge's ItemStackHandler (a simple NBT-serializable item container
 * with insert/extract semantics, commonly wrapped in an anonymous subclass overriding
 * onContentsChanged). This shim replicates that API so the ported block entities that used it
 * need only mechanical type substitutions.
 */
public class ItemStackHandler {
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    protected void onContentsChanged(int slot) {
    }

    protected void onLoad() {
    }

    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }

    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    public int getSlots() {
        return stacks.size();
    }

    @NotNull
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        validateSlotIndex(slot);
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;

        validateSlotIndex(slot);
        ItemStack existing = stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }

        if (limit <= 0) return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlotIndex(slot);

        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getCount());

        if (toExtract >= existing.getCount()) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                onContentsChanged(slot);
            }
            return existing.copyWithCount(toExtract);
        }
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    /**
     * 26.3 port: block entities persist through ValueOutput/ValueInput now. Writes "Size" and the
     * vanilla "Items" list (same layout as the old NBT) into {@code output}.
     */
    public void save(ValueOutput output) {
        output.putInt("Size", stacks.size());
        ContainerHelper.saveAllItems(output, stacks);
    }

    public void load(ValueInput input) {
        setSize(input.getIntOr("Size", stacks.size()));
        ContainerHelper.loadAllItems(input, stacks);
        onLoad();
    }

    /** Standalone NBT form (e.g. for item components / packets); needs registries to encode components. */
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                ItemStackWithSlot.CODEC.encodeStart(ops, new ItemStackWithSlot(i, stacks.get(i)))
                        .ifSuccess(nbtTagList::add);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        setSize(nbt.getIntOr("Size", stacks.size()));
        for (Tag tag : nbt.getListOrEmpty("Items")) {
            ItemStackWithSlot.CODEC.parse(ops, tag).ifSuccess(item -> {
                if (item.isValidInContainer(stacks.size())) {
                    stacks.set(item.slot(), item.stack());
                }
            });
        }
        onLoad();
    }
}
