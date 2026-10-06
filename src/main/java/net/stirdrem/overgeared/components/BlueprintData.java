package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.stirdrem.overgeared.BlueprintQuality;

/**
 * Blueprint item data (was the blueprint's "Quality", "ToolType" and "Uses" NBT keys).
 */
public record BlueprintData(String quality, String toolType, int uses) {
    public static final String DEFAULT_TOOL_TYPE = "sword";

    public static final Codec<BlueprintData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("quality", BlueprintQuality.POOR.getId()).forGetter(BlueprintData::quality),
            Codec.STRING.optionalFieldOf("tool_type", DEFAULT_TOOL_TYPE).forGetter(BlueprintData::toolType),
            Codec.INT.optionalFieldOf("uses", 0).forGetter(BlueprintData::uses)
    ).apply(instance, BlueprintData::new));

    public static final StreamCodec<ByteBuf, BlueprintData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BlueprintData::quality,
            ByteBufCodecs.STRING_UTF8, BlueprintData::toolType,
            ByteBufCodecs.VAR_INT, BlueprintData::uses,
            BlueprintData::new);

    public static BlueprintData createDefault() {
        return new BlueprintData(BlueprintQuality.POOR.getId(), DEFAULT_TOOL_TYPE, 0);
    }

    public BlueprintData withQuality(String quality) {
        return new BlueprintData(quality, toolType, uses);
    }

    public BlueprintData withToolType(String toolType) {
        return new BlueprintData(quality, toolType, uses);
    }

    public BlueprintData withUses(int uses) {
        return new BlueprintData(quality, toolType, uses);
    }

    public BlueprintQuality getQualityEnum() {
        return BlueprintQuality.fromString(quality);
    }
}
