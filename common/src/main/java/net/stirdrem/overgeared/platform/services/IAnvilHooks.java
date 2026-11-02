package net.stirdrem.overgeared.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;

/**
 * Loader-specific behaviour used by the smithing anvil block and block entity: networking, minigame
 * session handling and screen opening. Implemented per loader and loaded through {@code Services.ANVIL}.
 */
public interface IAnvilHooks {

    /** Client: send the result of a minigame hit to the server. */
    void sendHitPacket(BlockPos pos, String quality);

    /** Client: show or hide the minigame overlay for the anvil at {@code pos}. */
    void setMinigameVisible(BlockPos pos, boolean visible);

    void resetMinigameForAnvil(Level level, BlockPos pos);

    void resetMinigameForPlayer(ServerPlayer player);

    void resetMinigameForPlayer(ServerPlayer player, BlockPos pos);

    void hideMinigame(ServerPlayer player);

    void releaseAnvil(ServerPlayer player, BlockPos pos);

    void openAnvilScreen(ServerPlayer player, AbstractSmithingAnvilBlockEntity anvil, BlockPos pos);

    void damageHammer(ItemStack held, Player player, InteractionHand hand);

    /** Server: a player is breaking the anvil. */
    default void onPlayerDestroyAnvil(ServerPlayer player, BlockPos pos) {
    }

    /** Server: the anvil was destroyed by an explosion. */
    default void onAnvilExploded(Level level, BlockPos pos) {
    }
}
