package net.stirdrem.overgeared.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.stirdrem.overgeared.recipe.castcooking.CastBlastingRecipe;
import net.stirdrem.overgeared.recipe.castcooking.CastSmeltingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla furnaces output {@code Recipe#getResultItem}; Forge patches them to call {@code assemble}.
 * Cast recipes need {@code assemble} so the cast is returned holding the tool (and takes durability damage),
 * instead of the bare tool being output and the cast consumed.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceCastMixin {

    @WrapOperation(method = {"canBurn", "burn"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/Recipe;getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack overgeared$assembleCastRecipe(Recipe<?> recipe, RegistryAccess registryAccess,
                                                           Operation<ItemStack> original,
                                                           @Local(argsOnly = true) NonNullList<ItemStack> items) {
        if (recipe instanceof CastSmeltingRecipe smelting) {
            return smelting.assemble(new SimpleContainer(items.get(0)), registryAccess);
        }
        if (recipe instanceof CastBlastingRecipe blasting) {
            return blasting.assemble(new SimpleContainer(items.get(0)), registryAccess);
        }
        return original.call(recipe, registryAccess);
    }
}
