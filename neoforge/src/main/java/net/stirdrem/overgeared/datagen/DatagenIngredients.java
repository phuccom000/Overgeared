package net.stirdrem.overgeared.datagen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/** Ingredient helpers for the mod's custom recipe builders. */
final class DatagenIngredients {
    private DatagenIngredients() {
    }

    /**
     * A tag ingredient for data generation. Tags are not loaded while generating, so this uses an
     * unbound named set; only its tag id is written to the recipe file.
     */
    @SuppressWarnings("deprecation")
    static Ingredient tag(TagKey<Item> tag) {
        return Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
    }
}
