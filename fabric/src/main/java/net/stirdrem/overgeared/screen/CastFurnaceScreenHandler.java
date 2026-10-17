package net.stirdrem.overgeared.screen;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.block.entity.CastFurnaceBlockEntity;
import net.stirdrem.overgeared.util.ConfigHelper;
import net.stirdrem.overgeared.util.ModTags;

public class CastFurnaceScreenHandler extends AbstractContainerMenu {

    private final CastFurnaceBlockEntity blockEntity;
    private final ContainerData data;

    public CastFurnaceScreenHandler(int syncId, Inventory playerInv, CastFurnaceBlockEntity be, ContainerData data) {
        super(ModMenuTypes.CAST_FURNACE, syncId);
        this.blockEntity = be;
        this.data = data;

        checkContainerSize(be, 4);
        addDataSlots(data);

        // Input
        this.addSlot(new Slot(be, CastFurnaceBlockEntity.SLOT_INPUT, 56, 24));

        // Fuel
        this.addSlot(new Slot(be, CastFurnaceBlockEntity.SLOT_FUEL, 8, 53));

        // Output
        this.addSlot(new FurnaceResultSlot(
                playerInv.player,
                be,
                CastFurnaceBlockEntity.SLOT_OUTPUT,
                116,
                35
        ) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                be.awardStoredExperience(player);
            }
        });

        // Cast
        this.addSlot(new Slot(be, CastFurnaceBlockEntity.SLOT_CAST, 56, 46));

        /* ---------- Player inventory ---------- */
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    /* ---------- Progress helpers ---------- */

    public int getBurnProgress() {
        int burn = data.get(0);
        int total = data.get(1);
        return total == 0 ? 0 : burn * 13 / total;
    }

    public int getCookProgress() {
        int cook = data.get(2);
        int total = data.get(3);
        return total == 0 ? 0 : cook * 24 / total;
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    /* ---------- Shift-click ---------- */

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();

        if (index == CastFurnaceBlockEntity.SLOT_OUTPUT) {
            if (!this.moveItemStackTo(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index >= 4) {
            if (stack.is(ModTags.Items.TOOL_CAST)) {
                if (!this.moveItemStackTo(
                        stack,
                        CastFurnaceBlockEntity.SLOT_CAST,
                        CastFurnaceBlockEntity.SLOT_CAST + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            } else {
                Integer fuelTime = FuelRegistry.INSTANCE.get(stack.getItem());
                if (fuelTime != null && fuelTime > 0) {
                    if (!this.moveItemStackTo(
                            stack,
                            CastFurnaceBlockEntity.SLOT_FUEL,
                            CastFurnaceBlockEntity.SLOT_FUEL + 1,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (ConfigHelper.isValidMaterial(stack)) {
                    if (!this.moveItemStackTo(
                            stack,
                            CastFurnaceBlockEntity.SLOT_INPUT,
                            CastFurnaceBlockEntity.SLOT_INPUT + 1,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 31 && index < 40) {
                    if (!this.moveItemStackTo(stack, 4, 31, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stack, 31, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            if (!this.moveItemStackTo(stack, 4, 40, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }
}
