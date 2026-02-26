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

        // Add TE slots first (indices 0-11)
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {

            // Crafting slots (0-8)
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    int slotIndex = this.addSlot(new SlotItemHandler(iItemHandler, j + i * 3, 30 + j * 18, 17 + i * 18) {
                        @Override
                        public boolean mayPickup(Player player) {
                            return true; // This is crucial for JEI transfers
                        }
                    }).index;
                    craftingSlotIndices.add(slotIndex);
                }
            }

            // Hammer slot (index 9)
            this.addSlot(new SlotItemHandler(iItemHandler, 9, 152, 61) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(ModTags.Items.SMITHING_HAMMERS);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return true;
                }
            });

            // Output slot (index 10)
            this.resultSlot = new SlotItemHandler(iItemHandler, 10, 124, 35) {
                private int removeCount;

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return true;
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

            // Blueprint slot (index 11) - optional
            if (hasBlueprint && ServerConfig.ENABLE_BLUEPRINT_FORGING.get()) {
                this.addSlot(new SlotItemHandler(iItemHandler, 11, 95, 53) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return stack.is(ModItems.BLUEPRINT.get()) || stack.getItem() instanceof SmithingTemplateItem;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return true;
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }

                    @Override
                    public int getMaxStackSize(@NotNull ItemStack stack) {
                        return 1;
                    }

                    @Override
                    public void set(@NotNull ItemStack stack) {
                        super.set(stack.copyWithCount(1));
                    }
                });
            }
        });

        // Add player inventory slots AFTER TE slots (starting at index 12)
        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // Player inventory slots (indices 12-38)
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                int slotIndex = l + i * 9 + 9; // Player inventory slots start at index 9 in Inventory
                this.addSlot(new Slot(playerInventory, slotIndex, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        // Hotbar slots (indices 39-47)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 12;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 12;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Calculate slot ranges
        int totalSlots = slots.size();

        // TE slots ranges
        int teSlotsStart = 0;
        int teSlotsEnd = 11; // inclusive

        // Specific TE slot indices
        int HAMMER_SLOT = 9;
        int OUTPUT_SLOT = 10;
        int BLUEPRINT_SLOT = 11;

        // Player inventory slots
        int playerInvStart = 12;
        int playerInvEnd = totalSlots - 1;

        // If clicking on player inventory slot
        if (pIndex >= playerInvStart && pIndex <= playerInvEnd) {

            // Check if it's a hammer
            if (sourceStack.is(ModTags.Items.SMITHING_HAMMERS)) {
                // Try to move to hammer slot first
                if (!moveItemStackTo(sourceStack, HAMMER_SLOT, HAMMER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Check if it's a blueprint
            else if (ServerConfig.ENABLE_BLUEPRINT_FORGING.get() &&
                    (sourceStack.is(ModItems.BLUEPRINT.get()) || sourceStack.getItem() instanceof SmithingTemplateItem)) {
                // Try to move to blueprint slot
                if (!moveItemStackTo(sourceStack, BLUEPRINT_SLOT, BLUEPRINT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Regular item - try to move to crafting grid (slots 0-8)
            else {
                // First try to move to empty crafting slots
                if (!moveItemStackTo(sourceStack, teSlotsStart, HAMMER_SLOT, false)) {
                    // If no space in crafting grid, try other TE slots (excluding output)
                    if (!moveItemStackTo(sourceStack, HAMMER_SLOT, OUTPUT_SLOT, false)) {
                        // If still no space, try remaining TE slots
                        if (!moveItemStackTo(sourceStack, BLUEPRINT_SLOT, BLUEPRINT_SLOT + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
        }
        // If clicking on TE slot
        else if (pIndex >= teSlotsStart && pIndex <= teSlotsEnd) {

            // Don't allow moving items from output slot back to inventory via quick move
            // (prevents duping/exploits)
            if (pIndex == OUTPUT_SLOT) {
                return ItemStack.EMPTY;
            }

            // Move from TE to player inventory
            if (!moveItemStackTo(sourceStack, playerInvStart, playerInvEnd + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Invalid slot
        else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
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

        boolean isValid = block.defaultBlockState().is(ModTags.Blocks.SMITHING_ANVIL);

        return AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                block
        ) && isValid;
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

        for (int i = 0; i < this.craftingContainer.getContainerSize(); i++) {
            ItemStack stack = this.craftingContainer.getItem(i);

            if (!stack.isEmpty()) {
                // Try to move back to player inventory
                if (!this.moveItemStackTo(stack,
                        VANILLA_FIRST_SLOT_INDEX,
                        VANILLA_SLOT_COUNT,
                        false)) {

                    // If inventory full → drop to player
                    this.player.drop(stack, false);
                }

                this.craftingContainer.setItem(i, ItemStack.EMPTY);
            }
        }
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
    public RecipeBookType getRecipeBookType() {
        return OvergearedMod.FORGING;
    }

    @Override
    public boolean shouldMoveToInventory(int slot) {
        return slot < (getGridWidth() * getGridHeight());
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
            this.clearCraftingContent();
        }

        StackedContents stacked = new StackedContents();
        player.getInventory().fillStackedContents(stacked);

        if (!stacked.canCraft(recipe, null)) {
            return;
        }

        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        int recipeWidth = forgingRecipe.getWidth();
        int recipeHeight = forgingRecipe.getHeight();

        for (int row = 0; row < recipeHeight; row++) {
            for (int col = 0; col < recipeWidth; col++) {

                int recipeIndex = col + row * recipeWidth;
                if (recipeIndex >= ingredients.size()) continue;

                Ingredient ingredient = ingredients.get(recipeIndex);
                if (ingredient.isEmpty()) continue;

                int gridIndex = col + row * 3;
                int slotIndex = craftingSlotIndices.get(gridIndex);
                Slot targetSlot = this.slots.get(slotIndex);

                // If already matching stack exists → grow it
                if (targetSlot.hasItem() && ingredient.test(targetSlot.getItem())) {

                    for (int invIndex = VANILLA_FIRST_SLOT_INDEX;
                         invIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
                         invIndex++) {

                        Slot invSlot = this.slots.get(invIndex);
                        ItemStack stack = invSlot.getItem();

                        if (!stack.isEmpty() && ingredient.test(stack)) {

                            stack.shrink(1);
                            targetSlot.getItem().grow(1);

                            if (stack.isEmpty()) {
                                invSlot.set(ItemStack.EMPTY);
                            } else {
                                invSlot.setChanged();
                            }

                            break;
                        }
                    }
                }
                // Empty slot → place fresh
                else if (!targetSlot.hasItem()) {

                    for (int invIndex = VANILLA_FIRST_SLOT_INDEX;
                         invIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
                         invIndex++) {

                        Slot invSlot = this.slots.get(invIndex);
                        ItemStack stack = invSlot.getItem();

                        if (!stack.isEmpty() && ingredient.test(stack)) {

                            ItemStack moved = stack.split(1);
                            targetSlot.set(moved);

                            if (stack.isEmpty()) {
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

    public AbstractSmithingAnvilBlockEntity getBlockEntity() {
        return blockEntity;
    }
}