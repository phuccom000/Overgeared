package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;

import java.util.function.UnaryOperator;

/**
 * Item data components, replacing the 1.20.1 item NBT keys. Component ids match the upstream
 * NeoForge port so item data stays interchangeable between loaders.
 */
public class ModComponents {
    // "Heated"
    public static final DataComponentType<Boolean> HEATED = register("heated",
            b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    // "HeatedSince" - game time the item was heated at
    public static final DataComponentType<Long> HEATED_TIME = register("heated_time",
            b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    // "ForgingQuality" - quality of a forged tool/armor piece
    public static final DataComponentType<ForgingQuality> FORGING_QUALITY = register("forging_quality",
            b -> b.persistent(ForgingQuality.CODEC).networkSynchronized(ForgingQuality.STREAM_CODEC));

    // "Creator" - name of the player who forged the item
    public static final DataComponentType<String> CREATOR = register("creator",
            b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // "Polished" - false while the item still needs grinding/polishing
    public static final DataComponentType<Boolean> POLISHED = register("polished",
            b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    // "LingeringPotion" - potion item marked as lingering for arrow tipping
    public static final DataComponentType<Boolean> LINGERING_STATUS = register("lingering_status",
            b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    // "TippedUsed" - how many times a potion has been used to tip arrows
    public static final DataComponentType<Integer> TIPPED_USES = register("tipped_uses",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    // "failedResult" - marks a failed crafting result shown in recipe viewers
    public static final DataComponentType<Boolean> FAILED_RESULT = register("failed_result",
            b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    // "Required" - whether a blueprint is required, shown in recipe viewers
    public static final DataComponentType<Boolean> BLUEPRINT_REQUIRED = register("blueprint_required",
            b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    // "ReducedMaxDurability" - how many times grinding has reduced the item's max durability
    public static final DataComponentType<Integer> REDUCED_GRIND_COUNT = register("reduced_grind_count",
            b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    // Blueprint "Quality" / "ToolType" / "Uses"
    public static final DataComponentType<BlueprintData> BLUEPRINT_DATA = register("blueprint_data",
            b -> b.persistent(BlueprintData.CODEC).networkSynchronized(BlueprintData.STREAM_CODEC));

    // Tool cast "Quality" / "ToolType" / "Materials" / "Amount" / "MaxAmount" / "input" / "Output" / "Heated"
    public static final DataComponentType<CastData> CAST_DATA = register("cast_data",
            b -> b.persistent(CastData.CODEC).networkSynchronized(CastData.STREAM_CODEC).cacheEncoding());

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Overgeared.id(name),
                builder.apply(DataComponentType.builder()).build());
    }

    public static void register() {
    }
}
