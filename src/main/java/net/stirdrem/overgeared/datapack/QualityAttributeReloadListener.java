package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QualityAttributeReloadListener
        extends SimpleJsonResourceReloadListener {

    public static final QualityAttributeReloadListener INSTANCE =
            new QualityAttributeReloadListener();

    private static final List<QualityAttributeDefinition> definitions = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public QualityAttributeReloadListener() {
        super(new Gson(), "quality_attributes");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> jsons,
            ResourceManager manager,
            ProfilerFiller profiler
    ) {
        definitions.clear();

        for (JsonElement element : jsons.values()) {
            definitions.add(parse(element.getAsJsonObject()));
        }
        OvergearedMod.LOGGER.info(
                "Loaded {} quality attribute files", jsons.size()
        );
    }

    public List<QualityAttributeDefinition> getAll() {
        return definitions;
    }

    private static QualityAttributeDefinition parse(JsonObject json) {

        // ---- attribute ----
        ResourceLocation attributeId = ResourceLocation.parse(
                GsonHelper.getAsString(json, "attribute")
        );

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

            AttributeModifier.Operation operation =
                    AttributeModifier.Operation.valueOf(
                            GsonHelper.getAsString(value, "operation")
                                    .toUpperCase()
                    );

            double amount = GsonHelper.getAsDouble(value, "amount");

            qualities.put(quality, new QualityValue(operation, amount));
        }

        return new QualityAttributeDefinition(
                attributeId,
                targets,
                qualities
        );
    }

}
