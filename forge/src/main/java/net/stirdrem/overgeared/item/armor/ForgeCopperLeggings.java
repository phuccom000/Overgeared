package net.stirdrem.overgeared.item.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.stirdrem.overgeared.item.armor.model.ForgeCopperLeggingsModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ForgeCopperLeggings extends CopperLeggings {

    public ForgeCopperLeggings(ArmorMaterial material, Type type, Properties settings) {
        super(material, type, settings);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                Map<String, ModelPart> parts = new HashMap<>();
                parts.put("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("body", new ForgeCopperLeggingsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ForgeCopperLeggingsModel.LAYER_LOCATION)).Body);
                parts.put("right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("right_leg", new ForgeCopperLeggingsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ForgeCopperLeggingsModel.LAYER_LOCATION)).RightLeg);
                parts.put("left_leg", new ForgeCopperLeggingsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ForgeCopperLeggingsModel.LAYER_LOCATION)).LeftLeg);

                return new HumanoidModel<>(new ModelPart(Collections.emptyList(), parts));
            }
        });
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "overgeared:textures/models/armor/copper_layer_2.png";
    }
}
