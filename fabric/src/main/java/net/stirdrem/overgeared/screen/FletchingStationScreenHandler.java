package net.stirdrem.overgeared.screen;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.FletchingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import org.jetbrains.annotations.Nullable;
import java.util.List;
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
    private final RecipeManager recipeManager;
    private final Player player;

    public FletchingStationScreenHandler(int syncId, Inventory playerInv) {
        this(syncId, playerInv, ContainerLevelAccess.NULL);
    }

    public FletchingStationScreenHandler(int syncId, Inventory playerInv, ContainerLevelAccess access) {
        super(ModMenuTypes.FLETCHING_STATION_MENU, syncId);
        this.access = access;
        this.recipeManager = playerInv.player.level().getRecipeManager();
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
        Optional<FletchingRecipe> opt = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING, input, world);
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
                    PotionUtils.setPotion(tippedArrows, PotionUtils.getPotion(potion));
                    if (potion.hasTag()) {
                        tippedArrows.setTag(potion.getTag().copy());
                    }
                    resultStack = tippedArrows;
                } else if (potion.is(Items.LINGERING_POTION)) {
                    ItemStack lingeringArrows;
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows = input.getItem(slotNumber).copy();
                    } else {
                        lingeringArrows = new ItemStack(ModItems.LINGERING_ARROW, arrowCount);
                    }
                    PotionUtils.setPotion(lingeringArrows, PotionUtils.getPotion(potion));
                    if (potion.hasTag()) {
                        lingeringArrows.setTag(potion.getTag().copy());
                    }
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows.getOrCreateTag().putBoolean("LingeringPotion", true);
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
            ItemStack baseResult = recipe.assemble(input, world.registryAccess());

            if (!potion.isEmpty()) {
                boolean isUpgradeable = isUpgradeableArrow(baseResult);
                if ((isUpgradeable && !allowUpgradeableArrowConversion)) {
                    result.setItem(0, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
                String potionEffect = BuiltInRegistries.POTION.getKey(PotionUtils.getPotion(potion)).toString();

                if ((potion.is(Items.POTION) || potion.is(Items.SPLASH_POTION)) && !recipe.getTippedResult().isEmpty()) {
                    resultStack = recipe.getTippedResult().copy();
                    CompoundTag resultTag = resultStack.getOrCreateTag();
                    if (potion.hasTag()) {
                        resultTag.merge(potion.getTag().copy());
                    }
                    resultStack.setTag(potion.getTag() != null ? potion.getTag().copy() : resultTag);

                } else if (potion.is(Items.LINGERING_POTION) && !recipe.getLingeringResult().isEmpty()) {
                    resultStack = recipe.getLingeringResult().copy();

                    CompoundTag resultTag = resultStack.getOrCreateTag();

                    if (isUpgradeableArrow(resultStack)) {
                        resultTag.putBoolean("LingeringPotion", true);
                    } else if (recipe.getLingeringTag() != null) {
                        resultTag.putString(recipe.getLingeringTag(), potionEffect);
                    }

                    if (potion.hasTag()) {
                        resultTag.merge(potion.getTag().copy());
                    }

                    resultStack.setTag(resultTag);
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

        Optional<FletchingRecipe> opt = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING, input, world);
        if (opt.isPresent()) {
            FletchingRecipe recipe = opt.get();
            ItemStack baseResult = recipe.assemble(input, world.registryAccess());
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
        Optional<FletchingRecipe> opt = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING, input, world);
        return opt.isPresent();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.access != ContainerLevelAccess.NULL)
            for (int i = 0; i < input.getContainerSize(); i++) {
                ItemStack stack = input.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }
    }

    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

        if (tag.contains("Potion", 8)) {
            return BuiltInRegistries.POTION.get(net.minecraft.resources.ResourceLocation.tryParse(tag.getString("Potion")));
        }

        return Potions.EMPTY;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag compoundTag) {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(compoundTag).getEffects());
        getCustomEffects(compoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag compoundTag, List<MobEffectInstance> effectList) {
        if (compoundTag != null && compoundTag.contains("CustomPotionEffects", 9)) {
            ListTag listTag = compoundTag.getList("CustomPotionEffects", 10);

            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag nbtCompound = listTag.getCompound(i);
                MobEffectInstance effectInstance = MobEffectInstance.load(nbtCompound);
                if (effectInstance != null) {
                    effectList.add(effectInstance);
                }
            }
        }
    }
}
