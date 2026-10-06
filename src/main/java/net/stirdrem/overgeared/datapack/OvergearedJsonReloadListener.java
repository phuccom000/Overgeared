package net.stirdrem.overgeared.datapack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 26.3 port: shared base for the mod's custom JSON datapack folders. Vanilla's
 * SimpleJsonResourceReloadListener is now codec based; we keep the old "raw JsonElement" shape
 * (codec = ExtraCodecs.JSON) so every subclass keeps its hand-written parser.
 *
 * <p>Registered through Fabric's resource-loader-v1 ({@code ResourceLoader.get(SERVER_DATA)
 * .registerReloadListener(getFabricId(), listener)}). The registry lookup of the reload in progress
 * is captured in {@link #prepareSharedState} so ingredients/tags can be resolved.
 */
public abstract class OvergearedJsonReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {

    protected HolderLookup.@Nullable Provider registries;

    protected OvergearedJsonReloadListener(String directory) {
        super(ExtraCodecs.JSON, FileToIdConverter.json(directory));
    }

    /** Id used to register this listener with Fabric. */
    public abstract Identifier getFabricId();

    @Override
    public void prepareSharedState(SharedState currentReload) {
        try {
            this.registries = currentReload.get(ResourceLoader.REGISTRY_LOOKUP_KEY);
        } catch (NullPointerException e) {
            this.registries = null;
        }
    }

    protected HolderGetter<Item> itemLookup() {
        return registries != null ? registries.lookupOrThrow(Registries.ITEM) : BuiltInRegistries.ITEM;
    }

    /**
     * Parses an ingredient in either the legacy 1.20 shape ({"item": id}, {"tag": id}, or an array
     * of those) or the 1.21+ shape ("id", "#tag", or ["id", ...]).
     */
    protected Ingredient parseIngredient(JsonElement element) {
        return parseIngredient(element, itemLookup());
    }

    public static Ingredient parseIngredient(JsonElement element, HolderGetter<Item> items) {
        if (element == null || element.isJsonNull()) {
            throw new JsonSyntaxException("Missing ingredient");
        }
        if (element.isJsonPrimitive()) {
            String s = element.getAsString();
            if (s.startsWith("#")) {
                return Ingredient.of(items.getOrThrow(TagKey.create(Registries.ITEM, Identifier.parse(s.substring(1)))));
            }
            return Ingredient.of(parseItem(s));
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("item")) {
                return Ingredient.of(parseItem(obj.get("item").getAsString()));
            }
            if (obj.has("tag")) {
                return Ingredient.of(items.getOrThrow(TagKey.create(Registries.ITEM, Identifier.parse(obj.get("tag").getAsString()))));
            }
            throw new JsonSyntaxException("Ingredient object needs an 'item' or 'tag' entry");
        }
        JsonArray array = element.getAsJsonArray();
        if (array.isEmpty()) {
            throw new JsonSyntaxException("Ingredient array cannot be empty");
        }
        if (array.size() == 1) {
            return parseIngredient(array.get(0), items);
        }
        List<Holder<Item>> holders = new ArrayList<>();
        for (JsonElement e : array) {
            parseIngredient(e, items).items().forEach(holders::add);
        }
        return Ingredient.of(HolderSet.direct(holders));
    }

    public static Item parseItem(String id) {
        Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
        if (item == null || item == Items.AIR) {
            throw new JsonSyntaxException("Unknown item '" + id + "'");
        }
        return item;
    }

    /** Replacement for the removed Ingredient#getItems(). */
    public static ItemStack[] stacksOf(Ingredient ingredient) {
        return ingredient.items().map(h -> new ItemStack(h.value())).toArray(ItemStack[]::new);
    }
}
