package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.screen.RockKnappingScreenHandler;

/** C2S: the player chipped spot {@code index} (0-8) of the open knapping grid. */
public record KnappingChipC2SPacket(int index) implements CustomPacketPayload {
    public static final Type<KnappingChipC2SPacket> TYPE = new Type<>(Overgeared.id("knapping_chip"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KnappingChipC2SPacket> STREAM_CODEC =
            ByteBufCodecs.INT.<KnappingChipC2SPacket>map(KnappingChipC2SPacket::new, KnappingChipC2SPacket::index).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Server thread (Fabric runs play receivers on the server thread). */
    public static void handle(KnappingChipC2SPacket msg, ServerPlayer player) {
        if (player.containerMenu instanceof RockKnappingScreenHandler menu) {
            if (msg.index >= 0 && msg.index < 9) {
                menu.setChip(msg.index);
                Overgeared.LOGGER.debug("Player {} chipped spot {} in knapping grid", player.getName().getString(), msg.index);
            }
        }
    }
}
