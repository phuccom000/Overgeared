package net.stirdrem.overgeared.block.entity;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.stirdrem.overgeared.registry.DeferredBlock;
import net.stirdrem.overgeared.registry.DeferredHolder;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OvergearedMod.MOD_ID);

    /** Shared by every tiered smithing anvil; keeps the id of the original iron anvil's block entity. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TieredSmithingAnvilBlockEntity>> TIERED_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("smithing_table_be", () ->
                    new BlockEntityType<>(TieredSmithingAnvilBlockEntity::new, Set.of(ModBlocks.TIERED_ANVILS.values().stream().map(DeferredBlock::get).toArray(Block[]::new))));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoneSmithingAnvilBlockEntity>> STONE_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("stone_smithing_table_be", () ->
                    new BlockEntityType<>(StoneSmithingAnvilBlockEntity::new, Set.of(ModBlocks.STONE_SMITHING_ANVIL.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloySmelterBlockEntity>> ALLOY_FURNACE_BE =
            BLOCK_ENTITIES.register("alloy_furnace_be", () ->
                    new BlockEntityType<>(AlloySmelterBlockEntity::new, Set.of(ModBlocks.ALLOY_FURNACE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetherAlloySmelterBlockEntity>> NETHER_ALLOY_FURNACE_BE =
            BLOCK_ENTITIES.register("nether_alloy_furnace_be", () ->
                    new BlockEntityType<>(NetherAlloySmelterBlockEntity::new, Set.of(ModBlocks.NETHER_ALLOY_FURNACE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastingSmelterBlockEntity>> CAST_FURNACE_BE =
            BLOCK_ENTITIES.register("casting_furnace_be", () ->
                    new BlockEntityType<>(CastingSmelterBlockEntity::new, Set.of(ModBlocks.CAST_FURNACE.get())));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
