package net.stirdrem.overgeared.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.stirdrem.overgeared.recipe.CastBlastingRecipe;
import net.stirdrem.overgeared.recipe.CastSmeltingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla furnaces output {@code Recipe#getResultItem}; NeoForge patches them to call {@code assemble}.
 * Cast recipes need {@code assemble} so the cast is returned holding the tool (and takes durability damage),
 * instead of the bare tool being output and the cast consumed.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceCastMixin {

    @WrapOperation(method = {"canBurn", "burn"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/Recipe;getResultItem(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack overgeared$assembleCastRecipe(Recipe<?> recipe, HolderLookup.Provider registries,
                                                           Operation<ItemStack> original,
                                                           @Local(argsOnly = true) NonNullList<ItemStack> items) {
        if (recipe instanceof CastSmeltingRecipe smelting) {
            return smelting.assemble(new SingleRecipeInput(items.get(0)), registries);
        }
        if (recipe instanceof CastBlastingRecipe blasting) {
            return blasting.assemble(new SingleRecipeInput(items.get(0)), registries);
        }
        return original.call(recipe, registries);
    }
}
