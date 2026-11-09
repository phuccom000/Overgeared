package net.stirdrem.overgeared.datapack;
import net.stirdrem.overgeared.util.ItemKinds;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class QualityAttributeReloadListener
        extends JsonDataReloadListener {

    public static final QualityAttributeReloadListener INSTANCE =
            new QualityAttributeReloadListener();

    private static final List<QualityAttributeDefinition> definitions = new ArrayList<>();

    public QualityAttributeReloadListener() {
        super("quality_attributes");
    }

    /**
     * Built on first use: item components and the new tags are only bound after the reload, so the
     * items cannot be classified (tool / armor / tag) while the listener runs.
     */
    private static volatile Set<Item> cachedItems;

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons,
                         ResourceManager manager,
                         ProfilerFiller profiler) {

        definitions.clear();
        cachedItems = null;

        for (JsonElement element : jsons.values()) {
            QualityAttributeDefinition def = parse(element.getAsJsonObject());
            definitions.add(def);
        }

        OvergearedMod.LOGGER.info("Loaded {} quality attribute files", jsons.size());
    }

    public static List<QualityAttributeDefinition> getAll() {
        return definitions;
    }

    private static QualityAttributeDefinition parse(JsonObject json) {

        // ---- attribute ----
        Identifier attributeId = Identifier.tryParse(
                GsonHelper.getAsString(json, "attribute")
        );

        // ---- targets ----
        List<QualityTarget> targets = new ArrayList<>();
        JsonArray targetsJson = GsonHelper.getAsJsonArray(json, "targets");

        for (JsonElement elem : targetsJson) {
            JsonObject obj = elem.getAsJsonObject();

            QualityTarget.TargetType type = QualityTarget.TargetType
                    .valueOf(GsonHelper.getAsString(obj, "type").toUpperCase(Locale.ROOT));

            Identifier id = obj.has("id")
                    ? Identifier.tryParse(GsonHelper.getAsString(obj, "id"))
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
                qualities
        );
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

    public static Set<Item> resolveItems() {
        Set<Item> items = new HashSet<>();

        for (QualityAttributeDefinition def : INSTANCE.getAll()) {
            for (QualityTarget target : def.targets()) {

                switch (target.type()) {

                    case ITEM -> {
                        if (target.id() != null) {
                            Item item = BuiltInRegistries.ITEM.getValue(target.id());
                            items.add(item);
                        }
                    }

                    case ITEM_TAG -> {
                        if (target.id() != null) {
                            TagKey<Item> tag = TagKey.create(Registries.ITEM, target.id());
                            BuiltInRegistries.ITEM.listElements()
                                    .filter(holder -> holder.is(tag))
                                    .forEach(holder -> items.add(holder.value()));
                        }
                    }

                    case WEAPON -> {
                        BuiltInRegistries.ITEM.stream().forEach(item -> {
                            if (ItemKinds.isTiered(item) ||
                                    item instanceof ProjectileWeaponItem) {
                                items.add(item);
                            }
                        });
                    }

                    case ARMOR -> {
                        BuiltInRegistries.ITEM.stream().forEach(item -> {
                            if (ItemKinds.isArmor(item)) {
                                items.add(item);
                            }
                        });
                    }

                    case ITEM_ALL -> {
                        BuiltInRegistries.ITEM.stream().forEach(items::add);
                    }
                }
            }
        }

        return items;
    }

    public Set<Item> getAllItems() {
        Set<Item> items = cachedItems;
        if (items == null) {
            items = resolveItems();
            cachedItems = items;
        }
        return items;
    }
}
