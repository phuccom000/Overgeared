package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RockInteractionReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final RockInteractionReloadListener INSTANCE = new RockInteractionReloadListener();
    private static final Gson GSON = new Gson();

    private static final Map<Identifier, RockInteractionData> DATA = new ConcurrentHashMap<>();

    public RockInteractionReloadListener() {
        super(GSON, "rock_interactions");
    }

    @Override
    public Identifier getFabricId() {
        return Overgeared.id("rock_interactions_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons, ResourceManager manager, Profiler profiler) {
        DATA.clear();

        for (Map.Entry<Identifier, JsonElement> entry : jsons.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement value = entry.getValue();

            try {
                // Handle single object or array of objects
                if (value.isJsonObject()) {
                    parseAndAddRockInteraction(id, value.getAsJsonObject());
                } else if (value.isJsonArray()) {
                    JsonArray array = value.getAsJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        JsonElement element = array.get(i);
                        if (!element.isJsonObject()) {
                            throw new JsonParseException("Expected object in array at index " + i);
                        }

                        // Create a synthetic ID for each array entry
                        Identifier entryId = new Identifier(id.getNamespace(), id.getPath() + "_" + i);

                        parseAndAddRockInteraction(entryId, element.getAsJsonObject());
                    }
                } else {
                    throw new JsonParseException("Expected object or array");
                }

            } catch (Exception e) {
                Overgeared.LOGGER.error("Failed to load rock interaction {}: {}", id, e.getMessage());
            }
        }

        if (DATA.isEmpty()) {
            Overgeared.LOGGER
                    .warn("No valid rock interactions found in datapacks. Using default config interaction.");
            addDefaultInteraction();
        } else {
            Overgeared.LOGGER.info("Loaded {} rock interactions from datapacks", DATA.size());
        }
    }

    private void parseAndAddRockInteraction(Identifier id, JsonObject obj) {
        // ---------- BLOCKS ----------
        Identifier inputId = Identifier.tryParse(JsonHelper.getString(obj, "input_block"));
        Block inputBlock = Registries.BLOCK.get(inputId);
        if (inputBlock == null || inputBlock == Blocks.AIR)
            throw new JsonParseException("Unknown input_block '" + inputId + "'");

        Identifier resultId = Identifier.tryParse(JsonHelper.getString(obj, "result_block"));
        Block resultBlock = Registries.BLOCK.get(resultId);
        if (resultBlock == null || resultBlock == Blocks.AIR)
            throw new JsonParseException("Unknown result_block '" + resultId + "'");

        // ---------- TOOLS ----------
        List<RockInteractionData.ToolEntry> tools = new ArrayList<>();
        JsonArray toolsArray = JsonHelper.getArray(obj, "tools");

        for (JsonElement toolEl : toolsArray) {
            JsonObject toolObj = toolEl.getAsJsonObject();

            Ingredient ingredient;

            if (toolObj.has("item")) {
                JsonObject ingObj = new JsonObject();
                ingObj.addProperty("item", JsonHelper.getString(toolObj, "item"));
                ingredient = Ingredient.fromJson(ingObj);

            } else if (toolObj.has("tag")) {
                JsonObject ingObj = new JsonObject();
                ingObj.addProperty("tag", JsonHelper.getString(toolObj, "tag"));
                ingredient = Ingredient.fromJson(ingObj);

            } else {
                throwMissing(id, "Tool must have 'item' or 'tag'");
                return; // unreachable but required
            }

            Identifier dropId = Identifier.tryParse(JsonHelper.getString(toolObj, "drop_item"));
            Item dropItem = Registries.ITEM.get(dropId);
            if (dropItem == null || dropItem == Items.AIR)
                throw new JsonParseException("Unknown drop_item '" + dropId + "'");

            float dropChance = JsonHelper.getFloat(toolObj, "drop_chance");
            float breakChance = JsonHelper.getFloat(toolObj, "break_chance");

            tools.add(new RockInteractionData.ToolEntry(ingredient, new ItemStack(dropItem), dropChance, breakChance));
        }

        RockInteractionData data = new RockInteractionData(inputBlock, tools, resultBlock);
        DATA.put(id, data);
    }

    public Collection<RockInteractionData> getAll() {
        return DATA.values();
    }

    private static <T> T throwMissing(Identifier id, String msg) {
        throw new JsonParseException("Error in " + id + ": " + msg);
    }

    private void addDefaultInteraction() {
        if (!ServerConfig.GET_ROCK_USING_FLINT.get())
            return;
        Block inputBlock = Blocks.STONE;
        Block resultBlock = Blocks.COBBLESTONE;

        Ingredient flint = Ingredient.ofItems(Items.FLINT);

        ItemStack drop = new ItemStack(ModItems.ROCK);

        float dropChance = ServerConfig.ROCK_DROPPING_CHANCE.get().floatValue();
        float breakChance = ServerConfig.FLINT_BREAKING_CHANCE.get().floatValue();

        List<RockInteractionData.ToolEntry> tools = List.of(
                new RockInteractionData.ToolEntry(flint, drop, dropChance, breakChance));

        RockInteractionData data = new RockInteractionData(inputBlock, tools, resultBlock);

        Identifier id = new Identifier(Overgeared.MOD_ID, "default_flint_on_stone");
        DATA.put(id, data);

        Overgeared.LOGGER.info("Loaded default rock interaction (flint -> stone)");
    }

}
