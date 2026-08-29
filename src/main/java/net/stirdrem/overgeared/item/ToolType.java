package net.stirdrem.overgeared.item;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Locale;

public class ToolType {

    // Vanilla known types (minecraft lang keys)

    public static final ToolType SWORD = new ToolType("SWORD");
    public static final ToolType AXE = new ToolType("AXE");
    public static final ToolType PICKAXE = new ToolType("PICKAXE");
    public static final ToolType SHOVEL = new ToolType("SHOVEL");
    public static final ToolType HOE = new ToolType("HOE");

    // Overgeared example
    public static final ToolType MULTITOOL = new ToolType("MULTITOOL");

    private final String id;
    private final String translationKey;

    public ToolType(String id) {
        if (id == null || id.isEmpty())
            throw new IllegalArgumentException("Tool type ID cannot be null or empty");

        if (!id.matches("^[A-Za-z0-9_]+$"))
            throw new IllegalArgumentException("Tool type ID must be alphanumeric with underscores");

        this.id = id.toLowerCase(Locale.ROOT); // internal canonical form
        this.translationKey = "tooltype.overgeared." + this.id;
    }

    public String getId() {
        return id.toLowerCase(Locale.ROOT);
    }

    public MutableText getDisplayName() {
        return Text.translatable(translationKey).copy();
    }

    public static ToolType of(String id) {
        if (id == null || id.isEmpty())
            throw new IllegalArgumentException("Tool type ID cannot be null or empty");

        if (!id.matches("^[A-Za-z0-9_]+$"))
            throw new IllegalArgumentException("Tool type ID must be alphanumeric with underscores");

        String key = id.toLowerCase(Locale.ROOT);
        return ToolTypeRegistry.BY_ID.computeIfAbsent(key, ToolType::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ToolType)) return false;
        return id.equals(((ToolType) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
