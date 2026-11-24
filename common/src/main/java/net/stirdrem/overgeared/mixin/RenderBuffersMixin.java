package net.stirdrem.overgeared.mixin;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.stirdrem.overgeared.client.HeatGlintRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.SequencedMap;

// Glint uses an EQUAL depth test, so it must flush after the item it covers;
// a fixed buffer keeps it out of the shared buffer, which flushes first.
@Mixin(RenderBuffers.class)
public abstract class RenderBuffersMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;immediateWithBuffers(Ljava/util/SequencedMap;Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;)Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"),
            index = 0)
    private SequencedMap<RenderType, ByteBufferBuilder> overgeared$addHeatGlintBuffers(SequencedMap<RenderType, ByteBufferBuilder> buffers) {
        buffers.put(HeatGlintRenderTypes.HEAT_GLINT, new ByteBufferBuilder(HeatGlintRenderTypes.HEAT_GLINT.bufferSize()));
        buffers.put(HeatGlintRenderTypes.HEAT_GLINT_TRANSLUCENT, new ByteBufferBuilder(HeatGlintRenderTypes.HEAT_GLINT_TRANSLUCENT.bufferSize()));
        return buffers;
    }
}
