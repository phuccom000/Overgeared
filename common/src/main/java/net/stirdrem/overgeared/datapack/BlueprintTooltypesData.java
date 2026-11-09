package net.stirdrem.overgeared.datapack;

import net.minecraft.resources.Identifier;

import java.util.List;

public class BlueprintTooltypesData {

    private final Identifier id;
    private final List<String> toolTypes;

    public BlueprintTooltypesData(Identifier id, List<String> toolTypes) {
        this.id = id;
        this.toolTypes = toolTypes;
    }

    public Identifier getId() {
        return id;
    }

    public List<String> getToolTypes() {
        return toolTypes;
    }
}
