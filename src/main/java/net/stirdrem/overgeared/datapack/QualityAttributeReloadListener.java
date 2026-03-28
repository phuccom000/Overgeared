package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class QualityAttributeReloadListener
        extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Map<ResourceLocation, QualityAttributeDefinition> DEFINITIONS = new HashMap<>();

    public QualityAttributeReloadListener() {
        super(GSON, "quality_attributes");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> jsons,
            ResourceManager manager,
            ProfilerFiller profiler) {

        DEFINITIONS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                QualityAttributeDefinition def = parse(json);
                DEFINITIONS.put(entry.getKey(), def);
                OvergearedMod.LOGGER.debug("Loaded quality attribute: {}", entry.getKey());
            } catch (Exception e) {
                OvergearedMod.LOGGER.error("Failed to parse quality attribute {}: {}", entry.getKey(), e.getMessage());
            }
        }

        OvergearedMod.LOGGER.info("Loaded {} quality attribute definitions", DEFINITIONS.size());
    }

    public static List<QualityAttributeDefinition> getAll() {
        return new ArrayList<>(DEFINITIONS.values());
    }

    public static Optional<QualityAttributeDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static void clear() {
        DEFINITIONS.clear();
    }

    private static QualityAttributeDefinition parse(JsonObject json) {
        // ---- attribute ----
        ResourceLocation attributeId = ResourceLocation.parse(
                GsonHelper.getAsString(json, "attribute"));

        // ---- targets ----
        List<QualityTarget> targets = new ArrayList<>();
        JsonArray targetsJson = GsonHelper.getAsJsonArray(json, "targets");

        for (JsonElement elem : targetsJson) {
            JsonObject obj = elem.getAsJsonObject();

            QualityTarget.TargetType type = QualityTarget.TargetType
                    .valueOf(GsonHelper.getAsString(obj, "type").toUpperCase());

            ResourceLocation id = obj.has("id")
                    ? ResourceLocation.parse(GsonHelper.getAsString(obj, "id"))
                    : null;

            targets.add(new QualityTarget(type, id));
        }

        // ---- qualities ----
        Map<String, QualityValue> qualities = new HashMap<>();
        JsonObject qualitiesJson = GsonHelper.getAsJsonObject(json, "qualities");

        for (Map.Entry<String, JsonElement> entry : qualitiesJson.entrySet()) {
            String quality = entry.getKey();
            JsonObject value = entry.getValue().getAsJsonObject();

            String opString = GsonHelper.getAsString(value, "operation")
                    .toLowerCase(Locale.ROOT);

            AttributeModifier.Operation operation = getOperation(opString);

            double amount = GsonHelper.getAsDouble(value, "amount");

            qualities.put(quality, new QualityValue(operation, amount));
        }

        return new QualityAttributeDefinition(
                attributeId,
                targets,
                qualities);
    }

    private static AttributeModifier.@NotNull Operation getOperation(String opString) {
        return switch (opString) {
            case "add" -> AttributeModifier.Operation.ADD_VALUE;
            case "mult_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "mult_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> throw new JsonSyntaxException(
                    "Unknown operation: " + opString +
                            ". Valid values: add, mult_base, mult_total");
        };
    }
}