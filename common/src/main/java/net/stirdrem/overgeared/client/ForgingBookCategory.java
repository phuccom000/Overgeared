package net.stirdrem.overgeared.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.StringRepresentable;

import java.util.EnumSet;

public enum ForgingBookCategory implements StringRepresentable {
    TOOL_HEADS("tool_heads"),
    ARMORS("armors"),
    MISC("misc");

    private final String name;

    public static final Codec<ForgingBookCategory> CODEC = Codec.STRING.flatXmap(s -> {
        ForgingBookCategory tab = findByName(s);
        if (tab == null) {
            return DataResult.error(() -> "Optional field 'category' does not match any valid tab. If defined, must be one of the following: " + EnumSet.allOf(ForgingBookCategory.class));
        }
        return DataResult.success(tab);
    }, tab -> DataResult.success(tab.toString()));

    ForgingBookCategory(String name) {
        this.name = name;
    }

    public static ForgingBookCategory findByName(String name) {
        for (ForgingBookCategory category : values()) {
            // Case-insensitive comparison
            if (category.getFolderName().equalsIgnoreCase(name) ||
                    category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return MISC;
    }

    public String getFolderName() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
