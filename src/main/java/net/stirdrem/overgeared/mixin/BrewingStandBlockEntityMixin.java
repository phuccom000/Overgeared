package net.stirdrem.overgeared.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.TippedPotionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds the Thick Potion + Chorus Fruit -> Dragon's Breath brew (toggled by config, so it is not a
 * plain data recipe) and keeps the mod's TIPPED_USES / POTION_DURATION_SCALE on brewed potions.
 *
 * <p>26.3: brewing is recipe based ({@code RecipeType.BREWING}); {@code isBrewable(ServerLevel,
 * BrewingStandBlockEntity)} / {@code doBrew(ServerLevel, BlockPos, BrewingStandBlockEntity)} replaced the
 * old NonNullList based statics. A thick potion + chorus fruit has no vanilla recipe, so vanilla's
 * doBrew leaves the (already replaced) slot untouched.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Unique
    private static NonNullList<ItemStack> overgeared$items(BrewingStandBlockEntity entity) {
        return ((BrewingStandBlockEntityMixin) (Object) entity).items;
    }

    @Unique
    private static boolean overgeared$isThickPotion(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.POTION)
                && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.THICK);
    }

    @Unique
    private static boolean overgeared$isDragonBreathIngredient(NonNullList<ItemStack> slots) {
        if (!ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) return false;
        if (!slots.get(3).is(Items.CHORUS_FRUIT)) return false;

        for (int i = 0; i < 3; i++) {
            if (overgeared$isThickPotion(slots.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
    private static void overgeared$canCraftDragonBreath(ServerLevel level, BrewingStandBlockEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (overgeared$isDragonBreathIngredient(overgeared$items(entity))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void overgeared$craftDragonBreath(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity, CallbackInfo ci) {
        NonNullList<ItemStack> slots = overgeared$items(entity);
        if (!ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) return;
        if (!slots.get(3).is(Items.CHORUS_FRUIT)) return;

        for (int i = 0; i < 3; i++) {
            if (overgeared$isThickPotion(slots.get(i))) {
                slots.set(i, new ItemStack(Items.DRAGON_BREATH));
            }
        }
    }

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void overgeared$isValidDragonBreathIngredient(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 3 && ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get() && stack.is(Items.CHORUS_FRUIT)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * BrewingRecipe#assemble builds a fresh stack per potion slot, dropping the mod's TIPPED_USES
     * (and the matching POTION_DURATION_SCALE) - cache before vanilla runs and restore afterwards.
     */
    @Unique
    private static final Map<BlockPos, int[]> overgeared$tippedUsedCache = new ConcurrentHashMap<>();

    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void overgeared$cacheTippedUsed(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity, CallbackInfo ci) {
        NonNullList<ItemStack> slots = overgeared$items(entity);
        int[] cache = new int[3];
        for (int i = 0; i < 3; i++) {
            cache[i] = slots.get(i).getOrDefault(ModComponents.TIPPED_USES, -1);
        }
        overgeared$tippedUsedCache.put(pos.immutable(), cache);
    }

    @Inject(method = "doBrew", at = @At("TAIL"))
    private static void overgeared$restoreTippedUsed(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity, CallbackInfo ci) {
        int[] cache = overgeared$tippedUsedCache.remove(pos);
        if (cache == null) return;

        NonNullList<ItemStack> slots = overgeared$items(entity);
        for (int i = 0; i < 3; i++) {
            if (cache[i] != -1) {
                ItemStack brewed = slots.get(i);
                if (!brewed.isEmpty()) {
                    TippedPotionHelper.restoreAfterBrewing(brewed, cache[i]);
                }
            }
        }
    }
}
