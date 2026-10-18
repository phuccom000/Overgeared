package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

public class TierASmithingAnvil extends SteelSmithingAnvil {

    public TierASmithingAnvil(AnvilTier tier, Properties settings) {
        super(tier, settings);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TierASmithingAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClientSide) {
            return validateTicker(type, ModBlockEntities.TIER_A_SMITHING_ANVIL_BE,
                    (lvl, pos, st, be) -> be.tick(lvl, pos, st));
        }
        return null;
    }

    @Override
    public TagKey<Item> hammerTag() {
        if (Overgeared.getConfig().ENABLE_TIERED_HAMMERS()) {
            return ModTags.Items.TIER_A_SMITHING_HAMMERS;
        }
        return super.hammerTag();
    }
}
