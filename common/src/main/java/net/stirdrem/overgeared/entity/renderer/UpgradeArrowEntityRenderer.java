package net.stirdrem.overgeared.entity.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class UpgradeArrowEntityRenderer extends ArrowRenderer<UpgradeArrowEntity, UpgradeArrowEntityRenderer.State> {
    public static class State extends ArrowRenderState {
        public ArrowTier tier = ArrowTier.FLINT;
    }

    public UpgradeArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(UpgradeArrowEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tier = entity.getArrowTier();
    }

    @Override
    protected Identifier getTextureLocation(State state) {
        // e.g. textures/entity/projectiles/arrows/iron.png
        return Identifier.fromNamespaceAndPath(OvergearedMod.MOD_ID, "textures/entity/projectiles/arrows/" + state.tier.getSerializedName() + ".png");
    }
}
