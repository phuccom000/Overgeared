package net.stirdrem.overgeared.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.component.CustomData;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Codecs shared by the Overgeared recipe serializers.
 * <p>
 * Ingredients and results use the 26.x vanilla formats ({@code "minecraft:stick"}, {@code "#minecraft:planks"},
 * {@code {"id": ..., "count": ..., "components": {...}}}), but also accept the 1.20.1 object forms
 * ({@code {"item": ...}}, {@code {"tag": ...}}, {@code {"item": ..., "count": ...}}) so that older datapacks
 * written for this mod keep loading. Encoding always produces the modern format.
 */
public final class RecipeCodecs {
    private RecipeCodecs() {
    }

    /** {@link Ingredient#CODEC}, also accepting legacy {@code {"item": id}} / {@code {"tag": id}} objects (or lists of them). */
    public static final Codec<Ingredient> INGREDIENT = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<Ingredient, T>> modern = Ingredient.CODEC.decode(ops, input);
            if (modern.isSuccess()) return modern;
            return legacyIngredient(ops, input)
                    .map(converted -> Ingredient.CODEC.decode(ops, converted))
                    .orElse(modern);
        }

        @Override
        public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            return Ingredient.CODEC.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "OvergearedIngredient";
        }
    };

    /** {@link ItemStackTemplate#CODEC}, also accepting the legacy {@code {"item": id, "count": n}} result object. */
    public static final Codec<ItemStackTemplate> RESULT = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ItemStackTemplate, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<ItemStackTemplate, T>> modern = ItemStackTemplate.CODEC.decode(ops, input);
            if (modern.isSuccess()) return modern;
            return legacyResult(ops, input)
                    .map(converted -> ItemStackTemplate.CODEC.decode(ops, converted))
                    .orElse(modern);
        }

        @Override
        public <T> DataResult<T> encode(ItemStackTemplate input, DynamicOps<T> ops, T prefix) {
            return ItemStackTemplate.CODEC.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "OvergearedResult";
        }
    };

    /** Lenient forging quality codec (case-insensitive, unknown values fall back to POOR like the 1.20.1 parser). */
    public static final Codec<ForgingQuality> FORGING_QUALITY =
            Codec.STRING.xmap(ForgingQuality::fromString, ForgingQuality::getDisplayName);

    /** A single pattern-key symbol. */
    public static final Codec<Character> SYMBOL = Codec.STRING.comapFlatMap(symbol -> symbol.length() != 1
                    ? DataResult.error(() -> "Invalid key entry: '" + symbol + "' is an invalid symbol (must be 1 character only).")
                    : DataResult.success(symbol.charAt(0)),
            String::valueOf);

    /** Pattern rows: 1..maxSize rows, each 1..maxSize wide, all the same width. */
    public static Codec<List<String>> pattern(int maxSize) {
        return Codec.STRING.listOf().comapFlatMap(rows -> {
            if (rows.isEmpty()) return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            if (rows.size() > maxSize)
                return DataResult.error(() -> "Invalid pattern: too many rows, " + maxSize + " is maximum");
            int width = rows.getFirst().length();
            for (String row : rows) {
                if (row.length() > maxSize)
                    return DataResult.error(() -> "Invalid pattern: too many columns, " + maxSize + " is maximum");
                if (row.length() != width)
                    return DataResult.error(() -> "Invalid pattern: each row must be the same width");
            }
            return DataResult.success(List.copyOf(rows));
        }, rows -> rows);
    }

    public static <T> Codec<Map<Character, T>> key(Codec<T> valueCodec) {
        return Codec.unboundedMap(SYMBOL, valueCodec);
    }

    /** Reads {@code name} as either a single value or a list of values. Encodes as a list. */
    public static <T> Codec<List<T>> singleOrList(Codec<T> codec) {
        return Codec.withAlternative(codec.listOf(), codec.xmap(List::of, list -> list.getFirst()));
    }

    /** Material requirement map used by casting recipes ({"iron": 27, ...}); keys are lower-cased, values must be > 0. */
    public static final Codec<Map<String, Double>> MATERIALS = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
            .comapFlatMap(map -> {
                Map<String, Double> out = new LinkedHashMap<>();
                for (Map.Entry<String, Double> e : map.entrySet()) {
                    if (e.getValue() <= 0) {
                        return DataResult.error(() -> "Material '" + e.getKey() + "' must be > 0, got: " + e.getValue());
                    }
                    out.put(e.getKey().toLowerCase(java.util.Locale.ROOT), e.getValue());
                }
                return DataResult.success(out);
            }, map -> map);

    /** Field holding a legacy 1.20.1 NBT compound (converted to components with {@link #legacyNbtToPatch}). */
    public static MapCodec<CompoundTag> legacyNbtField(String name) {
        return CompoundTag.CODEC.optionalFieldOf(name, new CompoundTag());
    }

    /**
     * Translates the 1.20.1 item NBT keys used by Overgeared into data components (see the table in
     * PORTING_NOTES.md). Unknown keys are kept as {@code minecraft:custom_data}.
     */
    public static DataComponentPatch legacyNbtToPatch(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return DataComponentPatch.EMPTY;
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        CompoundTag custom = new CompoundTag();
        for (String key : tag.keySet()) {
            switch (key) {
                case "Heated" -> tag.getBoolean(key).ifPresent(v -> builder.set(ModComponents.HEATED, v));
                case "HeatedSince" -> tag.getLong(key).ifPresent(v -> builder.set(ModComponents.HEATED_TIME, v));
                case "Polished" -> tag.getBoolean(key).ifPresent(v -> builder.set(ModComponents.POLISHED, v));
                case "ForgingQuality" -> tag.getString(key).ifPresent(v -> builder.set(ModComponents.FORGING_QUALITY, ForgingQuality.fromString(v)));
                case "Creator" -> tag.getString(key).ifPresent(v -> builder.set(ModComponents.CREATOR, v));
                case "failedResult" -> tag.getBoolean(key).ifPresent(v -> builder.set(ModComponents.FAILED_RESULT, v));
                case "Required" -> tag.getBoolean(key).ifPresent(v -> builder.set(ModComponents.BLUEPRINT_REQUIRED, v));
                case "LingeringPotion" -> tag.getBoolean(key).ifPresent(v -> builder.set(ModComponents.LINGERING_STATUS, v));
                case "TippedUsed" -> tag.getInt(key).ifPresent(v -> builder.set(ModComponents.TIPPED_USES, v));
                case "ReducedMaxDurability" -> tag.getInt(key).ifPresent(v -> builder.set(ModComponents.REDUCED_GRIND_COUNT, v));
                case "Damage" -> tag.getInt(key).ifPresent(v -> builder.set(DataComponents.DAMAGE, v));
                default -> {
                    Tag value = tag.get(key);
                    if (value != null) custom.put(key, value.copy());
                }
            }
        }
        if (!custom.isEmpty()) builder.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        return builder.build();
    }

    // ------------------------------------------------------------------------------------------
    // legacy JSON conversion
    // ------------------------------------------------------------------------------------------

    private static <T> Optional<T> legacyIngredient(DynamicOps<T> ops, T input) {
        Optional<MapLike<T>> map = ops.getMap(input).result();
        if (map.isPresent()) {
            T item = map.get().get("item");
            if (item != null) return ops.getStringValue(item).result().map(ops::createString);
            T tag = map.get().get("tag");
            if (tag != null) return ops.getStringValue(tag).result().map(s -> ops.createString("#" + s));
            return Optional.empty();
        }
        Optional<Stream<T>> list = ops.getStream(input).result();
        if (list.isPresent()) {
            List<T> converted = new ArrayList<>();
            for (T element : list.get().toList()) {
                if (ops.getStringValue(element).result().isPresent()) {
                    converted.add(element);
                    continue;
                }
                Optional<T> c = legacyIngredient(ops, element);
                if (c.isEmpty()) return Optional.empty();
                converted.add(c.get());
            }
            return Optional.of(ops.createList(converted.stream()));
        }
        return Optional.empty();
    }

    private static <T> Optional<T> legacyResult(DynamicOps<T> ops, T input) {
        Optional<MapLike<T>> map = ops.getMap(input).result();
        if (map.isEmpty() || map.get().get("item") == null || map.get().get("id") != null) return Optional.empty();
        Map<T, T> entries = new LinkedHashMap<>();
        map.get().entries().forEach(entry -> {
            String key = ops.getStringValue(entry.getFirst()).result().orElse("");
            switch (key) {
                case "item" -> entries.put(ops.createString("id"), entry.getSecond());
                case "nbt", "tag" -> {
                    // 26.3 port: legacy result NBT is not translated here; use "components" instead
                }
                default -> entries.put(entry.getFirst(), entry.getSecond());
            }
        });
        return Optional.of(ops.createMap(entries));
    }
}
