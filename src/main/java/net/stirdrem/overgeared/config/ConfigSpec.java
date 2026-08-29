package net.stirdrem.overgeared.config;

import com.google.gson.*;
import net.stirdrem.overgeared.Overgeared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Minimal reimplementation of Forge's ForgeConfigSpec builder API (define/defineInRange/
 * defineList/push/pop/comment), backed by a flat JSON file instead of TOML. Exists so the
 * original mod's config classes can be ported with type-name substitutions only, instead of
 * a rewrite - every {@code .get()} call site elsewhere in the mod stays unchanged.
 *
 * push()/pop()/comment() are accepted for source compatibility but don't affect the file
 * layout: this stores everything as a flat key/value JSON object rather than mirroring
 * Forge's nested TOML categories.
 */
public class ConfigSpec {
    private final Map<String, ConfigValue<?>> values;
    private Path loadedPath;

    private ConfigSpec(Map<String, ConfigValue<?>> values) {
        this.values = values;
    }

    public void load(Path path) {
        this.loadedPath = path;
        if (Files.exists(path)) {
            try {
                JsonElement parsed = JsonParser.parseString(Files.readString(path));
                if (parsed.isJsonObject()) {
                    JsonObject json = parsed.getAsJsonObject();
                    for (Map.Entry<String, ConfigValue<?>> entry : values.entrySet()) {
                        if (json.has(entry.getKey())) {
                            entry.getValue().loadFromJson(json.get(entry.getKey()));
                        }
                    }
                }
            } catch (IOException | JsonParseException e) {
                Overgeared.LOGGER.warn("Failed to read config {}, using defaults", path, e);
            }
        }
        save(path);
    }

    /**
     * Re-saves to the path last passed to load(). Used by the in-game config screen after
     * edits, since it only holds references to the individual ConfigValues, not the path.
     */
    public void save() {
        if (loadedPath != null) save(loadedPath);
    }

    public void save(Path path) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, ConfigValue<?>> entry : values.entrySet()) {
            entry.getValue().writeToJson(json, entry.getKey());
        }
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(json));
        } catch (IOException e) {
            Overgeared.LOGGER.warn("Failed to write config {}", path, e);
        }
    }

    public static class Builder {
        private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();

        public Builder push(String category) {
            return this;
        }

        public Builder pop() {
            return this;
        }

        public Builder comment(String... lines) {
            return this;
        }

        public BooleanValue define(String key, boolean defaultValue) {
            BooleanValue v = new BooleanValue(defaultValue);
            values.put(key, v);
            return v;
        }

        public IntValue defineInRange(String key, int defaultValue, int min, int max) {
            IntValue v = new IntValue(defaultValue, min, max);
            values.put(key, v);
            return v;
        }

        public DoubleValue defineInRange(String key, double defaultValue, double min, double max) {
            DoubleValue v = new DoubleValue(defaultValue, min, max);
            values.put(key, v);
            return v;
        }

        public <T> ListValue<T> defineList(String key, List<T> defaultValue, Predicate<Object> elementValidator) {
            ListValue<T> v = new ListValue<>(defaultValue, elementValidator);
            values.put(key, v);
            return v;
        }

        public <T> ListValue<T> defineListAllowEmpty(String key, List<T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(key, defaultValue, elementValidator);
        }

        public <T> ListValue<T> defineListAllowEmpty(List<String> path, Supplier<List<T>> defaultSupplier, Predicate<Object> elementValidator) {
            ListValue<T> v = new ListValue<>(defaultSupplier.get(), elementValidator);
            values.put(String.join(".", path), v);
            return v;
        }

        public ConfigSpec build() {
            return new ConfigSpec(values);
        }
    }

    // --- Value types ---

    public static abstract class ConfigValue<T> {
        protected T value;

        protected ConfigValue(T defaultValue) {
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T newValue) {
            this.value = newValue;
        }

        abstract void loadFromJson(JsonElement element);

        abstract void writeToJson(JsonObject parent, String key);
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(boolean defaultValue) {
            super(defaultValue);
        }

        @Override
        void loadFromJson(JsonElement e) {
            if (e.isJsonPrimitive()) value = e.getAsBoolean();
        }

        @Override
        void writeToJson(JsonObject parent, String key) {
            parent.addProperty(key, value);
        }
    }

    public static class IntValue extends ConfigValue<Integer> {
        private final int min, max;

        IntValue(int defaultValue, int min, int max) {
            super(defaultValue);
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public void set(Integer newValue) {
            super.set(Math.max(min, Math.min(max, newValue)));
        }

        @Override
        void loadFromJson(JsonElement e) {
            if (e.isJsonPrimitive()) value = Math.max(min, Math.min(max, e.getAsInt()));
        }

        @Override
        void writeToJson(JsonObject parent, String key) {
            parent.addProperty(key, value);
        }
    }

    public static class DoubleValue extends ConfigValue<Double> {
        private final double min, max;

        DoubleValue(double defaultValue, double min, double max) {
            super(defaultValue);
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        @Override
        public void set(Double newValue) {
            super.set(Math.max(min, Math.min(max, newValue)));
        }

        @Override
        void loadFromJson(JsonElement e) {
            if (e.isJsonPrimitive()) value = Math.max(min, Math.min(max, e.getAsDouble()));
        }

        @Override
        void writeToJson(JsonObject parent, String key) {
            parent.addProperty(key, value);
        }
    }

    public static class ListValue<T> extends ConfigValue<List<T>> {
        private final Predicate<Object> validator;

        ListValue(List<T> defaultValue, Predicate<Object> validator) {
            super(new ArrayList<>(defaultValue));
            this.validator = validator;
        }

        @SuppressWarnings("unchecked")
        @Override
        void loadFromJson(JsonElement e) {
            if (!e.isJsonArray()) return;
            List<T> result = new ArrayList<>();
            for (JsonElement el : e.getAsJsonArray()) {
                Object parsed = parseElement(el);
                if (parsed != null && validator.test(parsed)) result.add((T) parsed);
            }
            value = result;
        }

        private Object parseElement(JsonElement el) {
            if (el.isJsonArray()) {
                List<Object> nested = new ArrayList<>();
                for (JsonElement sub : el.getAsJsonArray()) nested.add(parseElement(sub));
                return nested;
            }
            if (el.isJsonPrimitive()) {
                JsonPrimitive p = el.getAsJsonPrimitive();
                if (p.isBoolean()) return p.getAsBoolean();
                if (p.isNumber()) {
                    double d = p.getAsDouble();
                    return (d == Math.floor(d) && !Double.isInfinite(d)) ? (Object) (int) d : (Object) d;
                }
                return p.getAsString();
            }
            return null;
        }

        @Override
        void writeToJson(JsonObject parent, String key) {
            parent.add(key, toJson(value));
        }

        private JsonElement toJson(Object o) {
            if (o instanceof List<?> list) {
                JsonArray arr = new JsonArray();
                for (Object item : list) arr.add(toJson(item));
                return arr;
            }
            if (o instanceof String s) return new JsonPrimitive(s);
            if (o instanceof Number n) return new JsonPrimitive(n);
            if (o instanceof Boolean b) return new JsonPrimitive(b);
            return JsonNull.INSTANCE;
        }
    }
}
