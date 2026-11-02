package net.stirdrem.overgeared.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.datapack.DurabilityBlacklistReloadListener;
import net.stirdrem.overgeared.datapack.GrindingBlacklistReloadListener;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.recipe.CoolingRecipe;
import net.stirdrem.overgeared.recipe.GrindingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.stirdrem.overgeared.Overgeared.getCooledItem;

/**
 * Loader-independent item interaction logic shared by the Forge and Fabric ModItemInteractEvents:
 * cooling heated items (water, cauldron, dropped, in containers), grinding/polishing on a grindstone,
 * and the related recipe lookups. The loader classes only translate their events into these calls.
 */
public final class ItemInteractionHelper {
    private static final String HEATED_TAG = "Heated";
    private static final String HEATED_TIME_TAG = "HeatedSince";

    private static final Map<Item, Boolean> COOLING_CACHE = new HashMap<>();

    private ItemInteractionHelper() {
    }

    // =========================
    // Right-click item: cooling, grinding, polishing, durability repair, cleanup
    // =========================

    /**
     * Cools a held heated metal when the player right-clicks a water source. Returns true if it did.
     */
    public static boolean tryCoolInWater(Player player, ItemStack stack, Level level) {
        if (!stack.is(ModTags.Items.HEATED_METALS)) return false;

        // includeFluids = true: the ray must stop at the water surface, otherwise it hits the block below
        HitResult hit = player.pick(5.0D, 0.0F, true);
        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.getFluidState().isSource() && state.getBlock() == Blocks.WATER) {
            coolItem(player, stack);
            return true;
        }
        return false;
    }

    /**
     * Sneak + right-click on a grindstone with the main-hand item. In priority order: grinding recipe,
     * polishing, durability repair.
     *
     * @return null if nothing happened (the event should continue), otherwise the result to cancel it with
     * (PASS when the item is on the grinding blacklist, SUCCESS otherwise).
     */
    @Nullable
    public static InteractionResult tryGrind(Player player, ItemStack stack, Level level) {
        if (!player.isShiftKeyDown()) return null;

        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) return null;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(ModTags.Blocks.GRINDSTONES)) return null;
        if (player.getMainHandItem() != stack) return null;

        if (hasGrindingRecipe(stack.getItem(), level)) {
            grindItem(player, stack);
            playGrindEffects(level, pos);
            return InteractionResult.SUCCESS;
        }
        if (tryPolish(player, stack, level, pos)) return InteractionResult.SUCCESS;
        return tryDurabilityGrind(stack, level, pos);
    }

    private static boolean tryPolish(Player player, ItemStack stack, Level level, BlockPos pos) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Polished") || tag.getBoolean("Polished")) return false;

        ItemStack resultItem;
        if (stack.getCount() > 1) {
            resultItem = stack.copy();
            resultItem.setCount(1);
            stack.shrink(1);
        } else {
            resultItem = stack;
        }

        CompoundTag resultTag = resultItem.getOrCreateTag();
        resultTag.putBoolean("Polished", true);

        // Polishing a still-heated item costs a quality level
        if (tag.getBoolean(HEATED_TAG) && tag.contains("ForgingQuality")) {
            ForgingQuality quality = ForgingQuality.fromString(tag.getString("ForgingQuality"));
            if (quality != null) {
                ForgingQuality downgraded = quality.getLowerQuality();
                if (downgraded != null) {
                    resultTag.putString("ForgingQuality", downgraded.getDisplayName());
                }
            }
        }

        if (resultItem != stack && !player.getInventory().add(resultItem)) {
            player.drop(resultItem, false);
        }

        playGrindEffects(level, pos);
        return true;
    }

    @Nullable
    private static InteractionResult tryDurabilityGrind(ItemStack stack, Level level, BlockPos pos) {
        if (!stack.isDamageableItem() || stack.getDamageValue() <= 0) return null;

        if (!Overgeared.getConfig().GRINDING_RESTORE_DURABILITY()) return InteractionResult.SUCCESS;
        if (isGrindingBlacklisted(stack)) return InteractionResult.PASS;

        applyDurabilityRepair(stack);
        playGrindEffects(level, pos);
        return InteractionResult.SUCCESS;
    }

    /**
     * Hides the anvil minigame unless the player is holding a smithing hammer and looking at a smithing anvil.
     */
    public static void handleMinigameCleanup(Player player, Level level) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            Services.ANVIL.hideMinigame(serverPlayer);
            return;
        }

        BlockState state = level.getBlockState(((BlockHitResult) hit).getBlockPos());
        if (!player.getMainHandItem().is(ModTags.Items.SMITHING_HAMMERS) || !state.is(ModTags.Blocks.SMITHING_ANVIL)) {
            Services.ANVIL.hideMinigame(serverPlayer);
        }
    }

    // =========================
    // Grinding
    // =========================

    public static boolean isGrindingBlacklisted(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        for (String entry : Overgeared.getConfig().GRINDING_BLACKLIST()) {
            if (entry.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
                if (tagId != null && stack.is(TagKey.create(Registries.ITEM, tagId))) return true;
            } else if (itemId != null && itemId.equals(ResourceLocation.tryParse(entry))) {
                return true;
            }
        }
        return GrindingBlacklistReloadListener.isBlacklisted(stack);
    }

    public static void applyDurabilityRepair(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        int reducedCount = tag.getInt("ReducedMaxDurability");
        int originalDurability = stack.getItem().getMaxDamage();

        float baseMultiplier = (float) Overgeared.getConfig().BASE_DURABILITY_MULTIPLIER();
        float reduction = (float) Overgeared.getConfig().DURABILITY_REDUCE_PER_GRIND();

        float qualityMultiplier = tag.contains("ForgingQuality")
                ? QualityHelper.getDurabilityMultiplier(stack)
                : 1.0f;

        int adjustedMax = (int) (originalDurability * baseMultiplier * qualityMultiplier);

        float penalty = Math.max(0.1f, 1.0f - (reducedCount * reduction));
        int effectiveMax = Math.max(1, (int) (adjustedMax * penalty));

        int currentDamage = stack.getDamageValue();

        if (currentDamage <= (adjustedMax - effectiveMax)) {
            tag.putInt("ReducedMaxDurability", reducedCount + 1);
            stack.setDamageValue(0);
            return;
        }

        float restorePercent = (float) Overgeared.getConfig().DAMAGE_RESTORE_PER_GRIND();
        int repairAmount = Math.max(1, (int) (adjustedMax * restorePercent));

        int newDamage = Math.max(adjustedMax - effectiveMax, currentDamage - repairAmount);

        stack.setDamageValue(newDamage);
        tag.putInt("ReducedMaxDurability", reducedCount + 1);
    }

    public static void playGrindEffects(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
        spawnGrindParticles(level, pos);
    }

    public static void spawnGrindParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    public static void grindItem(Player player, ItemStack heldStack) {
        Item cooledItem = getGrindable(heldStack.getItem(), player.level());
        if (cooledItem == null) return;

        ItemStack cooledIngot = new ItemStack(cooledItem);
        if (heldStack.hasTag()) {
            cooledIngot.setTag(heldStack.getTag().copy());
        }
        cooledIngot.getOrCreateTag().putBoolean("Polished", true);
        heldStack.shrink(1);

        if (heldStack.isEmpty()) {
            player.setItemInHand(player.getUsedItemHand(), cooledIngot);
        } else if (!player.getInventory().add(cooledIngot)) {
            player.drop(cooledIngot, false);
        }

        // Server-side only: Player#playSound would skip this player, so broadcast to everyone
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * The grinding recipe's result item, or the input item itself if there is no matching recipe.
     */
    @Nullable
    public static Item getGrindable(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null) return null;

        SimpleContainer container = new SimpleContainer(new ItemStack(heatedItem));
        Optional<GrindingRecipe> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())
                .stream()
                .filter(r -> r.matches(container, level))
                .findFirst();

        if (recipeOpt.isEmpty()) return heatedItem;

        ItemStack result = recipeOpt.get().getResultItem(level.registryAccess());
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    public static boolean hasGrindingRecipe(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null) return false;

        SimpleContainer container = new SimpleContainer(new ItemStack(heatedItem));
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())
                .stream()
                .filter(r -> r.matches(container, level))
                .findFirst()
                .map(recipe -> !recipe.getResultItem(level.registryAccess()).isEmpty())
                .orElse(false);
    }

    // =========================
    // Cooling
    // =========================

    public static boolean hasCoolingRecipe(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null) return false;

        SimpleContainer container = new SimpleContainer(new ItemStack(heatedItem));
        Optional<CoolingRecipe> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE.get())
                .stream()
                .filter(r -> r.matches(container, level))
                .findFirst();

        return recipeOpt.map(recipe -> !recipe.getResultItem(level.registryAccess()).isEmpty())
                .orElse(false);
    }

    public static boolean hasCoolingRecipeCached(Item item, Level level) {
        return COOLING_CACHE.computeIfAbsent(item, i -> hasCoolingRecipe(i, level));
    }

    /**
     * Cools one held item when it's dipped into a cauldron that still has water.
     */
    public static void coolInCauldron(Player player, ItemStack heldStack, BlockState cauldronState) {
        if (cauldronState.getValue(LayeredCauldronBlock.LEVEL) > 0) {
            coolItem(player, heldStack);
        }
    }

    /**
     * The cooled version of a stack (same count), with the heat tags stripped. Returns the input if it can't cool.
     */
    public static ItemStack coolSingleStack(ItemStack stack, Level level) {
        Item cooled = getCooledItem(stack.getItem(), level);
        if (cooled == null) return stack;

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());
        cooledStack.setTag(copyWithoutHeatTags(stack));
        return cooledStack;
    }

    /**
     * Cools one item out of a player's stack, putting the cooled item in their hand or inventory.
     */
    public static void coolItem(Player player, ItemStack stack) {
        Item cooled = getCooledItem(stack.getItem(), player.level());
        if (cooled == null) return;
        if (stack.getCount() <= 0) return;

        coolToolCastOutput(stack, player.level());

        ItemStack cooledStack = new ItemStack(cooled, 1);
        if (stack.hasTag()) {
            cooledStack.setTag(stack.getTag().copy());
            cooledStack.removeTagKey(HEATED_TIME_TAG);
            cooledStack.removeTagKey(HEATED_TAG);
        }

        stack.shrink(1);

        if (stack.isEmpty()) {
            if (player.getMainHandItem() == stack) {
                player.setItemInHand(InteractionHand.MAIN_HAND, cooledStack);
            } else if (player.getOffhandItem() == stack) {
                player.setItemInHand(InteractionHand.OFF_HAND, cooledStack);
            } else if (!player.getInventory().add(cooledStack)) {
                player.drop(cooledStack, false);
            }
        } else if (!player.getInventory().add(cooledStack)) {
            player.drop(cooledStack, false);
        }

        // Player#playSound excludes the player themselves (it assumes their client already played it);
        // this runs server-side only, so broadcast to everyone including the player.
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Cools a dropped item entity in place.
     */
    public static void coolItemEntity(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        Level level = entity.level();

        Item cooled = getCooledItem(stack.getItem(), level);
        if (cooled == null || stack.getCount() <= 0) return;

        coolToolCastOutput(stack, level);

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());
        cooledStack.setTag(copyWithoutHeatTags(stack));
        entity.setItem(cooledStack);
    }

    /**
     * Cools every heated item in an open container menu whose heat timer has run out.
     */
    public static void checkContainerMenu(ServerPlayer player, AbstractContainerMenu menu) {
        long gameTime = player.level().getGameTime();
        int cooldown = Overgeared.getConfig().HEATED_ITEM_COOLDOWN_TICKS();
        boolean playedSound = false;

        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(HEATED_TIME_TAG)) {
                long heatedAt = tag.getLong(HEATED_TIME_TAG);
                if (gameTime - heatedAt >= cooldown) {
                    coolItemInContainerSlot(player, slot);

                    if (!playedSound) {
                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F);
                        playedSound = true;
                    }
                }
            }
        }
    }

    public static void coolItemInContainerSlot(ServerPlayer player, Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        Item cooled = getCooledItem(stack.getItem(), player.level());
        if (cooled == null) return;

        // Tool cast: cool the cast's output too, keeping its NBT as-is
        if (stack.getItem() instanceof ToolCastItem && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Output", Tag.TAG_COMPOUND)) {
                ItemStack output = ItemStack.of(tag.getCompound("Output"));
                Item cooledOutputItem = getCooledItem(output.getItem(), player.level());
                if (cooledOutputItem != null) {
                    ItemStack cooledOutput = new ItemStack(cooledOutputItem, output.getCount());
                    if (output.hasTag()) {
                        cooledOutput.setTag(output.getTag().copy());
                    }
                    tag.put("Output", cooledOutput.save(new CompoundTag()));
                }
            }
        }

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());
        cooledStack.setTag(copyWithoutHeatTags(stack));
        slot.set(cooledStack);
    }

    /**
     * The same heat timer items in an inventory use (ItemStackMixin#onInventoryTick): the stack's HeatedSince
     * tag, stamped the first time the item is seen hot. Reading it from the stack on every check keeps a dropped
     * item in step with the rest of its stack still in the player's hand.
     */
    public static long getOrStampHeatedSince(ItemStack stack, long now) {
        CompoundTag tag = stack.getOrCreateTag();
        long heatedSince = tag.getLong(HEATED_TIME_TAG);
        if (heatedSince == 0L) {
            tag.putLong(HEATED_TIME_TAG, now);
            heatedSince = now;
        }
        return heatedSince;
    }

    /**
     * If the stack is a tool cast holding an output, replaces that output with its cooled version.
     */
    private static void coolToolCastOutput(ItemStack stack, Level level) {
        if (!(stack.getItem() instanceof ToolCastItem)) return;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Output", Tag.TAG_COMPOUND)) {
            ItemStack output = ItemStack.of(tag.getCompound("Output"));
            tag.put("Output", coolSingleStack(output, level).save(new CompoundTag()));
        }
    }

    /**
     * A copy of the stack's NBT without the heat tags, or null if nothing else is left.
     */
    @Nullable
    private static CompoundTag copyWithoutHeatTags(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        CompoundTag copy = tag.copy();
        copy.remove(HEATED_TAG);
        copy.remove(HEATED_TIME_TAG);
        return copy.isEmpty() ? null : copy;
    }

    /**
     * True if the item is excluded from the base durability multiplier, either by the
     * BASE_DURABILITY_BLACKLIST config (item ids or "#tag" entries) or the durability_blacklist datapack.
     */
    public static boolean isDurabilityBlacklisted(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        for (String entry : Overgeared.getConfig().BASE_DURABILITY_BLACKLIST()) {
            if (entry.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(entry.substring(1));
                if (tagId != null && stack.is(TagKey.create(Registries.ITEM, tagId))) return true;
            } else if (itemId != null && itemId.equals(ResourceLocation.tryParse(entry))) {
                return true;
            }
        }
        return DurabilityBlacklistReloadListener.isBlacklisted(stack);
    }
}
