package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;

/** C2S: the quality the player hit in the anvil minigame at {@code pos}. */
public record PacketSendCounterC2SPacket(BlockPos pos, String quality) implements CustomPacketPayload {
    public static final Type<PacketSendCounterC2SPacket> TYPE = new Type<>(Overgeared.id("send_counter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSendCounterC2SPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketSendCounterC2SPacket::pos,
            ByteBufCodecs.STRING_UTF8, PacketSendCounterC2SPacket::quality,
            PacketSendCounterC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public String getCounter() {
        return quality;
    }

    public static void handle(PacketSendCounterC2SPacket msg, ServerPlayer sender) {
        if (sender.level().getBlockState(msg.pos).getBlock() instanceof AbstractSmithingAnvil) {
            AbstractSmithingAnvil.setQuality(msg.getCounter());
        }
    }
}
