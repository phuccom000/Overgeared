package net.stirdrem.overgeared.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.stirdrem.overgeared.loot.LootQuality;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Gives generated loot a forging quality, where NeoForge applies its global loot modifiers.
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"))
    private ObjectArrayList<ItemStack> overgeared$applyQuality(ObjectArrayList<ItemStack> loot, LootContext context) {
        LootQuality.apply(loot, context);
        return loot;
    }
}
