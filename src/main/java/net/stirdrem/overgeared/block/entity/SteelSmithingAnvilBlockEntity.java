package net.stirdrem.overgeared.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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
    public Text getDisplayName() {
        return Text.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!player.isSneaking()) {
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
