package net.stirdrem.overgeared.recipebook;

import net.minecraft.world.inventory.RecipeBookType;

/**
 * Fabric has no API for new recipe-book types, so {@code RecipeBookTypeMixin} appends one to the vanilla enum
 * (the Forge side uses {@code RecipeBookType.create("FORGING")}).
 */
public final class FabricRecipeBookTypes {
    /** Name of the constant added by {@code RecipeBookTypeMixin}. */
    public static final String FORGING_NAME = "OVERGEARED_FORGING";

    private static RecipeBookType forging;

    private FabricRecipeBookTypes() {
    }

    public static RecipeBookType forging() {
        if (forging == null) {
            forging = RecipeBookType.valueOf(FORGING_NAME);
        }
        return forging;
    }
}
