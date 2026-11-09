package net.stirdrem.overgeared.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.stirdrem.overgeared.components.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Unique
    private static final Map<BlockPos, int[]> overgeared$tippedUsedCache = new ConcurrentHashMap<>();

    @Unique
    private static NonNullList<ItemStack> overgeared$items(BrewingStandBlockEntity entity) {
        return ((BrewingStandBlockEntityMixin) (Object) entity).items;
    }

    /** Partly used potions keep their remaining uses (and duration scale) when brewed further. */
    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void cacheTippedUsedTags(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity, CallbackInfo ci) {
        NonNullList<ItemStack> items = overgeared$items(entity);
        int[] cache = new int[3];
        for (int i = 0; i < 3; i++) {
            Integer tippedUses = items.get(i).get(ModComponents.TIPPED_USES.get());
            cache[i] = (tippedUses != null ? tippedUses : -1);
        }
        overgeared$tippedUsedCache.put(pos.immutable(), cache);
    }

    @Inject(method = "doBrew", at = @At("TAIL"))
    private static void restoreTippedUsedTags(ServerLevel level, BlockPos pos, BrewingStandBlockEntity entity, CallbackInfo ci) {
        int[] cache = overgeared$tippedUsedCache.remove(pos);
        if (cache == null) return;

        NonNullList<ItemStack> items = overgeared$items(entity);
        for (int i = 0; i < 3; i++) {
            if (cache[i] != -1) {
                ItemStack brewed = items.get(i);
                if (brewed.isEmpty()) continue;
                brewed.set(ModComponents.TIPPED_USES.get(), cache[i]);
                if (brewed.is(Items.POTION)) {
                    brewed.set(DataComponents.POTION_DURATION_SCALE, Math.max(0.1f, 1.0f - (cache[i] / 8.0f)));
                } else {
                    // As in 1.20.1, splash and lingering potions keep the count but their normal duration
                    Float defaultScale = brewed.getItem().components().get(DataComponents.POTION_DURATION_SCALE);
                    if (defaultScale != null) {
                        brewed.set(DataComponents.POTION_DURATION_SCALE, defaultScale);
                    } else {
                        brewed.remove(DataComponents.POTION_DURATION_SCALE);
                    }
                }
            }
        }
    }
}
