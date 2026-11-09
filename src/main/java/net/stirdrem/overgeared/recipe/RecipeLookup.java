package net.stirdrem.overgeared.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Recipe queries that work on both logical sides. Since 1.21.2 vanilla no longer sends recipes
 * to clients; every Overgeared serializer is registered with Fabric's RecipeSynchronization
 * (see ModRecipes), so the synchronized recipe set is complete for our types on the client too.
 */
public final class RecipeLookup {
    private RecipeLookup() {
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> firstMatch(Level level, RecipeType<T> type, I input) {
        return level.recipeAccess().getSynchronizedRecipes().getFirstMatch(type, input, level);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Optional<T> firstMatchValue(Level level, RecipeType<T> type, I input) {
        return firstMatch(level, type, input).map(RecipeHolder::value);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> all(Level level, RecipeType<T> type) {
        return level.recipeAccess().getSynchronizedRecipes().getAllOfType(type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> List<T> allValues(Level level, RecipeType<T> type) {
        return RecipeLookup.<I, T>all(level, type).stream().map(RecipeHolder::value).toList();
    }
}
