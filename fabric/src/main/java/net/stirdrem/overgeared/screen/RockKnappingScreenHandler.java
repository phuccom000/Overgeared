package net.stirdrem.overgeared.screen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.datapack.KnappingResourceReloadListener;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;
import net.stirdrem.overgeared.util.ModTags;

public class RockKnappingScreenHandler extends AbstractContainerMenu {
    private final Container craftingGrid = new SimpleContainer(9); // 3x3 grid
    private final Container resultContainer = new SimpleContainer(1); // Output slot
    private final Level world;
    private final RecipeManager recipeManager;
    private final Player player;
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

    public RockKnappingScreenHandler(int syncId, Inventory playerInv, RecipeManager recipeManager) {
        super(ModMenuTypes.ROCK_KNAPPING_MENU, syncId);
        this.world = playerInv.player.level();
        this.recipeManager = recipeManager;
        this.player = playerInv.player;

        // Check if player has a knappable rock in either hand
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        boolean valid = false;

        if (mainHandItem.is(ModTags.Items.KNAPPABLE) && offHandItem.is(ModTags.Items.KNAPPABLE)) {
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
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }

        // Result/output slot
        this.addSlot(new Slot(resultContainer, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                knappingFinished = true;
                resultCollected = true;
                if (player instanceof ServerPlayer serverPlayer) {
                    ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                }
            }

            @Override
            public boolean mayPickup(Player player) {
                return !getItem().isEmpty() && !knappingFinished;
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

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public ResourceLocation getUnchippedTexture() {
        return KnappingResourceReloadListener.getTexture(inputRock);
    }

    public SoundEvent getSound() {
        return KnappingResourceReloadListener.getSound(inputRock);
    }

    @Override
    public boolean stillValid(Player player) {
        // Only close if the input rock disappears before being consumed
        if (!rockConsumed) {
            return hasInputRock(player);
        }

        // After rock is consumed, keep menu open until player closes it manually
        return true;
    }

    private boolean hasInputRock(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean hasRock =
                mainHand.is(ModTags.Items.KNAPPABLE) &&
                        offHand.is(ModTags.Items.KNAPPABLE);

        if (!hasRock && !player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
        }

        return hasRock;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == RESULT_SLOT_INDEX) {
                if (!this.moveItemStackTo(itemstack1, PLAYER_FIRST_SLOT_INDEX, PLAYER_LAST_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);

                if (itemstack1.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                    knappingFinished = true;
                    resultCollected = true;

                    if (player instanceof ServerPlayer serverPlayer) {
                        ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                    }
                } else {
                    slot.setChanged();
                }

                return itemstack;
            } else if (index >= PLAYER_FIRST_SLOT_INDEX && index <= PLAYER_LAST_SLOT_INDEX) {
                if (index < PLAYER_FIRST_SLOT_INDEX + 27) {
                    if (!this.moveItemStackTo(itemstack1, PLAYER_FIRST_SLOT_INDEX + 27, PLAYER_LAST_SLOT_INDEX + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(itemstack1, PLAYER_FIRST_SLOT_INDEX, PLAYER_FIRST_SLOT_INDEX + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (index >= GRID_FIRST_SLOT_INDEX && index <= GRID_LAST_SLOT_INDEX) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public void setChip(int index) {
        if (knappingFinished || resultCollected) return;

        if (!rockConsumed) {
            consumeInputRock();
            rockConsumed = true;
        }

        if (!craftingGrid.getItem(index).isEmpty()) {
            craftingGrid.setItem(index, ItemStack.EMPTY);
        } else {
            craftingGrid.setItem(index, new ItemStack(inputRock.getItem()));
        }

        updateResult();
    }

    private void consumeInputRock() {
        if (world.isClientSide) return;

        ItemStack mainHand = player.getMainHandItem();

        if (ItemStack.isSameItemSameTags(mainHand, inputRock) && mainHand.getCount() > 0) {
            mainHand.shrink(1);
            player.getInventory().setChanged();
        }
    }

    private void updateResult() {
        if (world == null || knappingFinished || resultCollected) return;

        RockKnappingRecipe matchingRecipe = recipeManager
                .getAllRecipesFor(ModRecipeTypes.KNAPPING)
                .stream()
                .filter(recipe -> recipe.getIngredient().test(inputRock))
                .filter(recipe -> recipe.matches(craftingGrid, world))
                .findFirst()
                .orElse(null);

        if (matchingRecipe != null) {
            resultContainer.setItem(0,
                    matchingRecipe.getResultItem(world.registryAccess()).copy());
        } else {
            resultContainer.setItem(0, ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    public boolean isChipped(int index) {
        return !craftingGrid.getItem(index).isEmpty();
    }

    public void clearGrid() {
        for (int i = 0; i < 9; i++) {
            craftingGrid.setItem(i, ItemStack.EMPTY);
        }
        resultContainer.setItem(0, ItemStack.EMPTY);
        this.broadcastChanges();
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
    public void removed(Player player) {
        super.removed(player);
        if (player.containerMenu != this) {
            return;
        }
        if (!player.level().isClientSide) {
            ItemStack result = resultContainer.getItem(0);
            if (!result.isEmpty() && !resultCollected) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                }

                if (!player.getInventory().add(result.copy())) {
                    player.drop(result.copy(), false);
                }

                resultContainer.setItem(0, ItemStack.EMPTY);
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
