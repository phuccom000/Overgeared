package net.stirdrem.overgeared.client;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.item.armor.model.CustomCopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CustomCopperLeggings;

/**
 * Replaces the vanilla humanoid armor model with the Blockbench-authored geometry for the
 * head and leg slots only - the chestplate/boots keep the vanilla model, matching the
 * original Forge port (which only overrode getHumanoidArmorModel for helmet/leggings).
 */
public class CopperArmorRenderer implements ArmorRenderer {
    private static final Identifier HELMET_TEXTURE = Overgeared.id("textures/models/armor/copper_layer_1.png");
    private static final Identifier LEGGINGS_TEXTURE = Overgeared.id("textures/models/armor/copper_layer_2.png");

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                        LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
        var modelLoader = MinecraftClient.getInstance().getEntityModelLoader();

        if (slot == EquipmentSlot.HEAD) {
            ModelPart root = modelLoader.getModelPart(CustomCopperHelmet.LAYER_LOCATION);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("Head"), HELMET_TEXTURE);
        } else if (slot == EquipmentSlot.LEGS) {
            ModelPart root = modelLoader.getModelPart(CustomCopperLeggings.LAYER_LOCATION);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("Body"), LEGGINGS_TEXTURE);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("RightLeg"), LEGGINGS_TEXTURE);
            renderPart(matrices, vertexConsumers, light, stack, root.getChild("LeftLeg"), LEGGINGS_TEXTURE);
        }
    }

    /**
     * Equivalent to ArmorRenderer.renderPart, which takes a Model rather than a bare ModelPart -
     * our geometry is extracted straight from baked TexturedModelData with no Model wrapper.
     */
    private static void renderPart(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                     ItemStack stack, ModelPart part, Identifier texture) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(
                vertexConsumers, RenderLayer.getArmorCutoutNoCull(texture), false, stack.hasGlint());
        part.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
    }
}
