package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final RegistrationProvider<BlockEntityType<?>> BLOCK_ENTITIES = RegistrationProvider.get(Registries.BLOCK_ENTITY_TYPE);

    /** Shared by every tiered anvil; keeps the iron anvil's old id so placed anvils keep their contents. */
    public static final RegistryEntry<BlockEntityType<TieredSmithingAnvilBlockEntity>> TIERED_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("smithing_table_be", () -> Services.PLATFORM.createBlockEntityType(
                    TieredSmithingAnvilBlockEntity::new,
                    ModBlocks.TIERED_ANVILS.values().stream().map(RegistryEntry::get).toArray(Block[]::new)));
    public static final RegistryEntry<BlockEntityType<StoneSmithingAnvilBlockEntity>> STONE_SMITHING_ANVIL_BE =
            register("stone_smithing_table_be", StoneSmithingAnvilBlockEntity::new, ModBlocks.STONE_SMITHING_ANVIL);
    public static final RegistryEntry<BlockEntityType<AlloySmelterBlockEntity>> ALLOY_FURNACE_BE =
            register("alloy_furnace_be", AlloySmelterBlockEntity::new, ModBlocks.ALLOY_FURNACE);
    public static final RegistryEntry<BlockEntityType<NetherAlloySmelterBlockEntity>> NETHER_ALLOY_FURNACE_BE =
            register("nether_alloy_furnace_be", NetherAlloySmelterBlockEntity::new, ModBlocks.NETHER_ALLOY_FURNACE);
    public static final RegistryEntry<BlockEntityType<CastFurnaceBlockEntity>> CAST_FURNACE_BE =
            register("casting_furnace_be", CastFurnaceBlockEntity::new, ModBlocks.CAST_FURNACE);

    @SafeVarargs
    private static <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> register(
            String name, IPlatformHelper.BlockEntityFactory<T> factory, Supplier<? extends Block>... blocks) {
        return BLOCK_ENTITIES.register(name, () -> {
            Block[] valid = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++) valid[i] = blocks[i].get();
            return Services.PLATFORM.createBlockEntityType(factory, valid);
        });
    }

    /** Class-loads this registry; call from the loader entrypoint after {@link ModBlocks#init()}. */
    public static void init() {
    }
}
