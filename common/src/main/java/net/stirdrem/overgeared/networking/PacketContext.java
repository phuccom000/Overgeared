package net.stirdrem.overgeared.networking;

import net.minecraft.world.entity.player.Player;

/**
 * Loader-independent view of the context a packet is handled in.
 */
public interface PacketContext {
    /** The sending player for serverbound packets, the local player for clientbound ones. */
    Player player();

    /** Runs the task on the main thread of the receiving side. */
    void enqueueWork(Runnable task);
}
