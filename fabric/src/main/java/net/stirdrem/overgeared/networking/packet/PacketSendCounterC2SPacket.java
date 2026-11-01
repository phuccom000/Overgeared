package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;

public class PacketSendCounterC2SPacket {
    private final String quality;
    private final BlockPos pos;

    public PacketSendCounterC2SPacket(BlockPos pos, String quality) {
        this.quality = quality;
        this.pos = pos;
    }

    public static void encode(PacketSendCounterC2SPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeUtf(pkt.quality);
    }

    public static PacketSendCounterC2SPacket decode(FriendlyByteBuf buf) {
        return new PacketSendCounterC2SPacket(buf.readBlockPos(), buf.readUtf());
    }

    public String getCounter() {
        return quality;
    }

    public static void handle(PacketSendCounterC2SPacket msg, MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> {
            if (sender.level().getBlockState(msg.pos).getBlock() instanceof AbstractSmithingAnvil) {
                AbstractSmithingAnvil.setQuality(msg.getCounter());
            }
        });
    }
}
