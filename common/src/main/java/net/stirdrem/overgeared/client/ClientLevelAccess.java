package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Client-only accessors. Keeping client classes (e.g. ClientLevel) out of common code
 * prevents the bytecode verifier from loading them on a dedicated server.
 */
public final class ClientLevelAccess {
    private ClientLevelAccess() {
    }

    @Nullable
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }
}
