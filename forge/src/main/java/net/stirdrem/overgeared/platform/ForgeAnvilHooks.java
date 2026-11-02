package net.stirdrem.overgeared.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ForgeAnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;
import net.stirdrem.overgeared.platform.services.IAnvilHooks;

/** Forge anvil hooks (previously the ForgeAbstractSmithingAnvil subclass). */
public class ForgeAnvilHooks implements IAnvilHooks {

    @Override
    public void sendHitPacket(BlockPos pos, String quality) {
        ModMessages.sendToServer(new PacketSendCounterC2SPacket(pos, quality));
    }

    @Override
    public void setMinigameVisible(BlockPos pos, boolean visible) {
        ForgeAnvilMinigameEvents.setIsVisible(pos, visible);
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
        ModEvents.resetMinigameForPlayer(player, pos);
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
        NetworkHooks.openScreen(player, anvil, pos);
    }

    @Override
    public void damageHammer(ItemStack held, Player player, InteractionHand hand) {
        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
    }

    @Override
    public void onPlayerDestroyAnvil(ServerPlayer player, BlockPos pos) {
        resetMinigameForPlayer(player, pos);
    }

    @Override
    public void onAnvilExploded(Level level, BlockPos pos) {
        resetMinigameForAnvil(level, pos);
    }
}
