package net.stirdrem.overgeared.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.util.ModTags;

import java.util.EnumMap;
import java.util.Map;

public class ModArmorMaterials {
    public static final ResourceKey<EquipmentAsset> STEEL_ASSET = asset("steel");

    // Durability is a multiplier on ArmorType's base durability, as in 1.20.1.
    public static final ArmorMaterial STEEL = new ArmorMaterial(
            26, defense(3, 7, 5, 2, 7), 12, SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F,
            ModTags.Items.REPAIRS_STEEL_ARMOR, STEEL_ASSET);

    private static Map<ArmorType, Integer> defense(int helmet, int chestplate, int leggings, int boots, int body) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.HELMET, helmet);
        map.put(ArmorType.CHESTPLATE, chestplate);
        map.put(ArmorType.LEGGINGS, leggings);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.BODY, body);
        return map;
    }

    private static ResourceKey<EquipmentAsset> asset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Overgeared.id(name));
    }
}
