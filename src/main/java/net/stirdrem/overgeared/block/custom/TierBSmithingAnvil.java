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
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

public class TierBSmithingAnvil extends SteelSmithingAnvil {

    public TierBSmithingAnvil(AnvilTier tier, Properties properties) {
        super(tier, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TierBSmithingAnvilBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (!pLevel.isClientSide && pBlockEntityType == ModBlockEntities.TIER_B_SMITHING_ANVIL_BE.get()) {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.TIER_B_SMITHING_ANVIL_BE.get(),
                    (pLevel1, pPos, pState1, pBlockEntity) ->
                            pBlockEntity.tick(pLevel, pPos, pState1));
        }
        return null;
    }

    @Override
    public TagKey<Item> hammerTag() {
        if (ServerConfig.ENABLE_TIERED_HAMMERS.get()) {
            return ModTags.Items.TIER_B_SMITHING_HAMMERS;
        }
        return super.hammerTag();
    }
}