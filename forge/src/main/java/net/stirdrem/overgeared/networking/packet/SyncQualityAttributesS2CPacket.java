package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.QualityAttributeSync;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;

import java.util.List;
import java.util.function.Supplier;

/** Sends the quality_attributes definitions to a client, for item tooltips. See {@link QualityAttributeSync}. */
public class SyncQualityAttributesS2CPacket {
    private final List<QualityAttributeDefinition> definitions;

    public SyncQualityAttributesS2CPacket() {
        this.definitions = null; // written from the server's current definitions
    }

    public SyncQualityAttributesS2CPacket(FriendlyByteBuf buf) {
        this.definitions = QualityAttributeSync.read(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        QualityAttributeSync.write(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> QualityAttributeReloadListener.acceptSyncedDefinitions(definitions));
        return true;
    }
}
