package net.stirdrem.overgeared.recipe.ingredients;

import java.util.stream.Stream;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.MapCodec;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ingredient codecs for the mod's recipe types.
 * <p>
 * Since 1.21.2 an ingredient is written as {@code "minecraft:stick"}, {@code "#minecraft:planks"} or a
 * list of item ids. {@link #LENIENT} also reads the older {@code {"item": ...}} / {@code {"tag": ...}}
 * objects (and lists of them) so datapacks written for earlier versions keep loading.
 */
public final class IngredientCodecs {
    public static final Codec<Ingredient> LENIENT = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<Ingredient, T>> modern = Ingredient.CODEC.decode(ops, input);
            if (modern.isSuccess()) return modern;
            Optional<T> converted = convertLegacy(ops, input);
            if (converted.isEmpty()) return modern;
            DataResult<Pair<Ingredient, T>> legacy = Ingredient.CODEC.decode(ops, converted.get());
            return legacy.isSuccess() ? legacy : modern;
        }

        @Override
        public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            return Ingredient.CODEC.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "LenientIngredient";
        }
    };

    /**
     * An ingredient written inside a larger JSON object. New data writes it as an {@code "ingredient"}
     * field; the pre-1.21.2 inline forms {@code {"item": ...}} and {@code {"tag": ...}} are still read.
     */
    public static final MapCodec<Ingredient> INLINE_MAP_CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of("ingredient", "item", "tag").map(ops::createString);
        }

        @Override
        public <T> DataResult<Ingredient> decode(DynamicOps<T> ops, MapLike<T> input) {
            T ingredient = input.get("ingredient");
            if (ingredient != null) {
                return LENIENT.parse(ops, ingredient);
            }
            T item = input.get("item");
            if (item != null) {
                return LENIENT.parse(ops, item);
            }
            T tag = input.get("tag");
            if (tag != null) {
                return ops.getStringValue(tag).flatMap(t -> Ingredient.CODEC.parse(ops, ops.createString("#" + t)));
            }
            return DataResult.error(() -> "Missing 'ingredient' (or legacy 'item' / 'tag')");
        }

        @Override
        public <T> RecordBuilder<T> encode(Ingredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix.add("ingredient", Ingredient.CODEC.encodeStart(ops, input));
        }
    };

    private IngredientCodecs() {
    }

    private static <T> Optional<T> convertLegacy(DynamicOps<T> ops, T input) {
        Optional<T> single = convertEntry(ops, input);
        if (single.isPresent()) return single;
        return ops.getStream(input).result().flatMap(stream -> {
            List<T> entries = new ArrayList<>();
            for (T element : stream.toList()) {
                Optional<T> entry = convertEntry(ops, element);
                if (entry.isEmpty()) {
                    // already a plain item id string
                    if (ops.getStringValue(element).isSuccess()) {
                        entries.add(element);
                        continue;
                    }
                    return Optional.empty();
                }
                entries.add(entry.get());
            }
            return Optional.of(ops.createList(entries.stream()));
        });
    }

    /** {"item": "x"} -> "x", {"tag": "y"} -> "#y". */
    private static <T> Optional<T> convertEntry(DynamicOps<T> ops, T input) {
        return ops.getMap(input).result().flatMap(map -> {
            T item = map.get("item");
            if (item != null) {
                return ops.getStringValue(item).result().map(ops::createString);
            }
            T tag = map.get("tag");
            if (tag != null) {
                return ops.getStringValue(tag).result().map(t -> ops.createString("#" + t));
            }
            return Optional.empty();
        });
    }
}
