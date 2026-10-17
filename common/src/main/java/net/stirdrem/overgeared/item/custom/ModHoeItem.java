package net.stirdrem.overgeared.item.custom;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;

// See ModPickaxeItem - HoeItem's constructor is protected in Yarn 1.20.1.
public class ModHoeItem extends HoeItem {
    public ModHoeItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
