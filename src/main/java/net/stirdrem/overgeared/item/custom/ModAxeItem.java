package net.stirdrem.overgeared.item.custom;

import net.minecraft.item.AxeItem;
import net.minecraft.item.ToolMaterial;

// See ModPickaxeItem - AxeItem's constructor is protected in Yarn 1.20.1.
public class ModAxeItem extends AxeItem {
    public ModAxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
