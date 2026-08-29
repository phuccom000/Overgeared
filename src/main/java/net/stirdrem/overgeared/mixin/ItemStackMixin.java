package net.stirdrem.overgeared.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
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

    @Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    private void overgeared$modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (isBroken(stack)) {
            cir.setReturnValue(0.0F);
            return;
        }
        if (!stack.isSuitableFor(state)) {
            return;
        }
        NbtCompound tag = stack.getNbt();
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

        NbtCompound tag = stack.getNbt();
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
    private void overgeared$onInventoryTick(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;
        if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            return;
        }

        long tick = world.getTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

        for (ItemStack stack : player.getInventory().main) {
            if (stack.isEmpty()) continue;
            NbtCompound stackTag = stack.getNbt();
            if (!stack.isIn(ModTags.Items.HEATED_METALS) && !(stackTag != null && stackTag.contains("Heated")))
                continue;

            NbtCompound tag = stack.getOrCreateNbt();
            long heatedSince = tag.getLong(HEATED_TIME_TAG);
            if (heatedSince == 0L) {
                tag.putLong(HEATED_TIME_TAG, tick);
            } else if (tick - heatedSince >= cooldownTicks) {
                Item cooled = getCooledItem(stack.getItem(), world);
                if (cooled != null) {
                    ItemStack newStack = new ItemStack(cooled, stack.getCount());
                    NbtCompound currentTag = stack.getNbt();
                    if (currentTag != null) {
                        NbtCompound newTag = currentTag.copy();
                        newTag.remove("Heated");
                        newTag.remove(HEATED_TIME_TAG);
                        if (!newTag.isEmpty()) {
                            newStack.setNbt(newTag);
                        }
                    }
                    boolean isMain = stack == player.getMainHandStack();
                    boolean isOff = stack == player.getOffHandStack();

                    stack.decrement(stack.getCount());

                    if (isMain) {
                        player.setStackInHand(Hand.MAIN_HAND, newStack);
                    } else if (isOff) {
                        player.setStackInHand(Hand.OFF_HAND, newStack);
                    } else if (!player.getInventory().insertStack(newStack)) {
                        player.dropItem(newStack, false);
                    }

                    world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.7f, 1.0f);
                }
            }
        }

        boolean hasHotItem = false;
        for (ItemStack s : player.getInventory().main) {
            if (s.isEmpty()) continue;
            NbtCompound sTag = s.getNbt();
            if (s.isIn(ModTags.Items.HEATED_METALS) || s.isIn(ModTags.Items.HOT_ITEMS) || (sTag != null && sTag.contains("Heated"))) {
                hasHotItem = true;
                break;
            }
        }
        ItemStack mainCheck = player.getMainHandStack();
        ItemStack offCheck = player.getOffHandStack();
        hasHotItem = hasHotItem
                || mainCheck.isIn(ModTags.Items.HEATED_METALS) || mainCheck.isIn(ModTags.Items.HOT_ITEMS)
                || offCheck.isIn(ModTags.Items.HEATED_METALS) || offCheck.isIn(ModTags.Items.HOT_ITEMS);

        if (!hasHotItem) return;

        UUID uuid = player.getUuid();
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        ItemStack tongsStack;
        if (!main.isEmpty() && main.isIn(ModTags.Items.TONGS)) {
            tongsStack = main;
        } else if (!off.isEmpty() && off.isIn(ModTags.Items.TONGS)) {
            tongsStack = off;
        } else {
            tongsStack = ItemStack.EMPTY;
        }

        if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            return;
        }

        if (!tongsStack.isEmpty()) {
            if (tick % 40 != 0) return;
            long last = overgeared$lastTongsHit.getOrDefault(uuid, -1L);
            if (last != tick) {
                Hand hand = tongsStack == player.getMainHandStack() ? Hand.MAIN_HAND : Hand.OFF_HAND;
                tongsStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                overgeared$lastTongsHit.put(uuid, tick);
            }
        } else {
            player.damage(world.getDamageSources().hotFloor(), 1.0f);
        }
    }

    @Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
    private void overgeared$fixDurabilityBar(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageable()) return;

        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamage();

        if (damage >= maxDamage) {
            cir.setReturnValue(0);
            return;
        }

        int width = Math.round(13.0F - (float) damage * 13.0F / (float) maxDamage);
        cir.setReturnValue(width);
    }

    @Inject(method = "getItemBarColor", at = @At("HEAD"), cancellable = true)
    private void overgeared$fixDurabilityBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageable()) return;

        int max = stack.getMaxDamage();
        int damage = stack.getDamage();

        if (max <= 0) {
            cir.setReturnValue(0xFFFFFF);
            return;
        }

        float ratio = Math.max(0.0F, 1.0F - (float) damage / (float) max);
        float hue = ratio / 3.0F;

        int color = MathHelper.hsvToRgb(hue, 1.0F, 1.0F);

        cir.setReturnValue(color);
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void overgeared$qualityBasedBreak(int amount, LivingEntity entity, Consumer<LivingEntity> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        int currentDamage = stack.getDamage();
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

            stack.setDamage(max);
            ForgingQuality.downgradeDamageableItems(stack);

            if (stack.getDamage() < 0) {
                stack.setDamage(0);
            } else if (stack.getDamage() > stack.getMaxDamage()) {
                stack.setDamage(stack.getMaxDamage());
            }

            if (entity instanceof PlayerEntity player) {
                Hand hand = player.getMainHandStack() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
                player.sendToolBreakStatus(hand);
            } else {
                entity.getWorld().playSound(
                        null,
                        entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.ENTITY_ITEM_BREAK,
                        SoundCategory.PLAYERS,
                        0.8F,
                        0.8F + entity.getWorld().random.nextFloat() * 0.4F
                );
            }

            ci.cancel();
        }
    }

    @Unique
    private static float overgeared$getBreakChance(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
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
    private void overgeared$brokenToolAttributes(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageable() || stack.getDamage() < stack.getMaxDamage()) {
            return;
        }

        Multimap<EntityAttribute, EntityAttributeModifier> original = cir.getReturnValue();

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();

        if (original.containsKey(EntityAttributes.GENERIC_ATTACK_SPEED)) {
            for (EntityAttributeModifier mod : original.get(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, mod);
            }
        }

        cir.setReturnValue(builder.build());
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void overgeared$disableUseOn(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void overgeared$disableUse(World world, PlayerEntity player, Hand hand,
                                        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(TypedActionResult.fail(stack));
        }
    }
}
