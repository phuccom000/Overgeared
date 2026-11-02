package net.stirdrem.overgeared.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.FabricAnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.client.ClientModMessages;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;
import net.stirdrem.overgeared.platform.services.IAnvilHooks;

/** Fabric anvil hooks (previously the FabricAbstractSmithingAnvil subclass). */
public class FabricAnvilHooks implements IAnvilHooks {

    @Override
    public void sendHitPacket(BlockPos pos, String quality) {
        var buf = ModMessages.buf();
        PacketSendCounterC2SPacket.encode(new PacketSendCounterC2SPacket(pos, quality), buf);
        ClientModMessages.sendToServer(ModMessages.SEND_COUNTER, buf);
    }

    @Override
    public void setMinigameVisible(BlockPos pos, boolean visible) {
        FabricAnvilMinigameEvents.setIsVisible(pos, visible);
    }

    @Override
    public void resetMinigameForAnvil(Level level, BlockPos pos) {
        ModEvents.resetMinigameForAnvil(level, pos);
    }

    @Override
    public void resetMinigameForPlayer(ServerPlayer player) {
        ModEvents.resetMinigameForPlayer(player);
    }

    @Override
    public void resetMinigameForPlayer(ServerPlayer player, BlockPos pos) {
        // The Fabric port always reset the player's whole session here
        ModEvents.resetMinigameForPlayer(player);
    }

    @Override
    public void hideMinigame(ServerPlayer player) {
        ModItemInteractEvents.hideMinigame(player);
    }

    @Override
    public void releaseAnvil(ServerPlayer player, BlockPos pos) {
        ModItemInteractEvents.releaseAnvil(player, pos);
    }

    @Override
    public void openAnvilScreen(ServerPlayer player, AbstractSmithingAnvilBlockEntity anvil, BlockPos pos) {
        net.stirdrem.overgeared.platform.Services.PLATFORM.openMenu(player, anvil, pos);
    }

    @Override
    public void damageHammer(ItemStack held, Player player, InteractionHand hand) {
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        held.hurtAndBreak(1, player, p -> {
            p.broadcastBreakEvent(slot);
            if (p instanceof ServerPlayer serverPlayer) {
                ModEvents.resetMinigameForPlayer(serverPlayer);
            }
        });
    }
}
