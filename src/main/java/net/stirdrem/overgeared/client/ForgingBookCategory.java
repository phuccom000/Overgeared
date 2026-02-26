package net.stirdrem.overgeared.client;

public enum ForgingBookCategory {
    TOOL_HEADS("tool_heads"),
    ARMORS("armors"),
    MISC("misc");

    private final String name;

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
}
