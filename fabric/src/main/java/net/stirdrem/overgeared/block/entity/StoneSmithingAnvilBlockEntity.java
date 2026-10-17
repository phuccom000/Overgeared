package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.StoneSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.screen.StoneSmithingAnvilScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class StoneSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {
    private int craftCount = 0;

    public StoneSmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
        super((StoneSmithingAnvil) state.getBlock(), AnvilTier.STONE, ModBlockEntities.STONE_SMITHING_ANVIL_BE, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        if (!player.isShiftKeyDown()) {
            return new StoneSmithingAnvilScreenHandler(syncId, playerInventory, this, this.data);
        } else return null;
    }

    @Override
    protected String determineForgingQuality() {
        String quality = anvilBlock.getQuality();
        if (quality == null) {
            return ForgingQuality.POOR.getDisplayName();
        }

        return switch (quality.toLowerCase(Locale.ROOT)) {
            case "poor" -> ForgingQuality.POOR.getDisplayName();
            default -> ForgingQuality.WELL.getDisplayName();
        };
    }

    @Override
    public String blueprintQuality() {
        return BlueprintQuality.WELL.getDisplayName();
    }

    @Override
    protected void craftItem() {
        super.craftItem();
        if (ServerConfig.STONE_ANVIL_MAX_USES.get() == 0) return;
        craftCount++;

        if (craftCount >= ServerConfig.STONE_ANVIL_MAX_USES.get()) {
            breakAnvil();
        }
    }

    private void breakAnvil() {
        if (level != null && !level.isClientSide) {
            level.destroyBlock(worldPosition, true, null, 512);
        }
    }
}
