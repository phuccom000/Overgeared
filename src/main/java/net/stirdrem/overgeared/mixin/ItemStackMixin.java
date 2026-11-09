package net.stirdrem.overgeared.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.Prediction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static net.stirdrem.overgeared.Overgeared.getCooledItem;
import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Quality / durability / heated-item behaviour on ItemStack. The attribute-modifier part lives in
 * ItemStackAttributeMixin.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void overgeared$modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (isBroken(stack)) {
            cir.setReturnValue(0.0F);
            return;
        }
        if (!stack.isCorrectToolForDrops(state)) {
            return;
        }
        if (stack.has(ModComponents.FORGING_QUALITY)) {
            float baseSpeed = cir.getReturnValueF();
            float multiplier = QualityHelper.getMiningSpeedMultiplier(stack);
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }

    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void overgeared$modifyDurabilityBasedOnQuality(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        int originalDurability = cir.getReturnValueI();

        if (originalDurability <= 0) {
            return;
        }

        boolean blacklisted = Overgeared.isDurabilityBlacklisted(stack);

        float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
        int newBaseDurability = blacklisted ? originalDurability : (int) (originalDurability * baseMultiplier);

        if (stack.has(ModComponents.FORGING_QUALITY)) {
            float multiplier = QualityHelper.getDurabilityMultiplier(stack);
            newBaseDurability = (int) (newBaseDurability * multiplier);
        }

        Integer reductions = stack.get(ModComponents.REDUCED_GRIND_COUNT);
        if (reductions != null) {
            float durabilityPenaltyMultiplier = 1.0f - (reductions * ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue());
            durabilityPenaltyMultiplier = Math.max(0.1f, durabilityPenaltyMultiplier);
            newBaseDurability = (int) (newBaseDurability * durabilityPenaltyMultiplier);
        }
        cir.setReturnValue(newBaseDurability);
    }

    @Unique
    private static final Map<UUID, Long> overgeared$lastTongsHit = new WeakHashMap<>();

    @Unique
    private static boolean overgeared$isHeated(ItemStack stack) {
        return stack.is(ModTags.Items.HEATED_METALS) || stack.has(ModComponents.HEATED);
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void overgeared$onInventoryTick(Level world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        if (!(world instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof Player player)) return;
        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return;
        }
        if (player.isCreative()) return;

        long tick = world.getGameTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (!overgeared$isHeated(stack)) continue;

            long heatedSince = stack.getOrDefault(ModComponents.HEATED_TIME, 0L);
            if (heatedSince == 0L) {
                stack.set(ModComponents.HEATED_TIME, tick);
            } else if (tick - heatedSince >= cooldownTicks) {
                Item cooled = getCooledItem(stack.getItem(), world);
                if (cooled != null) {
                    // transmuteCopy keeps every other component (quality, creator, ...)
                    ItemStack newStack = stack.transmuteCopy(cooled, stack.getCount());
                    newStack.remove(ModComponents.HEATED);
                    newStack.remove(ModComponents.HEATED_TIME);

                    boolean isMain = stack == player.getMainHandItem();
                    boolean isOff = stack == player.getOffhandItem();

                    stack.shrink(stack.getCount());

                    if (isMain) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
                    } else if (isOff) {
                        player.setItemInHand(InteractionHand.OFF_HAND, newStack);
                    } else if (!player.getInventory().add(newStack)) {
                        player.drop(newStack, false, Prediction.SERVER_ONLY);
                    }

                    world.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
                }
            }
        }

        boolean hasHotItem = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack s = inventory.getItem(i);
            if (s.isEmpty()) continue;
            if (overgeared$isHeated(s) || s.is(ModTags.Items.HOT_ITEMS)) {
                hasHotItem = true;
                break;
            }
        }

        if (!hasHotItem) return;

        UUID uuid = player.getUUID();
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        ItemStack tongsStack;
        if (!main.isEmpty() && main.is(ModTags.Items.TONGS)) {
            tongsStack = main;
        } else if (!off.isEmpty() && off.is(ModTags.Items.TONGS)) {
            tongsStack = off;
        } else {
            tongsStack = ItemStack.EMPTY;
        }

        if (!tongsStack.isEmpty()) {
            if (tick % 40 != 0) return;
            long last = overgeared$lastTongsHit.getOrDefault(uuid, -1L);
            if (last != tick) {
                InteractionHand hand = tongsStack == player.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                tongsStack.hurtAndBreak(1, player, hand);
                overgeared$lastTongsHit.put(uuid, tick);
            }
        } else {
            // Holding hot metal without tongs sets you on fire (upstream "Hot ingot changed to fire damage").
            player.setSharedFlagOnFire(true);
            player.setRemainingFireTicks(20);
        }
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void overgeared$fixDurabilityBar(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamageValue();

        if (damage >= maxDamage) {
            cir.setReturnValue(0);
            return;
        }

        int width = Math.round(13.0F - (float) damage * 13.0F / (float) maxDamage);
        cir.setReturnValue(width);
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void overgeared$fixDurabilityBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int max = stack.getMaxDamage();
        int damage = stack.getDamageValue();

        if (max <= 0) {
            cir.setReturnValue(0xFFFFFF);
            return;
        }

        float ratio = Math.max(0.0F, 1.0F - (float) damage / (float) max);
        float hue = ratio / 3.0F;

        int color = Mth.hsvToRgb(hue, 1.0F, 1.0F);

        cir.setReturnValue(color);
    }

    /**
     * Quality break system: when a player or mob wears an item down to zero durability it can stay
     * at max damage ("broken") and drop a quality tier instead of being destroyed, unless the
     * quality-based break chance roll says it really breaks.
     * <p>
     * Hooks the LivingEntity overload, like the 1.20.1 hurtAndBreak(int, LivingEntity, Consumer)
     * hook did: dispensers (shears, flint and steel) damage items through the ServerLevel overload
     * directly and break normally, as they did in 1.20.1.
     */
    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"), cancellable = true)
    private void overgeared$qualityBasedBreak(int amount, LivingEntity owner, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!(owner.level() instanceof ServerLevel level)) return;
        if (!stack.isDamageableItem()) return;
        if (owner instanceof Player player && player.hasInfiniteMaterials()) return;

        int currentDamage = stack.getDamageValue();
        int newDamage = currentDamage + amount;
        int max = stack.getMaxDamage();

        if (currentDamage < max && newDamage >= max) {
            if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
                return;
            }

            float breakChance = overgeared$getBreakChance(stack);

            if (level.getRandom().nextFloat() < breakChance) {
                return;
            }

            stack.setDamageValue(max);
            ForgingQuality.downgradeDamageableItems(stack);

            if (stack.getDamageValue() < 0) {
                stack.setDamageValue(0);
            } else if (stack.getDamageValue() > stack.getMaxDamage()) {
                stack.setDamageValue(stack.getMaxDamage());
            }

            // entity event -> clients play the break sound/particles (was broadcastBreakEvent)
            owner.onEquippedItemBroken(stack, slot);

            ci.cancel();
        }
    }

    @Unique
    private static float overgeared$getBreakChance(ItemStack stack) {
        ForgingQuality quality = ForgingQuality.get(stack);
        if (quality == null) {
            return ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        }

        return switch (quality) {
            case POOR -> ServerConfig.BREAK_CHANCE_POOR.get().floatValue();
            case EXPERT -> ServerConfig.BREAK_CHANCE_EXPERT.get().floatValue();
            case PERFECT -> ServerConfig.BREAK_CHANCE_PERFECT.get().floatValue();
            case MASTER -> ServerConfig.BREAK_CHANCE_MASTER.get().floatValue();
            default -> ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        };
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void overgeared$disableUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void overgeared$disableUse(Level world, Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
