package net.stirdrem.overgeared.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Vanilla gates the sweep-attack on {@code itemStack.getItem() instanceof SwordItem} inside
 * Player.attack(). That is the only ItemStack.getItem() call in attack(), and its result feeds
 * only that instanceof check, so swapping the result for AIR when the stack is broken disables
 * the sweep without touching anything else.
 * <p>
 * This deliberately uses a plain INVOKE target instead of a MixinExtras {@code @Definition}/
 * {@code @Expression}: Loom's annotation processor writes {@code @At} targets into the refmap,
 * but it does not remap {@code @Definition} method strings, so an expression-based injector only
 * matches in the dev (Mojang-mapped) environment and fails in production with
 * "Critical injection failure ... Scanned 0 target(s)".
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyExpressionValue(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"
            )
    )
    private Item overgeared$disableSweepWhenBroken(Item original) {
        ItemStack stack = ((Player) (Object) this).getMainHandItem();
        if (isBroken(stack)) {
            return Items.AIR;
        }
        return original;
    }
}
