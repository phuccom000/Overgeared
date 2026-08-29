package net.stirdrem.overgeared.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.config.ConfigSpec;
import net.stirdrem.overgeared.config.ServerConfig;

/**
 * Mod Menu / Cloth Config screen covering every ServerConfig/ClientConfig entry. The original
 * Forge mod built an equivalent screen (OvergearedConfigScreen) but never actually wired its
 * registration up (commented out in ClientInit), so this is a fresh implementation against
 * this port's ConfigSpec (a flat-JSON reimplementation of ForgeConfigSpec, see ConfigSpec.java)
 * rather than a port of that dead code.
 *
 * The two nested-list settings (castingToolTypes, materialSetting) aren't exposed here - Cloth
 * Config's list widgets only support flat String/number lists, and those two are lists of
 * [String, Number] / [String, String, Number] tuples. They stay JSON-file-only.
 */
public class OvergearedConfigScreen implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return OvergearedConfigScreen::buildScreen;
    }

    private static net.minecraft.client.gui.screen.Screen buildScreen(net.minecraft.client.gui.screen.Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("Overgeared Config"))
                .setSavingRunnable(() -> {
                    ServerConfig.SERVER_CONFIG.save();
                    ClientConfig.CLIENT_CONFIG.save();
                });

        ConfigEntryBuilder eb = builder.entryBuilder();

        buildGeneral(builder, eb);
        buildAnvilConversion(builder, eb);
        buildStoneAnvil(builder, eb);
        buildHeatedItems(builder, eb);
        buildArrowFletching(builder, eb);
        buildMinigameCommon(builder, eb);
        buildForgingZone(builder, eb, "Default (No Blueprint)",
                ServerConfig.DEFAULT_ZONE_STARTING_SIZE, ServerConfig.DEFAULT_ZONE_SHRINK_FACTOR,
                ServerConfig.DEFAULT_MIN_PERFECT_ZONE, ServerConfig.DEFAULT_ARROW_SPEED,
                ServerConfig.DEFAULT_ARROW_SPEED_INCREASE, ServerConfig.DEFAULT_MAX_ARROW_SPEED);
        buildForgingZone(builder, eb, "Poorly Forged",
                ServerConfig.POOR_ZONE_STARTING_SIZE, ServerConfig.POOR_ZONE_SHRINK_FACTOR,
                ServerConfig.POOR_MIN_PERFECT_ZONE, ServerConfig.POOR_ARROW_SPEED,
                ServerConfig.POOR_ARROW_SPEED_INCREASE, ServerConfig.POOR_MAX_ARROW_SPEED);
        buildForgingZone(builder, eb, "Well Forged",
                ServerConfig.WELL_ZONE_STARTING_SIZE, ServerConfig.WELL_ZONE_SHRINK_FACTOR,
                ServerConfig.WELL_MIN_PERFECT_ZONE, ServerConfig.WELL_ARROW_SPEED,
                ServerConfig.WELL_ARROW_SPEED_INCREASE, ServerConfig.WELL_MAX_ARROW_SPEED);
        buildForgingZone(builder, eb, "Expertly Forged",
                ServerConfig.EXPERT_ZONE_STARTING_SIZE, ServerConfig.EXPERT_ZONE_SHRINK_FACTOR,
                ServerConfig.EXPERT_MIN_PERFECT_ZONE, ServerConfig.EXPERT_ARROW_SPEED,
                ServerConfig.EXPERT_ARROW_SPEED_INCREASE, ServerConfig.EXPERT_MAX_ARROW_SPEED);
        buildForgingZone(builder, eb, "Perfectly Forged",
                ServerConfig.PERFECT_ZONE_STARTING_SIZE, ServerConfig.PERFECT_ZONE_SHRINK_FACTOR,
                ServerConfig.PERFECT_MIN_PERFECT_ZONE, ServerConfig.PERFECT_ARROW_SPEED,
                ServerConfig.PERFECT_ARROW_SPEED_INCREASE, ServerConfig.PERFECT_MAX_ARROW_SPEED);
        buildForgingZone(builder, eb, "Masterwork",
                ServerConfig.MASTER_ZONE_STARTING_SIZE, ServerConfig.MASTER_ZONE_SHRINK_FACTOR,
                ServerConfig.MASTER_MIN_PERFECT_ZONE, ServerConfig.MASTER_ARROW_SPEED,
                ServerConfig.MASTER_ARROW_SPEED_INCREASE, ServerConfig.MASTER_MAX_ARROW_SPEED);
        buildDurabilityGrinding(builder, eb);
        buildQualityFailureChances(builder, eb);
        buildBlueprintToolTypes(builder, eb);
        buildDurabilityBonuses(builder, eb);
        buildMiningSpeedBonuses(builder, eb);
        buildItemBreakChance(builder, eb);
        buildKnapping(builder, eb);
        buildLootQuality(builder, eb);
        buildCasting(builder, eb);
        buildClient(builder, eb);

        return builder.build();
    }

    private static void buildGeneral(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("General"));
        addBool(cat, eb, "Enable Mod Tooltips", "Toggle for the mod's custom tooltips", ServerConfig.ENABLE_MOD_TOOLTIPS);
        addBool(cat, eb, "Enable Creative Tab Items", "Toggle for the mod's items to appear in vanilla creative tabs", ServerConfig.ENABLE_CREATIVE_TAB_ITEMS);
    }

    private static void buildAnvilConversion(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Anvil Conversion"));
        addBool(cat, eb, "Enable Stone to Anvil", "Allow shift-right-clicking stone to convert into Stone Smithing Anvil", ServerConfig.ENABLE_STONE_TO_ANVIL);
        addBool(cat, eb, "Enable Anvil to Smithing", "Allow shift-right-clicking vanilla anvil to convert into Smithing Anvil", ServerConfig.ENABLE_ANVIL_TO_SMITHING);
        addBool(cat, eb, "Enable Blueprint Forging", "Requires blueprint to obtain higher quality items", ServerConfig.ENABLE_BLUEPRINT_FORGING);
        addBool(cat, eb, "Enable Tier A", "Enable Tier A Smithing Anvil to appear", ServerConfig.ENABLE_TIER_A);
        addBool(cat, eb, "Enable Tier B", "Enable Tier B Smithing Anvil to appear", ServerConfig.ENABLE_TIER_B);
    }

    private static void buildStoneAnvil(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Stone Smithing Anvil"));
        addIntField(cat, eb, "Max Uses", "Number of uses before the Stone Smithing Anvil breaks. Set to 0 to disable.", ServerConfig.STONE_ANVIL_MAX_USES);
        addBool(cat, eb, "Enable Anvil to Stone", "Enable Stone Smithing Anvil turning into cobblestone after falling", ServerConfig.ENABLE_STONE_ANVIL_BREAKING);
    }

    private static void buildHeatedItems(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Heated Items"));
        addIntField(cat, eb, "Heated Item Cooldown Ticks", "How many ticks before a heated item cools off in inventory (default: 1200 = 60s)", ServerConfig.HEATED_ITEM_COOLDOWN_TICKS);
    }

    private static void buildArrowFletching(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Arrow Fletching"));
        addBool(cat, eb, "Enable Fletching Recipes", "Enable or disable all Fletching recipes and related tab", ServerConfig.ENABLE_FLETCHING_RECIPES);
        addBool(cat, eb, "Enable Dragon Breath Recipe", "Enable or disable Dragon Breath's brewing recipe", ServerConfig.ENABLE_DRAGON_BREATH_RECIPE);
        addBool(cat, eb, "Enable Tipping", "Enable or disable arrow tipping into potion arrow", ServerConfig.TIPPING_TOGGLE);
        addBool(cat, eb, "Enable Upgrade Arrow Tipping", "Toggle for the ability to tip iron, steel, diamond arrows", ServerConfig.UPGRADE_ARROW_POTION_TOGGLE);
        addIntField(cat, eb, "Max Potion Tipping Use", "How many arrows a bottle of potion can tip before it's depleted", ServerConfig.MAX_POTION_TIPPING_USE);
    }

    private static void buildMinigameCommon(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Minigame Common Settings"));
        addBool(cat, eb, "Enable Minigame", "Toggle for the forging minigame", ServerConfig.ENABLE_MINIGAME);
        addBool(cat, eb, "Ingredients Define Max Quality", "Toggle for if ingredients' quality defines the result's", ServerConfig.INGREDIENTS_DEFINE_MAX_QUALITY);
        addDouble(cat, eb, "Master Quality Chance", "How likely it is to get Masterwork when Perfectly Forged. 0 disables it.", ServerConfig.MASTER_QUALITY_CHANCE);
        addDouble(cat, eb, "Master From Ingredient Chance", "Chance that using a Master-quality ingredient results in a Master-quality result", ServerConfig.MASTER_FROM_INGREDIENT_CHANCE);
        addIntField(cat, eb, "Max Anvil Distance", "Maximum distance you can go from your Smithing Anvil before minigame reset", ServerConfig.MAX_ANVIL_DISTANCE);
        addBool(cat, eb, "Enable Author Tooltips", "Toggle for if the result item has the player's name", ServerConfig.PLAYER_AUTHOR_TOOLTIPS);
        addDouble(cat, eb, "Perfect Quality Score", "Lowest score required to get perfect quality", ServerConfig.PERFECT_QUALITY_SCORE);
        addDouble(cat, eb, "Expert Quality Score", "Lowest score required to get expert quality", ServerConfig.EXPERT_QUALITY_SCORE);
        addDouble(cat, eb, "Well Quality Score", "Lowest score required to get well quality", ServerConfig.WELL_QUALITY_SCORE);
    }

    private static void buildForgingZone(ConfigBuilder builder, ConfigEntryBuilder eb, String categoryName,
                                          ConfigSpec.IntValue startingSize, ConfigSpec.DoubleValue shrinkFactor,
                                          ConfigSpec.IntValue minPerfectZone, ConfigSpec.DoubleValue arrowSpeed,
                                          ConfigSpec.DoubleValue arrowSpeedIncrease, ConfigSpec.DoubleValue maxArrowSpeed) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of(categoryName));
        addIntSlider(cat, eb, "Zone Starting Size", "Zone starting size (in % chance)", startingSize);
        addDouble(cat, eb, "Zone Shrink Factor", "Zone shrink factor", shrinkFactor);
        addIntSlider(cat, eb, "Min Perfect Zone", "Minimum perfect zone size", minPerfectZone);
        addDouble(cat, eb, "Arrow Speed", "Arrow speed", arrowSpeed);
        addDouble(cat, eb, "Arrow Speed Increase", "Arrow speed increase per hit", arrowSpeedIncrease);
        addDouble(cat, eb, "Max Arrow Speed", "Maximum arrow speed", maxArrowSpeed);
    }

    private static void buildDurabilityGrinding(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Durability & Grinding"));
        addDouble(cat, eb, "Base Durability Multiplier", "The base durability multiplier of all items that have durability", ServerConfig.BASE_DURABILITY_MULTIPLIER);
        addStringList(cat, eb, "Base Durability Blacklist", "Items or tags that will NOT receive the base durability multiplier", ServerConfig.BASE_DURABILITY_BLACKLIST);
        addBool(cat, eb, "Grinding Restore Durability", "Can the grindstone be used for restoring durability or not", ServerConfig.GRINDING_RESTORE_DURABILITY);
        addStringList(cat, eb, "Grinding Blacklist", "Items or tags that cannot be repaired or affected by grinding. Prefix with '#' for a tag.", ServerConfig.GRINDING_BLACKLIST);
        addDouble(cat, eb, "Durability Reduce Per Grind", "How much item durability reduces per grindstone use", ServerConfig.DURABILITY_REDUCE_PER_GRIND);
        addDouble(cat, eb, "Damage Restore Per Grind", "How much item durability restores per grindstone use", ServerConfig.DAMAGE_RESTORE_PER_GRIND);
    }

    private static void buildQualityFailureChances(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Quality Failure Chances"));
        addDouble(cat, eb, "Fail on Well Quality Chance", "Chance that forging with WELL quality fails", ServerConfig.FAIL_ON_WELL_QUALITY_CHANCE);
        addDouble(cat, eb, "Fail on Expert Quality Chance", "Chance that forging with EXPERT quality fails", ServerConfig.FAIL_ON_EXPERT_QUALITY_CHANCE);
    }

    private static void buildBlueprintToolTypes(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Blueprint & Tool Types"));
        addStringList(cat, eb, "Available Tool Types", "List of available tool types for blueprints (sword, axe, pickaxe, shovel, hoe, or custom)", ServerConfig.AVAILABLE_TOOL_TYPES);
        addStringList(cat, eb, "Hidden Tool Types", "Tool types that exist but do NOT appear in the Drafting Table", ServerConfig.HIDDEN_TOOL_TYPES);
        addBool(cat, eb, "Expert Above Increases Blueprint", "Only increase blueprint's use if you get Expert or above in the minigame", ServerConfig.EXPERT_ABOVE_INCREASE_BLUEPRINT);
        addIntField(cat, eb, "Expert Max Use", "Uses required to reach the next quality after Expert", ServerConfig.EXPERT_MAX_USE);
        addIntField(cat, eb, "Well Max Use", "Uses required to reach the next quality after Well", ServerConfig.WELL_MAX_USE);
        addIntField(cat, eb, "Poor Max Use", "Uses required to reach the next quality after Poor", ServerConfig.POOR_MAX_USE);
    }

    private static void buildDurabilityBonuses(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Durability Bonuses"));
        addDouble(cat, eb, "Master Durability Bonus", null, ServerConfig.MASTER_DURABILITY_BONUS);
        addDouble(cat, eb, "Perfect Durability Bonus", null, ServerConfig.PERFECT_DURABILITY_BONUS);
        addDouble(cat, eb, "Expert Durability Bonus", null, ServerConfig.EXPERT_DURABILITY_BONUS);
        addDouble(cat, eb, "Well Durability Bonus", null, ServerConfig.WELL_DURABILITY_BONUS);
        addDouble(cat, eb, "Poor Durability Bonus", null, ServerConfig.POOR_DURABILITY_BONUS);
    }

    private static void buildMiningSpeedBonuses(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Mining Speed Bonuses"));
        addDouble(cat, eb, "Master Mining Speed Bonus", null, ServerConfig.MASTER_MINING_SPEED_BONUS);
        addDouble(cat, eb, "Perfect Mining Speed Bonus", null, ServerConfig.PERFECT_MINING_SPEED_BONUS);
        addDouble(cat, eb, "Expert Mining Speed Bonus", null, ServerConfig.EXPERT_MINING_SPEED_BONUS);
        addDouble(cat, eb, "Well Mining Speed Bonus", null, ServerConfig.WELL_MINING_SPEED_BONUS);
        addDouble(cat, eb, "Poor Mining Speed Bonus", null, ServerConfig.POOR_MINING_SPEED_BONUS);
    }

    private static void buildItemBreakChance(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Item Break Chance"));
        addBool(cat, eb, "Enable Quality Break System", "Enable quality-based break chance system", ServerConfig.ENABLE_QUALITY_BREAK_SYSTEM);
        addStringList(cat, eb, "Quality Break Blacklist", "Items or tags that will NOT receive the quality-based break chance system", ServerConfig.QUALITY_BREAK_BLACKLIST);
        addDouble(cat, eb, "Break Chance Poor", "Break chance at 0 durability for POOR quality", ServerConfig.BREAK_CHANCE_POOR);
        addDouble(cat, eb, "Break Chance Well", "Break chance for WELL quality", ServerConfig.BREAK_CHANCE_WELL);
        addDouble(cat, eb, "Break Chance Expert", "Break chance for EXPERT quality", ServerConfig.BREAK_CHANCE_EXPERT);
        addDouble(cat, eb, "Break Chance Perfect", "Break chance for PERFECT quality", ServerConfig.BREAK_CHANCE_PERFECT);
        addDouble(cat, eb, "Break Chance Master", "Break chance for MASTER quality", ServerConfig.BREAK_CHANCE_MASTER);
    }

    private static void buildKnapping(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Knapping Settings"));
        addBool(cat, eb, "Get Rock Using Flint", "Fallback: allow getting rocks by using flint on stone when no datapack interaction is found", ServerConfig.GET_ROCK_USING_FLINT);
        addDouble(cat, eb, "Rock Dropping Chance", "Fallback: chance to drop a rock when using flint on stone", ServerConfig.ROCK_DROPPING_CHANCE);
        addDouble(cat, eb, "Flint Breaking Chance", "Fallback: chance for flint breaking when used on stone", ServerConfig.FLINT_BREAKING_CHANCE);
    }

    private static void buildLootQuality(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Loot Quality"));
        addBool(cat, eb, "Enable Loot Quality", "Toggle for loot quality", ServerConfig.ENABLE_LOOT_QUALITY);
        addIntField(cat, eb, "Weight Poor Quality", "Weight for Poor quality for loot-tools/weapons. 0 disables it.", ServerConfig.QUALITY_WEIGHT_POOR);
        addIntField(cat, eb, "Weight Well Quality", "Weight for Well quality for loot-tools/weapons. 0 disables it.", ServerConfig.QUALITY_WEIGHT_WELL);
        addIntField(cat, eb, "Weight Expert Quality", "Weight for Expert quality for loot-tools/weapons. 0 disables it.", ServerConfig.QUALITY_WEIGHT_EXPERT);
        addIntField(cat, eb, "Weight Perfect Quality", "Weight for Perfect quality for loot-tools/weapons. 0 disables it.", ServerConfig.QUALITY_WEIGHT_PERFECT);
        addIntField(cat, eb, "Weight Master Quality", "Weight for Master quality for loot-tools/weapons. 0 disables it.", ServerConfig.QUALITY_WEIGHT_MASTER);
    }

    private static void buildCasting(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Casting"));
        addBool(cat, eb, "Enable Casting", "Affects normal progression since your first smithing hammers require casting", ServerConfig.ENABLE_CASTING);
        addIntField(cat, eb, "Fired Cast Durability", "Durability of the Fired Tool Cast", ServerConfig.FIRED_CAST_DURABILITY);
        addStringList(cat, eb, "Material Types", "Material type ids usable by casting/forging (advanced - see materialSetting in the JSON file for source items)", ServerConfig.MATERIAL_TYPES);
    }

    private static void buildClient(ConfigBuilder builder, ConfigEntryBuilder eb) {
        ConfigCategory cat = builder.getOrCreateCategory(Text.of("Client Settings"));
        addIntField(cat, eb, "Minigame Overlay Height", "Vertical position of the minigame overlay", ClientConfig.MINIGAME_OVERLAY_HEIGHT);
        addBool(cat, eb, "Show Minigame Popup", "If the minigame's pop up appears during the minigame", ClientConfig.POP_UP_TOGGLE);
        addBool(cat, eb, "Enable Anvil Recipe Book", "Toggle the Recipe Book for Smithing Anvils", ClientConfig.ENABLE_ANVIL_RECIPE_BOOK);
    }

    // --- entry helpers ---

    private static void addBool(ConfigCategory cat, ConfigEntryBuilder eb, String name, String tooltip, ConfigSpec.BooleanValue value) {
        var b = eb.startBooleanToggle(Text.of(name), value.get())
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);
        if (tooltip != null) b.setTooltip(Text.of(tooltip));
        cat.addEntry(b.build());
    }

    private static void addIntField(ConfigCategory cat, ConfigEntryBuilder eb, String name, String tooltip, ConfigSpec.IntValue value) {
        var b = eb.startIntField(Text.of(name), value.get())
                .setDefaultValue(value.get())
                .setMin(value.getMin())
                .setMax(value.getMax())
                .setSaveConsumer(value::set);
        if (tooltip != null) b.setTooltip(Text.of(tooltip));
        cat.addEntry(b.build());
    }

    private static void addIntSlider(ConfigCategory cat, ConfigEntryBuilder eb, String name, String tooltip, ConfigSpec.IntValue value) {
        var b = eb.startIntSlider(Text.of(name), value.get(), value.getMin(), value.getMax())
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);
        if (tooltip != null) b.setTooltip(Text.of(tooltip));
        cat.addEntry(b.build());
    }

    private static void addDouble(ConfigCategory cat, ConfigEntryBuilder eb, String name, String tooltip, ConfigSpec.DoubleValue value) {
        var b = eb.startDoubleField(Text.of(name), value.get())
                .setDefaultValue(value.get())
                .setMin(value.getMin())
                .setMax(value.getMax())
                .setSaveConsumer(value::set);
        if (tooltip != null) b.setTooltip(Text.of(tooltip));
        cat.addEntry(b.build());
    }

    private static void addStringList(ConfigCategory cat, ConfigEntryBuilder eb, String name, String tooltip, ConfigSpec.ListValue<String> value) {
        var b = eb.startStrList(Text.of(name), value.get())
                .setDefaultValue(value.get())
                .setSaveConsumer(value::set);
        if (tooltip != null) b.setTooltip(Text.of(tooltip));
        cat.addEntry(b.build());
    }
}
