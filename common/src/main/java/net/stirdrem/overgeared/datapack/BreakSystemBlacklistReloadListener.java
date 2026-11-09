package net.stirdrem.overgeared.datapack;
import net.stirdrem.overgeared.recipe.ingredients.Ingredients;
import net.stirdrem.overgeared.recipe.ingredients.IngredientCodecs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BreakSystemBlacklistReloadListener extends JsonDataReloadListener {

    private static final Map<Identifier, Ingredient> DATA = new ConcurrentHashMap<>();
    public static final BreakSystemBlacklistReloadListener INSTANCE = new BreakSystemBlacklistReloadListener();
    private static final Gson GSON = new Gson();

    public BreakSystemBlacklistReloadListener() {
        super("broken_blacklist");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        System.out.println("Found " + resources.size() + " broken blacklist resources:");
        for (Identifier id : resources.keySet()) {
            System.out.println(" - " + id);
        }
        for (Map.Entry<Identifier, JsonElement> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();
                    Ingredient ingredient = parseIngredient(json);
                    DATA.put(id, ingredient);
                } else {
                    throw new JsonSyntaxException("Expected JSON object for broken blacklist entry: " + id);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse broken blacklist entry: " + id + ", error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Loaded " + DATA.size() + " broken blacklist entries");

        // Debug: print all loaded ingredients
        for (Map.Entry<Identifier, Ingredient> entry : DATA.entrySet()) {
            System.out.println("Blacklist entry: " + entry.getKey() + " -> " + Ingredients.stacks(entry.getValue()));
        }
    }

    private Ingredient parseIngredient(JsonObject json) {
        if (!json.has("item")) {
            throw new JsonSyntaxException("Missing 'item' for broken blacklist entry");
        }

        JsonElement element = json.get("item");

        return IngredientCodecs.LENIENT
                .parse(jsonOps(), element)
                .getOrThrow(JsonSyntaxException::new);
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
            ItemStack[] stacks = Ingredients.stackArray(ingredient);
            if (stacks.length > 0) {
                Collections.addAll(allItems, stacks);
            }
        }
        return allItems;
    }

    public static boolean isBlacklisted(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

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