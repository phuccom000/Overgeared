package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.Constants;

// Extends RenderType to reach its protected state shards. Built through the public constructor
// because RenderType#create is private in vanilla 1.20.1.
public final class HeatGlintRenderTypes extends RenderType {
    public static final ResourceLocation HEATED_GLINT = new ResourceLocation(Constants.MOD_ID, "textures/misc/heated_glint.png");

    // Same states as vanilla's glint, glint_direct and glint_translucent, with the heated texture.
    public static final RenderType HEAT_GLINT = glint("heat_glint", RENDERTYPE_GLINT_SHADER, MAIN_TARGET);
    public static final RenderType HEAT_GLINT_DIRECT = glint("heat_glint_direct", RENDERTYPE_GLINT_DIRECT_SHADER, MAIN_TARGET);
    public static final RenderType HEAT_GLINT_TRANSLUCENT = glint("heat_glint_translucent", RENDERTYPE_GLINT_TRANSLUCENT_SHADER, ITEM_ENTITY_TARGET);

    private HeatGlintRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                 boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }

    private static RenderType glint(String name, ShaderStateShard shader, OutputStateShard output) {
        RenderStateShard[] shards = {
                new TextureStateShard(HEATED_GLINT, true, false),
                shader,
                GLINT_TRANSPARENCY,
                EQUAL_DEPTH_TEST,
                NO_CULL,
                NO_LIGHTMAP,
                NO_OVERLAY,
                output,
                GLINT_TEXTURING,
                COLOR_WRITE
        };
        return new HeatGlintRenderTypes(Constants.MOD_ID + "_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false,
                () -> {
                    for (RenderStateShard shard : shards) shard.setupRenderState();
                },
                () -> {
                    for (RenderStateShard shard : shards) shard.clearRenderState();
                });
    }
}
