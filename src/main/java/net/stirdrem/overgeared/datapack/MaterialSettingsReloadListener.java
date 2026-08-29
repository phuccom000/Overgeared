package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.stirdrem.overgeared.Overgeared;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MaterialSettingsReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static class MaterialEntry {
        private final String itemOrTag;
        private final String materialId;
        private final int materialValue;
        private final boolean isTag;

        public MaterialEntry(String itemOrTag, String materialId, int materialValue) {
            this.itemOrTag = itemOrTag;
            this.materialId = materialId;
            this.materialValue = materialValue;
            this.isTag = itemOrTag.startsWith("#");
        }

        public String getItemOrTag() {
            return itemOrTag;
        }

        public String getMaterialId() {
            return materialId;
        }

        public int getMaterialValue() {
            return materialValue;
        }

        public boolean isTag() {
            return isTag;
        }

        public String getActualTagName() {
            return isTag ? itemOrTag.substring(1) : itemOrTag;
        }
    }

    private static final Map<Identifier, List<MaterialEntry>> DATA = new ConcurrentHashMap<>();
    public static final MaterialSettingsReloadListener INSTANCE = new MaterialSettingsReloadListener();
    private static final Gson GSON = new Gson();

    public MaterialSettingsReloadListener() {
        super(GSON, "material_settings");
    }

    @Override
    public Identifier getFabricId() {
        return Overgeared.id("material_settings_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, Profiler profiler) {
        DATA.clear();

        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();
                    List<MaterialEntry> materialEntries = parseMaterialEntries(json);
                    DATA.put(id, materialEntries);
                } else {
                    throw new JsonSyntaxException("Expected JSON object for material settings entry: " + id);
                }
            } catch (Exception e) {
                Overgeared.LOGGER.error("Failed to parse material settings entry: {}", id, e);
            }
        }

        Overgeared.LOGGER.info("Loaded {} material settings entries", DATA.size());
    }

    private List<MaterialEntry> parseMaterialEntries(JsonObject json) {
        List<MaterialEntry> entries = new ArrayList<>();

        if (!json.has("materials")) {
            throw new JsonSyntaxException("Missing 'materials' array for material settings entry");
        }

        JsonArray materialsArray = json.getAsJsonArray("materials");
        for (JsonElement materialElement : materialsArray) {
            if (!materialElement.isJsonArray()) {
                throw new JsonSyntaxException("Expected array for material entry");
            }

            JsonArray materialArray = materialElement.getAsJsonArray();
            if (materialArray.size() != 3) {
                throw new JsonSyntaxException("Material entry must have exactly 3 elements: [item_or_tag, material_id, material_value]");
            }

            String itemOrTag = materialArray.get(0).getAsString();
            String materialId = materialArray.get(1).getAsString();
            int materialValue = materialArray.get(2).getAsInt();

            if (materialValue <= 0) {
                throw new JsonSyntaxException("Material value must be positive: " + materialValue);
            }

            entries.add(new MaterialEntry(itemOrTag, materialId, materialValue));
        }

        return entries;
    }

    public static Map<Identifier, List<MaterialEntry>> getData() {
        return Collections.unmodifiableMap(DATA);
    }

    public static List<MaterialEntry> getAllMaterialEntries() {
        List<MaterialEntry> allEntries = new ArrayList<>();
        for (List<MaterialEntry> entries : DATA.values()) {
            allEntries.addAll(entries);
        }
        return allEntries;
    }

    public static List<MaterialEntry> getEntriesForMaterial(String materialId) {
        List<MaterialEntry> result = new ArrayList<>();
        for (MaterialEntry entry : getAllMaterialEntries()) {
            if (entry.getMaterialId().equals(materialId)) {
                result.add(entry);
            }
        }
        return result;
    }

    public static Optional<MaterialEntry> getEntryForItem(String itemId) {
        for (MaterialEntry entry : getAllMaterialEntries()) {
            if (entry.getItemOrTag().equals(itemId)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static Map<String, Integer> getMaterialValuesForItem(String itemId) {
        Map<String, Integer> result = new HashMap<>();
        for (MaterialEntry entry : getAllMaterialEntries()) {
            if (entry.getItemOrTag().equals(itemId)) {
                result.put(entry.getMaterialId(), entry.getMaterialValue());
            }
        }
        return result;
    }

    public static void clear() {
        DATA.clear();
    }
}
