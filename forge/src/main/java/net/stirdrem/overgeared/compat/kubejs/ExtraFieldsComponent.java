package net.stirdrem.overgeared.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.MapJS;

import java.util.List;
import java.util.Map;

/**
 * Another component's JSON object with extra fields merged in, such as a forging key ingredient with
 * {@code "requires_heated": true}. Scripts pass either the plain value ({@code 'minecraft:iron_ingot'}) or an object
 * holding both ({@code {item: 'minecraft:iron_ingot', requires_heated: true}}).
 */
public record ExtraFieldsComponent<T>(RecipeComponent<T> base, List<String> fields)
        implements RecipeComponent<ExtraFieldsComponent.Value<T>> {

    public static <T> ExtraFieldsComponent<T> of(RecipeComponent<T> base, String... fields) {
        return new ExtraFieldsComponent<>(base, List.of(fields));
    }

    /** The wrapped value and the extra fields that were set; {@code extra} is empty when there are none. */
    public record Value<T>(T value, JsonObject extra) {
    }

    @Override
    public String componentType() {
        return base.componentType();
    }

    @Override
    public Class<?> componentClass() {
        return Value.class;
    }

    @Override
    public ComponentRole role() {
        return base.role();
    }

    @Override
    public JsonElement write(RecipeJS recipe, Value<T> value) {
        JsonElement json = base.write(recipe, value.value());
        if (value.extra().size() == 0) {
            return json;
        }
        if (!(json instanceof JsonObject object)) {
            throw new IllegalArgumentException("Fields " + value.extra().keySet()
                    + " need a single item or tag, not " + json);
        }
        JsonObject merged = object.deepCopy();
        value.extra().entrySet().forEach(entry -> merged.add(entry.getKey(), entry.getValue()));
        return merged;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Value<T> read(RecipeJS recipe, Object from) {
        if (from instanceof Value<?> value) {
            return (Value<T>) value;
        }
        JsonObject json = from instanceof JsonObject object ? object
                : from instanceof Map<?, ?> ? MapJS.json(from) : null;
        if (json != null && fields.stream().anyMatch(json::has)) {
            JsonObject rest = json.deepCopy();
            JsonObject extra = new JsonObject();
            for (String field : fields) {
                if (rest.has(field)) {
                    extra.add(field, rest.remove(field));
                }
            }
            return new Value<>(base.read(recipe, rest), extra);
        }
        return new Value<>(base.read(recipe, from), new JsonObject());
    }

    @Override
    public boolean isInput(RecipeJS recipe, Value<T> value, ReplacementMatch match) {
        return base.isInput(recipe, value.value(), match);
    }

    @Override
    public Value<T> replaceInput(RecipeJS recipe, Value<T> original, ReplacementMatch match, InputReplacement with) {
        T replaced = base.replaceInput(recipe, original.value(), match, with);
        return replaced == original.value() ? original : new Value<>(replaced, original.extra());
    }

    @Override
    public boolean isOutput(RecipeJS recipe, Value<T> value, ReplacementMatch match) {
        return base.isOutput(recipe, value.value(), match);
    }

    @Override
    public Value<T> replaceOutput(RecipeJS recipe, Value<T> original, ReplacementMatch match, OutputReplacement with) {
        T replaced = base.replaceOutput(recipe, original.value(), match, with);
        return replaced == original.value() ? original : new Value<>(replaced, original.extra());
    }

    @Override
    public String toString() {
        return base + "+" + fields;
    }
}
