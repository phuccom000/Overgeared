package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/** Tool stats come from {@code Item.Properties#tool} (mines the overgeared:smithing block tag). */
public class Tongs extends Item {

    public Tongs(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.overgeared.tongs.tooltip").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!world.isClientSide() && state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    // Forge's canAttackBlock (block creative insta-mine with Tongs) and canPerformAction
    // (deny all ToolActions) have no Fabric equivalent; Tongs isn't registered as a
    // pickaxe/axe/etc. so it already can't perform those actions by default.
}
