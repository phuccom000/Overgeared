package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.screen.BlueprintWorkbenchScreenHandler;

import java.util.Optional;

/** C2S: the player picked a tool type in the blueprint workbench. */
public record SelectToolTypeC2SPacket(String toolTypeId, int containerId) implements CustomPacketPayload {
    public static final Type<SelectToolTypeC2SPacket> TYPE = new Type<>(Overgeared.id("select_tool_type"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectToolTypeC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SelectToolTypeC2SPacket::toolTypeId,
            ByteBufCodecs.INT, SelectToolTypeC2SPacket::containerId,
            SelectToolTypeC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectToolTypeC2SPacket msg, ServerPlayer player) {
        Optional<ToolType> optional = ToolTypeRegistry.byId(msg.toolTypeId);
        if (optional.isPresent()) {
            Overgeared.LOGGER.debug("ToolType '{}' found. Proceeding to create blueprint.", msg.toolTypeId);
            if (player.containerMenu instanceof BlueprintWorkbenchScreenHandler menu) {
                menu.createBlueprint(optional.get());
                menu.broadcastChanges(); // ensure client sync
            } else {
                Overgeared.LOGGER.warn("Player '{}' is not in BlueprintWorkbenchScreenHandler, but in {}",
                        player.getGameProfile().name(),
                        player.containerMenu.getClass().getSimpleName());
            }
        } else {
            Overgeared.LOGGER.error("ToolTypeRegistry.byId('{}') returned empty; cannot create blueprint.", msg.toolTypeId);
        }
    }
}
