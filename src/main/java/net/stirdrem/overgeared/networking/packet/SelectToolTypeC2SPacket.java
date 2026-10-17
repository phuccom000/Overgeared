package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(toolTypeId);
        buf.writeInt(containerId);
    }

    public static SelectToolTypeC2SPacket decode(FriendlyByteBuf buf) {
        return new SelectToolTypeC2SPacket(buf.readUtf(), buf.readInt());
    }

    public static void handle(SelectToolTypeC2SPacket msg, MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            Optional<ToolType> optional = ToolTypeRegistry.byId(msg.toolTypeId);
            if (optional.isPresent()) {
                Overgeared.LOGGER.debug("ToolType '{}' found. Proceeding to create blueprint.", msg.toolTypeId);
                if (player.containerMenu instanceof BlueprintWorkbenchScreenHandler menu) {
                    menu.createBlueprint(optional.get());
                    menu.broadcastChanges(); // ensure client sync
                } else {
                    Overgeared.LOGGER.warn("Player '{}' is not in BlueprintWorkbenchScreenHandler, but in {}",
                            player.getGameProfile().getName(),
                            player.containerMenu.getClass().getSimpleName());
                }
            } else {
                Overgeared.LOGGER.error("ToolTypeRegistry.byId('{}') returned empty; cannot create blueprint.", msg.toolTypeId);
            }
        });
    }

}
