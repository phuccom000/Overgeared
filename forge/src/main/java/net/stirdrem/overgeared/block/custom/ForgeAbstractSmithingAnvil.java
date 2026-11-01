package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.network.NetworkHooks;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.ForgeAbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ForgeAnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;

public abstract class ForgeAbstractSmithingAnvil extends AbstractSmithingAnvil {
    public ForgeAbstractSmithingAnvil(AnvilTier anvilTier, Properties properties) {
        super(anvilTier, properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // For player destruction, we can still use the targeted approach
            resetMinigameForPlayer(serverPlayer, pos);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide()) {
            resetMinigameForAnvil(level, pos);
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    @Override
    protected void sendHitPacket(BlockPos pos, String quality) {
        ModMessages.sendToServer(new PacketSendCounterC2SPacket(pos, quality));
    }

    @Override
    protected void setMinigameVisible(BlockPos pos, boolean visible) {
        ForgeAnvilMinigameEvents.setIsVisible(pos, visible);
    }

    @Override
    protected void resetMinigameForAnvil(Level level, BlockPos pos) {
        ModEvents.resetMinigameForAnvil(level, pos);
    }

    @Override
    protected void resetMinigameForPlayer(ServerPlayer player, BlockPos pos) {
        ModEvents.resetMinigameForPlayer(player, pos);
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
        NetworkHooks.openScreen(player, (ForgeAbstractSmithingAnvilBlockEntity) anvil, pos);
    }

    @Override
    protected void damageHammer(ItemStack held, Player player, InteractionHand hand) {
        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
    }
}