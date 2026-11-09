package net.stirdrem.overgeared.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * Opens menus whose types were created with {@link IPlatformHelper#createMenuType}.
 * Does nothing on the client, like NeoForge's {@code Player#openMenu} overloads.
 */
public final class Menus {
    private Menus() {
    }

    public static void openMenu(Player player, MenuProvider provider, BlockPos pos) {
        openMenu(player, provider, buf -> buf.writeBlockPos(pos));
    }

    public static void openMenu(Player player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.openMenu(serverPlayer, provider, extraData);
        }
    }
}
