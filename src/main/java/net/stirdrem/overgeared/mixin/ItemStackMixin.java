package net.stirdrem.overgeared.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;
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
 * Priority 2000 (default is 1000) so this mixin's getAttributeModifiers RETURN injection runs
 * after ItemStackAttributeMixin's - broken tools should discard the quality attribute bonus
 * along with everything else, not have it computed and left in place.
 */
@Mixin(value = ItemStack.class, priority = 2000)
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
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ForgingQuality")) {
            float baseSpeed = cir.getReturnValue();
            float multiplier = QualityHelper.getMiningSpeedMultiplier(stack);
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }

    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void overgeared$modifyDurabilityBasedOnQuality(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        int originalDurability = cir.getReturnValue();

        if (originalDurability <= 0) {
            return;
        }

        boolean blacklisted = Overgeared.isDurabilityBlacklisted(stack);

        float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
        int newBaseDurability = blacklisted ? originalDurability : (int) (originalDurability * baseMultiplier);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ForgingQuality")) {
            float multiplier = QualityHelper.getDurabilityMultiplier(stack);
            newBaseDurability = (int) (newBaseDurability * multiplier);
        }

        if (tag != null && tag.contains("ReducedMaxDurability")) {
            int reductions = tag.getInt("ReducedMaxDurability");
            float durabilityPenaltyMultiplier = 1.0f - (reductions * ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue());
            durabilityPenaltyMultiplier = Math.max(0.1f, durabilityPenaltyMultiplier);
            newBaseDurability = (int) (newBaseDurability * durabilityPenaltyMultiplier);
        }
        cir.setReturnValue(newBaseDurability);
    }

    @Unique
    private static final Map<UUID, Long> overgeared$lastTongsHit = new WeakHashMap<>();

    @Unique
    private static final String HEATED_TIME_TAG = "HeatedSince";

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void overgeared$onInventoryTick(Level world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (world.isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return;
        }

        long tick = world.getGameTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            CompoundTag stackTag = stack.getTag();
            if (!stack.is(ModTags.Items.HEATED_METALS) && !(stackTag != null && stackTag.contains("Heated")))
                continue;

            CompoundTag tag = stack.getOrCreateTag();
            long heatedSince = tag.getLong(HEATED_TIME_TAG);
            if (heatedSince == 0L) {
                tag.putLong(HEATED_TIME_TAG, tick);
            } else if (tick - heatedSince >= cooldownTicks) {
                Item cooled = getCooledItem(stack.getItem(), world);
                if (cooled != null) {
                    ItemStack newStack = new ItemStack(cooled, stack.getCount());
                    CompoundTag currentTag = stack.getTag();
                    if (currentTag != null) {
                        CompoundTag newTag = currentTag.copy();
                        newTag.remove("Heated");
                        newTag.remove(HEATED_TIME_TAG);
                        if (!newTag.isEmpty()) {
                            newStack.setTag(newTag);
                        }
                    }
                    boolean isMain = stack == player.getMainHandItem();
                    boolean isOff = stack == player.getOffhandItem();

                    stack.shrink(stack.getCount());

                    if (isMain) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
                    } else if (isOff) {
                        player.setItemInHand(InteractionHand.OFF_HAND, newStack);
                    } else if (!player.getInventory().add(newStack)) {
                        player.drop(newStack, false);
                    }

                    world.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
                }
            }
        }

        boolean hasHotItem = false;
        for (ItemStack s : player.getInventory().items) {
            if (s.isEmpty()) continue;
            CompoundTag sTag = s.getTag();
            if (s.is(ModTags.Items.HEATED_METALS) || s.is(ModTags.Items.HOT_ITEMS) || (sTag != null && sTag.contains("Heated"))) {
                hasHotItem = true;
                break;
            }
        }
        ItemStack mainCheck = player.getMainHandItem();
        ItemStack offCheck = player.getOffhandItem();
        hasHotItem = hasHotItem
                || mainCheck.is(ModTags.Items.HEATED_METALS) || mainCheck.is(ModTags.Items.HOT_ITEMS)
                || offCheck.is(ModTags.Items.HEATED_METALS) || offCheck.is(ModTags.Items.HOT_ITEMS);

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

        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return;
        }

        if (!tongsStack.isEmpty()) {
            if (tick % 40 != 0) return;
            long last = overgeared$lastTongsHit.getOrDefault(uuid, -1L);
            if (last != tick) {
                InteractionHand hand = tongsStack == player.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                tongsStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                overgeared$lastTongsHit.put(uuid, tick);
            }
        } else {
            player.hurt(world.damageSources().hotFloor(), 1.0f);
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

    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private void overgeared$qualityBasedBreak(int amount, LivingEntity entity, Consumer<LivingEntity> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        int currentDamage = stack.getDamageValue();
        int newDamage = currentDamage + amount;
        int max = stack.getMaxDamage();

        if (currentDamage < max && newDamage >= max) {
            if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
                return;
            }

            float breakChance = overgeared$getBreakChance(stack);

            if (entity.getRandom().nextFloat() < breakChance) {
                return;
            }

            stack.setDamageValue(max);
            ForgingQuality.downgradeDamageableItems(stack);

            if (stack.getDamageValue() < 0) {
                stack.setDamageValue(0);
            } else if (stack.getDamageValue() > stack.getMaxDamage()) {
                stack.setDamageValue(stack.getMaxDamage());
            }

            if (entity instanceof Player player) {
                InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                player.broadcastBreakEvent(hand);
            } else {
                entity.level().playSound(
                        null,
                        entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.PLAYERS,
                        0.8F,
                        0.8F + entity.level().random.nextFloat() * 0.4F
                );
            }

            ci.cancel();
        }
    }

    @Unique
    private static float overgeared$getBreakChance(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("ForgingQuality")) {
            return ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        }

        ForgingQuality quality = ForgingQuality.fromString(tag.getString("ForgingQuality"));

        return switch (quality) {
            case POOR -> ServerConfig.BREAK_CHANCE_POOR.get().floatValue();
            case EXPERT -> ServerConfig.BREAK_CHANCE_EXPERT.get().floatValue();
            case PERFECT -> ServerConfig.BREAK_CHANCE_PERFECT.get().floatValue();
            case MASTER -> ServerConfig.BREAK_CHANCE_MASTER.get().floatValue();
            default -> ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        };
    }

    /**
     * Runs after ItemStackAttributeMixin's RETURN injection (see the priority=2000 class
     * annotation) so a broken tool's quality attribute bonus gets discarded along with
     * everything else, keeping only attack speed - matching a vanilla broken tool.
     */
    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void overgeared$brokenToolAttributes(EquipmentSlot slot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage()) {
            return;
        }

        Multimap<Attribute, AttributeModifier> original = cir.getReturnValue();

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        if (original.containsKey(Attributes.ATTACK_SPEED)) {
            for (AttributeModifier mod : original.get(Attributes.ATTACK_SPEED)) {
                builder.put(Attributes.ATTACK_SPEED, mod);
            }
        }

        cir.setReturnValue(builder.build());
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
                                        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }
}
