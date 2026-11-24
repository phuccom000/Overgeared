package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.OvergearedMod;

// Extends RenderType only to reach its protected state shards.
public final class HeatGlintRenderTypes extends RenderType {
    public static final ResourceLocation HEATED_GLINT = OvergearedMod.loc("textures/misc/heated_glint.png");

    // Same states as vanilla's glint and glint_translucent, with the heated texture.
    public static final RenderType HEAT_GLINT = create(
            OvergearedMod.MOD_ID + "_heat_glint",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_GLINT_SHADER)
                    .setTextureState(new TextureStateShard(HEATED_GLINT, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(GLINT_TEXTURING)
                    .createCompositeState(false)
    );

    public static final RenderType HEAT_GLINT_TRANSLUCENT = create(
            OvergearedMod.MOD_ID + "_heat_glint_translucent",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(HEATED_GLINT, true, false))
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(EQUAL_DEPTH_TEST)
                    .setTransparencyState(GLINT_TRANSPARENCY)
                    .setTexturingState(GLINT_TEXTURING)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false)
    );

    private HeatGlintRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                 boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }
}
