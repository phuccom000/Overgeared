package net.stirdrem.overgeared.screen;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.AlloySmelterBlockEntity;

public class AlloySmelterScreenHandler extends ScreenHandler {

    private static final int VANILLA_SLOT_COUNT = 36; // 9 hotbar + 27 inventory
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 6;

    private final AlloySmelterBlockEntity blockEntity;
    private final PropertyDelegate data;

    public AlloySmelterScreenHandler(int syncId, PlayerInventory inv, AlloySmelterBlockEntity blockEntity, PropertyDelegate data) {
        super(ModMenuTypes.ALLOY_SMELTER_MENU, syncId);
        checkSize(blockEntity, TE_INVENTORY_SLOT_COUNT);
        this.blockEntity = blockEntity;
        this.data = data;
        this.addProperties(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new Slot(blockEntity, 0, 39, 26)); // Input 1
        this.addSlot(new Slot(blockEntity, 1, 57, 26)); // Input 2
        this.addSlot(new Slot(blockEntity, 2, 39, 44)); // Input 3
        this.addSlot(new Slot(blockEntity, 3, 57, 44)); // Input 4
        this.addSlot(new Slot(blockEntity, 4, 8, 53)); // Fuel
        this.addSlot(new FurnaceOutputSlot(inv.player, blockEntity, 5, 124, 35) {
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
                blockEntity.awardStoredExperience(player);
            }
        });
    }

    private void addPlayerInventory(PlayerInventory playerInv) {
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    }

    private void addPlayerHotbar(PlayerInventory playerInv) {
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(ScreenHandlerContext.create(player.getWorld(), blockEntity.getPos()),
                player, ModBlocks.ALLOY_FURNACE);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSource = sourceStack.copy();

        int startPlayer = 0;
        int endPlayer = VANILLA_SLOT_COUNT;
        int startTE = TE_INVENTORY_FIRST_SLOT_INDEX;
        int endTE = TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT;

        int inputStart = startTE;
        int inputEnd = startTE + 4;
        int fuelSlot = startTE + 4;
        int outputSlot = startTE + 5;

        if (index >= startTE && index < endTE) {
            if (index == outputSlot) {
                if (!insertItem(sourceStack, startPlayer, endPlayer, true)) {
                    return ItemStack.EMPTY;
                }
                sourceSlot.onQuickTransfer(sourceStack, copyOfSource);
            } else {
                if (!insertItem(sourceStack, startPlayer, endPlayer, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (index >= startPlayer && index < endPlayer) {
            Integer fuelTime = FuelRegistry.INSTANCE.get(sourceStack.getItem());
            if (fuelTime != null && fuelTime > 0) {
                if (!insertItem(sourceStack, fuelSlot, fuelSlot + 1, false)) {
                    if (!insertItem(sourceStack, inputStart, inputEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!insertItem(sourceStack, inputStart, inputEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.setStack(ItemStack.EMPTY);
        } else {
            sourceSlot.markDirty();
        }

        sourceSlot.onTakeItem(player, sourceStack);
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
