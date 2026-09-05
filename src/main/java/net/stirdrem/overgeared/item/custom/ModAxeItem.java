package net.stirdrem.overgeared.item.custom;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;

// See ModPickaxeItem - AxeItem's constructor is protected in Yarn 1.20.1.
public class ModAxeItem extends AxeItem {
    public ModAxeItem(Tier material, float attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
