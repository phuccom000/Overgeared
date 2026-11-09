package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.Overgeared;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// NOTE: in the original Forge mod this listener is never actually registered with
// AddReloadListenerEvent (see ReloadListenerRegistry), so DATA stays permanently empty
// and BrokenHelper.isBlacklisted() always returns false upstream. Ported as-is (including
// leaving it unregistered in ReloadListenerRegistry) to match that real upstream behavior.
public class BreakSystemBlacklistReloadListener extends OvergearedJsonReloadListener {

    private static final Map<Identifier, Ingredient> DATA = new ConcurrentHashMap<>();
    public static final BreakSystemBlacklistReloadListener INSTANCE = new BreakSystemBlacklistReloadListener();
    private static final Gson GSON = new Gson();

    public BreakSystemBlacklistReloadListener() {
        super("broken_blacklist");
    }
    public Identifier getFabricId() {
        return Overgeared.id("break_system_blacklist_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        Overgeared.LOGGER.info("Found {} broken blacklist resources", resources.size());

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
                Overgeared.LOGGER.error("Failed to parse broken blacklist entry: {}", id, e);
            }
        }

        Overgeared.LOGGER.info("Loaded {} broken blacklist entries", DATA.size());
    }

    private Ingredient parseIngredient(JsonObject json) {
        if (!json.has("item")) {
            throw new JsonSyntaxException("Missing 'item' for broken blacklist entry");
        }

        JsonElement itemElement = json.get("item");
        return parseIngredient(itemElement);
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
            ItemStack[] stacks = stacksOf(ingredient);
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
