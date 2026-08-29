package net.stirdrem.overgeared.block;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.item.custom.LingeringArrowItem;
import net.stirdrem.overgeared.item.custom.UpgradeArrowItem;

public class UpgradeArrowDispenseBehavior extends ItemDispenserBehavior {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.getWorld();
        Position position = DispenserBlock.getOutputLocation(pointer);
        Direction direction = pointer.getBlockState().get(Properties.FACING);

        if (stack.getItem() instanceof UpgradeArrowItem arrowItem) {
            createAndShootArrow(arrowItem.getTier(), world, position, direction, stack);
        } else if (stack.getItem() instanceof LingeringArrowItem lingeringArrowItem) {
            createAndShootArrow(lingeringArrowItem.getTier(), world, position, direction, stack);
        }

        stack.decrement(1);
        return stack;
    }

    private void createAndShootArrow(ArrowTier tier, World world, Position position, Direction direction, ItemStack stack) {
        UpgradeArrowEntity arrow = new UpgradeArrowEntity(
                tier,
                world,
                position.getX(),
                position.getY(),
                position.getZ(),
                stack.copy()
        );

        arrow.setVelocity(
                direction.getOffsetX(),
                direction.getOffsetY() + 0.1F, // Added slight upward bias like vanilla arrows
                direction.getOffsetZ(),
                1.1F, // Power
                6.0F  // Spread/inaccuracy
        );
        arrow.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        world.spawnEntity(arrow);
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        pointer.getWorld().syncWorldEvent(1002, pointer.getPos(), 0);
    }
}
