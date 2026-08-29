package net.stirdrem.overgeared.item.armor.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.stirdrem.overgeared.Overgeared;

/**
 * Blockbench-authored custom armor geometry, ported from the Forge original's
 * {@code EntityModel<T>} subclass. Only the baked-part extraction is needed here -
 * rendering goes through Fabric's {@code ArmorRenderer.renderPart}, see
 * {@link net.stirdrem.overgeared.client.CopperArmorRenderer}.
 */
public class CustomCopperHelmet {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Overgeared.id("copper_helmet"), "main");

    public static TexturedModelData createBodyLayer() {
        ModelData modelData = new ModelData();
        ModelPartData partData = modelData.getRoot();

        ModelPartData head = partData.addChild("Head", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(1.0F))
                        .uv(34, -15).cuboid(0.0F, -15.0F, -5.0F, 0.0F, 6.0F, 15.0F, Dilation.NONE)
                        .uv(54, 1).cuboid(0.0F, -9.0F, 5.0F, 0.0F, 2.0F, 5.0F, Dilation.NONE),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        head.addChild("cube_r1", ModelPartBuilder.create()
                        .uv(54, 3).cuboid(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -8.0F, 6.0F, 0.0F, -0.1963F, 0.0F));

        head.addChild("cube_r2", ModelPartBuilder.create()
                        .uv(54, 3).cuboid(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -8.0F, 6.0F, 0.0F, -0.3927F, 0.0F));

        head.addChild("cube_r3", ModelPartBuilder.create()
                        .uv(54, 3).cuboid(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -8.0F, 6.0F, 0.0F, 0.1963F, 0.0F));

        head.addChild("cube_r4", ModelPartBuilder.create()
                        .uv(54, 3).cuboid(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -8.0F, 6.0F, 0.0F, 0.3927F, 0.0F));

        head.addChild("cube_r5", ModelPartBuilder.create()
                        .uv(34, -15).cuboid(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, -0.1963F));

        head.addChild("cube_r6", ModelPartBuilder.create()
                        .uv(34, -15).cuboid(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        head.addChild("cube_r7", ModelPartBuilder.create()
                        .uv(34, -15).cuboid(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, 0.1963F));

        head.addChild("cube_r8", ModelPartBuilder.create()
                        .uv(34, -15).cuboid(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, Dilation.NONE),
                ModelTransform.of(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

        return TexturedModelData.of(modelData, 64, 32);
    }
}
