package net.stirdrem.overgeared.fabric.mixin;

import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Adds the {@code OVERGEARED_FORGING} recipe book type, which NeoForge adds through enum extensions.
 */
@Mixin(RecipeBookType.class)
public abstract class RecipeBookTypeMixin {
    @Shadow
    @Final
    @Mutable
    private static RecipeBookType[] $VALUES;

    @Invoker("<init>")
    private static RecipeBookType overgeared$create(String name, int ordinal) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$addForging(CallbackInfo ci) {
        RecipeBookType[] values = Arrays.copyOf($VALUES, $VALUES.length + 1);
        values[values.length - 1] = overgeared$create("OVERGEARED_FORGING", values.length - 1);
        $VALUES = values;
    }
}
