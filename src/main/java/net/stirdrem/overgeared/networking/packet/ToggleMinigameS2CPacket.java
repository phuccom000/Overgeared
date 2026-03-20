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

import static net.stirdrem.overgeared.event.ModItemInteractEvents.playerMinigameVisibility;

public record ToggleMinigameS2CPacket(BlockPos pos, boolean visible)
        implements CustomPacketPayload {

    public static final Type<ToggleMinigameS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "toggle_minigame"));

    public static final StreamCodec<FriendlyByteBuf, ToggleMinigameS2CPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> {
                        buf.writeBlockPos(msg.pos);
                        buf.writeBoolean(msg.visible);
                    },
                    buf -> new ToggleMinigameS2CPacket(
                            buf.readBlockPos(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleMinigameS2CPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            AnvilMinigameEvents.setIsVisible(msg.pos(), msg.visible());
            playerMinigameVisibility.put(player.getUUID(), msg.visible());
        });
    }
}