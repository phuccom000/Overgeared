package net.stirdrem.overgeared.mixin.recipebook.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookCategories.class)
public interface RecipeBookCategoriesAccessor {
    @Invoker("<init>")
    static RecipeBookCategories overgeared$create(String name, int ordinal, ItemStack... icons) {
        throw new AssertionError();
    }
}
