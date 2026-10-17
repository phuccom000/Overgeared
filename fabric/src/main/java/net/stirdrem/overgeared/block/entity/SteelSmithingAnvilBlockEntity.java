package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.block.custom.SteelSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilScreenHandler;
import org.jetbrains.annotations.Nullable;

public class SteelSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {

    public SteelSmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
        super((SteelSmithingAnvil) state.getBlock(), AnvilTier.IRON, ModBlockEntities.STEEL_SMITHING_ANVIL_BE, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        if (!player.isShiftKeyDown()) {
            return new SteelSmithingAnvilScreenHandler(syncId, playerInventory, this, this.data);
        } else return null;
    }

    @Override
    protected String determineForgingQuality() {
        if (!ServerConfig.ENABLE_BLUEPRINT_FORGING.get()) {
            return super.determineForgingQualityNoBlueprint();
        } else return super.determineForgingQuality();
    }

    @Override
    public String blueprintQuality() {
        if (!ServerConfig.ENABLE_BLUEPRINT_FORGING.get())
            return BlueprintQuality.PERFECT.getDisplayName();
        else return super.blueprintQuality();
    }

    @Override
    protected void craftItem() {
        super.craftItem();
        super.craftItemWithBlueprint();
    }

    @Override
    public boolean hasRecipe() {
        return super.hasRecipeWithBlueprint();
    }
}
