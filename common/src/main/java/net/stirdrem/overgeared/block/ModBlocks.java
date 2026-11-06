package net.stirdrem.overgeared.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.custom.*;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK);

    /**
     * One smithing anvil per tier except stone, in tier order. Filled by {@link #registerTieredAnvils()}, which the
     * loaders call once KubeJS startup scripts have run, as they can add tiers (Forge: when blocks register,
     * Fabric: in the client/dedicated server entrypoint, after every mod's main entrypoint).
     */
    public static final Map<AnvilTier, RegistryEntry<TieredSmithingAnvil>> TIERED_ANVILS = new LinkedHashMap<>();

    /** The iron smithing anvil, the default tier. */
    public static final Supplier<TieredSmithingAnvil> IRON_SMITHING_ANVIL = () -> TIERED_ANVILS.get(AnvilTier.iron()).get();

    public static final RegistryEntry<Block> STONE_SMITHING_ANVIL = registerBlock(AnvilTier.STONE_ID + "_smithing_anvil",
            () -> new StoneSmithingAnvil(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()));
    public static final RegistryEntry<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryEntry<Block> DRAFTING_TABLE = registerBlock("drafting_table",
            () -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)));
    public static final RegistryEntry<Block> ALLOY_FURNACE = registerBlock("alloy_furnace",
            () -> new AlloySmelterBlock(BlockBehaviour.Properties.copy(Blocks.BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));
    public static final RegistryEntry<Block> NETHER_ALLOY_FURNACE = registerBlock("nether_alloy_furnace",
            () -> new NetherAlloySmelterBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));
    public static final RegistryEntry<Block> CAST_FURNACE = registerBlock("casting_furnace",
            () -> new CastFurnaceBlock(BlockBehaviour.Properties.copy(Blocks.RED_NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));

    /** Registers a smithing anvil for each tier except stone, and their shared block entity type. */
    public static void registerTieredAnvils() {
        if (!TIERED_ANVILS.isEmpty()) {
            throw new IllegalStateException("Tiered smithing anvils are already registered");
        }
        for (AnvilTier tier : AnvilTier.all()) {
            if (tier.isStone()) continue; // has its own block, STONE_SMITHING_ANVIL
            TIERED_ANVILS.put(tier, registerBlock(tier.blockName(),
                    () -> new TieredSmithingAnvil(tier, BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion())));
        }
        ModBlockEntities.registerTieredAnvilBlockEntity();
    }

    /** The smithing anvil block of a tier. */
    public static AbstractSmithingAnvil anvilFor(AnvilTier tier) {
        return tier.isStone() ? (AbstractSmithingAnvil) STONE_SMITHING_ANVIL.get() : TIERED_ANVILS.get(tier).get();
    }

    /** Ids renamed since earlier versions, so existing worlds keep their blocks, items and block entities. */
    public static void registerAliases() {
        // Before anvil tiers became configurable the iron anvil was "smithing_anvil" and the stone one "stone_anvil"
        alias(Registries.BLOCK, "smithing_anvil", "iron_smithing_anvil");
        alias(Registries.ITEM, "smithing_anvil", "iron_smithing_anvil");
        alias(Registries.BLOCK, "stone_anvil", "stone_smithing_anvil");
        alias(Registries.ITEM, "stone_anvil", "stone_smithing_anvil");
        // All tiered anvils now share the iron anvil's block entity type
        alias(Registries.BLOCK_ENTITY_TYPE, "tier_a_smithing_table_be", "smithing_table_be");
        alias(Registries.BLOCK_ENTITY_TYPE, "tier_b_smithing_table_be", "smithing_table_be");
    }

    private static void alias(ResourceKey<? extends Registry<?>> registry, String oldName, String newName) {
        Services.PLATFORM.registerAlias(registry, Overgeared.id(oldName), Overgeared.id(newName));
    }

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return state -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    private static <T extends Block> RegistryEntry<T> registerBlock(String name, Supplier<T> block) {
        RegistryEntry<T> entry = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(entry.get(), new Item.Properties()));
        return entry;
    }

    /** Class-loads this registry; call from the loader entrypoint (Forge: mod constructor, Fabric: onInitialize). */
    public static void init() {
    }
}
