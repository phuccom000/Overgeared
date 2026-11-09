package net.stirdrem.overgeared.block.entity;

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
                    BlockEntityType.Builder.of(TieredSmithingAnvilBlockEntity::new,
                                    ModBlocks.TIERED_ANVILS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)).
                            build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoneSmithingAnvilBlockEntity>> STONE_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("stone_smithing_table_be", () ->
                    BlockEntityType.Builder.of(StoneSmithingAnvilBlockEntity::new,
                                    ModBlocks.STONE_SMITHING_ANVIL.get()).
                            build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloySmelterBlockEntity>> ALLOY_FURNACE_BE =
            BLOCK_ENTITIES.register("alloy_furnace_be", () ->
                    BlockEntityType.Builder.of(AlloySmelterBlockEntity::new,
                            ModBlocks.ALLOY_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetherAlloySmelterBlockEntity>> NETHER_ALLOY_FURNACE_BE =
            BLOCK_ENTITIES.register("nether_alloy_furnace_be", () ->
                    BlockEntityType.Builder.of(NetherAlloySmelterBlockEntity::new,
                            ModBlocks.NETHER_ALLOY_FURNACE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastingSmelterBlockEntity>> CAST_FURNACE_BE =
            BLOCK_ENTITIES.register("casting_furnace_be", () ->
                    BlockEntityType.Builder.of(CastingSmelterBlockEntity::new,
                            ModBlocks.CAST_FURNACE.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
