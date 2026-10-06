package net.stirdrem.overgeared.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.datapack.OvergearedJsonReloadListener;

public final class ShapedAlloySerializerUtil {

    private ShapedAlloySerializerUtil() {
    }

    public static String[] trimPattern(List<String> rows) {
        int minX = Integer.MAX_VALUE;
        int maxX = 0;
        int minY = 0;
        int maxY = rows.size();

        while (minY < maxY && rows.get(minY).trim().isEmpty()) minY++;
        while (maxY > minY && rows.get(maxY - 1).trim().isEmpty()) maxY--;

        for (int y = minY; y < maxY; y++) {
            String row = rows.get(y);
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) != ' ') {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                }
            }
        }

        if (minX == Integer.MAX_VALUE) return new String[0];

        String[] result = new String[maxY - minY];
        for (int i = 0; i < result.length; i++) {
            result[i] = rows.get(i + minY).substring(minX, maxX + 1);
        }
        return result;
    }

    public static ParsedPattern parsePattern(JsonArray patternArray, int maxSize) {
        List<String> raw = new ArrayList<>();
        for (int i = 0; i < patternArray.size(); i++) {
            raw.add(patternArray.get(i).getAsString());
        }

        String[] pattern = trimPattern(raw);
        int height = pattern.length;
        int width = pattern[0].length();

        if (width > maxSize || height > maxSize)
            throw new JsonSyntaxException("Pattern cannot exceed " + maxSize + "x" + maxSize);

        return new ParsedPattern(pattern, width, height);
    }

    /**
     * 26.3 port: Ingredient.EMPTY no longer exists - empty cells are {@code Optional.empty()}
     * (same convention as vanilla ShapedRecipePattern). Accepts legacy and 1.21+ ingredient JSON.
     */
    public static Map<Character, Optional<Ingredient>> parseKey(JsonObject keyJson) {
        return parseKey(keyJson, BuiltInRegistries.ITEM);
    }

    public static Map<Character, Optional<Ingredient>> parseKey(JsonObject keyJson, HolderGetter<Item> items) {
        Map<Character, Optional<Ingredient>> map = new HashMap<>();
        map.put(' ', Optional.empty());

        for (var e : keyJson.entrySet()) {
            if (e.getKey().length() != 1)
                throw new JsonSyntaxException("Invalid key: " + e.getKey());
            map.put(e.getKey().charAt(0), Optional.of(OvergearedJsonReloadListener.parseIngredient(e.getValue(), items)));
        }
        return map;
    }

    public static List<Optional<Ingredient>> buildIngredientList(
            String[] pattern,
            int width,
            int height,
            Map<Character, Optional<Ingredient>> key
    ) {
        List<Optional<Ingredient>> list = new ArrayList<>(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                list.add(key.getOrDefault(pattern[y].charAt(x), Optional.empty()));
            }
        }
        return list;
    }

    public record ParsedPattern(String[] pattern, int width, int height) {
    }
}
