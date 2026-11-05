package net.stirdrem.overgeared.config.core;

import net.stirdrem.overgeared.OvergearedMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A small, loader-independent TOML config, covering the parts of NeoForge's {@code ModConfigSpec} this mod uses.
 * Values are readable (with their defaults) before the file is loaded.
 */
public final class ConfigSpec {
    private final List<ConfigValue<?>> values;
    private final Map<String, ConfigValue<?>> byPath = new LinkedHashMap<>();
    private volatile Path file;
    /** Content last read from or written to {@link #file}; lets reloads skip our own writes. */
    private String lastContent;

    private ConfigSpec(List<ConfigValue<?>> values) {
        this.values = List.copyOf(values);
        for (ConfigValue<?> value : values) {
            byPath.put(String.join(".", value.path), value);
        }
    }

    // ---- File handling ----

    /**
     * Loads values from {@code file}, creating it with defaults if missing. Missing or invalid entries fall back
     * to their defaults (numbers out of range are clamped) and the file is rewritten.
     */
    public synchronized void load(Path file) {
        this.file = file;
        Map<String, Object> data = null;
        boolean needsSave = !Files.exists(file);

        if (!needsSave) {
            try {
                String content = Files.readString(file);
                lastContent = content;
                data = TomlIO.read(content);
            } catch (IOException | TomlIO.ParseException e) {
                OvergearedMod.LOGGER.error("Failed to read config {}, backing it up and resetting to defaults", file, e);
                backup(file);
                needsSave = true;
            }
        }

        for (ConfigValue<?> value : values) {
            Object raw = data == null ? null : TomlIO.lookup(data, value.path);
            if (!value.load(raw)) needsSave = true;
        }

        if (needsSave) save();
    }

    /**
     * Re-reads the file after it changed on disk (see {@link ConfigWatcher}). Does nothing if the content is the
     * same as what was last read or written. Unlike {@link #load(Path)}, a file that doesn't parse (e.g. saved
     * mid-edit) is left alone and the current values are kept.
     */
    public synchronized void reload() {
        if (file == null || !Files.exists(file)) return;

        String content;
        Map<String, Object> data;
        try {
            content = Files.readString(file);
            if (content.equals(lastContent)) return;
            data = TomlIO.read(content);
        } catch (IOException | TomlIO.ParseException e) {
            OvergearedMod.LOGGER.warn("Config {} changed but couldn't be read, keeping current values: {}", file, e.getMessage());
            return;
        }
        lastContent = content;

        boolean needsSave = false;
        for (ConfigValue<?> value : values) {
            if (!value.load(TomlIO.lookup(data, value.path))) needsSave = true;
        }
        OvergearedMod.LOGGER.info("Reloaded config {}", file.getFileName());
        if (needsSave) save();
    }

    /** @return true once {@link #load(Path)} has run */
    public boolean isLoaded() {
        return file != null;
    }

    Path getFile() {
        return file;
    }

    /** Writes the current values to the file this spec was loaded from. */
    public synchronized void save() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            String content = TomlIO.write(values);
            lastContent = content;
            Files.writeString(file, content);
        } catch (IOException e) {
            OvergearedMod.LOGGER.error("Failed to save config {}", file, e);
        }
    }

    private static void backup(Path file) {
        try {
            Files.copy(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            OvergearedMod.LOGGER.error("Failed to back up config {}", file, e);
        }
    }

    // ---- Access by dotted path, e.g. "Loot Quality.enableLootQuality" ----

    public Object getValue(String path) {
        ConfigValue<?> value = byPath.get(path);
        return value == null ? null : value.get();
    }

    /** Sets a value by path, correcting it like a loaded value would be. Unknown paths are ignored. */
    public void setValue(String path, Object newValue) {
        ConfigValue<?> value = byPath.get(path);
        if (value != null) value.load(newValue);
    }

    // ---- Builder ----

    public static final class Builder {
        private final List<ConfigValue<?>> values = new ArrayList<>();
        private final List<String> currentPath = new ArrayList<>();
        private List<String> pendingComment = List.of();

        public Builder push(String section) {
            currentPath.add(section);
            return this;
        }

        public Builder pop() {
            if (currentPath.isEmpty()) throw new IllegalStateException("pop() without matching push()");
            currentPath.removeLast();
            return this;
        }

        public Builder comment(String... lines) {
            List<String> split = new ArrayList<>();
            for (String line : lines) {
                split.addAll(Arrays.asList(line.split("\n", -1)));
            }
            // Text blocks end with a newline; drop the trailing blank line it leaves
            while (!split.isEmpty() && split.getLast().isBlank()) split.removeLast();
            pendingComment = split;
            return this;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return add(new BooleanValue(path(name), takeComment(), defaultValue));
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return add(new IntValue(path(name), takeComment(), defaultValue, min, max));
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
            return add(new DoubleValue(path(name), takeComment(), defaultValue, min, max));
        }

        public <T> ConfigValue<List<? extends T>> defineList(String name, List<? extends T> defaultValue,
                                                             Predicate<Object> elementValidator) {
            return defineList(List.of(name), () -> defaultValue, elementValidator, false);
        }

        public <T> ConfigValue<List<? extends T>> defineList(List<String> name, List<? extends T> defaultValue,
                                                             Predicate<Object> elementValidator) {
            return defineList(name, () -> defaultValue, elementValidator, false);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String name, List<? extends T> defaultValue,
                                                                       Predicate<Object> elementValidator) {
            return defineList(List.of(name), () -> defaultValue, elementValidator, true);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> name, List<? extends T> defaultValue,
                                                                       Predicate<Object> elementValidator) {
            return defineList(name, () -> defaultValue, elementValidator, true);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> name,
                                                                       Supplier<List<? extends T>> defaultValue,
                                                                       Predicate<Object> elementValidator) {
            return defineList(name, defaultValue, elementValidator, true);
        }

        private <T> ConfigValue<List<? extends T>> defineList(List<String> name, Supplier<List<? extends T>> defaultValue,
                                                              Predicate<Object> elementValidator, boolean allowEmpty) {
            List<String> path = new ArrayList<>(currentPath);
            path.addAll(name);
            return add(new ListValue<>(List.copyOf(path), takeComment(), defaultValue.get(), elementValidator, allowEmpty));
        }

        public ConfigSpec build() {
            if (!currentPath.isEmpty()) throw new IllegalStateException("Unbalanced push(): " + currentPath);
            return new ConfigSpec(values);
        }

        private List<String> path(String name) {
            List<String> path = new ArrayList<>(currentPath);
            path.add(name);
            return List.copyOf(path);
        }

        private List<String> takeComment() {
            List<String> comment = pendingComment;
            pendingComment = List.of();
            return comment;
        }

        private <V extends ConfigValue<?>> V add(V value) {
            values.add(value);
            return value;
        }
    }

    // ---- Values ----

    public abstract static class ConfigValue<T> implements Supplier<T> {
        final List<String> path;
        final List<String> comment;
        private final T defaultValue;
        private volatile T value;

        ConfigValue(List<String> path, List<String> comment, T defaultValue) {
            this.path = path;
            this.comment = comment;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        @Override
        public T get() {
            return value;
        }

        public T getDefault() {
            return defaultValue;
        }

        public void set(T newValue) {
            this.value = Objects.requireNonNull(newValue);
        }

        public List<String> getPath() {
            return path;
        }

        /** Extra comment lines written under the user comment (default value, range). */
        List<String> extraComment() {
            return List.of();
        }

        /**
         * Converts a raw value read from the file into this value's type, or {@code null} if it's unusable.
         */
        abstract T correct(Object raw);

        /** @return true if {@code raw} was valid as-is (no correction or default needed) */
        boolean load(Object raw) {
            T corrected = raw == null ? null : correct(raw);
            if (corrected == null) {
                value = defaultValue;
                return defaultValue.equals(raw); // e.g. an empty list whose default is empty
            }
            value = corrected;
            return corrected.equals(raw);
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        BooleanValue(List<String> path, List<String> comment, boolean defaultValue) {
            super(path, comment, defaultValue);
        }

        @Override
        Boolean correct(Object raw) {
            return raw instanceof Boolean b ? b : null;
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        IntValue(List<String> path, List<String> comment, int defaultValue, int min, int max) {
            super(path, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        List<String> extraComment() {
            return List.of("Default: " + getDefault(), "Range: " + TomlIO.rangeText(min, max, Integer.MIN_VALUE, Integer.MAX_VALUE));
        }

        @Override
        Integer correct(Object raw) {
            if (!(raw instanceof Number n)) return null;
            long l = n instanceof Double || n instanceof Float ? Math.round(n.doubleValue()) : n.longValue();
            return (int) Math.max(min, Math.min(max, l));
        }
    }

    public static final class DoubleValue extends ConfigValue<Double> {
        private final double min;
        private final double max;

        DoubleValue(List<String> path, List<String> comment, double defaultValue, double min, double max) {
            super(path, comment, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        List<String> extraComment() {
            return List.of("Default: " + getDefault(), "Range: " + TomlIO.rangeText(min, max, -Double.MAX_VALUE, Double.MAX_VALUE));
        }

        @Override
        Double correct(Object raw) {
            if (!(raw instanceof Number n)) return null;
            double d = n.doubleValue();
            if (Double.isNaN(d)) return null;
            return Math.max(min, Math.min(max, d));
        }
    }

    public static final class ListValue<T> extends ConfigValue<List<? extends T>> {
        private final Predicate<Object> elementValidator;
        private final boolean allowEmpty;

        ListValue(List<String> path, List<String> comment, List<? extends T> defaultValue,
                  Predicate<Object> elementValidator, boolean allowEmpty) {
            super(path, comment, Collections.unmodifiableList(new ArrayList<>(defaultValue)));
            this.elementValidator = elementValidator;
            this.allowEmpty = allowEmpty;
        }

        @Override
        @SuppressWarnings("unchecked")
        List<? extends T> correct(Object raw) {
            if (!(raw instanceof List<?> list)) return null;
            // Drop invalid elements, as NeoForge does
            List<T> valid = new ArrayList<>();
            for (Object element : list) {
                if (elementValidator.test(element)) valid.add((T) element);
            }
            if (valid.isEmpty() && !allowEmpty) return null;
            return Collections.unmodifiableList(valid);
        }
    }
}
