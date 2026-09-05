package net.stirdrem.overgeared.item.custom;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

// PickaxeItem's constructor is protected in Yarn 1.20.1 (unlike SwordItem/ShovelItem) - this
// trivial subclass just exposes it for our tool registrations.
public class ModPickaxeItem extends PickaxeItem {
    public ModPickaxeItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
