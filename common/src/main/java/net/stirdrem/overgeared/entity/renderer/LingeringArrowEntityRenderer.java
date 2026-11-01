package net.stirdrem.overgeared.entity.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;

public class LingeringArrowEntityRenderer extends ArrowRenderer<LingeringArrowEntity> {
    public LingeringArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LingeringArrowEntity entity) {
        return ResourceLocation.tryBuild(Overgeared.MOD_ID, "textures/entity/projectiles/arrows/flint.png");
    }
}
