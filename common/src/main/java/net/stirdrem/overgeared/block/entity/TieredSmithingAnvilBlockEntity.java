package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.custom.TieredSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.screen.TieredSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

public class TieredSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {

    public TieredSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this((TieredSmithingAnvil) pBlockState.getBlock(), pPos, pBlockState);
    }

    private TieredSmithingAnvilBlockEntity(TieredSmithingAnvil block, BlockPos pPos, BlockState pBlockState) {
        super(block, block.getAnvilTier(), ModBlockEntities.TIERED_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }

    public TieredSmithingAnvilBlockEntity(AbstractSmithingAnvil anvilBlock, AnvilTier tier, BlockEntityType<?> type,
                                         BlockPos pPos, BlockState pBlockState) {
        super(anvilBlock, tier, type, pPos, pBlockState);

    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new TieredSmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        }
        return null;
    }

    @Override
    public void craftItem() {
        super.craftItem();
        super.craftItemWithBlueprint();
    }

    @Override
    public boolean hasRecipe() {
        return super.hasRecipeWithBlueprint();
    }

    @Override
    protected ForgingQuality determineForgingQuality() {
        if (!ServerConfig.ENABLE_BLUEPRINT_FORGING.get()) {
            return super.determineForgingQualityNoBlueprint();
        } else return super.determineForgingQuality();
    }
}
