package net.stirdrem.overgeared.item.armor.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.stirdrem.overgeared.Overgeared;

public class CustomCopperLeggings {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Overgeared.id("copper_leggings"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition partData = modelData.getRoot();

        partData.addOrReplaceChild("Body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F))
                        .texOffs(16, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.65F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partData.addOrReplaceChild("RightLeg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
                        .texOffs(40, 0).addBox(-2.1F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.65F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        partData.addOrReplaceChild("LeftLeg", CubeListBuilder.create()
                        .texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                        .texOffs(40, 0).addBox(-5.9F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.65F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(modelData, 64, 32);
    }
}
