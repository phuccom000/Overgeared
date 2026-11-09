package net.stirdrem.overgeared.fabric.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.stirdrem.overgeared.config.ServerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Thick potion + chorus fruit = dragon's breath. On NeoForge this is a brewing recipe registered in
 * {@code RegisterBrewingRecipesEvent}; Fabric's brewing builder only supports potion-to-potion mixes.
 */
@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {

    @Unique
    private static boolean overgeared$enabled() {
        return ServerConfig.SERVER_CONFIG.isLoaded() && ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get();
    }

    @Unique
    private static boolean overgeared$isThickPotion(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.THICK);
    }

    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void overgeared$isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(Items.CHORUS_FRUIT) && overgeared$enabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void overgeared$hasMix(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (ingredient.is(Items.CHORUS_FRUIT) && overgeared$isThickPotion(input) && overgeared$enabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void overgeared$mix(ItemStack ingredient, ItemStack potion, CallbackInfoReturnable<ItemStack> cir) {
        if (ingredient.is(Items.CHORUS_FRUIT) && overgeared$isThickPotion(potion) && overgeared$enabled()) {
            cir.setReturnValue(new ItemStack(Items.DRAGON_BREATH));
        }
    }
}
