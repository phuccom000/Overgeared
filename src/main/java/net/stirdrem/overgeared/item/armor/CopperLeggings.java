package net.stirdrem.overgeared.item.armor;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;

// Custom Blockbench armor model is rendered via CopperArmorRenderer (Fabric ArmorRenderer),
// registered client-side in OvergearedClient - see item.armor.model.CustomCopperLeggings.
public class CopperLeggings extends ArmorItem {
    public CopperLeggings(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings);
    }
}
