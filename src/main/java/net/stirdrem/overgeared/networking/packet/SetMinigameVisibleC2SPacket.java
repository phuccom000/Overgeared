package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

/** C2S: the client toggled the visibility of the minigame at the anvil at {@code pos}. */
public record SetMinigameVisibleC2SPacket(BlockPos pos, boolean visible) implements CustomPacketPayload {
    public static final Type<SetMinigameVisibleC2SPacket> TYPE = new Type<>(Overgeared.id("set_minigame_visible"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMinigameVisibleC2SPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetMinigameVisibleC2SPacket::pos,
            ByteBufCodecs.BOOL, SetMinigameVisibleC2SPacket::visible,
            SetMinigameVisibleC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public boolean getVisible() {
        return visible;
    }

    public static void handle(SetMinigameVisibleC2SPacket msg, ServerPlayer sender) {
        if (sender.level().getBlockEntity(msg.pos) instanceof AbstractSmithingAnvilBlockEntity anvilBlock) {
            anvilBlock.setMinigameOn(msg.visible);
            ModItemInteractEvents.playerMinigameVisibility.put(sender.getUUID(), msg.visible);
        }
    }
}
