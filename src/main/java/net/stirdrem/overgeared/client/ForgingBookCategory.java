package net.stirdrem.overgeared.client;

import net.minecraft.util.StringRepresentable;

public enum ForgingBookCategory implements StringRepresentable {
    TOOLS("tool_heads"),
    ARMORS("armors"),
    MISC("misc");

    public static final StringRepresentable.EnumCodec<ForgingBookCategory> CODEC =
            StringRepresentable.fromEnum(ForgingBookCategory::values);

    private final String name;

    ForgingBookCategory(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static ForgingBookCategory findByName(String name) {
        for (ForgingBookCategory value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

    public String getFolderName() {
        return this.name;
    }
}
