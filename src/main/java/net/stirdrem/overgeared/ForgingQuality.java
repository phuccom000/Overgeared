package net.stirdrem.overgeared;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import org.jetbrains.annotations.Nullable;

public enum ForgingQuality implements StringRepresentable {
    POOR("poor"),
    WELL("well"),
    EXPERT("expert"),
    PERFECT("perfect"),
    MASTER("master"),
    NONE("none");

    public static final Codec<ForgingQuality> CODEC = StringRepresentable.fromEnum(ForgingQuality::values);
    public static final StreamCodec<ByteBuf, ForgingQuality> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], ForgingQuality::ordinal);

    private final String displayName;

    ForgingQuality(String displayName) {
        this.displayName = displayName;
    }

    public static ForgingQuality fromString(String quality) {
        for (ForgingQuality q : values()) {
            if (q.displayName.equalsIgnoreCase(quality)) return q;
        }
        return POOR; // fallback
    }

    /** The quality stored on the stack, or null if it has none. */
    @Nullable
    public static ForgingQuality get(ItemStack stack) {
        return stack.get(ModComponents.FORGING_QUALITY);
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return displayName;
    }

    public ForgingQuality getLowerQuality() {
        // NONE should never downgrade to MASTER
        if (this == NONE) {
            return NONE;
        }

        ForgingQuality[] values = values();
        int index = this.ordinal();
        return index > 0 ? values[index - 1] : this; // POOR stays POOR
    }

    public static void downgradeDamageableItems(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        if (!stack.isDamageableItem()) return;

        ForgingQuality quality = get(stack);
        if (quality == null) {
            // Check if item is affected by datapack
            boolean affected = QualityAttributeReloadListener.INSTANCE
                    .getAllItems()
                    .contains(stack.getItem());

            if (!affected) return;

            // Default to WELL
            quality = ForgingQuality.WELL;
        }

        stack.set(ModComponents.FORGING_QUALITY, quality.getLowerQuality());
    }
}
