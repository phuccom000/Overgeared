package net.stirdrem.overgeared.util;

import com.google.gson.*;
import net.minecraft.nbt.*;

public class JsonToNBT {

    public static CompoundTag parseCompound(JsonObject json) {
        CompoundTag tag = new CompoundTag();

        for (String key : json.keySet()) {
            tag.put(key, parseElement(json.get(key)));
        }

        return tag;
    }

    private static Tag parseElement(JsonElement element) {
        if (element.isJsonObject()) {
            return parseCompound(element.getAsJsonObject());
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            ListTag list = new ListTag();

            for (JsonElement e : array) {
                list.add(parseElement(e));
            }

            return list;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isBoolean()) {
                return ByteTag.valueOf(primitive.getAsBoolean());
            }

            if (primitive.isNumber()) {
                return parseNumber(primitive);
            }

            if (primitive.isString()) {
                return StringTag.valueOf(primitive.getAsString());
            }
        }

        throw new JsonParseException("Invalid NBT element: " + element);
    }

    private static Tag parseNumber(JsonPrimitive primitive) {
        String raw = primitive.getAsString();

        try {
            if (raw.endsWith("b")) return ByteTag.valueOf(Byte.parseByte(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("s")) return ShortTag.valueOf(Short.parseShort(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("l")) return LongTag.valueOf(Long.parseLong(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("f")) return FloatTag.valueOf(Float.parseFloat(raw.substring(0, raw.length() - 1)));
            if (raw.endsWith("d")) return DoubleTag.valueOf(Double.parseDouble(raw.substring(0, raw.length() - 1)));

            // default = int
            return IntTag.valueOf(Integer.parseInt(raw));
        } catch (NumberFormatException e) {
            return DoubleTag.valueOf(primitive.getAsDouble());
        }
    }
}