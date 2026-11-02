package net.stirdrem.overgeared.mixin.recipebook;

import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookType.class)
public interface RecipeBookTypeAccessor {
    @Invoker("<init>")
    static RecipeBookType overgeared$create(String name, int ordinal) {
        throw new AssertionError();
    }
}
