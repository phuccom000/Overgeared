package net.stirdrem.overgeared.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeBookTypes;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbstractSmithingAnvilMenu extends RecipeBookMenu<Container> {
    private final Container craftingContainer;
    public final AbstractSmithingAnvilBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private final ResultContainer resultContainer = new ResultContainer();
    private Slot resultSlot;
    private final Player player;
    private final boolean hasBlueprint;
    private final List<Integer> craftingSlotIndices = new ArrayList<>();

    public AbstractSmithingAnvilMenu(MenuType<?> pMenuType, int pContainerId, Inventory inv, AbstractSmithingAnvilBlockEntity entity, ContainerData data, boolean hasBlueprint) {
        super(pMenuType, pContainerId);
        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        craftingContainer = new Container() {
            @Override
            public int getContainerSize() {
                return 9;
            }

            @Override
            public boolean isEmpty() {
                for (int slot : craftingSlotIndices) {
                    if (!slots.get(slot).getItem().isEmpty()) return false;
                }
                return true;
            }

            @Override
            public ItemStack getItem(int index) {
                return slots.get(craftingSlotIndices.get(index)).getItem();
            }

            @Override
            public ItemStack removeItem(int index, int count) {
                return slots.get(craftingSlotIndices.get(index)).remove(count);
            }

            @Override
            public ItemStack removeItemNoUpdate(int index) {
                Slot slot = slots.get(craftingSlotIndices.get(index));
                ItemStack stack = slot.getItem();
                slot.set(ItemStack.EMPTY);
                return stack;
            }

            @Override
            public void setItem(int index, ItemStack stack) {
                slots.get(craftingSlotIndices.get(index)).set(stack);
            }

            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }

            @Override
            public void clearContent() {
                for (int slot : craftingSlotIndices) {
                    slots.get(slot).set(ItemStack.EMPTY);
                }
            }
        };
        checkContainerSize(inv, 12);
        blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;
        this.player = inv.player;

        this.hasBlueprint = hasBlueprint;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 9, 152, 61) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    if (stack.is(ModTags.Items.SMITHING_HAMMERS)) {
                        return true;
                    } else return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return true; // This is crucial for JEI transfers
                }
            }); //hammer

            if (hasBlueprint && ServerConfig.ENABLE_BLUEPRINT_FORGING.get())
                this.addSlot(new SlotItemHandler(iItemHandler, 11, 95, 53) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        if (stack.is(ModItems.BLUEPRINT.get()) || stack.getItem() instanceof SmithingTemplateItem) {
                            return true;
                        } else return false;
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
                    public int getMaxStackSize(@NotNull ItemStack stack) {
                        return 1;
                    }

                    @Override
                    public void set(@NotNull ItemStack stack) {
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
            //System.out.println("Crafting slots: " + craftingSlotIndices);

            /*this.addSlot(new SlotItemHandler(iItemHandler, 9, 124, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false; // Prevent inserting any item
                }
            });*/
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
                    Container craftingContainer = AbstractSmithingAnvilMenu.this.craftingContainer;
                    NonNullList<ItemStack> remainders = player.level()
                            .getRecipeManager().getRemainingItemsFor(ModRecipeTypes.FORGING.get(), AbstractSmithingAnvilMenu.this.craftingContainer, player.level());
                    for (int i = 0; i < remainders.size(); ++i) {
                        ItemStack toRemove = craftingContainer.getItem(i);
                        ItemStack toReplace = remainders.get(i);
                        if (!toRemove.isEmpty()) {
                            craftingContainer.removeItem(i, 1);
                            toRemove = craftingContainer.getItem(i);
                        }

                        if (!toReplace.isEmpty()) {
                            if (toRemove.isEmpty())
                                craftingContainer.setItem(i, toRemove);
                            else if (ItemStack.isSameItemSameTags(toRemove, toReplace)) {
                                toReplace.grow(toRemove.getCount());
                                craftingContainer.setItem(i, toReplace);
                            } else if (!player.getInventory().add(toReplace))
                                player.drop(toReplace, false);
                        }
                    }
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
                        stack.onCraftedBy(AbstractSmithingAnvilMenu.this.player.level(), AbstractSmithingAnvilMenu.this.player, this.removeCount);
                    if (this.container instanceof RecipeHolder recipeHolder)
                        recipeHolder.awardUsedRecipes(AbstractSmithingAnvilMenu.this.player, List.of());
                    this.removeCount = 0;
                }
            };
            this.addSlot(this.resultSlot); //slot 0


        });


        addDataSlots(data);
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
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
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 12;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT - 1, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
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
        boolean isValid = block.defaultBlockState().is(ModTags.Blocks.SMITHING_ANVIL);

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
        //ModMessages.sendToServer(new UpdateAnvilProgressC2SPacket(maxProgress - progress));
        return maxProgress - progress;
    }

    public ItemStack getResultItem() {
        // Check if the block entity exists and has an item handler
        if (blockEntity != null) {
            return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .map(handler -> {
                        // Slot 10 is the output slot based on your menu setup
                        ItemStack result = handler.getStackInSlot(10);
                        // Return a copy to prevent modification of the original stack
                        return result.copy();
                    })
                    .orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getGhostResult() {
        // Return the expected result based on current inputs
        // This could be from a recipe match or your custom logic
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
            contents.accountStack(this.craftingContainer.getItem(i));
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
    public boolean recipeMatches(Recipe<? super Container> recipe) {
        return recipe.matches(this.craftingContainer, this.level);
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
        return ModRecipeBookTypes.FORGING;
    }

    @Override
    public boolean shouldMoveToInventory(int slotIndex) {
        // If slot is one of our forging machine slots, do NOT move to inventory
        if (slotIndex >= TE_INVENTORY_FIRST_SLOT_INDEX &&
                slotIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            return false;
        }

        // Player inventory + hotbar
        return true;
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
            // Only run on client
            if (Minecraft.getInstance().screen instanceof RecipeUpdateListener screen) {
                screen.getRecipeBookComponent()
                        .setupGhostRecipe(recipe, this.slots);
            }


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
                         invIndex < VANILLA_SLOT_COUNT;
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
                         invIndex < VANILLA_SLOT_COUNT;
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
