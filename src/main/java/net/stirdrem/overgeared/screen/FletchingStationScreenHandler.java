package net.stirdrem.overgeared.screen;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.FletchingRecipe;
import net.stirdrem.overgeared.recipe.ItemListInput;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RecipeLookup;

import java.util.Optional;

public class FletchingStationScreenHandler extends AbstractContainerMenu {
    private static final int INPUT_SLOT_TIP = 0;
    private static final int INPUT_SLOT_SHAFT = 1;
    private static final int INPUT_SLOT_FEATHER = 2;
    private static final int INPUT_SLOT_POTION = 3;
    private static final int OUTPUT_SLOT = 4;
    private static final int PLAYER_INVENTORY_START = 5;
    private static final int PLAYER_INVENTORY_END = 32;
    private static final int PLAYER_HOTBAR_START = 33;
    private static final int PLAYER_HOTBAR_END = 40;

    private final Level world;
    private final ContainerLevelAccess access;
    private final Container input;
    private final ResultContainer result = new ResultContainer();
    private final Player player;

    public FletchingStationScreenHandler(int syncId, Inventory playerInv) {
        this(syncId, playerInv, ContainerLevelAccess.NULL);
    }

    public FletchingStationScreenHandler(int syncId, Inventory playerInv, ContainerLevelAccess access) {
        super(ModMenuTypes.FLETCHING_STATION_MENU, syncId);
        this.access = access;
        this.player = playerInv.player;
        this.world = playerInv.player.level();
        this.input = new SimpleContainer(4) {
            @Override
            public void setItem(int i, ItemStack stack) {
                super.setItem(i, stack);
                FletchingStationScreenHandler.this.slotsChanged(this);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                FletchingStationScreenHandler.this.slotsChanged(this);
            }
        };

        // Input slots
        addSlot(new Slot(input, INPUT_SLOT_TIP, 66, 17) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                updateResultSlot();
                super.onTake(player, stack);
            }
        });
        addSlot(new Slot(input, INPUT_SLOT_SHAFT, 48, 35) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                updateResultSlot();
                super.onTake(player, stack);
            }
        });
        addSlot(new Slot(input, INPUT_SLOT_FEATHER, 30, 53) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                updateResultSlot();
                super.onTake(player, stack);
            }
        });

        addSlot(new Slot(input, INPUT_SLOT_POTION, 92, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isPotion(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 1;
            }
        });

        // Output slot
        addSlot(new Slot(result, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                consumeInputs(stack);
                super.onTake(player, stack);
            }
        });

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    private boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, net.minecraft.world.level.block.Blocks.FLETCHING_TABLE);
    }

    @Override
    public void slotsChanged(Container inventory) {
        updateResultSlot();
        super.slotsChanged(inventory);
    }

    private boolean isUpgradeableArrow(ItemStack stack) {
        return stack.is(ModItems.IRON_UPGRADE_ARROW)
                || stack.is(ModItems.STEEL_UPGRADE_ARROW)
                || stack.is(ModItems.DIAMOND_UPGRADE_ARROW);
    }

    /** Recipe input over the 4 input slots (tip, shaft, feather, potion). */
    private ItemListInput recipeInput() {
        return ItemListInput.of(input);
    }

    private Optional<FletchingRecipe> findRecipe() {
        return RecipeLookup.firstMatchValue(world, ModRecipeTypes.FLETCHING, recipeInput());
    }

    private void updateResultSlot() {
        if (world.isClientSide()) return;

        boolean hasInput = false;
        for (int i = 0; i < 3; i++) {
            if (!input.getItem(i).isEmpty()) {
                hasInput = true;
                break;
            }
        }
        if (!hasInput) {
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }
        Optional<FletchingRecipe> opt = findRecipe();
        ItemStack resultStack = ItemStack.EMPTY;
        ItemStack potion = input.getItem(INPUT_SLOT_POTION);
        boolean allowUpgradeableArrowConversion = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get();
        if (!potion.isEmpty()) {
            int arrowSlots = 0;
            int arrowCount = 0;
            int slotNumber = -1;
            for (int i = 0; i < 3; i++) {
                ItemStack slotStack = input.getItem(i);
                if (slotStack.is(Items.ARROW) || (allowUpgradeableArrowConversion && isUpgradeableArrow(slotStack))) {
                    arrowSlots++;
                    arrowCount = slotStack.getCount();
                    slotNumber = i;
                }
            }

            if (arrowSlots == 1) {
                ItemStack arrowStack = input.getItem(slotNumber);
                boolean isUpgradeable = isUpgradeableArrow(arrowStack);

                if (isUpgradeable && !allowUpgradeableArrowConversion) {
                    result.setItem(0, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
                if (potion.is(Items.POTION)) {
                    ItemStack tippedArrows;
                    if (isUpgradeableArrow(input.getItem(slotNumber)))
                        tippedArrows = input.getItem(slotNumber).copy();
                    else tippedArrows = new ItemStack(Items.TIPPED_ARROW, arrowCount);
                    PotionContents potionContents = potion.get(DataComponents.POTION_CONTENTS);
                    if (potionContents != null) {
                        tippedArrows.set(DataComponents.POTION_CONTENTS, potionContents);
                    }
                    resultStack = tippedArrows;
                } else if (potion.is(Items.LINGERING_POTION)) {
                    ItemStack lingeringArrows;
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows = input.getItem(slotNumber).copy();
                    } else {
                        lingeringArrows = new ItemStack(ModItems.LINGERING_ARROW, arrowCount);
                    }
                    PotionContents potionContents = potion.get(DataComponents.POTION_CONTENTS);
                    if (potionContents != null) {
                        lingeringArrows.set(DataComponents.POTION_CONTENTS, potionContents);
                    }
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows.set(ModComponents.LINGERING_STATUS, true);
                    }
                    resultStack = lingeringArrows;
                }
            }
        }

        if (opt.isPresent()) {
            FletchingRecipe recipe = opt.get();

            int tipCount = input.getItem(INPUT_SLOT_TIP).getCount();
            int shaftCount = input.getItem(INPUT_SLOT_SHAFT).getCount();
            int featherCount = input.getItem(INPUT_SLOT_FEATHER).getCount();

            int craftCount = Math.max(Math.min(Math.min(tipCount, shaftCount), featherCount), 1);
            ItemStack baseResult = recipe.assemble(recipeInput());

            if (!potion.isEmpty()) {
                boolean isUpgradeable = isUpgradeableArrow(baseResult);
                if ((isUpgradeable && !allowUpgradeableArrowConversion)) {
                    result.setItem(0, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
                PotionContents potionContents = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

                // 26.3 port: the recipe's free-form tipped/lingering NBT tag keys have no component
                // equivalent; the potion travels in POTION_CONTENTS (+ LINGERING_STATUS on upgrade
                // arrows), as in the upstream 1.21.1 port.
                if ((potion.is(Items.POTION) || potion.is(Items.SPLASH_POTION)) && !recipe.getTippedResult().isEmpty()) {
                    resultStack = recipe.getTippedResult().copy();
                    if (!potionContents.equals(PotionContents.EMPTY)) {
                        resultStack.set(DataComponents.POTION_CONTENTS, potionContents);
                    }
                } else if (potion.is(Items.LINGERING_POTION) && !recipe.getLingeringResult().isEmpty()) {
                    resultStack = recipe.getLingeringResult().copy();

                    if (isUpgradeableArrow(resultStack)) {
                        resultStack.set(ModComponents.LINGERING_STATUS, true);
                    }
                    if (!potionContents.equals(PotionContents.EMPTY)) {
                        resultStack.set(DataComponents.POTION_CONTENTS, potionContents);
                    }
                } else {
                    result.setItem(0, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
            } else {
                resultStack = baseResult.copy();
            }

            if (!resultStack.isEmpty()) {
                int outPer = baseResult.getCount();
                int maxStack = resultStack.getMaxStackSize();
                int maxCraftCount = Math.min(maxStack / outPer, craftCount);
                resultStack.setCount(outPer * maxCraftCount);
            }
        }
        result.setItem(0, resultStack);
        broadcastChanges();
    }

    private void consumeInputs(ItemStack takenResult) {
        if (takenResult.isEmpty()) return;

        Optional<FletchingRecipe> opt = findRecipe();
        if (opt.isPresent()) {
            FletchingRecipe recipe = opt.get();
            ItemStack baseResult = recipe.assemble(recipeInput());
            int baseCount = baseResult.getCount();
            int tookCount = takenResult.getCount();
            int batchesTaken = Math.max(1, tookCount / baseCount);

            for (int i = 0; i < 3; i++) {
                ItemStack stack = input.getItem(i);
                if (!stack.isEmpty()) {
                    stack.shrink(batchesTaken);
                    input.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }
        } else {
            int tookCount = takenResult.getCount();
            for (int i = 0; i < 3; i++) {
                ItemStack stack = input.getItem(i);
                if (!stack.isEmpty()) {
                    stack.shrink(tookCount);
                    input.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }
        }
        ItemStack potionStack = input.getItem(INPUT_SLOT_POTION);
        if (!potionStack.isEmpty()) {
            potionStack.shrink(1);
            input.setItem(INPUT_SLOT_POTION, potionStack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : potionStack);
        }
        updateResultSlot();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copiedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            copiedStack = slotStack.copy();

            if (index == OUTPUT_SLOT) {
                while (canStillCraft()) {
                    ItemStack craftResult = slot.getItem().copy();
                    int maxTransfer = craftResult.getMaxStackSize();

                    int craftCount = Math.min(craftResult.getCount(), maxTransfer);
                    craftResult.setCount(craftCount);

                    if (!moveItemStackTo(craftResult, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, true)) {
                        break;
                    }
                    slot.onQuickCraft(craftResult, copiedStack);
                    consumeInputs(copiedStack);

                    if (slot.getItem().isEmpty()) break;
                }

                slot.setChanged();
            } else if (index >= PLAYER_INVENTORY_START && index <= PLAYER_HOTBAR_END) {
                if (isPotion(slotStack)) {
                    if (!moveItemStackTo(slotStack, INPUT_SLOT_POTION, INPUT_SLOT_POTION + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(slotStack, INPUT_SLOT_TIP, INPUT_SLOT_POTION, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INPUT_SLOT_TIP && index <= INPUT_SLOT_POTION) {
                if (!moveItemStackTo(slotStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == copiedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return copiedStack;
    }

    private boolean canStillCraft() {
        return findRecipe().isPresent();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.access != ContainerLevelAccess.NULL)
            for (int i = 0; i < input.getContainerSize(); i++) {
                ItemStack stack = input.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false, Prediction.SERVER_ONLY);
                    }
                }
            }
    }
}
