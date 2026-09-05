package net.stirdrem.overgeared.item.armor.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.stirdrem.overgeared.Overgeared;

/**
 * Blockbench-authored custom armor geometry, ported from the Forge original's
 * {@code EntityModel<T>} subclass. Only the baked-part extraction is needed here -
 * rendering goes through Fabric's {@code ArmorRenderer.renderPart}, see
 * {@link net.stirdrem.overgeared.client.CopperArmorRenderer}.
 */
public class CustomCopperHelmet {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Overgeared.id("copper_helmet"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition partData = modelData.getRoot();

        PartDefinition head = partData.addOrReplaceChild("Head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F))
                        .texOffs(34, -15).addBox(0.0F, -15.0F, -5.0F, 0.0F, 6.0F, 15.0F, CubeDeformation.NONE)
                        .texOffs(54, 1).addBox(0.0F, -9.0F, 5.0F, 0.0F, 2.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(54, 3).addBox(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -8.0F, 6.0F, 0.0F, -0.1963F, 0.0F));

        head.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(54, 3).addBox(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -8.0F, 6.0F, 0.0F, -0.3927F, 0.0F));

        head.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                        .texOffs(54, 3).addBox(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -8.0F, 6.0F, 0.0F, 0.1963F, 0.0F));

        head.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                        .texOffs(54, 3).addBox(0.0F, -7.0F, -1.0F, 0.0F, 9.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -8.0F, 6.0F, 0.0F, 0.3927F, 0.0F));

        head.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                        .texOffs(34, -15).addBox(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, -0.1963F));

        head.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                        .texOffs(34, -15).addBox(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        head.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                        .texOffs(34, -15).addBox(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, 0.1963F));

        head.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                        .texOffs(34, -15).addBox(0.0F, -5.0F, -5.0F, 0.0F, 5.0F, 15.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

        return LayerDefinition.create(modelData, 64, 32);
    }
}
