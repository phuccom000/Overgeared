package net.stirdrem.overgeared.fabric.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers NBT tag fields for recipe book types added via {@link RecipeBookTypeMixin}.
 * Without this, {@code RecipeBookSettings.read(CompoundTag)} omits them and
 * {@code replaceFrom} throws an NPE when loading player data (NeoForge does this via
 * {@code CommonHooks.buildRecipeBookTypeTagFields}).
 */
@Mixin(RecipeBookSettings.class)
public abstract class RecipeBookSettingsMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<RecipeBookType, Pair<String, String>> TAG_FIELDS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$addModdedTagFields(CallbackInfo ci) {
        Map<RecipeBookType, Pair<String, String>> fields = new HashMap<>(TAG_FIELDS);
        for (RecipeBookType type : RecipeBookType.values()) {
            fields.computeIfAbsent(type, t -> {
                String name = t.name().toLowerCase(java.util.Locale.ROOT);
                return Pair.of("is_" + name + "_gui_open", "is_" + name + "_filtering_craftable");
            });
        }
        TAG_FIELDS = Map.copyOf(fields);
    }
}
