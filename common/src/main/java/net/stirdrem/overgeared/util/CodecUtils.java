package net.stirdrem.overgeared.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public class CodecUtils {

    public static final Codec<Map<String, Integer>> MATERIAL_INT_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.INT);

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Integer>> MATERIAL_INT_MAP_STREAM =
            StreamCodec.of(
                    CodecUtils::encodeMaterialIntMap,
                    CodecUtils::decodeMaterialIntMap
            );

    public static void encodeMaterialIntMap(RegistryFriendlyByteBuf buf, Map<String, Integer> map) {
        ByteBufCodecs.VAR_INT.encode(buf, map.size());
        map.forEach((key, value) -> {
            ByteBufCodecs.STRING_UTF8.encode(buf, key);
            ByteBufCodecs.VAR_INT.encode(buf, value);
        });
    }

    public static Map<String, Integer> decodeMaterialIntMap(RegistryFriendlyByteBuf buf) {
        int size = ByteBufCodecs.VAR_INT.decode(buf);
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = ByteBufCodecs.STRING_UTF8.decode(buf);
            int value = ByteBufCodecs.VAR_INT.decode(buf);
            map.put(key, value);
        }
        return map;
    }
}
