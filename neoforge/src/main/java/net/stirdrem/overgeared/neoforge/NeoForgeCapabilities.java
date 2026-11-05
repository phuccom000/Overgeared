package net.stirdrem.overgeared.neoforge;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;

import java.util.function.Function;

public final class NeoForgeCapabilities {
    private NeoForgeCapabilities() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(NeoForgeCapabilities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerSmelter(event, ModBlockEntities.ALLOY_FURNACE_BE.get(), be -> be.getItemHandler());
        registerSmelter(event, ModBlockEntities.NETHER_ALLOY_FURNACE_BE.get(), be -> be.getItemHandler());

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TIERED_SMITHING_ANVIL_BE.get(), SidedInvWrapper::new);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), SidedInvWrapper::new);
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity & net.minecraft.world.WorldlyContainer> void registerSmelter(
            RegisterCapabilitiesEvent event, BlockEntityType<T> type,
            Function<T, net.stirdrem.overgeared.util.items.IItemHandler> handler) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (blockEntity, side) -> {
            if (side == null) {
                return wrap(handler.apply(blockEntity));
            }
            if (side == Direction.UP) {
                return new SidedInvWrapper(blockEntity, Direction.UP);
            } else if (side == Direction.DOWN) {
                return new SidedInvWrapper(blockEntity, Direction.DOWN);
            } else {
                return new SidedInvWrapper(blockEntity, Direction.NORTH);
            }
        });
    }

    /** Exposes the mod's own item handler through NeoForge's interface. */
    static IItemHandler wrap(net.stirdrem.overgeared.util.items.IItemHandler handler) {
        return new IItemHandlerModifiable() {
            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                if (handler instanceof net.stirdrem.overgeared.util.items.IItemHandlerModifiable modifiable) {
                    modifiable.setStackInSlot(slot, stack);
                }
            }

            @Override
            public int getSlots() {
                return handler.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return handler.getStackInSlot(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return handler.insertItem(slot, stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return handler.extractItem(slot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return handler.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return handler.isItemValid(slot, stack);
            }
        };
    }
}
