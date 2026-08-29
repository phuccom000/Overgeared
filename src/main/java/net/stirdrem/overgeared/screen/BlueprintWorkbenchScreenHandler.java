package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolType;

public class BlueprintWorkbenchScreenHandler extends ScreenHandler {
    private final Inventory inputContainer;
    private final Inventory outputContainer;
    private final PropertyDelegate data;

    public BlueprintWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), new SimpleInventory(1), new ArrayPropertyDelegate(2));
    }

    public BlueprintWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory,
                                            Inventory inputContainer, Inventory outputContainer,
                                            PropertyDelegate data) {
        super(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU, syncId);
        this.inputContainer = inputContainer;
        this.outputContainer = outputContainer;
        this.data = data;

        // Input slot (slot 0)
        this.addSlot(new Slot(inputContainer, 0, 48, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.EMPTY_BLUEPRINT);
            }
        });

        // Output slot (slot 1) - not player interactable
        this.addSlot(new Slot(outputContainer, 0, 106, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        // Player inventory (slots 2-37)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar (slots 38-46)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addProperties(data);
    }

    public void createBlueprint(ToolType toolType) {
        ItemStack input = this.inputContainer.getStack(0);
        if (!input.isOf(ModItems.EMPTY_BLUEPRINT) || input.isEmpty()) {
            return;
        }

        ItemStack output = this.outputContainer.getStack(0);

        if (output.isEmpty()) {
            ItemStack newOutput = new ItemStack(ModItems.BLUEPRINT);
            NbtCompound tag = newOutput.getOrCreateNbt();
            BlueprintQuality quality = BlueprintQuality.WELL;
            tag.putString("ToolType", toolType.getId());
            tag.putString("Quality", quality.getDisplayName());
            tag.putInt("Uses", 0);

            this.outputContainer.setStack(0, newOutput);
            input.decrement(1);
            if (input.isEmpty()) {
                this.inputContainer.setStack(0, ItemStack.EMPTY);
            } else {
                this.inputContainer.setStack(0, input);
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 1) {
                if (!this.insertItem(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemstack1, itemstack);
            } else if (index == 0) {
                if (!this.insertItem(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.isOf(ModItems.EMPTY_BLUEPRINT)) {
                if (!this.insertItem(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 2 && index < 29) {
                if (!this.insertItem(itemstack1, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 29 && index < 38) {
                if (!this.insertItem(itemstack1, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inputContainer.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, inputContainer);
        this.dropInventory(player, outputContainer);
    }
}
