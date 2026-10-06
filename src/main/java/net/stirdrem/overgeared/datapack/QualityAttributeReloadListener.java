package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class QualityAttributeReloadListener extends OvergearedJsonReloadListener {

    public static final QualityAttributeReloadListener INSTANCE =
            new QualityAttributeReloadListener();

    private static final List<QualityAttributeDefinition> definitions = new ArrayList<>();

    public QualityAttributeReloadListener() {
        super("quality_attributes");
    }

    private static final Set<Item> cachedItems = new HashSet<>();
    // 26.3: item default components are bound only after reload listeners finish, so the WEAPON /
    // ARMOR targets (which inspect components) are resolved lazily on first use after a reload.
    private static boolean cacheDirty = true;
    public Identifier getFabricId() {
        return Overgeared.id("quality_attributes_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons,
                          ResourceManager manager,
                          ProfilerFiller profiler) {

        definitions.clear();
        cachedItems.clear();

        for (JsonElement element : jsons.values()) {
            QualityAttributeDefinition def = parse(element.getAsJsonObject());
            definitions.add(def);
        }

        cacheDirty = true;

        Overgeared.LOGGER.info("Loaded {} quality attribute files", jsons.size());
    }

    public List<QualityAttributeDefinition> getAll() {
        return definitions;
    }

    private static QualityAttributeDefinition parse(JsonObject json) {

        // ---- attribute ----
        Identifier attributeId = Identifier.tryParse(
                GsonHelper.getAsString(json, "attribute")
        );
        // 26.3 port: 1.21 dropped the "generic." / "player." prefixes from attribute ids; accept old packs.
        if (attributeId != null && attributeId.getNamespace().equals("minecraft")) {
            String path = attributeId.getPath();
            if (path.startsWith("generic.")) attributeId = Identifier.withDefaultNamespace(path.substring("generic.".length()));
            else if (path.startsWith("player.")) attributeId = Identifier.withDefaultNamespace(path.substring("player.".length()));
        }

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
        AttributeModifier.Operation operation;

        switch (opString) {
            case "add" -> operation = AttributeModifier.Operation.ADD_VALUE;
            case "mult_base" -> operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "mult_total" -> operation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
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
                            BuiltInRegistries.ITEM.getOptional(target.id()).ifPresent(items::add);
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
                            if (isWeaponItem(item)) {
                                items.add(item);
                            }
                        }
                    }

                    case ARMOR -> {
                        for (Item item : BuiltInRegistries.ITEM) {
                            if (isArmorItem(item)) {
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

    public synchronized Set<Item> getAllItems() {
        if (cacheDirty) {
            cachedItems.clear();
            cachedItems.addAll(resolveItems());
            cacheDirty = false;
        }
        return cachedItems;
    }

    /**
     * 26.3 port: TieredItem (swords + digger tools) no longer exists. Tools and weapons are now
     * identified by their default TOOL / WEAPON components; bows/crossbows are still ProjectileWeaponItem.
     */
    public static boolean isWeaponItem(Item item) {
        if (item instanceof ProjectileWeaponItem) return true;
        if (item instanceof ShearsItem) return false;
        var components = item.components();
        return components.has(DataComponents.WEAPON) || components.has(DataComponents.TOOL);
    }

    /**
     * 26.3 port: ArmorItem no longer exists. Armor = equippable in a humanoid armor slot and granting armor.
     */
    public static boolean isArmorItem(Item item) {
        var components = item.components();
        Equippable equippable = components.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return false;
        ItemAttributeModifiers modifiers = components.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return modifiers.modifiers().stream().anyMatch(e -> e.attribute().is(Attributes.ARMOR));
    }
}
