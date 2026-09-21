package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.custom.TierASmithingAnvil;
import net.stirdrem.overgeared.screen.TierASmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

public class TierASmithingAnvilBlockEntity extends SteelSmithingAnvilBlockEntity {

    public TierASmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super((TierASmithingAnvil) pBlockState.getBlock(), AnvilTier.ABOVE_A, ModBlockEntities.TIER_A_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }

    @Override
    protected @Nullable AbstractContainerMenu createMenuInternal(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new TierASmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
    }
}
