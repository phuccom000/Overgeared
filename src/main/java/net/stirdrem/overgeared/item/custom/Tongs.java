package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

public class Tongs extends DiggerItem {

    public Tongs(Tier material, int attackDamageModifier, float attackSpeedModifier, Item.Properties settings) {
        super(attackDamageModifier, attackSpeedModifier, material, ModTags.Blocks.SMITHING, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.translatable("tooltip.overgeared.tongs.tooltip").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    // Forge's canAttackBlock (block creative insta-mine with Tongs) and canPerformAction
    // (deny all ToolActions) have no Fabric equivalent; Tongs isn't registered as a
    // pickaxe/axe/etc. so it already can't perform those actions by default.
}
