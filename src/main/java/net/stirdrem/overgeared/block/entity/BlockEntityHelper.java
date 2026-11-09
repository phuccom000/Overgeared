package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.util.ItemStackHandler;

import java.util.Optional;

/**
 * Shared 26.3 helpers for Overgeared's block entities: ValueOutput/ValueInput inventory
 * persistence for the ItemStackHandler shim, and furnace fuel handling (fuel is now the vanilla
 * COOKING_FUEL data component, resolved against a loot context - Fabric's FuelRegistry is gone).
 */
public final class BlockEntityHelper {
    private BlockEntityHelper() {
    }

    public static void saveInventory(ValueOutput output, String key, ItemStackHandler handler) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < handler.getSlots(); i++) stacks.set(i, handler.getStackInSlot(i));
        ContainerHelper.saveAllItems(output.child(key), stacks);
    }

    public static void loadInventory(ValueInput input, String key, ItemStackHandler handler) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input.childOrEmpty(key), stacks);
        for (int i = 0; i < stacks.size(); i++) handler.setStackInSlot(i, stacks.get(i));
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.has(DataComponents.COOKING_FUEL);
    }

    /** Same loot context vanilla's BaseContainerBlockEntity uses to resolve fuel values. */
    public static <B extends BlockEntity & Container> int getBurnDuration(ServerLevel level, B blockEntity, ItemStack fuel) {
        LootContext context = new LootContext.Builder(
                new LootParams.Builder(level)
                        .withParameter(LootContextParams.BLOCK_STATE, blockEntity.getBlockState())
                        .withParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockEntity.getBlockPos()))
                        .withParameter(LootContextParams.CONTAINER, blockEntity)
                        .create(LootContextParamSets.CONTAINER_PROCESS))
                .create(Optional.empty());
        return ResolvableInt.getFromItem(fuel, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
    }

    /** The crafting remainder of {@code stack}'s item (empty if none). */
    public static ItemStack remainder(ItemStack stack) {
        ItemStackTemplate template = stack.getItem().getCraftingRemainder();
        return template == null ? ItemStack.EMPTY : template.create();
    }
}
