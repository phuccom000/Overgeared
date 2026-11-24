package net.stirdrem.overgeared.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.client.HeatGlintRenderTypes;
import net.stirdrem.overgeared.heateditem.HeatedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Unique
    private static final String RENDER = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V";

    @WrapOperation(method = RENDER, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer overgeared$heatGlintDirect(MultiBufferSource buffers, RenderType type, boolean noEntity, boolean glint,
                                                      Operation<VertexConsumer> original,
                                                      @Local(argsOnly = true) ItemStack stack) {
        VertexConsumer base = original.call(buffers, type, noEntity, glint);
        if (!HeatedItem.isHeated(stack)) return base;
        return VertexMultiConsumer.create(buffers.getBuffer(HeatGlintRenderTypes.HEAT_GLINT), base);
    }

    @WrapOperation(method = RENDER, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer overgeared$heatGlint(MultiBufferSource buffers, RenderType type, boolean isItem, boolean glint,
                                                Operation<VertexConsumer> original,
                                                @Local(argsOnly = true) ItemStack stack) {
        VertexConsumer base = original.call(buffers, type, isItem, glint);
        if (!HeatedItem.isHeated(stack)) return base;
        return VertexMultiConsumer.create(buffers.getBuffer(overgeared$glintFor(type)), base);
    }

    // Mirrors vanilla getFoilBuffer: Fabulous graphics draws translucent items to a separate target.
    @Unique
    private static RenderType overgeared$glintFor(RenderType type) {
        return Minecraft.useShaderTransparency() && type == Sheets.translucentItemSheet()
                ? HeatGlintRenderTypes.HEAT_GLINT_TRANSLUCENT
                : HeatGlintRenderTypes.HEAT_GLINT;
    }
}
