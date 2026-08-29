package net.stirdrem.overgeared.block.custom;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

public class TierASmithingAnvil extends SteelSmithingAnvil {

    public TierASmithingAnvil(AnvilTier tier, Settings settings) {
        super(tier, settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TierASmithingAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient) {
            return validateTicker(type, ModBlockEntities.TIER_A_SMITHING_ANVIL_BE,
                    (lvl, pos, st, be) -> be.tick(lvl, pos, st));
        }
        return null;
    }

    @Override
    public TagKey<Item> hammerTag() {
        if (ServerConfig.ENABLE_TIERED_HAMMERS.get()) {
            return ModTags.Items.TIER_A_SMITHING_HAMMERS;
        }
        return super.hammerTag();
    }
}
