package net.stirdrem.overgeared.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbstractSmithingAnvilMenu extends RecipeBookMenu<RecipeWrapper> {
    private final Container craftingContainer = new Container() {
        @Override
        public int getContainerSize() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < 9; i++) {
                if (!blockEntity.getItemHandler().getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return blockEntity.getItemHandler().getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = blockEntity.getItemHandler().getStackInSlot(slot).copy();
            if (!stack.isEmpty()) {
                if (stack.getCount() <= amount) {
                    blockEntity.getItemHandler().setStackInSlot(slot, ItemStack.EMPTY);
                    return stack;
                } else {
                    ItemStack split = stack.split(amount);
                    blockEntity.getItemHandler().setStackInSlot(slot, stack);
                    return split;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = blockEntity.getItemHandler().getStackInSlot(slot).copy();
            blockEntity.getItemHandler().setStackInSlot(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            blockEntity.getItemHandler().setStackInSlot(slot, stack);
        }

        @Override
        public void setChanged() {
            blockEntity.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < 9; i++) {
                blockEntity.getItemHandler().setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    };
    public final AbstractSmithingAnvilBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private Slot resultSlot;
    private final Player player;
    private final List<Integer> craftingSlotIndices = new ArrayList<>();

    public AbstractSmithingAnvilMenu(MenuType<?> pMenuType, int pContainerId, Inventory inv, AbstractSmithingAnvilBlockEntity entity, ContainerData data, boolean hasBlueprint) {
        super(pMenuType, pContainerId);
        checkContainerSize(inv, 12);
        blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;
        this.player = inv.player;

        IItemHandler iItemHandler = this.blockEntity.getItemHandler();

        this.addSlot(new SlotItemHandler(iItemHandler, 9, 152, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.SMITHING_HAMMERS);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        }); //hammer

        if (hasBlueprint && ServerConfig.ENABLE_BLUEPRINT_FORGING.get())
            this.addSlot(new SlotItemHandler(iItemHandler, 11, 95, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.BLUEPRINT.get()) || stack.getItem() instanceof SmithingTemplateItem;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return true; // This is crucial for JEI transfers
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
                public void set(ItemStack stack) {
                    // Always limit to a single item
                    super.set(stack.copyWithCount(1));
                }
            });

        //crafting slot
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Slot slotIndex = this.addSlot(new SlotItemHandler(iItemHandler, j + i * 3, 30 + j * 18, 17 + i * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return true; // This is crucial for JEI transfers
                    }
                });
                craftingSlotIndices.add(slotIndex.index); // Store the index, not the Slot object
            }
        }

        //output slot
        this.resultSlot = new SlotItemHandler(iItemHandler, 10, 124, 35) {
            private int removeCount;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                this.checkTakeAchievements(stack);
                super.onTake(player, stack);
            }

            @Override
            public ItemStack remove(int amount) {
                if (this.hasItem())
                    this.removeCount += Math.min(amount, this.getItem().getCount());
                return super.remove(amount);
            }

            @Override
            public void onQuickCraft(ItemStack output, int amount) {
                this.removeCount += amount;
                this.checkTakeAchievements(output);
            }

            @Override
            protected void onSwapCraft(int amount) {
                this.removeCount = amount;
            }

            @Override
            protected void checkTakeAchievements(ItemStack stack) {
                if (this.removeCount > 0)
                    stack.onCraftedBy(AbstractSmithingAnvilMenu.this.player.level(),
                            AbstractSmithingAnvilMenu.this.player, this.removeCount);
                if (this.container instanceof RecipeHolder recipeHolder)
                    recipeHolder.awardUsedRecipes(AbstractSmithingAnvilMenu.this.player, List.of());
                this.removeCount = 0;
            }
        };
        this.addSlot(this.resultSlot);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
    }

    /**
     * Gets the result slot directly.
     * This is preferred over using slot index which varies based on blueprint presence.
     */
    public Slot getResultSlot() {
        return this.resultSlot;
    }


    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static int blueprintEnabled = ServerConfig.ENABLE_BLUEPRINT_FORGING.get() ? 1 : 0;
    private static int VANILLA_FIRST_SLOT_INDEX = 11 + blueprintEnabled;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 12;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot clickedSlot = this.slots.get(index);
        if (clickedSlot == null || !clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = clickedSlot.getItem();
        ItemStack copy = stack.copy();

        int totalSlots = this.slots.size();

        // Determine boundaries dynamically
        int firstPlayerSlot = -1;

        // Player inventory always starts after result slot
        firstPlayerSlot = this.slots.indexOf(this.resultSlot) + 1;

        // =========================
        // If clicking TE slot
        // =========================
        if (index < firstPlayerSlot) {

            // Prevent shift-clicking output
            if (clickedSlot == this.resultSlot) {
                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(stack, firstPlayerSlot, totalSlots, true)) {
                return ItemStack.EMPTY;
            }
        }
        // =========================
        // If clicking Player inventory
        // =========================
        else {

            // Hammer slot detection
            Slot hammerSlot = this.slots.get(0); // first slot added
            boolean blueprintEnabled = ServerConfig.ENABLE_BLUEPRINT_FORGING.get();
            Slot blueprintSlot = blueprintEnabled ? this.slots.get(1) : null;

            // Move hammer
            if (stack.is(ModTags.Items.SMITHING_HAMMERS)) {
                if (!moveItemStackTo(stack,
                        hammerSlot.index,
                        hammerSlot.index + 1,
                        false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Move blueprint
            else if (blueprintEnabled &&
                    (stack.is(ModItems.BLUEPRINT.get())
                            || stack.getItem() instanceof SmithingTemplateItem)) {

                if (!moveItemStackTo(stack,
                        blueprintSlot.index,
                        blueprintSlot.index + 1,
                        false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Move to crafting grid
            else {
                boolean moved = false;

                for (int gridIndex : craftingSlotIndices) {
                    if (moveItemStackTo(stack, gridIndex, gridIndex + 1, false)) {
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
            clickedSlot.set(ItemStack.EMPTY);
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
        int maxProgress = this.data.get(1);  // Max Progress
        int progressArrowSize = 24; // This is the height in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        // get block at the container’s position
        BlockState state = player.level().getBlockState(blockEntity.getBlockPos());
        Block block = state.getBlock();

        // check if the block at that position is in your tag
        boolean isValid = state.is(ModTags.Blocks.SMITHING_ANVIL);

        return AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                block  // only passed here for distance check
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
            return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .map(handler -> handler.getStackInSlot(10).copy())
                    .orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getGhostResult() {
        Optional<ForgingRecipe> recipeOptional = blockEntity.getCurrentRecipe();
        if (recipeOptional.isPresent()) {
            ForgingRecipe recipe = recipeOptional.get();
            if (blockEntity.hasRecipe()) {
                return recipe.getResultItem(level.registryAccess()).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        for (int i = 0; i < this.craftingContainer.getContainerSize(); ++i) {
            contents.accountSimpleStack(this.craftingContainer.getItem(i));
        }
    }

    @Override
    public void clearCraftingContent() {
        if (this.player.level().isClientSide) return;
        this.craftingContainer.clearContent();
    }

    @Override
    public boolean recipeMatches(Recipe<? super RecipeWrapper> recipe) {
        if (recipe instanceof ForgingRecipe forgingRecipe)
            return forgingRecipe.matches(this.craftingContainer, this.level);
        else return false;
    }

    @Override
    public int getResultSlotIndex() {
        return this.resultSlot.index;
    }

    @Override
    public int getGridWidth() {
        return 3;
    }

    @Override
    public int getGridHeight() {
        return 3;
    }

    @Override
    public int getSize() {
        return 9;
    }

    @Override
    public @NotNull RecipeBookType getRecipeBookType() {
        return OvergearedMod.FORGING;
    }

    @Override
    public boolean shouldMoveToInventory(int slot) {
        return slot < (getGridWidth() * getGridHeight());
    }

    public AbstractSmithingAnvilBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public void handlePlacement(boolean placeAll, Recipe<?> recipe, ServerPlayer player) {

        if (!(recipe instanceof ForgingRecipe forgingRecipe)) {
            super.handlePlacement(placeAll, recipe, player);
            return;
        }

        if (player.level().isClientSide) return;

        boolean gridMatches = this.recipeMatches(forgingRecipe);

        // If different recipe → clear first
        if (!gridMatches) {
            this.clearContainer(this.player, this.craftingContainer);
        }

        // Build stacked contents from player inventory
        StackedContents stacked = new StackedContents();
        player.getInventory().fillStackedContents(stacked);

        if (!stacked.canCraft(recipe, null)) {
            super.handlePlacement(placeAll, recipe, player);
            return;
        }

        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        int recipeWidth = forgingRecipe.getWidth();
        int recipeHeight = forgingRecipe.getHeight();

        // Determine where player inventory starts dynamically
        int firstPlayerSlot = this.slots.indexOf(this.resultSlot) + 1;
        int totalSlots = this.slots.size();

        for (int row = 0; row < recipeHeight; row++) {
            for (int col = 0; col < recipeWidth; col++) {

                int recipeIndex = col + row * recipeWidth;
                if (recipeIndex >= ingredients.size()) continue;

                Ingredient ingredient = ingredients.get(recipeIndex);
                if (ingredient.isEmpty()) continue;

                int gridIndex = col + row * 3;
                if (gridIndex >= craftingSlotIndices.size()) continue;

                int slotIndex = craftingSlotIndices.get(gridIndex);
                Slot targetSlot = this.slots.get(slotIndex);

                // If matching stack already exists → grow it
                if (targetSlot.hasItem() && ingredient.test(targetSlot.getItem())) {

                    for (int invIndex = firstPlayerSlot; invIndex < totalSlots; invIndex++) {
                        Slot invSlot = this.slots.get(invIndex);
                        ItemStack invStack = invSlot.getItem();

                        if (!invStack.isEmpty() && ingredient.test(invStack)) {

                            invStack.shrink(1);
                            targetSlot.getItem().grow(1);

                            if (invStack.isEmpty()) {
                                invSlot.set(ItemStack.EMPTY);
                            } else {
                                invSlot.setChanged();
                            }

                            break;
                        }
                    }
                }
                // Empty grid slot → place new stack
                else if (!targetSlot.hasItem()) {

                    for (int invIndex = firstPlayerSlot; invIndex < totalSlots; invIndex++) {
                        Slot invSlot = this.slots.get(invIndex);
                        ItemStack invStack = invSlot.getItem();

                        if (!invStack.isEmpty() && ingredient.test(invStack)) {

                            ItemStack moved = invStack.split(1);
                            targetSlot.set(moved);

                            if (invStack.isEmpty()) {
                                invSlot.set(ItemStack.EMPTY);
                            } else {
                                invSlot.setChanged();
                            }

                            break;
                        }
                    }
                }
            }
        }

        this.broadcastChanges();
    }

}
