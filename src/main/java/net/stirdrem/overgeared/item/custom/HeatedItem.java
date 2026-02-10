import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;

import static net.stirdrem.overgeared.util.ItemUtils.copyComponentsExceptHeated;
import static net.stirdrem.overgeared.util.ItemUtils.getCooledItem;

public class HeatedItem extends Item {
    // Per-entity last-hit tick to prevent multiple tongs damage per tick
    private static final Map<UUID, Long> lastTongsHit = new WeakHashMap<>();

    public HeatedItem(Properties properties) {
        super(properties);
    }

    // Handles ticking while in an entities inventory
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) return;

        if (entity instanceof LivingEntity lEntity) {
            if (!(lEntity instanceof Player || lEntity instanceof Mob)) return;

            long tick = level.getGameTime();
            UUID uuid = lEntity.getUUID();
            ItemStack main = lEntity.getMainHandItem();
            ItemStack off = lEntity.getOffhandItem();

            // Check for tongs in either hand
            ItemStack tongsStack;
            if (!main.isEmpty() && main.is(ModTags.Items.TONGS)) {
                tongsStack = main;
            } else if (!off.isEmpty() && off.is(ModTags.Items.TONGS)) {
                tongsStack = off;
            } else {
                tongsStack = ItemStack.EMPTY;
            }

            if (!tongsStack.isEmpty()) {
                // Living entity has tongs - damage them instead of the entity
                if (tick % 40 != 0) return; // Only damage every 2 seconds
                long last = lastTongsHit.getOrDefault(uuid, -1L);

                if (last != tick) {
                    // Determine correct hand for break animation
                    EquipmentSlot equipSlot = tongsStack == lEntity.getMainHandItem()
                            ? EquipmentSlot.MAINHAND
                            : EquipmentSlot.OFFHAND;
                    tongsStack.hurtAndBreak(1, player, equipSlot);
                    lastTongsHit.put(uuid, tick);
                }
            } else {
                // No tongs - damage the player
                if (!lEntity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    lEntity.hurt(lEntity.damageSources().hotFloor(), 1.0f);
                }
            }

            handleCoolingLivingEntity(stack, level, lEntity);
        }
    }

    // Handles ticking while dropped on the ground
    @Override
    public boolean onEntityTick(ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide) return false;

        handleCoolingItemEntity(stack, entity.level(), entity);
        return true;
    }

    private void handleCoolingLivingEntity(ItemStack stack, Level level, LivingEntity lEntity) {
        Item cooled = getCooledItem(stack.getItem(), level);
        if (cooled == null) return;
        if (!hasCooled(stack, level, entity.blockPosition())) return;

        setCooled(stack, lEntity, cooled);
    }

    private void handleCoolingItemEntity(ItemStack stack, Level level, ItemEntity entity) {
        Item cooled = getCooledItem(stack.getItem(), level);
        if (cooled == null) return;

        // Quick check if thrown into water
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(entity.getX(), entity.getY(), entity.getZ());
        BlockState state = level.getBlockState(pos);
        if (!(state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)))
            if (!hasCooled(stack, level, entity.blockPosition())) return;

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());
        copyComponentsExceptHeated(stack, cooledStack);

        entity.setItem(cooledStack);
    }

    // Not the biggest fan of this either, figure out a way for better in slot swapping
    public static boolean handleCoolingContainer(ItemStack stack, Level level) {
        if (level.isClientSide) return false;

        Item cooled = getCooledItem(stack.getItem(), level);
        if (cooled == null) return false;
        if (!hasCooled(stack, level)) return false;

        ItemStack tempStack = new ItemStack(cooled, stack.getCount());
        copyComponentsExceptHeated(tempStack, stack);

        // Double make sure
        target.remove(ModComponents.HEATED_COMPONENT);
        target.remove(ModComponents.HEATED_TIME);

        stack.setItem(cooled);
        return true;
    }

    public static void setCooled(ItemStack stack, LivingEntity lEntity) {
        Item cooled = getCooledItem(stack.getItem(), lEntity.level());
        setCooled(stack, lEntity, cooled);
    }

    public static void setCooled(ItemStack stack, LivingEntity lEntity, Item cooled) {
        if (cooled == null) return;

        ItemStack newStack = new ItemStack(cooled, stack.getCount());
        copyComponentsExceptHeated(stack, newStack);

        boolean isMain = stack == lEntity.getMainHandItem();
        boolean isOff = stack == lEntity.getOffhandItem();

        stack.setCount(0); // Remove old heated item

        if (isMain) {
            lEntity.setItemInHand(InteractionHand.MAIN_HAND, newStack);
        } else if (isOff) {
            lEntity.setItemInHand(InteractionHand.OFF_HAND, newStack);
        } else if (!lEntity.getInventory().add(newStack)) {
            lEntity.drop(newStack, false);
        }
    }

    private boolean hasCooled(ItemStack stack, Level level, BlockPos pos) {
        if (hasCooled(stack, level)) {
            if (pos != null) level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
            return true;
        }

        return false;
    }

    private boolean hasCooled(ItemStack stack, Level level) {
        //Neither of these should happen but still
        if (stack.isEmpty()) return false;
        if (!(stack.is(ModTags.Items.HEATED_METALS) || Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT)))) return false;

        long tick = level.getGameTime();
        if (tick % 10 != 0) return false;
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();
        Long heatedSince = stack.get(ModComponents.HEATED_TIME);

        // Start timer if missing
        if (heatedSince == null) {
            stack.set(ModComponents.HEATED_TIME, tick);
            return false;
        }

        // Still cooling
        if (tick - heatedSince < cooldownTicks) return false;

        return true;
    }
}