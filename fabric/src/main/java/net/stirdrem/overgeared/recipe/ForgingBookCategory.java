package net.stirdrem.overgeared.recipe;

// Upstream keeps this in the `client` package, but it's used from common recipe
// (de)serialization code (ForgingRecipe.Serializer) and has no client dependency itself,
// so it lives in `recipe` here to avoid a false client/common boundary.
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
