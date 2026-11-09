package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.*;

public class BlueprintTooltypesReloadListener
        extends JsonDataReloadListener {

    public static final Map<Identifier, BlueprintTooltypesData> DATA = new HashMap<>();
    
    private static final Gson GSON = new Gson();

    public BlueprintTooltypesReloadListener() {
        super("blueprint_tooltypes");
    }

    @Override
    protected void apply(
            Map<Identifier, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        DATA.clear();

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

                BlueprintTooltypesData data =
                        new BlueprintTooltypesData(id, toolTypes);

                DATA.put(id, data);

            } catch (Exception e) {
                OvergearedMod.LOGGER.error(
                        "Failed to load blueprint tooltypes: {}", id, e
                );
            }
        }

        OvergearedMod.LOGGER.info(
                "Loaded {} blueprint tooltype packs",
                DATA.size()
        );

        // ✅ IMPORTANT: rebuild registry after /reload
        ToolTypeRegistry.init();
    }
}
