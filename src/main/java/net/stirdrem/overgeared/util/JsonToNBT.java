package net.stirdrem.overgeared.util;

import com.google.gson.*;
import net.minecraft.nbt.*;

public class JsonToNBT {

    public static NbtCompound parseCompound(JsonObject json) {
        NbtCompound tag = new NbtCompound();

        for (String key : json.keySet()) {
            tag.put(key, parseElement(json.get(key)));
        }

        return tag;
    }

    private static NbtElement parseElement(JsonElement element) {
        if (element.isJsonObject()) {
            return parseCompound(element.getAsJsonObject());
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            NbtList list = new NbtList();

            for (JsonElement e : array) {
                list.add(parseElement(e));
            }

            return list;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isBoolean()) {
                return NbtByte.of(primitive.getAsBoolean());
            }

            if (primitive.isNumber()) {
                return parseNumber(primitive);
            }

            if (primitive.isString()) {
                return NbtString.of(primitive.getAsString());
            }
        }

        throw new JsonParseException("Invalid NBT element: " + element);
    }

    private static NbtElement parseNumber(JsonPrimitive primitive) {
        String raw = primitive.getAsString();

        try {
            if (raw.endsWith("b")) return NbtByte.of(Byte.parseByte(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("s")) return NbtShort.of(Short.parseShort(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("l")) return NbtLong.of(Long.parseLong(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("f")) return NbtFloat.of(Float.parseFloat(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("d")) return NbtDouble.of(Double.parseDouble(raw.substring(0, raw.length() - 1)));

            // default = int
            return NbtInt.of(Integer.parseInt(raw));
        } catch (NumberFormatException e) {
            return NbtDouble.of(primitive.getAsDouble());
        }
    }
}
