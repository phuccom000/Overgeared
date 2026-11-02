package net.stirdrem.overgeared.item.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.stirdrem.overgeared.item.armor.custom.ArmorModelHelper;
import net.stirdrem.overgeared.item.armor.model.ForgeCopperHelmetModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ForgeCopperHelmet extends CopperHelmet {

    public ForgeCopperHelmet(ArmorMaterial material, Type type, Properties settings) {
        super(material, type, settings);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return ArmorModelHelper.withPart("head", new ForgeCopperHelmetModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ForgeCopperHelmetModel.LAYER_LOCATION)).Head);
            }
        });
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "overgeared:textures/models/armor/copper_layer_1.png";
    }
}
