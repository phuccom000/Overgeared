package net.stirdrem.overgeared.item.custom;

import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;

// PickaxeItem's constructor is protected in Yarn 1.20.1 (unlike SwordItem/ShovelItem) - this
// trivial subclass just exposes it for our tool registrations.
public class ModPickaxeItem extends PickaxeItem {
    public ModPickaxeItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
