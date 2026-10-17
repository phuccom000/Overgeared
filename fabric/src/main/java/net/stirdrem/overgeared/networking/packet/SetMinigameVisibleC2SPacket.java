package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public class SetMinigameVisibleC2SPacket {
    private final Boolean visible;
    private final BlockPos pos;

    public SetMinigameVisibleC2SPacket(BlockPos pos, Boolean visible) {
        this.visible = visible;
        this.pos = pos;
    }

    public static void encode(SetMinigameVisibleC2SPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeBoolean(pkt.visible);
    }

    public static SetMinigameVisibleC2SPacket decode(FriendlyByteBuf buf) {
        return new SetMinigameVisibleC2SPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public Boolean getVisible() {
        return visible;
    }

    public static void handle(SetMinigameVisibleC2SPacket msg, MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> {
            if (sender.level().getBlockEntity(msg.pos) instanceof AbstractSmithingAnvilBlockEntity anvilBlock) {
                anvilBlock.setMinigameOn(msg.getVisible());
                ModItemInteractEvents.playerMinigameVisibility.put(sender.getUUID(), msg.getVisible());
            }
        });
    }


}
