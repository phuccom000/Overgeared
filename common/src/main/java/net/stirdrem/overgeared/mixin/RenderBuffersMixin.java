package net.stirdrem.overgeared.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.stirdrem.overgeared.client.HeatGlintRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

// Glint uses an EQUAL depth test, so it must flush after the item it covers;
// a fixed buffer keeps it out of the shared buffer, which flushes first.
@Mixin(RenderBuffers.class)
public abstract class RenderBuffersMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;immediateWithBuffers(Ljava/util/Map;Lcom/mojang/blaze3d/vertex/BufferBuilder;)Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"),
            index = 0)
    private Map<RenderType, BufferBuilder> overgeared$addHeatGlintBuffers(Map<RenderType, BufferBuilder> buffers) {
        overgeared$put(buffers, HeatGlintRenderTypes.HEAT_GLINT);
        overgeared$put(buffers, HeatGlintRenderTypes.HEAT_GLINT_DIRECT);
        overgeared$put(buffers, HeatGlintRenderTypes.HEAT_GLINT_TRANSLUCENT);
        return buffers;
    }

    @Unique
    private static void overgeared$put(Map<RenderType, BufferBuilder> buffers, RenderType type) {
        buffers.put(type, new BufferBuilder(type.bufferSize()));
    }
}
