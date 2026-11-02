package net.stirdrem.overgeared.mixin.recipebook;

import net.minecraft.world.inventory.RecipeBookType;
import net.stirdrem.overgeared.recipebook.FabricRecipeBookTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Appends the smithing-anvil recipe-book type to the vanilla enum. This runs at the end of the enum's own
 * static initialiser, before anything can cache values() (the EnumMap in RecipeBookSettings, switch maps).
 * Both sides have the mod, so the recipe-book settings sync (written per values()) stays aligned.
 */
@Mixin(RecipeBookType.class)
public abstract class RecipeBookTypeMixin {
    @Shadow
    @Final
    @Mutable
    private static RecipeBookType[] $VALUES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$addForgingType(CallbackInfo ci) {
        RecipeBookType[] values = $VALUES;
        RecipeBookType[] extended = Arrays.copyOf(values, values.length + 1);
        extended[values.length] = RecipeBookTypeAccessor.overgeared$create(FabricRecipeBookTypes.FORGING_NAME, values.length);
        $VALUES = extended;
    }
}
