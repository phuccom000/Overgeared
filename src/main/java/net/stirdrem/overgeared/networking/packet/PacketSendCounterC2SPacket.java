package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;

public class PacketSendCounterC2SPacket {
    private final String quality;
    private final BlockPos pos;

    public PacketSendCounterC2SPacket(BlockPos pos, String quality) {
        this.quality = quality;
        this.pos = pos;
    }

    public static void encode(PacketSendCounterC2SPacket pkt, PacketByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeString(pkt.quality);
    }

    public static PacketSendCounterC2SPacket decode(PacketByteBuf buf) {
        return new PacketSendCounterC2SPacket(buf.readBlockPos(), buf.readString());
    }

    public String getCounter() {
        return quality;
    }

    public static void handle(PacketSendCounterC2SPacket msg, MinecraftServer server, ServerPlayerEntity sender) {
        server.execute(() -> {
            if (sender.getWorld().getBlockState(msg.pos).getBlock() instanceof AbstractSmithingAnvil) {
                AbstractSmithingAnvil.setQuality(msg.getCounter());
            }
        });
    }
}
