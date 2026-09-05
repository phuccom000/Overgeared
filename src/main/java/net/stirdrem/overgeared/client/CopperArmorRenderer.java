package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.item.armor.model.CustomCopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CustomCopperLeggings;

/**
 * Replaces the vanilla humanoid armor model with the Blockbench-authored geometry for the
 * head and leg slots only - the chestplate/boots keep the vanilla model, matching the
 * original Forge port (which only overrode getHumanoidArmorModel for helmet/leggings).
 */
public class CopperArmorRenderer implements ArmorRenderer {
    private static final ResourceLocation HELMET_TEXTURE = Overgeared.id("textures/models/armor/copper_layer_1.png");
    private static final ResourceLocation LEGGINGS_TEXTURE = Overgeared.id("textures/models/armor/copper_layer_2.png");

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack,
                        LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel) {
        var modelLoader = Minecraft.getInstance().getEntityModels();

        if (slot == EquipmentSlot.HEAD) {
            ModelPart root = modelLoader.bakeLayer(CustomCopperHelmet.LAYER_LOCATION);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("Head"), HELMET_TEXTURE);
        } else if (slot == EquipmentSlot.LEGS) {
            ModelPart root = modelLoader.bakeLayer(CustomCopperLeggings.LAYER_LOCATION);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("Body"), LEGGINGS_TEXTURE);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("RightLeg"), LEGGINGS_TEXTURE);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("LeftLeg"), LEGGINGS_TEXTURE);
        }
    }

    /**
     * Equivalent to ArmorRenderer.renderPart, which takes a Model rather than a bare ModelPart -
     * our geometry is extracted straight from baked TexturedModelData with no Model wrapper.
     */
    private static void renderPart(PoseStack matrices, MultiBufferSource vertexConsumers, int light,
                                     ItemStack stack, ModelPart part, ResourceLocation texture) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                vertexConsumers, RenderType.armorCutoutNoCull(texture), false, stack.hasFoil());
        part.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
