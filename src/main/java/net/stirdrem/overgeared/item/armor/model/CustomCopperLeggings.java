package net.stirdrem.overgeared.item.armor.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.stirdrem.overgeared.Overgeared;

public class CustomCopperLeggings {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Overgeared.id("copper_leggings"), "main");

    public static TexturedModelData createBodyLayer() {
        ModelData modelData = new ModelData();
        ModelPartData partData = modelData.getRoot();

        partData.addChild("Body", ModelPartBuilder.create()
                        .uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.51F))
                        .uv(16, 0).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.65F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        partData.addChild("RightLeg", ModelPartBuilder.create()
                        .uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.5F))
                        .uv(40, 0).cuboid(-2.1F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.65F)),
                ModelTransform.pivot(-1.9F, 12.0F, 0.0F));

        partData.addChild("LeftLeg", ModelPartBuilder.create()
                        .uv(0, 16).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.5F)).mirrored(false)
                        .uv(40, 0).cuboid(-5.9F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.65F)),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 32);
    }
}
