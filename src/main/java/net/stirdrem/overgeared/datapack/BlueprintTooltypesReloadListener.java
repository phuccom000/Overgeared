package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlueprintTooltypesReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Map<Identifier, BlueprintTooltypesData> DATA = new ConcurrentHashMap<>();

    private static final Gson GSON = new Gson();

    public BlueprintTooltypesReloadListener() {
        super(GSON, "blueprint_tooltypes");
    }

    @Override
    public Identifier getFabricId() {
        return Overgeared.id("blueprint_tooltypes_listener");
    }

    @Override
    protected void apply(
            Map<Identifier, JsonElement> objects,
            ResourceManager resourceManager,
            Profiler profiler
    ) {
        DATA.clear();

        Map<Identifier, BlueprintTooltypesData> tempMap = new HashMap<>();

        for (Map.Entry<Identifier, JsonElement> entry : objects.entrySet()) {
            Identifier id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            try {
                if (!json.has("tooltypes")) {
                    throw new JsonSyntaxException("Missing 'tooltypes' array: " + id);
                }

                JsonArray arr = json.getAsJsonArray("tooltypes");
                List<String> toolTypes = new ArrayList<>();

                for (JsonElement e : arr) {
                    toolTypes.add(e.getAsString().toLowerCase(Locale.ROOT));
                }

                BlueprintTooltypesData data = new BlueprintTooltypesData(id, toolTypes);

                tempMap.put(id, data);

            } catch (Exception e) {
                Overgeared.LOGGER.error(
                        "Failed to load blueprint tooltypes: {}", id, e
                );
            }
        }

        DATA.putAll(tempMap);

        Overgeared.LOGGER.info(
                "Loaded {} blueprint tooltype packs",
                DATA.size()
        );

        ToolTypeRegistry.init();
    }

    public static Collection<BlueprintTooltypesData> getDataSnapshot() {
        return new ArrayList<>(DATA.values());
    }

    public static class BlueprintTooltypesData {
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
}
