package net.stirdrem.overgeared.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<Character, Ingredient> parseKey(JsonObject keyJson) {
        Map<Character, Ingredient> map = new HashMap<>();
        map.put(' ', Ingredient.EMPTY);

        for (var e : keyJson.entrySet()) {
            if (e.getKey().length() != 1)
                throw new JsonSyntaxException("Invalid key: " + e.getKey());
            map.put(e.getKey().charAt(0), Ingredient.fromJson(e.getValue()));
        }
        return map;
    }

    public static NonNullList<Ingredient> buildIngredientList(
            String[] pattern,
            int width,
            int height,
            Map<Character, Ingredient> key
    ) {
        NonNullList<Ingredient> list = NonNullList.withSize(width * height, Ingredient.EMPTY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                list.set(
                        y * width + x,
                        key.getOrDefault(pattern[y].charAt(x), Ingredient.EMPTY)
                );
            }
        }
        return list;
    }

    public record ParsedPattern(String[] pattern, int width, int height) {
    }
}
