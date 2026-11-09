package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.networking.PacketContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.screen.BlueprintWorkbenchMenu;

import java.util.Optional;

public record SelectToolTypeC2SPacket(String toolTypeId, int containerId) implements CustomPacketPayload {
    public static final Identifier ID = OvergearedMod.loc("select_tool_type");
    public static final CustomPacketPayload.Type<SelectToolTypeC2SPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SelectToolTypeC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, packet.toolTypeId);
                ByteBufCodecs.INT.encode(buffer, packet.containerId);
            },
            buffer -> new SelectToolTypeC2SPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    ByteBufCodecs.INT.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectToolTypeC2SPacket payload,  PacketContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Optional<ToolType> optional = ToolTypeRegistry.byId(payload.toolTypeId);
            if (optional.isPresent()) {
                OvergearedMod.LOGGER.debug("ToolType '{}' found. Proceeding to create blueprint.", payload.toolTypeId);
                if (player.containerMenu instanceof BlueprintWorkbenchMenu menu) {
                    menu.createBlueprint(optional.get());
                    menu.broadcastChanges(); // ensure client sync
                } else {
                    OvergearedMod.LOGGER.warn("Player '{}' is not in BlueprintWorkbenchMenu, but in {}",
                            player.getGameProfile().name(),
                            player.containerMenu.getClass().getSimpleName());
                }
            } else {
                OvergearedMod.LOGGER.error("ToolTypeRegistry.byId('{}') returned empty; cannot create blueprint.", payload.toolTypeId);
            }
        });
    }
}