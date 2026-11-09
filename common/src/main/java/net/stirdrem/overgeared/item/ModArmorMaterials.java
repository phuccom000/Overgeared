package net.stirdrem.overgeared.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.ModTags;

import java.util.EnumMap;
import java.util.Map;

public final class ModArmorMaterials {
    /** Equipment assets, see assets/overgeared/equipment/. */
    public static final ResourceKey<EquipmentAsset> STEEL_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, OvergearedMod.loc("steel"));

    // Copper armor is vanilla's since 26.x.
    // Durability is the per-piece multiplier (vanilla's base durabilities 13/15/16/11 times this).
    public static final ArmorMaterial STEEL = new ArmorMaterial(
            26, defense(2, 5, 7, 3), 12, SoundEvents.ARMOR_EQUIP_IRON, 1f, 0f,
            ModTags.Items.REPAIRS_STEEL_ARMOR, STEEL_ASSET);


    private static Map<ArmorType, Integer> defense(int boots, int legs, int chest, int helm) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.LEGGINGS, legs);
        map.put(ArmorType.CHESTPLATE, chest);
        map.put(ArmorType.HELMET, helm);
        map.put(ArmorType.BODY, chest);
        return map;
    }

    private ModArmorMaterials() {
    }
}
