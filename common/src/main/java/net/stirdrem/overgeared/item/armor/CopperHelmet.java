package net.stirdrem.overgeared.item.armor;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

// Custom Blockbench armor model is rendered via CopperArmorRenderer (Fabric ArmorRenderer),
// registered client-side in OvergearedClient - see item.armor.model.CustomCopperHelmet.
public class CopperHelmet extends ArmorItem {
    public CopperHelmet(ArmorMaterial material, Type type, Properties settings) {
        super(material, type, settings);
    }
}
