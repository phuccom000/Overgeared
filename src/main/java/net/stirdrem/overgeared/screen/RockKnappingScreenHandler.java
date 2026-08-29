package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.datapack.KnappingResourceReloadListener;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;
import net.stirdrem.overgeared.util.ModTags;

public class RockKnappingScreenHandler extends ScreenHandler {
    private final Inventory craftingGrid = new SimpleInventory(9); // 3x3 grid
    private final Inventory resultContainer = new SimpleInventory(1); // Output slot
    private final World world;
    private final RecipeManager recipeManager;
    private final PlayerEntity player;
    private ItemStack inputRock; // The rock being knapped
    private boolean knappingFinished = false;
    private boolean resultCollected = false;
    private boolean rockConsumed = false; // Track if rock has been consumed

    // Slot indices constants
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 36; // 27 main + 9 hotbar
    private static final int PLAYER_FIRST_SLOT_INDEX = 0;
    private static final int PLAYER_LAST_SLOT_INDEX = PLAYER_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT - 1;
    private static final int GRID_FIRST_SLOT_INDEX = PLAYER_LAST_SLOT_INDEX + 1;
    private static final int GRID_LAST_SLOT_INDEX = GRID_FIRST_SLOT_INDEX + 8;
    private static final int RESULT_SLOT_INDEX = GRID_LAST_SLOT_INDEX + 1;

    public RockKnappingScreenHandler(int syncId, PlayerInventory playerInv, RecipeManager recipeManager) {
        super(ModMenuTypes.ROCK_KNAPPING_MENU, syncId);
        this.world = playerInv.player.getWorld();
        this.recipeManager = recipeManager;
        this.player = playerInv.player;

        // Check if player has a knappable rock in either hand
        ItemStack mainHandItem = player.getMainHandStack();
        ItemStack offHandItem = player.getOffHandStack();
        boolean valid = false;

        if (mainHandItem.isIn(ModTags.Items.KNAPPABLE) && offHandItem.isIn(ModTags.Items.KNAPPABLE)) {
            this.inputRock = mainHandItem.copy();
            valid = true;
        }

        if (!valid) {
            this.inputRock = ItemStack.EMPTY; // dummy
        }

        addPlayerHotbar(playerInv);
        addPlayerInventory(playerInv);

        // Virtual grid slots (off-screen, just for tracking chip state)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(craftingGrid, i, -1000, -1000) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity player) {
                    return false;
                }
            });
        }

        // Result/output slot
        this.addSlot(new Slot(resultContainer, 0, 124, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
                knappingFinished = true;
                resultCollected = true;
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                }
            }

            @Override
            public boolean canTakeItems(PlayerEntity player) {
                return !getStack().isEmpty() && !knappingFinished;
            }
        });
    }

    public boolean isResultCollected() {
        return resultCollected;
    }

    public void markResultCollected() {
        this.resultCollected = true;
        this.knappingFinished = true;
    }

    private void addPlayerInventory(PlayerInventory playerInv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInv) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public Identifier getUnchippedTexture() {
        return KnappingResourceReloadListener.getTexture(inputRock);
    }

    public SoundEvent getSound() {
        return KnappingResourceReloadListener.getSound(inputRock);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // Only close if the input rock disappears before being consumed
        if (!rockConsumed) {
            return hasInputRock(player);
        }

        // After rock is consumed, keep menu open until player closes it manually
        return true;
    }

    private boolean hasInputRock(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        boolean hasRock =
                mainHand.isIn(ModTags.Items.KNAPPABLE) &&
                        offHand.isIn(ModTags.Items.KNAPPABLE);

        if (!hasRock && !player.getWorld().isClient && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.closeHandledScreen();
        }

        return hasRock;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == RESULT_SLOT_INDEX) {
                if (!this.insertItem(itemstack1, PLAYER_FIRST_SLOT_INDEX, PLAYER_LAST_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(itemstack1, itemstack);

                if (itemstack1.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                    knappingFinished = true;
                    resultCollected = true;

                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                    }
                } else {
                    slot.markDirty();
                }

                return itemstack;
            } else if (index >= PLAYER_FIRST_SLOT_INDEX && index <= PLAYER_LAST_SLOT_INDEX) {
                if (index < PLAYER_FIRST_SLOT_INDEX + 27) {
                    if (!this.insertItem(itemstack1, PLAYER_FIRST_SLOT_INDEX + 27, PLAYER_LAST_SLOT_INDEX + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.insertItem(itemstack1, PLAYER_FIRST_SLOT_INDEX, PLAYER_FIRST_SLOT_INDEX + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (index >= GRID_FIRST_SLOT_INDEX && index <= GRID_LAST_SLOT_INDEX) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, itemstack1);
        }

        return itemstack;
    }

    public void setChip(int index) {
        if (knappingFinished || resultCollected) return;

        if (!rockConsumed) {
            consumeInputRock();
            rockConsumed = true;
        }

        if (!craftingGrid.getStack(index).isEmpty()) {
            craftingGrid.setStack(index, ItemStack.EMPTY);
        } else {
            craftingGrid.setStack(index, new ItemStack(inputRock.getItem()));
        }

        updateResult();
    }

    private void consumeInputRock() {
        if (world.isClient) return;

        ItemStack mainHand = player.getMainHandStack();

        if (ItemStack.canCombine(mainHand, inputRock) && mainHand.getCount() > 0) {
            mainHand.decrement(1);
            player.getInventory().markDirty();
        }
    }

    private void updateResult() {
        if (world == null || knappingFinished || resultCollected) return;

        RockKnappingRecipe matchingRecipe = recipeManager
                .listAllOfType(ModRecipeTypes.KNAPPING)
                .stream()
                .filter(recipe -> recipe.getIngredient().test(inputRock))
                .filter(recipe -> recipe.matches(craftingGrid, world))
                .findFirst()
                .orElse(null);

        if (matchingRecipe != null) {
            resultContainer.setStack(0,
                    matchingRecipe.getOutput(world.getRegistryManager()).copy());
        } else {
            resultContainer.setStack(0, ItemStack.EMPTY);
        }

        this.sendContentUpdates();
    }

    public boolean isChipped(int index) {
        return !craftingGrid.getStack(index).isEmpty();
    }

    public void clearGrid() {
        for (int i = 0; i < 9; i++) {
            craftingGrid.setStack(i, ItemStack.EMPTY);
        }
        resultContainer.setStack(0, ItemStack.EMPTY);
        this.sendContentUpdates();
    }

    public boolean isKnappingFinished() {
        return knappingFinished;
    }

    public boolean hasAnyChippedSpots() {
        for (int i = 0; i < 9; i++) {
            if (isChipped(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (player.currentScreenHandler != this) {
            return;
        }
        if (!player.getWorld().isClient) {
            ItemStack result = resultContainer.getStack(0);
            if (!result.isEmpty() && !resultCollected) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                }

                if (!player.getInventory().insertStack(result.copy())) {
                    player.dropItem(result.copy(), false);
                }

                resultContainer.setStack(0, ItemStack.EMPTY);
            }
        }
    }

    // Helper method to get the current grid state as a boolean array
    public boolean[][] getGridState() {
        boolean[][] grid = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            grid[row][col] = isChipped(i);
        }
        return grid;
    }
}
