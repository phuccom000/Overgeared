package net.stirdrem.overgeared.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.platform.FabricPlatformHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fabric has no tier sorting, so copper (level 1) and steel (level 2) can't pass the level check
// for iron ores and obsidian; their own needs_*_tool tag lifts them past it, as Forge's sorting does.
@Mixin(DiggerItem.class)
abstract class DiggerItemMixin {
    @Shadow @Final private TagKey<Block> blocks;

    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void overgeared$tierNeedsTag(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!(((TieredItem) (Object) this).getTier() instanceof FabricPlatformHelper.SimpleTier tier)) return;
        if (state.is(tier.needsTag()) && state.is(blocks)) cir.setReturnValue(true);
    }
}
