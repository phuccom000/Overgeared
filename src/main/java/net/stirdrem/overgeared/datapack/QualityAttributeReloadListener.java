package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class QualityAttributeReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final QualityAttributeReloadListener INSTANCE =
            new QualityAttributeReloadListener();

    private static final List<QualityAttributeDefinition> definitions = new ArrayList<>();

    public QualityAttributeReloadListener() {
        super(new Gson(), "quality_attributes");
    }

    private static final Set<Item> cachedItems = new HashSet<>();

    @Override
    public Identifier getFabricId() {
        return Overgeared.id("quality_attributes_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons,
                          ResourceManager manager,
                          Profiler profiler) {

        definitions.clear();
        cachedItems.clear();

        for (JsonElement element : jsons.values()) {
            QualityAttributeDefinition def = parse(element.getAsJsonObject());
            definitions.add(def);
        }

        // build cache
        cachedItems.addAll(resolveItems());

        Overgeared.LOGGER.info("Loaded {} quality attribute files", jsons.size());
    }

    public List<QualityAttributeDefinition> getAll() {
        return definitions;
    }

    private static QualityAttributeDefinition parse(JsonObject json) {

        // ---- attribute ----
        Identifier attributeId = Identifier.tryParse(
                JsonHelper.getString(json, "attribute")
        );

        // ---- targets ----
        List<QualityTarget> targets = new ArrayList<>();
        JsonArray targetsJson = JsonHelper.getArray(json, "targets");

        for (JsonElement elem : targetsJson) {
            JsonObject obj = elem.getAsJsonObject();

            QualityTarget.TargetType type = QualityTarget.TargetType
                    .valueOf(JsonHelper.getString(obj, "type").toUpperCase(Locale.ROOT));

            Identifier id = obj.has("id")
                    ? Identifier.tryParse(JsonHelper.getString(obj, "id"))
                    : null;

            targets.add(new QualityTarget(type, id));
        }

        // ---- qualities ----
        Map<String, QualityValue> qualities = new HashMap<>();
        JsonObject qualitiesJson = JsonHelper.getObject(json, "qualities");

        for (Map.Entry<String, JsonElement> entry : qualitiesJson.entrySet()) {
            String quality = entry.getKey();
            JsonObject value = entry.getValue().getAsJsonObject();

            String opString = JsonHelper.getString(value, "operation")
                    .toLowerCase(Locale.ROOT);

            EntityAttributeModifier.Operation operation = getOperation(opString);

            double amount = JsonHelper.getDouble(value, "amount");

            qualities.put(quality, new QualityValue(operation, amount));
        }

        return new QualityAttributeDefinition(
                attributeId,
                targets,
                qualities
        );
    }

    private static EntityAttributeModifier.@NotNull Operation getOperation(String opString) {
        EntityAttributeModifier.Operation operation;

        switch (opString) {
            case "add" -> operation = EntityAttributeModifier.Operation.ADDITION;
            case "mult_base" -> operation = EntityAttributeModifier.Operation.MULTIPLY_BASE;
            case "mult_total" -> operation = EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> throw new JsonSyntaxException(
                    "Unknown operation: " + opString +
                            ". Valid values: add, mult_base, mult_total"
            );
        }
        return operation;
    }

    public static Set<Item> resolveItems() {
        Set<Item> items = new HashSet<>();

        for (QualityAttributeDefinition def : INSTANCE.getAll()) {
            for (QualityTarget target : def.targets()) {

                switch (target.type()) {

                    case ITEM -> {
                        if (target.id() != null) {
                            Item item = Registries.ITEM.get(target.id());
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }

                    case ITEM_TAG -> {
                        if (target.id() != null) {
                            TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), target.id());

                            for (RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(tag)) {
                                items.add(entry.value());
                            }
                        }
                    }

                    case WEAPON -> {
                        for (Item item : Registries.ITEM) {
                            if (item instanceof ToolItem ||
                                    item instanceof RangedWeaponItem) {
                                items.add(item);
                            }
                        }
                    }

                    case ARMOR -> {
                        for (Item item : Registries.ITEM) {
                            if (item instanceof ArmorItem) {
                                items.add(item);
                            }
                        }
                    }

                    case ITEM_ALL -> Registries.ITEM.forEach(items::add);
                }
            }
        }

        return items;
    }

    public Set<Item> getAllItems() {
        return cachedItems;
    }
}
