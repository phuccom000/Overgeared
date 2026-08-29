package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.stirdrem.overgeared.Overgeared;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DurabilityBlacklistReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Map<Identifier, Ingredient> DATA = new ConcurrentHashMap<>();
    public static final DurabilityBlacklistReloadListener INSTANCE = new DurabilityBlacklistReloadListener();
    private static final Gson GSON = new Gson();

    public DurabilityBlacklistReloadListener() {
        super(GSON, "durability_blacklist");
    }

    @Override
    public Identifier getFabricId() {
        return Overgeared.id("durability_blacklist_listener");
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
                    Ingredient ingredient = parseIngredient(json);
                    DATA.put(id, ingredient);
                } else {
                    throw new JsonSyntaxException("Expected JSON object for durability multiplier blacklist entry: " + id);
                }
            } catch (Exception e) {
                Overgeared.LOGGER.error("Failed to parse durability multiplier blacklist entry: {}", id, e);
            }
        }

        Overgeared.LOGGER.info("Loaded {} durability blacklist entries", DATA.size());
    }

    private Ingredient parseIngredient(JsonObject json) {
        if (!json.has("item")) {
            throw new JsonSyntaxException("Missing 'item' for durability multiplier blacklist entry");
        }

        JsonElement itemElement = json.get("item");
        return Ingredient.fromJson(itemElement);
    }

    public static Map<Identifier, Ingredient> getData() {
        return Collections.unmodifiableMap(DATA);
    }

    public static List<Ingredient> getAllIngredients() {
        return List.copyOf(DATA.values());
    }

    public static List<ItemStack> getAllBlacklistedItems() {
        List<ItemStack> allItems = new ArrayList<>();
        for (Ingredient ingredient : DATA.values()) {
            ItemStack[] stacks = ingredient.getMatchingStacks();
            if (stacks.length > 0) {
                allItems.addAll(Arrays.asList(stacks));
            }
        }
        return allItems;
    }

    public static boolean isBlacklisted(ItemStack stack) {
        for (Ingredient ingredient : DATA.values()) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        DATA.clear();
    }
}
