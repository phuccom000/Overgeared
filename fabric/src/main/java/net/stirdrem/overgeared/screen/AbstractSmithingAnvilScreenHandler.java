package net.stirdrem.overgeared.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The original Forge version extends RecipeBookMenu for the vanilla recipe-book quick-fill
 * button. Porting that requires a whole parallel StackedContents/SlotItemHandler system tied to
 * Forge's item-handler capability, which this port doesn't have - so the recipe book integration
 * is dropped here. Everything else (slots, quick-move, hammer/blueprint filtering, crafting
 * rewards on take) is preserved.
 */
public class AbstractSmithingAnvilScreenHandler extends AbstractContainerMenu {
    public final AbstractSmithingAnvilBlockEntity blockEntity;
    private final Level world;
    private final ContainerData data;
    private final Slot resultSlot;
    private final Player player;
    private final List<Integer> craftingSlotIndices = new ArrayList<>();
    private final boolean blueprintEnabled;

    public AbstractSmithingAnvilScreenHandler(MenuType<?> type, int syncId, Inventory inv, AbstractSmithingAnvilBlockEntity entity, ContainerData data, boolean hasBlueprint) {
        super(type, syncId);
        checkContainerSize(entity, 12);
        this.blockEntity = entity;
        this.world = inv.player.level();
        this.data = data;
        this.player = inv.player;
        this.blueprintEnabled = hasBlueprint && ServerConfig.ENABLE_BLUEPRINT_FORGING.get();

        // hammer slot
        this.addSlot(new Slot(entity, 9, 152, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.SMITHING_HAMMERS);
            }
        });

        if (blueprintEnabled) {
            this.addSlot(new Slot(entity, 11, 95, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.BLUEPRINT) || stack.getItem() instanceof SmithingTemplateItem;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }

                @Override
                public void setByPlayer(ItemStack stack) {
                    super.setByPlayer(stack.copyWithCount(1));
                }
            });
        }

        // crafting slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Slot slot = this.addSlot(new Slot(entity, j + i * 3, 30 + j * 18, 17 + i * 18));
                craftingSlotIndices.add(slot.index);
            }
        }

        // output slot
        this.resultSlot = this.addSlot(new AnvilResultSlot(entity, 10, 124, 35, this.player));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
    }

    public Slot getResultSlot() {
        return this.resultSlot;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot clickedSlot = this.slots.get(index);
        if (clickedSlot == null || !clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = clickedSlot.getItem();
        ItemStack copy = stack.copy();

        int totalSlots = this.slots.size();
        int firstPlayerSlot = this.resultSlot.index + 1;

        Slot hammerSlot = this.slots.get(0);
        Slot blueprintSlot = blueprintEnabled ? this.slots.get(1) : null;

        if (clickedSlot == hammerSlot || (blueprintEnabled && clickedSlot == blueprintSlot)) {
            if (!moveItemStackTo(stack, firstPlayerSlot, totalSlots, true)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                clickedSlot.setByPlayer(ItemStack.EMPTY);
            } else {
                clickedSlot.setChanged();
            }

            clickedSlot.onTake(player, stack);
            return copy;
        }

        // Clicking a block-entity slot
        if (index < firstPlayerSlot) {
            if (!moveItemStackTo(stack, firstPlayerSlot, totalSlots, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Clicking player inventory
        else {
            if (stack.is(ModTags.Items.SMITHING_HAMMERS)) {
                if (!moveItemStackTo(stack, hammerSlot.index, hammerSlot.index + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (blueprintEnabled &&
                    (stack.is(ModItems.BLUEPRINT) || stack.getItem() instanceof SmithingTemplateItem)) {

                if (!moveItemStackTo(stack, blueprintSlot.index, blueprintSlot.index + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean moved = false;

                for (int gridId : craftingSlotIndices) {
                    if (moveItemStackTo(stack, gridId, gridId + 1, false)) {
                        moved = true;
                        break;
                    }
                }

                if (!moved) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            clickedSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }

        clickedSlot.onTake(player, stack);

        return copy;
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 24;

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockState state = player.level().getBlockState(blockEntity.getBlockPos());
        Block block = state.getBlock();

        boolean isValid = state.is(ModTags.Blocks.SMITHING_ANVIL);

        return AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(world, blockEntity.getBlockPos()),
                player,
                block
        ) && isValid;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getRemainingHits() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress - progress;
    }

    public ItemStack getResultItem() {
        if (blockEntity != null) {
            return blockEntity.getItem(10).copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getGhostResult() {
        Optional<ForgingRecipe> recipeOptional = blockEntity.getCurrentRecipe();
        if (recipeOptional.isPresent()) {
            ForgingRecipe recipe = recipeOptional.get();
            if (blockEntity.hasRecipe()) {
                return recipe.getResultItem(world.registryAccess()).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public AbstractSmithingAnvilBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * Awards recipe-crafted rewards (advancement/statistic tracking) on take - the vanilla
     * ResultSlot pattern, just targeting our block entity's Inventory directly instead of
     * SlotItemHandler.
     */
    private class AnvilResultSlot extends Slot {
        private int removeCount;

        public AnvilResultSlot(AbstractSmithingAnvilBlockEntity inventory, int index, int x, int y, Player player) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            if (this.hasItem()) {
                this.removeCount += Math.min(amount, this.getItem().getCount());
            }
            return super.remove(amount);
        }

        @Override
        protected void onQuickCraft(ItemStack stack, int amount) {
            this.removeCount += amount;
            this.checkTakeAchievements(stack);
        }

        protected void checkTakeAchievements(ItemStack stack) {
            if (this.removeCount > 0) {
                stack.onCraftedBy(AbstractSmithingAnvilScreenHandler.this.player.level(), AbstractSmithingAnvilScreenHandler.this.player, this.removeCount);
            }
            this.removeCount = 0;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            this.checkTakeAchievements(stack);
            super.onTake(player, stack);
        }
    }
}
