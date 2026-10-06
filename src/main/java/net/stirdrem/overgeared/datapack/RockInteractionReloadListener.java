package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RockInteractionReloadListener extends OvergearedJsonReloadListener {

    public static final RockInteractionReloadListener INSTANCE = new RockInteractionReloadListener();

    private static final Map<Identifier, RockInteractionData> DATA = new ConcurrentHashMap<>();

    public RockInteractionReloadListener() {
        super("rock_interactions");
    }
    public Identifier getFabricId() {
        return Overgeared.id("rock_interactions_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
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
                        Identifier entryId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_" + i);

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
        Identifier inputId = Identifier.tryParse(GsonHelper.getAsString(obj, "input_block"));
        Block inputBlock = BuiltInRegistries.BLOCK.getValue(inputId);
        if (inputBlock == null || inputBlock == Blocks.AIR)
            throw new JsonParseException("Unknown input_block '" + inputId + "'");

        Identifier resultId = Identifier.tryParse(GsonHelper.getAsString(obj, "result_block"));
        Block resultBlock = BuiltInRegistries.BLOCK.getValue(resultId);
        if (resultBlock == null || resultBlock == Blocks.AIR)
            throw new JsonParseException("Unknown result_block '" + resultId + "'");

        // ---------- TOOLS ----------
        List<RockInteractionData.ToolEntry> tools = new ArrayList<>();
        JsonArray toolsArray = GsonHelper.getAsJsonArray(obj, "tools");

        for (JsonElement toolEl : toolsArray) {
            JsonObject toolObj = toolEl.getAsJsonObject();

            Ingredient ingredient;

            if (toolObj.has("item")) {
                JsonObject ingObj = new JsonObject();
                ingObj.addProperty("item", GsonHelper.getAsString(toolObj, "item"));
                ingredient = parseIngredient(ingObj);

            } else if (toolObj.has("tag")) {
                JsonObject ingObj = new JsonObject();
                ingObj.addProperty("tag", GsonHelper.getAsString(toolObj, "tag"));
                ingredient = parseIngredient(ingObj);

            } else {
                throwMissing(id, "Tool must have 'item' or 'tag'");
                return; // unreachable but required
            }

            Identifier dropId = Identifier.tryParse(GsonHelper.getAsString(toolObj, "drop_item"));
            Item dropItem = BuiltInRegistries.ITEM.getValue(dropId);
            if (dropItem == null || dropItem == Items.AIR)
                throw new JsonParseException("Unknown drop_item '" + dropId + "'");

            float dropChance = GsonHelper.getAsFloat(toolObj, "drop_chance");
            float breakChance = GsonHelper.getAsFloat(toolObj, "break_chance");

            tools.add(new RockInteractionData.ToolEntry(ingredient, new ItemStackTemplate(dropItem), dropChance, breakChance));
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

        Ingredient flint = Ingredient.of(Items.FLINT);

        ItemStackTemplate drop = new ItemStackTemplate(ModItems.ROCK);

        float dropChance = ServerConfig.ROCK_DROPPING_CHANCE.get().floatValue();
        float breakChance = ServerConfig.FLINT_BREAKING_CHANCE.get().floatValue();

        List<RockInteractionData.ToolEntry> tools = List.of(
                new RockInteractionData.ToolEntry(flint, drop, dropChance, breakChance));

        RockInteractionData data = new RockInteractionData(inputBlock, tools, resultBlock);

        Identifier id = Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "default_flint_on_stone");
        DATA.put(id, data);

        Overgeared.LOGGER.info("Loaded default rock interaction (flint -> stone)");
    }

}
