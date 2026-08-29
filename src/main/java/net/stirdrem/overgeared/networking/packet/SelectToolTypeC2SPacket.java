package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.screen.BlueprintWorkbenchScreenHandler;

import java.util.Optional;

public class SelectToolTypeC2SPacket {
    private final String toolTypeId;
    private final int containerId;

    public SelectToolTypeC2SPacket(String toolTypeId, int containerId) {
        this.toolTypeId = toolTypeId;
        this.containerId = containerId;
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeString(toolTypeId);
        buf.writeInt(containerId);
    }

    public static SelectToolTypeC2SPacket decode(PacketByteBuf buf) {
        return new SelectToolTypeC2SPacket(buf.readString(), buf.readInt());
    }

    public static void handle(SelectToolTypeC2SPacket msg, MinecraftServer server, ServerPlayerEntity player) {
        server.execute(() -> {
            Optional<ToolType> optional = ToolTypeRegistry.byId(msg.toolTypeId);
            if (optional.isPresent()) {
                Overgeared.LOGGER.debug("ToolType '{}' found. Proceeding to create blueprint.", msg.toolTypeId);
                if (player.currentScreenHandler instanceof BlueprintWorkbenchScreenHandler menu) {
                    menu.createBlueprint(optional.get());
                    menu.sendContentUpdates(); // ensure client sync
                } else {
                    Overgeared.LOGGER.warn("Player '{}' is not in BlueprintWorkbenchScreenHandler, but in {}",
                            player.getGameProfile().getName(),
                            player.currentScreenHandler.getClass().getSimpleName());
                }
            } else {
                Overgeared.LOGGER.error("ToolTypeRegistry.byId('{}') returned empty; cannot create blueprint.", msg.toolTypeId);
            }
        });
    }

}
