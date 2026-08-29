package net.stirdrem.overgeared.entity.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class UpgradeArrowEntityRenderer extends ProjectileEntityRenderer<UpgradeArrowEntity> {
    public UpgradeArrowEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(UpgradeArrowEntity entity) {
        ArrowTier tier = entity.getArrowTier();
        return Overgeared.id("textures/entity/projectiles/arrows/" + tier.getSerializedName() + ".png");
    }
}
