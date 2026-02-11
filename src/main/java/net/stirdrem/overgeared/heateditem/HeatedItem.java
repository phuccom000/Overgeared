package net.stirdrem.overgeared.heateditem;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;

import static net.stirdrem.overgeared.util.ItemUtils.copyComponentsExceptHeated;
import static net.stirdrem.overgeared.util.ItemUtils.getCooledItem;

public final class HeatedItem {
    // Per-entity last-hit tick to prevent multiple tongs damage per tick
    private static final Map<UUID, Long> lastTongsHit = new WeakHashMap<>();

    private HeatedItem() {}

    public static boolean isHeated(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModTags.Items.HEATED_METALS)
                || Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT));
    }

    // Called from InventoryCoolingMixin — handles damage to holder + cooling
    public static void tickInventory(ItemStack stack, Level level, LivingEntity entity) {
        long tick = level.getGameTime();
        UUID uuid = entity.getUUID();
        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();

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
            // Living entity has tongs - damage them instead
            if (tick % 40 != 0) return;
            long last = lastTongsHit.getOrDefault(uuid, -1L);

            if (last != tick) {
                EquipmentSlot equipSlot = tongsStack == entity.getMainHandItem()
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND;
                tongsStack.hurtAndBreak(1, entity, equipSlot);
                lastTongsHit.put(uuid, tick);
            }
        } else {
            // No tongs - damage the entity
            if (!entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                entity.hurt(entity.damageSources().hotFloor(), 1.0f);
            }
        }

        handleCoolingLivingEntity(stack, level, entity);
    }

    // Called from ItemEntityCoolingMixin — handles water detection + timer cooling
    public static void tickItemEntity(ItemStack stack, Level level, ItemEntity entity) {
        Item cooled = getCooledItem(stack.getItem(), level);
        boolean hasComponent = Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT));
        if (cooled == null && !hasComponent) return;

        // Instant cool if in water
        BlockPos pos = entity.blockPosition();
        BlockState state = level.getBlockState(pos);
        boolean inWater = state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON);

        if (!inWater && !hasCooled(stack, level, pos)) return;

        if (cooled != null) {
            ItemStack cooledStack = new ItemStack(cooled, stack.getCount());
            copyComponentsExceptHeated(stack, cooledStack);
            entity.setItem(cooledStack);
        } else {
            removeHeat(stack);
        }
    }

    public static void handleCoolingLivingEntity(ItemStack stack, Level level, LivingEntity lEntity) {
        Item cooled = getCooledItem(stack.getItem(), level);
        boolean hasComponent = Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT));
        if (cooled == null && !hasComponent) return;
        if (!hasCooled(stack, level, lEntity.blockPosition())) return;

        setCooled(stack, lEntity, cooled);
    }

    public static boolean handleCoolingContainer(Slot slot, Level level) {
        return handleCoolingContainer(slot, level, false);
    }

    public static boolean handleCoolingContainer(Slot slot, Level level, boolean skipThrottle) {
        if (level.isClientSide) return false;
        ItemStack stack = slot.getItem();

        Item cooled = getCooledItem(stack.getItem(), level);
        if (cooled == null && !Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT))) return false;
        if (!hasCooled(stack, level, skipThrottle)) return false;

        if (cooled != null) {
            ItemStack newStack = new ItemStack(cooled, stack.getCount());
            copyComponentsExceptHeated(stack, newStack);
            slot.set(newStack);
        } else {
            removeHeat(stack);
        }
        return true;
    }

    public static boolean handleCoolingContainer(IItemHandler handler, int index, Level level) {
        if (level.isClientSide) return false;
        ItemStack stack = handler.getStackInSlot(index);

        Item cooled = getCooledItem(stack.getItem(), level);
        if (cooled == null && !Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT))) return false;
        if (!hasCooled(stack, level)) return false;

        if (cooled != null) {
            ItemStack newStack = new ItemStack(cooled, stack.getCount());
            copyComponentsExceptHeated(stack, newStack);
            handler.extractItem(index, stack.getCount(), false);
            handler.insertItem(index, newStack, false);
        } else {
            removeHeat(stack);
        }
        return true;
    }

    public static void setCooled(ItemStack stack, LivingEntity lEntity) {
        Item cooled = getCooledItem(stack.getItem(), lEntity.level());
        setCooled(stack, lEntity, cooled);
    }

    public static void setCooled(ItemStack stack, LivingEntity lEntity, Item cooled) {
        if (cooled != null) {
            ItemStack newStack = new ItemStack(cooled, stack.getCount());
            copyComponentsExceptHeated(stack, newStack);

            boolean isMain = stack == lEntity.getMainHandItem();
            boolean isOff = stack == lEntity.getOffhandItem();

            stack.setCount(0);

            if (isMain) {
                lEntity.setItemInHand(InteractionHand.MAIN_HAND, newStack);
            } else if (isOff) {
                lEntity.setItemInHand(InteractionHand.OFF_HAND, newStack);
            } else if (lEntity instanceof Player player) {
                if (!player.getInventory().add(newStack)) player.drop(newStack, false);
            }
        } else if (Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT))) {
            removeHeat(stack);
        }
    }

    public static void setCooledSingle(ItemStack stack, Player player) {
        Level level = player.level();
        Item cooled = getCooledItem(stack.getItem(), level);

        if (stack.getCount() <= 1) {
            setCooled(stack, player, cooled);
            level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
            return;
        }

        // Stack > 1: split off 1, cool it
        ItemStack cooledStack;
        if (cooled != null) {
            cooledStack = new ItemStack(cooled, 1);
            copyComponentsExceptHeated(stack, cooledStack);
        } else {
            cooledStack = stack.copyWithCount(1);
            removeHeat(cooledStack);
        }

        stack.shrink(1);

        if (!player.getInventory().add(cooledStack)) {
            player.drop(cooledStack, false);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
    }

    private static boolean hasCooled(ItemStack stack, Level level, BlockPos pos) {
        if (hasCooled(stack, level)) {
            if (pos != null) level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
            return true;
        }
        return false;
    }

    private static boolean hasCooled(ItemStack stack, Level level) {
        return hasCooled(stack, level, false);
    }

    private static boolean hasCooled(ItemStack stack, Level level, boolean skipThrottle) {
        if (stack.isEmpty()) return false;
        if (!isHeated(stack)) return false;

        long tick = level.getGameTime();
        if (!skipThrottle && tick % 10 != 0) return false;
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

    private static void removeHeat(ItemStack stack) {
        stack.remove(ModComponents.HEATED_COMPONENT);
        stack.remove(ModComponents.HEATED_TIME);
        CastData data = stack.get(ModComponents.CAST_DATA);
        if (data != null && data.heated()) {
            stack.set(ModComponents.CAST_DATA, data.withHeated(false));
        }
    }
}
