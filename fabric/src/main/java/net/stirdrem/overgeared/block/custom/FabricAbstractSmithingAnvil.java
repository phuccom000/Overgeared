package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.FabricAbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientModMessages;
import net.stirdrem.overgeared.client.FabricAnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;
import org.jetbrains.annotations.Nullable;

public abstract class FabricAbstractSmithingAnvil extends AbstractSmithingAnvil {

    public FabricAbstractSmithingAnvil(AnvilTier anvilTier, Properties properties) {
        super(anvilTier, properties);
    }

    @Override
    protected void sendHitPacket(BlockPos pos, String quality) {
        var buf = ModMessages.buf();
        PacketSendCounterC2SPacket.encode(
                new PacketSendCounterC2SPacket(pos, quality), buf
        );
        ClientModMessages.sendToServer(ModMessages.SEND_COUNTER, buf);
    }

    @Override
    protected void setMinigameVisible(BlockPos pos, boolean visible) {
        FabricAnvilMinigameEvents.setIsVisible(pos, visible);
    }

    @Override
    protected void resetMinigameForAnvil(Level level, BlockPos pos) {
        ModEvents.resetMinigameForAnvil(level, pos);
    }

    @Override
    protected void resetMinigameForPlayer(ServerPlayer player, BlockPos pos) {
        ModEvents.resetMinigameForPlayer(player);
    }

    @Override
    protected void hideMinigame(ServerPlayer player) {
        ModItemInteractEvents.hideMinigame(player);
    }

    @Override
    protected void releaseAnvil(ServerPlayer player, BlockPos pos) {
        ModItemInteractEvents.releaseAnvil(player, pos);
    }

    @Override
    protected void openAnvilScreen(
            ServerPlayer player,
            AbstractSmithingAnvilBlockEntity anvil,
            BlockPos pos
    ) {
        player.openMenu((FabricAbstractSmithingAnvilBlockEntity) anvil);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected static <T extends BlockEntity, E extends BlockEntity>
    BlockEntityTicker<T> validateTicker(
            BlockEntityType<T> givenType,
            BlockEntityType<E> expectedType,
            BlockEntityTicker<? super E> ticker
    ) {
        return expectedType == givenType
                ? (BlockEntityTicker<T>) ticker
                : null;
    }

    @Override
    protected void damageHammer(ItemStack held, Player player, InteractionHand hand) {
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;

        held.hurtAndBreak(1, player, p -> {
            p.broadcastBreakEvent(slot);

            if (p instanceof ServerPlayer serverPlayer) {
                ModEvents.resetMinigameForPlayer(serverPlayer);
            }
        });
    }
}
