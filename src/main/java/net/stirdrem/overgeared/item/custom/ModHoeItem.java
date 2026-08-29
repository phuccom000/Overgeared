package net.stirdrem.overgeared.item.custom;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ToolMaterial;

// See ModPickaxeItem - HoeItem's constructor is protected in Yarn 1.20.1.
public class ModHoeItem extends HoeItem {
    public ModHoeItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
