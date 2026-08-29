package net.stirdrem.overgeared.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModBlockEntities {
    public static final BlockEntityType<SteelSmithingAnvilBlockEntity> STEEL_SMITHING_ANVIL_BE =
            register("smithing_table_be", FabricBlockEntityTypeBuilder.create(
                    SteelSmithingAnvilBlockEntity::new, ModBlocks.SMITHING_ANVIL).build());

    public static final BlockEntityType<TierASmithingAnvilBlockEntity> TIER_A_SMITHING_ANVIL_BE =
            register("tier_a_smithing_table_be", FabricBlockEntityTypeBuilder.create(
                    TierASmithingAnvilBlockEntity::new, ModBlocks.TIER_A_SMITHING_ANVIL).build());

    public static final BlockEntityType<TierBSmithingAnvilBlockEntity> TIER_B_SMITHING_ANVIL_BE =
            register("tier_b_smithing_table_be", FabricBlockEntityTypeBuilder.create(
                    TierBSmithingAnvilBlockEntity::new, ModBlocks.TIER_B_SMITHING_ANVIL).build());

    public static final BlockEntityType<StoneSmithingAnvilBlockEntity> STONE_SMITHING_ANVIL_BE =
            register("stone_smithing_table_be", FabricBlockEntityTypeBuilder.create(
                    StoneSmithingAnvilBlockEntity::new, ModBlocks.STONE_SMITHING_ANVIL).build());

    public static final BlockEntityType<AlloySmelterBlockEntity> ALLOY_FURNACE_BE =
            register("alloy_furnace_be", FabricBlockEntityTypeBuilder.create(
                    AlloySmelterBlockEntity::new, ModBlocks.ALLOY_FURNACE).build());

    public static final BlockEntityType<NetherAlloySmelterBlockEntity> NETHER_ALLOY_FURNACE_BE =
            register("nether_alloy_furnace_be", FabricBlockEntityTypeBuilder.create(
                    NetherAlloySmelterBlockEntity::new, ModBlocks.NETHER_ALLOY_FURNACE).build());

    public static final BlockEntityType<CastFurnaceBlockEntity> CAST_FURNACE_BE =
            register("casting_furnace_be", FabricBlockEntityTypeBuilder.create(
                    CastFurnaceBlockEntity::new, ModBlocks.CAST_FURNACE).build());

    private static <T extends BlockEntityType<?>> T register(String name, T type) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Overgeared.id(name), type);
    }

    public static void register() {
    }
}
