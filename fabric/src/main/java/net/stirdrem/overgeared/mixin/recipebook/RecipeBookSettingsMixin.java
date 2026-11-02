package net.stirdrem.overgeared.mixin.recipebook;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import net.stirdrem.overgeared.recipebook.FabricRecipeBookTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Registers NBT keys for the smithing-anvil recipe-book type. {@code RecipeBookSettings.read(CompoundTag)} only
 * creates entries for types in TAG_FIELDS, so without this a loaded player's settings lack the type and
 * {@code copy()} crashes. Forge's {@code RecipeBookType.create} adds the equivalent entry; this also makes the
 * anvil book's open/filter state persist like it does on Forge.
 */
@Mixin(RecipeBookSettings.class)
public abstract class RecipeBookSettingsMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<RecipeBookType, Pair<String, String>> TAG_FIELDS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$addForgingTagFields(CallbackInfo ci) {
        TAG_FIELDS = ImmutableMap.<RecipeBookType, Pair<String, String>>builder()
                .putAll(TAG_FIELDS)
                .put(FabricRecipeBookTypes.forging(),
                        Pair.of("isOvergearedForgingGuiOpen", "isOvergearedForgingFilteringCraftable"))
                .build();
    }
}
