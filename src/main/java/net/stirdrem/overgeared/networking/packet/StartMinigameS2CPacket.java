package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

public record StartMinigameS2CPacket(BlockPos pos, int hits, String quality)
        implements CustomPacketPayload {

    public static final Type<StartMinigameS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "start_minigame"));

    public static final StreamCodec<FriendlyByteBuf, StartMinigameS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeBlockPos(msg.pos);
                        buf.writeInt(msg.hits);
                        buf.writeUtf(msg.quality);
                    },
                    buf -> new StartMinigameS2CPacket(
                            buf.readBlockPos(),
                            buf.readInt(),
                            buf.readUtf()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StartMinigameS2CPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            AnvilMinigameEvents.reset(msg.quality());
            AnvilMinigameEvents.setHitsRemaining(msg.hits());
            AnvilMinigameEvents.setAnvilPos(player.getUUID(), msg.pos());
            AnvilMinigameEvents.setMinigameStarted(msg.pos(), true);
            AnvilMinigameEvents.setIsVisible(msg.pos(), true);
        });
    }
}