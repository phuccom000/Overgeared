package net.stirdrem.overgeared.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.ModPackets;
import net.stirdrem.overgeared.networking.PacketContext;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class NeoForgeNetworking {
    private NeoForgeNetworking() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(OvergearedMod.MOD_ID);
        for (ModPackets.Packet<?> packet : ModPackets.CLIENTBOUND) {
            registerToClient(registrar, packet);
        }
        for (ModPackets.Packet<?> packet : ModPackets.SERVERBOUND) {
            registerToServer(registrar, packet);
        }
    }

    private static <T extends CustomPacketPayload> void registerToClient(PayloadRegistrar registrar, ModPackets.Packet<T> packet) {
        registrar.playToClient(packet.type(), packet.codec(), (payload, context) -> packet.handler().accept(payload, wrap(context)));
    }

    private static <T extends CustomPacketPayload> void registerToServer(PayloadRegistrar registrar, ModPackets.Packet<T> packet) {
        registrar.playToServer(packet.type(), packet.codec(), (payload, context) -> packet.handler().accept(payload, wrap(context)));
    }

    private static PacketContext wrap(IPayloadContext context) {
        return new PacketContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public void enqueueWork(Runnable task) {
                context.enqueueWork(task);
            }
        };
    }
}
