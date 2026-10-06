package net.stirdrem.overgeared.entity.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

import java.util.EnumMap;
import java.util.Map;

public class UpgradeArrowEntityRenderer extends ArrowRenderer<UpgradeArrowEntity, UpgradeArrowRenderState> {
    private static final Map<ArrowTier, Identifier> TEXTURES = new EnumMap<>(ArrowTier.class);

    static {
        for (ArrowTier tier : ArrowTier.values()) {
            TEXTURES.put(tier, Overgeared.id("textures/entity/projectiles/arrows/" + tier.getSerializedName() + ".png"));
        }
    }

    public UpgradeArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public UpgradeArrowRenderState createRenderState() {
        return new UpgradeArrowRenderState();
    }

    @Override
    public void extractRenderState(UpgradeArrowEntity entity, UpgradeArrowRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tier = entity.getArrowTier();
    }

    @Override
    protected Identifier getTextureLocation(UpgradeArrowRenderState state) {
        return TEXTURES.get(state.tier);
    }
}
