package net.stirdrem.overgeared.compat.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentBuilder;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentBuilderMap;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.util.TinyMap;

import java.util.Map;

/**
 * KubeJS recipe schemas for Overgeared's recipe types, usable as {@code event.recipes.overgeared.<type>(...)}.
 * Keys mirror the recipe JSON; optional ones are left out of the JSON unless set, so the mod's defaults apply.
 * Builder methods use camelCase ({@code .requiresBlueprint(true)}); the JSON names also work in objects.
 * <pre>
 * ServerEvents.recipes(event => {
 *   event.recipes.overgeared.forging('minecraft:iron_sword', [' A ', ' A ', ' B '], {
 *     A: {item: 'overgeared:heated_iron_ingot', requires_heated: true},
 *     B: 'minecraft:stick'
 *   }).tier('iron').hammering(4).blueprint('sword')
 *   event.recipes.overgeared.alloy_smelting('2x overgeared:crude_steel', ['minecraft:iron_ingot', 'minecraft:coal'])
 *   event.recipes.overgeared.casting('overgeared:copper_axe_head', {copper: 27}, 'axe').needPolishing(true)
 * })
 * </pre>
 */
public final class OvergearedRecipeSchemas {
    // Shared keys
    private static final RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");
    private static final RecipeKey<String[]> PATTERN = StringComponent.NON_EMPTY.asArray().key("pattern");
    private static final RecipeKey<String> GROUP = StringComponent.ANY.key("group").defaultOptional();
    private static final RecipeKey<String> CATEGORY = StringComponent.NON_EMPTY.key("category").defaultOptional();
    private static final RecipeKey<Float> EXPERIENCE = NumberComponent.FLOAT.key("experience").defaultOptional();
    private static final RecipeKey<Integer> COOKING_TIME =
            NumberComponent.INT.key("cookingtime").preferred("cookingTime").defaultOptional();

    // overgeared:forging
    /** Key ingredients may set {@code requires_heated} and {@code transfer_nbt}. */
    private static final RecipeKey<TinyMap<Character, ExtraFieldsComponent.Value<InputItem>>> FORGING_KEY =
            ExtraFieldsComponent.of(ItemComponents.INPUT, "requires_heated", "transfer_nbt").asPatternKey().key("key");
    public static final RecipeSchema FORGING = new RecipeSchema(
            RESULT, PATTERN, FORGING_KEY,
            StringComponent.NON_EMPTY.key("tier").defaultOptional(),
            NumberComponent.INT.min(1).key("hammering").defaultOptional(),
            StringComponent.NON_EMPTY.asArrayOrSelf().key("blueprint").defaultOptional(),
            flag("requires_blueprint", "requiresBlueprint"),
            flag("has_quality", "hasQuality"),
            flag("needs_minigame", "needsMinigame"),
            flag("has_polishing", "hasPolishing"),
            flag("need_quenching", "needQuenching"),
            flag("show_notification", "showNotification"),
            StringComponent.NON_EMPTY.key("minimum_quality").preferred("minimumQuality").defaultOptional(),
            StringComponent.NON_EMPTY.key("quality_difficulty").preferred("qualityDifficulty").defaultOptional(),
            ItemComponents.OUTPUT.key("result_failed").preferred("resultFailed").defaultOptional(),
            CATEGORY, GROUP
    ).constructor(RESULT, PATTERN, FORGING_KEY);

    // overgeared:rock_knapping — pattern rows use 'x' for the chipped-away spots
    private static final RecipeKey<InputItem> KNAPPING_INGREDIENT = ItemComponents.INPUT.key("ingredient");
    public static final RecipeSchema ROCK_KNAPPING = new RecipeSchema(
            RESULT, KNAPPING_INGREDIENT, PATTERN,
            BooleanComponent.BOOLEAN.key("mirrored").defaultOptional()
    ).constructor(RESULT, KNAPPING_INGREDIENT, PATTERN);

    // overgeared:fletching
    private static final RecipeKey<RecipeComponentBuilderMap> FLETCHING_MATERIAL = new RecipeComponentBuilder(3)
            .add(ItemComponents.INPUT.key("tip").defaultOptional())
            .add(ItemComponents.INPUT.key("shaft").defaultOptional())
            .add(ItemComponents.INPUT.key("feather").defaultOptional())
            .inputRole()
            .key("material");
    /** Tipped and lingering results may set {@code tag}, the NBT key the potion is stored under. */
    private static final ExtraFieldsComponent<OutputItem> TAGGED_OUTPUT = ExtraFieldsComponent.of(ItemComponents.OUTPUT, "tag");
    public static final RecipeSchema FLETCHING = new RecipeSchema(
            RESULT, FLETCHING_MATERIAL,
            ItemComponents.INPUT.key("potion").defaultOptional(),
            TAGGED_OUTPUT.key("result_tipped").preferred("resultTipped").defaultOptional(),
            TAGGED_OUTPUT.key("result_lingering").preferred("resultLingering").defaultOptional()
    ).constructor(RESULT, FLETCHING_MATERIAL);

    // overgeared:alloy_smelting (up to 4 ingredients), overgeared:nether_alloy_smelting (up to 9)
    private static final RecipeKey<InputItem[]> ALLOY_INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    public static final RecipeSchema ALLOY_SMELTING = new RecipeSchema(
            RESULT, ALLOY_INGREDIENTS, EXPERIENCE, COOKING_TIME, CATEGORY, GROUP
    ).constructor(RESULT, ALLOY_INGREDIENTS);

    // overgeared:shaped_alloy_smelting, overgeared:shaped_nether_alloy_smelting
    private static final RecipeKey<TinyMap<Character, InputItem>> SHAPED_KEY = ItemComponents.INPUT.asPatternKey().key("key");
    public static final RecipeSchema SHAPED_ALLOY_SMELTING = new RecipeSchema(
            RESULT, PATTERN, SHAPED_KEY, EXPERIENCE, COOKING_TIME, CATEGORY, GROUP
    ).constructor(RESULT, PATTERN, SHAPED_KEY);

    // overgeared:casting, overgeared:cast_smelting, overgeared:cast_blasting
    /** Material amounts by material id, e.g. {@code {iron: 3}}. */
    private static final RecipeKey<TinyMap<String, Double>> CAST_INPUT =
            NumberComponent.DOUBLE.asMap(StringComponent.NON_EMPTY).key("input");
    private static final RecipeKey<String> TOOL_TYPE = StringComponent.NON_EMPTY.key("tool_type").preferred("toolType");
    public static final RecipeSchema CASTING = new RecipeSchema(
            RESULT, CAST_INPUT, TOOL_TYPE, EXPERIENCE, COOKING_TIME,
            flag("need_polishing", "needPolishing"),
            GROUP
    ).constructor(RESULT, CAST_INPUT, TOOL_TYPE);

    // overgeared:cooling, overgeared:grinding
    private static final RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("output");
    private static final RecipeKey<InputItem> INPUT = ItemComponents.INPUT.key("input");
    public static final RecipeSchema COOLING = new RecipeSchema(OUTPUT, INPUT);

    // overgeared:item_to_tooltype
    private static final RecipeKey<InputItem> TOOLTYPE_ITEM = ItemComponents.INPUT.key("item");
    private static final RecipeKey<String> TOOLTYPE = StringComponent.NON_EMPTY.key("tooltype").preferred("toolType");
    public static final RecipeSchema ITEM_TO_TOOLTYPE = new RecipeSchema(TOOLTYPE_ITEM, TOOLTYPE);

    // overgeared:crafting_shapeless — ingredients may set "remainder" and "durability_decrease"
    private static final RecipeKey<ExtraFieldsComponent.Value<InputItem>[]> SHAPELESS_INGREDIENTS =
            ExtraFieldsComponent.of(ItemComponents.INPUT, "remainder", "durability_decrease").asArray().key("ingredients");
    public static final RecipeSchema CRAFTING_SHAPELESS = new RecipeSchema(
            RESULT, SHAPELESS_INGREDIENTS, CATEGORY, GROUP
    ).constructor(RESULT, SHAPELESS_INGREDIENTS);

    // overgeared:nbt_smelting, overgeared:nbt_blasting (keep the input's NBT)
    private static final RecipeKey<InputItem> COOKING_INGREDIENT = ItemComponents.INPUT.key("ingredient");
    public static final RecipeSchema NBT_COOKING = new RecipeSchema(
            RESULT, COOKING_INGREDIENT, EXPERIENCE, COOKING_TIME, CATEGORY, GROUP
    ).constructor(RESULT, COOKING_INGREDIENT);

    // overgeared:nbt_add_smelting, overgeared:nbt_add_blasting, overgeared:nbt_add_campfire_cooking
    public static final RecipeSchema NBT_ADD_COOKING = new RecipeSchema(
            RESULT, COOKING_INGREDIENT,
            JsonObjectComponent.INSTANCE.key("nbt").defaultOptional(),
            EXPERIENCE, COOKING_TIME, CATEGORY, GROUP
    ).constructor(RESULT, COOKING_INGREDIENT);

    private OvergearedRecipeSchemas() {
    }

    private static RecipeKey<Boolean> flag(String name, String preferred) {
        return BooleanComponent.BOOLEAN.key(name).preferred(preferred).defaultOptional();
    }

    public static void register(RegisterRecipeSchemasEvent event) {
        RecipeNamespace ns = event.namespace("overgeared");
        Map<String, RecipeSchema> schemas = Map.ofEntries(
                Map.entry("forging", FORGING),
                Map.entry("rock_knapping", ROCK_KNAPPING),
                Map.entry("fletching", FLETCHING),
                Map.entry("alloy_smelting", ALLOY_SMELTING),
                Map.entry("nether_alloy_smelting", ALLOY_SMELTING),
                Map.entry("shaped_alloy_smelting", SHAPED_ALLOY_SMELTING),
                Map.entry("shaped_nether_alloy_smelting", SHAPED_ALLOY_SMELTING),
                Map.entry("casting", CASTING),
                Map.entry("cast_smelting", CASTING),
                Map.entry("cast_blasting", CASTING),
                Map.entry("cooling", COOLING),
                Map.entry("grinding", COOLING),
                Map.entry("item_to_tooltype", ITEM_TO_TOOLTYPE),
                Map.entry("crafting_shapeless", CRAFTING_SHAPELESS),
                Map.entry("nbt_smelting", NBT_COOKING),
                Map.entry("nbt_blasting", NBT_COOKING),
                Map.entry("nbt_add_smelting", NBT_ADD_COOKING),
                Map.entry("nbt_add_blasting", NBT_ADD_COOKING),
                Map.entry("nbt_add_campfire_cooking", NBT_ADD_COOKING));
        schemas.forEach(ns::register);
        // Recipes with no fields of their own
        ns.special("crafting_cloning");
        ns.special("crafting_cast");
        ns.special("crafting_initial_cast");
    }
}
