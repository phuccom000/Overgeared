package net.stirdrem.overgeared.screen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
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
public class AbstractSmithingAnvilScreenHandler extends ScreenHandler {
    public final AbstractSmithingAnvilBlockEntity blockEntity;
    private final World world;
    private final PropertyDelegate data;
    private final Slot resultSlot;
    private final PlayerEntity player;
    private final List<Integer> craftingSlotIndices = new ArrayList<>();
    private final boolean blueprintEnabled;

    public AbstractSmithingAnvilScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory inv, AbstractSmithingAnvilBlockEntity entity, PropertyDelegate data, boolean hasBlueprint) {
        super(type, syncId);
        checkSize(entity, 12);
        this.blockEntity = entity;
        this.world = inv.player.getWorld();
        this.data = data;
        this.player = inv.player;
        this.blueprintEnabled = hasBlueprint && ServerConfig.ENABLE_BLUEPRINT_FORGING.get();

        // hammer slot
        this.addSlot(new Slot(entity, 9, 152, 61) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(ModTags.Items.SMITHING_HAMMERS);
            }
        });

        if (blueprintEnabled) {
            this.addSlot(new Slot(entity, 11, 95, 53) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.isOf(ModItems.BLUEPRINT) || stack.getItem() instanceof SmithingTemplateItem;
                }

                @Override
                public int getMaxItemCount() {
                    return 1;
                }

                @Override
                public int getMaxItemCount(ItemStack stack) {
                    return 1;
                }

                @Override
                public void setStack(ItemStack stack) {
                    super.setStack(stack.copyWithCount(1));
                }
            });
        }

        // crafting slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Slot slot = this.addSlot(new Slot(entity, j + i * 3, 30 + j * 18, 17 + i * 18));
                craftingSlotIndices.add(slot.id);
            }
        }

        // output slot
        this.resultSlot = this.addSlot(new AnvilResultSlot(entity, 10, 124, 35, this.player));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addProperties(data);
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
    }

    public Slot getResultSlot() {
        return this.resultSlot;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot clickedSlot = this.slots.get(index);
        if (clickedSlot == null || !clickedSlot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = clickedSlot.getStack();
        ItemStack copy = stack.copy();

        int totalSlots = this.slots.size();
        int firstPlayerSlot = this.resultSlot.id + 1;

        Slot hammerSlot = this.slots.get(0);
        Slot blueprintSlot = blueprintEnabled ? this.slots.get(1) : null;

        if (clickedSlot == hammerSlot || (blueprintEnabled && clickedSlot == blueprintSlot)) {
            if (!insertItem(stack, firstPlayerSlot, totalSlots, true)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }

            clickedSlot.onTakeItem(player, stack);
            return copy;
        }

        // Clicking a block-entity slot
        if (index < firstPlayerSlot) {
            if (!insertItem(stack, firstPlayerSlot, totalSlots, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Clicking player inventory
        else {
            if (stack.isIn(ModTags.Items.SMITHING_HAMMERS)) {
                if (!insertItem(stack, hammerSlot.id, hammerSlot.id + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (blueprintEnabled &&
                    (stack.isOf(ModItems.BLUEPRINT) || stack.getItem() instanceof SmithingTemplateItem)) {

                if (!insertItem(stack, blueprintSlot.id, blueprintSlot.id + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean moved = false;

                for (int gridId : craftingSlotIndices) {
                    if (insertItem(stack, gridId, gridId + 1, false)) {
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
            clickedSlot.setStack(ItemStack.EMPTY);
        } else {
            clickedSlot.markDirty();
        }

        clickedSlot.onTakeItem(player, stack);

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
    public boolean canUse(PlayerEntity player) {
        BlockState state = player.getWorld().getBlockState(blockEntity.getPos());
        Block block = state.getBlock();

        boolean isValid = state.isIn(ModTags.Blocks.SMITHING_ANVIL);

        return ScreenHandler.canUse(
                ScreenHandlerContext.create(world, blockEntity.getPos()),
                player,
                block
        ) && isValid;
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
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
            return blockEntity.getStack(10).copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getGhostResult() {
        Optional<ForgingRecipe> recipeOptional = blockEntity.getCurrentRecipe();
        if (recipeOptional.isPresent()) {
            ForgingRecipe recipe = recipeOptional.get();
            if (blockEntity.hasRecipe()) {
                return recipe.getOutput(world.getRegistryManager()).copy();
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

        public AnvilResultSlot(AbstractSmithingAnvilBlockEntity inventory, int index, int x, int y, PlayerEntity player) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack takeStack(int amount) {
            if (this.hasStack()) {
                this.removeCount += Math.min(amount, this.getStack().getCount());
            }
            return super.takeStack(amount);
        }

        @Override
        protected void onCrafted(ItemStack stack, int amount) {
            this.removeCount += amount;
            this.checkTakeAchievements(stack);
        }

        protected void checkTakeAchievements(ItemStack stack) {
            if (this.removeCount > 0) {
                stack.onCraft(AbstractSmithingAnvilScreenHandler.this.player.getWorld(), AbstractSmithingAnvilScreenHandler.this.player, this.removeCount);
            }
            this.removeCount = 0;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.checkTakeAchievements(stack);
            super.onTakeItem(player, stack);
        }
    }
}
