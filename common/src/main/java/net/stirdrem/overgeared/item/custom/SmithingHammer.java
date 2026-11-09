package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Tool behaviour (mining {@code #overgeared:smithing} blocks) comes from the TOOL component set on its properties. */
public class SmithingHammer extends Item {
    public SmithingHammer(Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }
}
