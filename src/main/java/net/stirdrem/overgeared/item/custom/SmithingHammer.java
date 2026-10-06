package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Tool stats come from {@code Item.Properties#tool} (mines the overgeared:smithing block tag). */
public class SmithingHammer extends Item {

    public SmithingHammer(Properties settings) {
        super(settings);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!world.isClientSide() && state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }
}
