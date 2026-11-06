package net.stirdrem.overgeared.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.MapJS;

/** A JSON object written as-is, such as the {@code nbt} of the NBT-adding cooking recipes. */
public final class JsonObjectComponent implements RecipeComponent<JsonObject> {
    public static final JsonObjectComponent INSTANCE = new JsonObjectComponent();

    private JsonObjectComponent() {
    }

    @Override
    public String componentType() {
        return "json_object";
    }

    @Override
    public Class<?> componentClass() {
        return JsonObject.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, JsonObject value) {
        return value;
    }

    @Override
    public JsonObject read(RecipeJS recipe, Object from) {
        if (from instanceof JsonObject object) {
            return object;
        }
        JsonObject json = MapJS.json(from);
        if (json == null) {
            throw new IllegalArgumentException("Expected an object, got " + from);
        }
        return json;
    }

    @Override
    public String toString() {
        return componentType();
    }
}
