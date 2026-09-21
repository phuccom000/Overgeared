package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.custom.SteelSmithingAnvil;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

public class SteelSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {

    public SteelSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super((SteelSmithingAnvil) pBlockState.getBlock(), AnvilTier.IRON, ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }

    public SteelSmithingAnvilBlockEntity(AbstractSmithingAnvil block, AnvilTier anvilTier, BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(block, anvilTier, type, pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Override
    protected @Nullable AbstractContainerMenu createMenuInternal(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new SteelSmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected String determineForgingQuality() {
        return super.determineForgingQualityInternal();
    }

    @Override
    public String blueprintQuality() {
        return super.blueprintQualityInternal();
    }

    @Override
    protected void craftItem() {
        super.craftItemInternal();
    }

    @Override
    public boolean hasRecipe() {
        return super.hasRecipeWithBlueprint();
    }

}
