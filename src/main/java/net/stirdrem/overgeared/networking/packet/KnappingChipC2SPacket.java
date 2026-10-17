package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.screen.RockKnappingScreenHandler;

public class KnappingChipC2SPacket {
    private final int index;

    public KnappingChipC2SPacket(int index) {
        this.index = index;
    }

    public static void encode(KnappingChipC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.index);
    }

    public static KnappingChipC2SPacket decode(FriendlyByteBuf buf) {
        return new KnappingChipC2SPacket(buf.readInt());
    }

    public static void handle(KnappingChipC2SPacket msg, MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            if (player.containerMenu instanceof RockKnappingScreenHandler menu) {
                if (msg.index >= 0 && msg.index < 9) {
                    menu.setChip(msg.index);
                    Overgeared.LOGGER.debug("Player {} chipped spot {} in knapping grid", player.getName().getString(), msg.index);
                }
            }
        });
    }
}
