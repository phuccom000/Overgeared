package net.stirdrem.overgeared.fabric.mixin.client;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.fabric.client.FabricRecipeCategories;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin {
    @Inject(method = "getCategory", at = @At("HEAD"), cancellable = true)
    private static void overgeared$forgingCategory(RecipeHolder<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {
        if (recipe.value().getType() == ModRecipeTypes.FORGING.get()) {
            cir.setReturnValue(FabricRecipeCategories.categoryFor(recipe));
        }
    }
}
