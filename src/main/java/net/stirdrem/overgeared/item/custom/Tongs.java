package net.stirdrem.overgeared.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

public class Tongs extends MiningToolItem {

    public Tongs(ToolMaterial material, int attackDamageModifier, float attackSpeedModifier, Item.Settings settings) {
        super(attackDamageModifier, attackSpeedModifier, material, ModTags.Blocks.SMITHING, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.overgeared.tongs.tooltip").formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getHardness(world, pos) != 0.0F) {
            stack.damage(2, entity, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    // Forge's canAttackBlock (block creative insta-mine with Tongs) and canPerformAction
    // (deny all ToolActions) have no Fabric equivalent; Tongs isn't registered as a
    // pickaxe/axe/etc. so it already can't perform those actions by default.
}
