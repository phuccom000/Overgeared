package net.stirdrem.overgeared.screen;

import com.google.common.collect.Lists;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.FletchingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;

public class FletchingStationScreenHandler extends ScreenHandler {
    private static final int INPUT_SLOT_TIP = 0;
    private static final int INPUT_SLOT_SHAFT = 1;
    private static final int INPUT_SLOT_FEATHER = 2;
    private static final int INPUT_SLOT_POTION = 3;
    private static final int OUTPUT_SLOT = 4;
    private static final int PLAYER_INVENTORY_START = 5;
    private static final int PLAYER_INVENTORY_END = 32;
    private static final int PLAYER_HOTBAR_START = 33;
    private static final int PLAYER_HOTBAR_END = 40;

    private final World world;
    private final ScreenHandlerContext access;
    private final Inventory input;
    private final CraftingResultInventory result = new CraftingResultInventory();
    private final RecipeManager recipeManager;
    private final PlayerEntity player;

    public FletchingStationScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, ScreenHandlerContext.EMPTY);
    }

    public FletchingStationScreenHandler(int syncId, PlayerInventory playerInv, ScreenHandlerContext access) {
        super(ModMenuTypes.FLETCHING_STATION_MENU, syncId);
        this.access = access;
        this.recipeManager = playerInv.player.getWorld().getRecipeManager();
        this.player = playerInv.player;
        this.world = playerInv.player.getWorld();
        this.input = new SimpleInventory(4) {
            @Override
            public void setStack(int i, ItemStack stack) {
                super.setStack(i, stack);
                FletchingStationScreenHandler.this.onContentChanged(this);
            }

            @Override
            public void markDirty() {
                super.markDirty();
                FletchingStationScreenHandler.this.onContentChanged(this);
            }
        };

        // Input slots
        addSlot(new Slot(input, INPUT_SLOT_TIP, 66, 17) {
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                updateResultSlot();
                super.onTakeItem(player, stack);
            }
        });
        addSlot(new Slot(input, INPUT_SLOT_SHAFT, 48, 35) {
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                updateResultSlot();
                super.onTakeItem(player, stack);
            }
        });
        addSlot(new Slot(input, INPUT_SLOT_FEATHER, 30, 53) {
            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                updateResultSlot();
                super.onTakeItem(player, stack);
            }
        });

        addSlot(new Slot(input, INPUT_SLOT_POTION, 92, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isPotion(stack);
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }

            @Override
            public int getMaxItemCount(ItemStack stack) {
                return 1;
            }
        });

        // Output slot
        addSlot(new Slot(result, 0, 124, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                consumeInputs(stack);
                super.onTakeItem(player, stack);
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
        return stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(access, player, net.minecraft.block.Blocks.FLETCHING_TABLE);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        updateResultSlot();
        super.onContentChanged(inventory);
    }

    private boolean isUpgradeableArrow(ItemStack stack) {
        return stack.isOf(ModItems.IRON_UPGRADE_ARROW)
                || stack.isOf(ModItems.STEEL_UPGRADE_ARROW)
                || stack.isOf(ModItems.DIAMOND_UPGRADE_ARROW);
    }

    private void updateResultSlot() {
        if (world.isClient()) return;

        boolean hasInput = false;
        for (int i = 0; i < 3; i++) {
            if (!input.getStack(i).isEmpty()) {
                hasInput = true;
                break;
            }
        }
        if (!hasInput) {
            result.setStack(0, ItemStack.EMPTY);
            sendContentUpdates();
            return;
        }
        Optional<FletchingRecipe> opt = recipeManager.getFirstMatch(ModRecipeTypes.FLETCHING, input, world);
        ItemStack resultStack = ItemStack.EMPTY;
        ItemStack potion = input.getStack(INPUT_SLOT_POTION);
        boolean allowUpgradeableArrowConversion = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get();
        if (!potion.isEmpty()) {
            int arrowSlots = 0;
            int arrowCount = 0;
            int slotNumber = -1;
            for (int i = 0; i < 3; i++) {
                ItemStack slotStack = input.getStack(i);
                if (slotStack.isOf(Items.ARROW) || (allowUpgradeableArrowConversion && isUpgradeableArrow(slotStack))) {
                    arrowSlots++;
                    arrowCount = slotStack.getCount();
                    slotNumber = i;
                }
            }

            if (arrowSlots == 1) {
                ItemStack arrowStack = input.getStack(slotNumber);
                boolean isUpgradeable = isUpgradeableArrow(arrowStack);

                if (isUpgradeable && !allowUpgradeableArrowConversion) {
                    result.setStack(0, ItemStack.EMPTY);
                    sendContentUpdates();
                    return;
                }
                if (potion.isOf(Items.POTION)) {
                    ItemStack tippedArrows;
                    if (isUpgradeableArrow(input.getStack(slotNumber)))
                        tippedArrows = input.getStack(slotNumber).copy();
                    else tippedArrows = new ItemStack(Items.TIPPED_ARROW, arrowCount);
                    PotionUtil.setPotion(tippedArrows, PotionUtil.getPotion(potion));
                    if (potion.hasNbt()) {
                        tippedArrows.setNbt(potion.getNbt().copy());
                    }
                    resultStack = tippedArrows;
                } else if (potion.isOf(Items.LINGERING_POTION)) {
                    ItemStack lingeringArrows;
                    if (isUpgradeableArrow(input.getStack(slotNumber))) {
                        lingeringArrows = input.getStack(slotNumber).copy();
                    } else {
                        lingeringArrows = new ItemStack(ModItems.LINGERING_ARROW, arrowCount);
                    }
                    PotionUtil.setPotion(lingeringArrows, PotionUtil.getPotion(potion));
                    if (potion.hasNbt()) {
                        lingeringArrows.setNbt(potion.getNbt().copy());
                    }
                    if (isUpgradeableArrow(input.getStack(slotNumber))) {
                        lingeringArrows.getOrCreateNbt().putBoolean("LingeringPotion", true);
                    }
                    resultStack = lingeringArrows;
                }
            }
        }

        if (opt.isPresent()) {
            FletchingRecipe recipe = opt.get();

            int tipCount = input.getStack(INPUT_SLOT_TIP).getCount();
            int shaftCount = input.getStack(INPUT_SLOT_SHAFT).getCount();
            int featherCount = input.getStack(INPUT_SLOT_FEATHER).getCount();

            int craftCount = Math.max(Math.min(Math.min(tipCount, shaftCount), featherCount), 1);
            ItemStack baseResult = recipe.craft(input, world.getRegistryManager());

            if (!potion.isEmpty()) {
                boolean isUpgradeable = isUpgradeableArrow(baseResult);
                if ((isUpgradeable && !allowUpgradeableArrowConversion)) {
                    result.setStack(0, ItemStack.EMPTY);
                    sendContentUpdates();
                    return;
                }
                String potionEffect = Registries.POTION.getId(PotionUtil.getPotion(potion)).toString();

                if ((potion.isOf(Items.POTION) || potion.isOf(Items.SPLASH_POTION)) && !recipe.getTippedResult().isEmpty()) {
                    resultStack = recipe.getTippedResult().copy();
                    NbtCompound resultTag = resultStack.getOrCreateNbt();
                    if (potion.hasNbt()) {
                        resultTag.copyFrom(potion.getNbt().copy());
                    }
                    resultStack.setNbt(potion.getNbt() != null ? potion.getNbt().copy() : resultTag);

                } else if (potion.isOf(Items.LINGERING_POTION) && !recipe.getLingeringResult().isEmpty()) {
                    resultStack = recipe.getLingeringResult().copy();

                    NbtCompound resultTag = resultStack.getOrCreateNbt();

                    if (isUpgradeableArrow(resultStack)) {
                        resultTag.putBoolean("LingeringPotion", true);
                    } else if (recipe.getLingeringTag() != null) {
                        resultTag.putString(recipe.getLingeringTag(), potionEffect);
                    }

                    if (potion.hasNbt()) {
                        resultTag.copyFrom(potion.getNbt().copy());
                    }

                    resultStack.setNbt(resultTag);
                } else {
                    result.setStack(0, ItemStack.EMPTY);
                    sendContentUpdates();
                    return;
                }
            } else {
                resultStack = baseResult.copy();
            }

            if (!resultStack.isEmpty()) {
                int outPer = baseResult.getCount();
                int maxStack = resultStack.getMaxCount();
                int maxCraftCount = Math.min(maxStack / outPer, craftCount);
                resultStack.setCount(outPer * maxCraftCount);
            }
        }
        result.setStack(0, resultStack);
        sendContentUpdates();
    }

    private void consumeInputs(ItemStack takenResult) {
        if (takenResult.isEmpty()) return;

        Optional<FletchingRecipe> opt = recipeManager.getFirstMatch(ModRecipeTypes.FLETCHING, input, world);
        if (opt.isPresent()) {
            FletchingRecipe recipe = opt.get();
            ItemStack baseResult = recipe.craft(input, world.getRegistryManager());
            int baseCount = baseResult.getCount();
            int tookCount = takenResult.getCount();
            int batchesTaken = Math.max(1, tookCount / baseCount);

            for (int i = 0; i < 3; i++) {
                ItemStack stack = input.getStack(i);
                if (!stack.isEmpty()) {
                    stack.decrement(batchesTaken);
                    input.setStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }
        } else {
            int tookCount = takenResult.getCount();
            for (int i = 0; i < 3; i++) {
                ItemStack stack = input.getStack(i);
                if (!stack.isEmpty()) {
                    stack.decrement(tookCount);
                    input.setStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }
        }
        ItemStack potionStack = input.getStack(INPUT_SLOT_POTION);
        if (!potionStack.isEmpty()) {
            potionStack.decrement(1);
            input.setStack(INPUT_SLOT_POTION, potionStack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : potionStack);
        }
        updateResultSlot();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack copiedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            copiedStack = slotStack.copy();

            if (index == OUTPUT_SLOT) {
                while (canStillCraft()) {
                    ItemStack craftResult = slot.getStack().copy();
                    int maxTransfer = craftResult.getMaxCount();

                    int craftCount = Math.min(craftResult.getCount(), maxTransfer);
                    craftResult.setCount(craftCount);

                    if (!insertItem(craftResult, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, true)) {
                        break;
                    }
                    slot.onQuickTransfer(craftResult, copiedStack);
                    consumeInputs(copiedStack);

                    if (slot.getStack().isEmpty()) break;
                }

                slot.markDirty();
            } else if (index >= PLAYER_INVENTORY_START && index <= PLAYER_HOTBAR_END) {
                if (isPotion(slotStack)) {
                    if (!insertItem(slotStack, INPUT_SLOT_POTION, INPUT_SLOT_POTION + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!insertItem(slotStack, INPUT_SLOT_TIP, INPUT_SLOT_POTION, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= INPUT_SLOT_TIP && index <= INPUT_SLOT_POTION) {
                if (!insertItem(slotStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (slotStack.getCount() == copiedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, slotStack);
        }

        return copiedStack;
    }

    private boolean canStillCraft() {
        Optional<FletchingRecipe> opt = recipeManager.getFirstMatch(ModRecipeTypes.FLETCHING, input, world);
        return opt.isPresent();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (this.access != ScreenHandlerContext.EMPTY)
            for (int i = 0; i < input.size(); i++) {
                ItemStack stack = input.removeStack(i);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().insertStack(stack)) {
                        player.dropItem(stack, false);
                    }
                }
            }
    }

    public static Potion getPotion(@Nullable NbtCompound tag) {
        if (tag == null) return Potions.EMPTY;

        if (tag.contains("Potion", 8)) {
            return Registries.POTION.get(net.minecraft.util.Identifier.tryParse(tag.getString("Potion")));
        }

        return Potions.EMPTY;
    }

    public static List<StatusEffectInstance> getAllEffects(@Nullable NbtCompound compoundTag) {
        List<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(compoundTag).getEffects());
        getCustomEffects(compoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable NbtCompound compoundTag, List<StatusEffectInstance> effectList) {
        if (compoundTag != null && compoundTag.contains("CustomPotionEffects", 9)) {
            NbtList listTag = compoundTag.getList("CustomPotionEffects", 10);

            for (int i = 0; i < listTag.size(); ++i) {
                NbtCompound nbtCompound = listTag.getCompound(i);
                StatusEffectInstance effectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                if (effectInstance != null) {
                    effectList.add(effectInstance);
                }
            }
        }
    }
}
