package net.stirdrem.overgeared.mixin;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.stirdrem.overgeared.config.ServerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds the Thick Potion + Chorus Fruit -> Dragon's Breath recipe. Vanilla's
 * BrewingRecipeRegistry only supports potion-to-potion transforms (same PotionItem container,
 * different Potion/Item type) - it has no way to express "consume the bottle entirely and
 * produce an unrelated item", which is what this recipe needs, so it can't be registered
 * through the vanilla registry the way Forge's IBrewingRecipe extension point allowed.
 */
@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Unique
    private static boolean overgeared$isDragonBreathIngredient(DefaultedList<ItemStack> slots) {
        if (!ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) return false;
        if (!slots.get(3).isOf(Items.CHORUS_FRUIT)) return false;

        for (int i = 0; i < 3; i++) {
            ItemStack potionStack = slots.get(i);
            if (!potionStack.isEmpty() && potionStack.isOf(Items.POTION) && PotionUtil.getPotion(potionStack) == Potions.THICK) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
    private static void overgeared$canCraftDragonBreath(DefaultedList<ItemStack> slots, CallbackInfoReturnable<Boolean> cir) {
        if (overgeared$isDragonBreathIngredient(slots)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "craft", at = @At("HEAD"))
    private static void overgeared$craftDragonBreath(World world, BlockPos pos, DefaultedList<ItemStack> slots, CallbackInfo ci) {
        if (!ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) return;
        if (!slots.get(3).isOf(Items.CHORUS_FRUIT)) return;

        // Replace matching slots before vanilla's craft() body runs; BrewingRecipeRegistry.craft
        // finds no recipe for a Dragon's Breath stack and returns it unchanged, so this is safe
        // to let vanilla's per-slot loop, ingredient decrement, and brew-event sound run as-is.
        for (int i = 0; i < 3; i++) {
            ItemStack potionStack = slots.get(i);
            if (!potionStack.isEmpty() && potionStack.isOf(Items.POTION) && PotionUtil.getPotion(potionStack) == Potions.THICK) {
                slots.set(i, new ItemStack(Items.DRAGON_BREATH));
            }
        }
    }

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void overgeared$isValidDragonBreathIngredient(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 3 && ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get() && stack.isOf(Items.CHORUS_FRUIT)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * BrewingRecipeRegistry.craft builds a fresh ItemStack per potion slot, dropping the mod's
     * "TippedUsed" tag (see PotionItemMixin / ModItemInteractEvents' arrow-dipping tracking) -
     * cache it before vanilla's craft() runs and restore it onto whatever landed in that slot.
     * Keyed per-position so concurrent brewing stands brewing in the same tick don't clash.
     */
    @Unique
    private static final Map<BlockPos, int[]> overgeared$tippedUsedCache = new ConcurrentHashMap<>();

    @Inject(method = "craft", at = @At("HEAD"))
    private static void overgeared$cacheTippedUsed(World world, BlockPos pos, DefaultedList<ItemStack> slots, CallbackInfo ci) {
        int[] cache = new int[3];

        for (int i = 0; i < 3; i++) {
            ItemStack stack = slots.get(i);
            NbtCompound tag = stack.getNbt();
            cache[i] = (tag != null && tag.contains("TippedUsed"))
                    ? tag.getInt("TippedUsed")
                    : -1;
        }

        overgeared$tippedUsedCache.put(pos, cache);
    }

    @Inject(method = "craft", at = @At("TAIL"))
    private static void overgeared$restoreTippedUsed(World world, BlockPos pos, DefaultedList<ItemStack> slots, CallbackInfo ci) {
        int[] cache = overgeared$tippedUsedCache.remove(pos);
        if (cache == null) return;

        for (int i = 0; i < 3; i++) {
            if (cache[i] != -1) {
                ItemStack brewed = slots.get(i);
                if (!brewed.isEmpty()) {
                    brewed.getOrCreateNbt().putInt("TippedUsed", cache[i]);
                }
            }
        }
    }
}
