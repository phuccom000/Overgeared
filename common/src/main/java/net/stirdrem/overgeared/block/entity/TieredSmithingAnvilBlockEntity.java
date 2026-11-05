package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.custom.TieredSmithingAnvil;
import net.stirdrem.overgeared.screen.TieredSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

/** The block entity of every {@link TieredSmithingAnvil}; the tier comes from the block. */
public class TieredSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {

    public TieredSmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
        this((TieredSmithingAnvil) state.getBlock(), pos, state);
    }

    private TieredSmithingAnvilBlockEntity(TieredSmithingAnvil block, BlockPos pos, BlockState state) {
        super(block, block.getAnvilTier(), ModBlockEntities.TIERED_SMITHING_ANVIL_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        if (!player.isShiftKeyDown()) {
            return new TieredSmithingAnvilMenu(syncId, playerInventory, this, this.data);
        } else return null;
    }

    @Override
    protected String determineForgingQuality() {
        if (!Overgeared.getConfig().ENABLE_BLUEPRINT_FORGING() || !anvilTier.hasBlueprint()) {
            return super.determineForgingQualityNoBlueprint();
        } else return super.determineForgingQuality();
    }

    @Override
    public String blueprintQuality() {
        if (!Overgeared.getConfig().ENABLE_BLUEPRINT_FORGING() || !anvilTier.hasBlueprint())
            return ForgingQuality.PERFECT.getDisplayName();
        else return super.blueprintQuality();
    }

    @Override
    protected void craftItem() {
        super.craftItem();
        if (anvilTier.hasBlueprint()) {
            super.craftItemWithBlueprint();
        }
    }

    @Override
    public boolean hasRecipe() {
        return anvilTier.hasBlueprint() ? super.hasRecipeWithBlueprint() : super.hasRecipe();
    }
}
