package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class QualityAttributeReloadListener extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

    public static final QualityAttributeReloadListener INSTANCE =
            new QualityAttributeReloadListener();

    private static final List<QualityAttributeDefinition> definitions = new ArrayList<>();

    public QualityAttributeReloadListener() {
        super(new Gson(), "quality_attributes");
    }

    private static final Set<Item> cachedItems = new HashSet<>();

    @Override
    public ResourceLocation getFabricId() {
        return Overgeared.id("quality_attributes_listener");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                          ResourceManager manager,
                          ProfilerFiller profiler) {

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
        ResourceLocation attributeId = ResourceLocation.tryParse(
                GsonHelper.getAsString(json, "attribute")
        );

        // ---- targets ----
        List<QualityTarget> targets = new ArrayList<>();
        JsonArray targetsJson = GsonHelper.getAsJsonArray(json, "targets");

        for (JsonElement elem : targetsJson) {
            JsonObject obj = elem.getAsJsonObject();

            QualityTarget.TargetType type = QualityTarget.TargetType
                    .valueOf(GsonHelper.getAsString(obj, "type").toUpperCase(Locale.ROOT));

            ResourceLocation id = obj.has("id")
                    ? ResourceLocation.tryParse(GsonHelper.getAsString(obj, "id"))
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
        AttributeModifier.Operation operation;

        switch (opString) {
            case "add" -> operation = AttributeModifier.Operation.ADDITION;
            case "mult_base" -> operation = AttributeModifier.Operation.MULTIPLY_BASE;
            case "mult_total" -> operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
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
                            Item item = BuiltInRegistries.ITEM.get(target.id());
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }

                    case ITEM_TAG -> {
                        if (target.id() != null) {
                            TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), target.id());

                            for (Holder<Item> entry : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                                items.add(entry.value());
                            }
                        }
                    }

                    case WEAPON -> {
                        for (Item item : BuiltInRegistries.ITEM) {
                            if (item instanceof TieredItem ||
                                    item instanceof ProjectileWeaponItem) {
                                items.add(item);
                            }
                        }
                    }

                    case ARMOR -> {
                        for (Item item : BuiltInRegistries.ITEM) {
                            if (item instanceof ArmorItem) {
                                items.add(item);
                            }
                        }
                    }

                    case ITEM_ALL -> BuiltInRegistries.ITEM.forEach(items::add);
                }
            }
        }

        return items;
    }

    public Set<Item> getAllItems() {
        return cachedItems;
    }
}
