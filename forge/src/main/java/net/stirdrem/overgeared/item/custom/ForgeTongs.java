package net.stirdrem.overgeared.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;

public class ForgeTongs extends Tongs {

    public ForgeTongs(Tier material, int attackDamageModifier, float attackSpeedModifier, Item.Properties settings) {
        super(material, attackDamageModifier, attackSpeedModifier, settings);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return false;
    }
}
