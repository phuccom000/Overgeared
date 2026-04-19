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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
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

import static net.stirdrem.overgeared.OvergearedMod.getCooledItem;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "getDestroySpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
            cir.setReturnValue(0.0F);
            return;
        }
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            float baseSpeed = cir.getReturnValueF();
            float multiplier = QualityHelper.getMiningSpeedMultiplier(stack);
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }


    @Inject(
            method = "getMaxDamage()I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyDurabilityBasedOnQuality(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        int originalDurability = cir.getReturnValue();

        if (originalDurability <= 0) {
            return;
        }

        boolean blacklisted = OvergearedMod.isDurabilityBlacklisted(stack);

        float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
        int newBaseDurability = blacklisted ? originalDurability : (int) (originalDurability * baseMultiplier);

        // Apply quality multiplier
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            float multiplier = QualityHelper.getDurabilityMultiplier(stack);
            newBaseDurability = (int) (newBaseDurability * multiplier);
        }

        // Apply durability reductions
        if (stack.hasTag() && stack.getTag().contains("ReducedMaxDurability")) {
            int reductions = stack.getTag().getInt("ReducedMaxDurability");
            float durabilityPenaltyMultiplier = 1.0f - (reductions * ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue());
            durabilityPenaltyMultiplier = Math.max(0.1f, durabilityPenaltyMultiplier);
            newBaseDurability = (int) (newBaseDurability * durabilityPenaltyMultiplier);
        }
        cir.setReturnValue(newBaseDurability);
    }


    // Per-player last-hit tick
    private static final Map<UUID, Long> lastTongsHit = new WeakHashMap<>();

    private static final String HEATED_TIME_TAG = "HeatedSince";
    private static final String HEATED_TAG = "Heated";

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onInventoryTick(Level level, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        //if (slot != 0) return; // Only process once per player per tick
        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return;
        }

        long tick = level.getGameTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get(); // add to your config

        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            if (!stack.is(ModTags.Items.HEATED_METALS) && !(stack.hasTag() && stack.getTag().contains("Heated")))
                continue;

            CompoundTag tag = stack.getOrCreateTag();
            long heatedSince = tag.getLong(HEATED_TIME_TAG);
            if (heatedSince == 0L) {
                tag.putLong(HEATED_TIME_TAG, tick); // Initialize the timestamp
            } else if (tick - heatedSince >= cooldownTicks) {
                Item cooled = getCooledItem(stack.getItem(), level);
                if (cooled != null) {
                    ItemStack newStack = new ItemStack(cooled, stack.getCount());
                    if (stack.hasTag()) {
                        CompoundTag newtag = stack.getTag().copy();

                        // Remove heated-related tags
                        newtag.remove("Heated");
                        newtag.remove(HEATED_TIME_TAG);

                        if (!newtag.isEmpty()) {
                            newStack.setTag(newtag);
                        }
                    }
                    boolean isMain = stack == player.getMainHandItem();
                    boolean isOff = stack == player.getOffhandItem();

                    stack.setCount(0); // Remove old heated item

                    if (isMain) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
                    } else if (isOff) {
                        player.setItemInHand(InteractionHand.OFF_HAND, newStack);
                    } else if (!player.getInventory().add(newStack)) {
                        player.drop(newStack, false); // Drop if inventory is full
                    }

                    level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
                }
            }
        }

        boolean hasHotItem = player.getInventory().items.stream()
                .anyMatch(s -> !s.isEmpty() && (s.is(ModTags.Items.HEATED_METALS) || s.is(ModTags.Items.HOT_ITEMS))
                        || (s.hasTag() && s.getTag().contains("Heated")))
                || player.getMainHandItem().is(ModTags.Items.HEATED_METALS) || player.getMainHandItem().is(ModTags.Items.HOT_ITEMS)
                || player.getOffhandItem().is(ModTags.Items.HEATED_METALS) || player.getOffhandItem().is(ModTags.Items.HOT_ITEMS);

        if (!hasHotItem) return;


        UUID uuid = player.getUUID();
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        // Check for tongs in either hand
        ItemStack tongsStack;
        if (!main.isEmpty() && main.getItem().builtInRegistryHolder().is(ModTags.Items.TONGS)) {
            tongsStack = main;
        } else if (!off.isEmpty() && off.getItem().builtInRegistryHolder().is(ModTags.Items.TONGS)) {
            tongsStack = off;
        } else {
            tongsStack = ItemStack.EMPTY;
        }

        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return;
        }

        if (!tongsStack.isEmpty()) {
            if (tick % 40 != 0) return;
            long last = lastTongsHit.getOrDefault(uuid, -1L);
            if (last != tick) {
                tongsStack.hurtAndBreak(1, player, p -> {
                    // Determine correct hand
                    InteractionHand hand = tongsStack == player.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    p.broadcastBreakEvent(hand);
                });
                lastTongsHit.put(uuid, tick);
            }
        } else {
            player.hurt(player.damageSources().hotFloor(), 1.0f);
        }
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void fixDurabilityBar(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int maxDamage = stack.getMaxDamage(); // this already includes your mixin override
        int damage = stack.getDamageValue();

        // Clamp to valid range
        if (damage >= maxDamage) {
            cir.setReturnValue(0);
            return;
        }

        int width = Math.round(13.0F - (float) damage * 13.0F / (float) maxDamage);
        cir.setReturnValue(width);
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void fixDurabilityBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int max = stack.getMaxDamage(); // Includes quality/durability changes
        int damage = stack.getDamageValue();

        if (max <= 0) {
            cir.setReturnValue(0xFFFFFF); // fallback white
            return;
        }

        float ratio = Math.max(0.0F, 1.0F - (float) damage / (float) max);

        // Vanilla bar color: hue from red (0.0) to green (0.333...)
        float hue = ratio / 3.0F; // [0, 0.33]

        int color = Mth.hsvToRgb(hue, 1.0F, 1.0F);

        cir.setReturnValue(color);
    }

    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private void qualityBasedBreak(int amount, LivingEntity entity, Consumer<LivingEntity> onBreak, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        int currentDamage = stack.getDamageValue();
        int newDamage = stack.getDamageValue() + amount;
        int max = stack.getMaxDamage();

        if (currentDamage < max && newDamage >= max) {
            if (!ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM.get()) {
                return;
            }

            float breakChance = getBreakChance(stack);

            if (entity.getRandom().nextFloat() < breakChance) {
                return;
            }
            stack.setDamageValue(max);
            ForgingQuality.downgrade(stack);
            ci.cancel();
        }
    }

    @Unique
    private float getBreakChance(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("ForgingQuality")) {
            return ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        }

        ForgingQuality quality = ForgingQuality.fromString(stack.getTag().getString("ForgingQuality"));

        return switch (quality) {
            case POOR -> ServerConfig.BREAK_CHANCE_POOR.get().floatValue();
            case EXPERT -> ServerConfig.BREAK_CHANCE_EXPERT.get().floatValue();
            case PERFECT -> ServerConfig.BREAK_CHANCE_PERFECT.get().floatValue();
            case MASTER -> ServerConfig.BREAK_CHANCE_MASTER.get().floatValue();
            default -> ServerConfig.BREAK_CHANCE_WELL.get().floatValue();
        };
    }

    @Inject(method = "getAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void brokenToolAttributes(EquipmentSlot slot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage()) {
            return;
        }

        // Get original modifiers (IMPORTANT: don't call cir yet)
        Multimap<Attribute, AttributeModifier> original =
                ((ItemStack) (Object) this).getItem().getAttributeModifiers(slot, stack);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        // Keep ONLY attack speed
        if (original.containsKey(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)) {
            for (AttributeModifier mod : original.get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)) {
                builder.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, mod);
            }
        }

        cir.setReturnValue(builder.build());
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void disableUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void disableUse(Level level, Player player, InteractionHand hand,
                            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (isBroken(stack)) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    @Unique
    private boolean isBroken(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage();
    }
}

