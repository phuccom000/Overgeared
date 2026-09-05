package net.stirdrem.overgeared.screen;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.NetherAlloySmelterBlockEntity;

public class NetherAlloySmelterScreenHandler extends AbstractContainerMenu {

    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 11;

    private final NetherAlloySmelterBlockEntity blockEntity;
    private final ContainerData data;

    public NetherAlloySmelterScreenHandler(int syncId, Inventory inv, NetherAlloySmelterBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU, syncId);
        checkContainerSize(blockEntity, TE_INVENTORY_SLOT_COUNT);
        this.blockEntity = blockEntity;
        this.data = data;
        this.addDataSlots(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(blockEntity, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }
        this.addSlot(new Slot(blockEntity, 9, 8, 53)); // Fuel
        this.addSlot(new FurnaceResultSlot(inv.player, blockEntity, 10, 124, 35) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                blockEntity.awardStoredExperience(player);
            }
        });
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), blockEntity.getBlockPos()),
                player, ModBlocks.NETHER_ALLOY_FURNACE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSource = sourceStack.copy();

        int startPlayer = 0;
        int endPlayer = VANILLA_SLOT_COUNT;
        int startTE = TE_INVENTORY_FIRST_SLOT_INDEX;
        int endTE = TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT;

        int inputStart = startTE;
        int inputEnd = startTE + 9;
        int fuelSlot = startTE + 9;
        int outputSlot = startTE + 10;

        if (index >= startTE && index < endTE) {
            if (index == outputSlot) {
                if (!moveItemStackTo(sourceStack, startPlayer, endPlayer, true)) {
                    return ItemStack.EMPTY;
                }
                sourceSlot.onQuickCraft(sourceStack, copyOfSource);
            } else {
                if (!moveItemStackTo(sourceStack, startPlayer, endPlayer, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (index >= startPlayer && index < endPlayer) {
            Integer fuelTime = FuelRegistry.INSTANCE.get(sourceStack.getItem());
            if (fuelTime != null && fuelTime > 0) {
                if (!moveItemStackTo(sourceStack, fuelSlot, fuelSlot + 1, false)) {
                    if (!moveItemStackTo(sourceStack, inputStart, inputEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(sourceStack, inputStart, inputEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copyOfSource;
    }

    public boolean isLit() {
        return data.get(0) > 0;
    }

    public int getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) i = 200;
        return this.data.get(0) * 13 / i;
    }

    public int getCookProgress() {
        int cookTime = this.data.get(2);
        int cookTimeTotal = this.data.get(3);
        return cookTimeTotal != 0 && cookTime != 0 ? cookTime * 24 / cookTimeTotal : 0;
    }
}
