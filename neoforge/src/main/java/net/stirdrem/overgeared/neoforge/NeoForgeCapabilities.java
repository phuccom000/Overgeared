package net.stirdrem.overgeared.neoforge;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;

/**
 * Exposes the mod's containers to NeoForge's item transfer API. The block entities are
 * {@link WorldlyContainer}s, so the sided wrappers honour their slot and face rules.
 */
public final class NeoForgeCapabilities {
    private NeoForgeCapabilities() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(NeoForgeCapabilities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerSmelter(event, ModBlockEntities.ALLOY_FURNACE_BE.get());
        registerSmelter(event, ModBlockEntities.NETHER_ALLOY_FURNACE_BE.get());

        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.TIERED_SMITHING_ANVIL_BE.get(), WorldlyContainerWrapper::new);
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), WorldlyContainerWrapper::new);
    }

    private static <T extends BlockEntity & WorldlyContainer> void registerSmelter(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, type, (blockEntity, side) -> {
            if (side == null) {
                return VanillaContainerWrapper.of(blockEntity);
            }
            // Top: inputs, bottom: output, every side face: fuel (as in 1.21.1)
            Direction face = side == Direction.UP || side == Direction.DOWN ? side : Direction.NORTH;
            return new WorldlyContainerWrapper(blockEntity, face);
        });
    }
}
