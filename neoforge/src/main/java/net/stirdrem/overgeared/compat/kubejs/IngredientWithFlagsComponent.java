package net.stirdrem.overgeared.compat.kubejs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An ingredient with extra fields in the same JSON object, such as a forging key's {@code requires_heated}.
 * Scripts pass a plain ingredient ({@code 'minecraft:iron_ingot'}, {@code '#c:ingots/iron'}) or an object holding
 * both ({@code {item: 'minecraft:iron_ingot', requires_heated: true}}); the mod's own codec writes the JSON.
 * Matching and replacing work on the ingredient and keep the fields.
 */
public record IngredientWithFlagsComponent<T>(
        RecipeComponentType<?> type,
        Codec<T> codec,
        Class<T> valueClass,
        List<String> fields,
        BiFunction<Ingredient, Map<String, Object>, T> create,
        Function<T, Ingredient> ingredient,
        BiFunction<T, Ingredient, T> withIngredient
) implements RecipeComponent<T> {

    private static RecipeComponent<Ingredient> ingredientComponent() {
        return IngredientComponent.INGREDIENT.instance();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(valueClass);
    }

    @Override
    public T wrap(RecipeScriptContext cx, Object from) {
        if (valueClass.isInstance(from)) {
            return valueClass.cast(from);
        }
        if (from instanceof Map<?, ?> map && fields.stream().anyMatch(map::containsKey)) {
            Map<Object, Object> rest = new LinkedHashMap<>(map);
            Map<String, Object> values = new HashMap<>();
            for (String field : fields) {
                if (rest.containsKey(field)) {
                    values.put(field, rest.remove(field));
                }
            }
            return create.apply(ingredientComponent().wrap(cx, rest), values);
        }
        return create.apply(ingredientComponent().wrap(cx, from), Map.of());
    }

    @Override
    public boolean hasPriority(RecipeMatchContext cx, Object from) {
        return valueClass.isInstance(from) || ingredientComponent().hasPriority(cx, from);
    }

    @Override
    public boolean matches(RecipeMatchContext cx, T value, ReplacementMatchInfo match) {
        return ingredientComponent().matches(cx, ingredient.apply(value), match);
    }

    @Override
    public T replace(RecipeScriptContext cx, T original, ReplacementMatchInfo match, Object with) {
        Ingredient before = ingredient.apply(original);
        Ingredient after = ingredientComponent().replace(cx, before, match, with);
        return after == before ? original : withIngredient.apply(original, after);
    }

    @Override
    public boolean isEmpty(T value) {
        return ingredient.apply(value).isEmpty();
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, T value) {
        ingredientComponent().buildUniqueId(builder, ingredient.apply(value));
    }

    @Override
    public String toString() {
        return type.toString();
    }

    /** A script flag value: {@code true}/{@code false}, or anything else as false. */
    static boolean bool(Map<String, Object> values, String field) {
        Object value = values.get(field);
        return value instanceof Boolean b ? b : value != null && Boolean.parseBoolean(value.toString());
    }

    /** A script number value, rounded down; 0 if missing. */
    static int integer(Map<String, Object> values, String field) {
        Object value = values.get(field);
        return value instanceof Number n ? n.intValue() : value == null ? 0 : Integer.parseInt(value.toString());
    }
}
