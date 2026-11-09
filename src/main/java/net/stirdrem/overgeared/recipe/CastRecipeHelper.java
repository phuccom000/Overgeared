package net.stirdrem.overgeared.recipe;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Shared tool-cast logic of {@link CastingRecipe} and the cast cooking recipes. */
public final class CastRecipeHelper {
    private CastRecipeHelper() {
    }

    /** The cast's data, or null if the stack has none. */
    public static CastData castData(ItemStack cast) {
        return cast.get(ModComponents.CAST_DATA);
    }

    /** True when {@code cast} carries cast data whose tool type equals {@code toolType} (case-insensitive). */
    public static boolean hasToolType(ItemStack cast, String toolType) {
        CastData data = castData(cast);
        return data != null && !data.toolType().isBlank() && toolType.equals(data.toolType().toLowerCase(Locale.ROOT));
    }

    /**
     * Applies what a cast passes on to the item cast in it (1.20.1 "ForgingQuality"/"Polished"/"Heated"/"Creator"):
     * the cast's quality (unless none), {@code polished=false} when polishing is needed, {@code heated=true}, and
     * the cast's custom name as creator when author tooltips are enabled.
     */
    public static void applyCastToResult(ItemStack result, ItemStack cast, boolean needPolishing) {
        CastData data = castData(cast);
        if (data != null && !data.quality().isBlank() && !data.quality().equalsIgnoreCase("none")) {
            result.set(ModComponents.FORGING_QUALITY, ForgingQuality.fromString(data.quality()));
        }
        if (needPolishing) {
            result.set(ModComponents.POLISHED, false);
        }
        result.set(ModComponents.HEATED, true);
        if (cast.has(DataComponents.CUSTOM_NAME) && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            result.set(ModComponents.CREATOR, cast.getHoverName().getString());
        }
    }

    /**
     * A display-only cast stack (for recipe viewers) holding exactly the required materials, like the 1.20.1
     * {@code getIngredients()} dummy cast. Fractional amounts are rounded up.
     */
    public static ItemStack displayCast(Item castItem, String toolType, Map<String, Double> requiredMaterials) {
        Map<String, Integer> materials = new LinkedHashMap<>();
        int total = 0;
        for (Map.Entry<String, Double> e : requiredMaterials.entrySet()) {
            int amount = (int) Math.ceil(e.getValue());
            materials.put(e.getKey(), amount);
            total += amount;
        }
        ItemStack stack = new ItemStack(castItem);
        stack.set(ModComponents.CAST_DATA, CastData.EMPTY
                .withToolType(toolType)
                .withMaterials(materials)
                .withAmount(total)
                .withMaxAmount(total));
        return stack;
    }
}
