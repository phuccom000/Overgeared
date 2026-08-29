package net.stirdrem.overgeared.entity.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;

public class LingeringArrowEntityRenderer extends ProjectileEntityRenderer<LingeringArrowEntity> {
    private static final Identifier TEXTURE = Overgeared.id("textures/entity/projectiles/arrows/flint.png");

    public LingeringArrowEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(LingeringArrowEntity entity) {
        return TEXTURE;
    }
}
