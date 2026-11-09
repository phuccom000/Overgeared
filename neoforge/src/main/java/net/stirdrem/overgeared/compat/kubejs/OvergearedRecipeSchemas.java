package net.stirdrem.overgeared.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.util.TinyMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ingredients.IngredientWithRemainder;

import java.util.List;
import java.util.Map;

/**
 * KubeJS recipe schemas for Overgeared's recipe types, usable as {@code event.recipes.overgeared.<type>(...)}.
 * Keys mirror the recipe JSON; optional ones are only written when set, so the mod's defaults apply. Builder methods
 * are the keys in camelCase ({@code .requiresBlueprint(true)}).
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
    /** A forging key ingredient; may set {@code requires_heated} and {@code transfer_nbt}. */
    public static final RecipeComponentType<ForgingRecipe.ForgingIngredient> FORGING_INGREDIENT = RecipeComponentType.unit(
            OvergearedMod.loc("forging_ingredient"),
            type -> new IngredientWithFlagsComponent<>(type, ForgingRecipe.ForgingIngredient.CODEC,
                    ForgingRecipe.ForgingIngredient.class, List.of("requires_heated", "transfer_nbt"),
                    (ingredient, flags) -> new ForgingRecipe.ForgingIngredient(ingredient,
                            IngredientWithFlagsComponent.bool(flags, "requires_heated"),
                            IngredientWithFlagsComponent.bool(flags, "transfer_nbt")),
                    ForgingRecipe.ForgingIngredient::ingredient,
                    (value, ingredient) -> new ForgingRecipe.ForgingIngredient(ingredient, value.requiresHeated(), value.transferNBT())));

    /** A shapeless crafting ingredient; may set {@code remainder} and {@code durability_decrease}. */
    public static final RecipeComponentType<IngredientWithRemainder> REMAINDER_INGREDIENT = RecipeComponentType.unit(
            OvergearedMod.loc("remainder_ingredient"),
            type -> new IngredientWithFlagsComponent<>(type, IngredientWithRemainder.CODEC,
                    IngredientWithRemainder.class, List.of("remainder", "durability_decrease"),
                    (ingredient, flags) -> new IngredientWithRemainder(ingredient,
                            IngredientWithFlagsComponent.bool(flags, "remainder"),
                            IngredientWithFlagsComponent.integer(flags, "durability_decrease")),
                    IngredientWithRemainder::getIngredient,
                    (value, ingredient) -> new IngredientWithRemainder(ingredient, value.hasRemainder(), value.getDurabilityDecrease())));

    private static final RecipeComponent<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.instance();
    private static final RecipeComponent<String> STRING = StringComponent.STRING.instance();

    // Shared keys
    private static final RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
    private static final RecipeKey<List<String>> PATTERN = STRING.asList().otherKey("pattern");
    private static final RecipeKey<String> CATEGORY = STRING.otherKey("category").defaultOptional();
    private static final RecipeKey<String> GROUP = StringComponent.OPTIONAL_STRING.otherKey("group").defaultOptional();
    private static final RecipeKey<Float> EXPERIENCE = NumberComponent.FLOAT.otherKey("experience").defaultOptional();
    private static final RecipeKey<Integer> COOKING_TIME = NumberComponent.INT.otherKey("cookingtime").defaultOptional();

    // overgeared:forging
    private static final RecipeKey<TinyMap<Character, ForgingRecipe.ForgingIngredient>> FORGING_KEY =
            FORGING_INGREDIENT.instance().asPatternKey().inputKey("key");
    public static final RecipeSchema FORGING = new RecipeSchema(
            RESULT, PATTERN, FORGING_KEY,
            STRING.otherKey("tier").defaultOptional(),
            NumberComponent.INT.min(1).otherKey("hammering").defaultOptional(),
            STRING.asListOrSelf().otherKey("blueprint").defaultOptional(),
            flag("requires_blueprint"),
            flag("has_quality"),
            flag("needs_minigame"),
            flag("has_polishing"),
            flag("need_quenching"),
            flag("show_notification"),
            STRING.otherKey("minimum_quality").defaultOptional(),
            STRING.otherKey("quality_difficulty").defaultOptional(),
            ItemStackComponent.OPTIONAL_ITEM_STACK.outputKey("result_failed").defaultOptional(),
            CATEGORY
    ).constructor(RESULT, PATTERN, FORGING_KEY);

    // overgeared:rock_knapping: pattern rows use 'x' for the chipped-away spots
    private static final RecipeKey<Ingredient> INGREDIENT_KEY = INGREDIENT.inputKey("ingredient");
    public static final RecipeSchema ROCK_KNAPPING = new RecipeSchema(
            RESULT, INGREDIENT_KEY, PATTERN,
            flag("mirrored")
    ).constructor(RESULT, INGREDIENT_KEY, PATTERN);

    // overgeared:fletching: the materials are top-level, all optional
    public static final RecipeSchema FLETCHING = new RecipeSchema(
            RESULT,
            optionalIngredient("tip"),
            optionalIngredient("shaft"),
            optionalIngredient("feather"),
            optionalIngredient("potion"),
            ItemStackComponent.OPTIONAL_ITEM_STACK.outputKey("result_tipped").defaultOptional(),
            ItemStackComponent.OPTIONAL_ITEM_STACK.outputKey("result_lingering").defaultOptional(),
            STRING.otherKey("tipped_tag").defaultOptional(),
            STRING.otherKey("lingering_tag").defaultOptional()
    ).constructor(RESULT);

    // overgeared:alloy_smelting (up to 4 ingredients), overgeared:nether_alloy_smelting (up to 9)
    private static final RecipeKey<List<Ingredient>> ALLOY_INGREDIENTS = INGREDIENT.asList().inputKey("ingredients");
    /** The alloy smelting codec requires "group", so it is always written. */
    private static final RecipeKey<String> REQUIRED_GROUP = StringComponent.OPTIONAL_STRING.otherKey("group").optional("").alwaysWrite();
    public static final RecipeSchema ALLOY_SMELTING = new RecipeSchema(
            RESULT, ALLOY_INGREDIENTS, EXPERIENCE, COOKING_TIME, CATEGORY, REQUIRED_GROUP
    ).constructor(RESULT, ALLOY_INGREDIENTS);
    public static final RecipeSchema NETHER_ALLOY_SMELTING = new RecipeSchema(
            RESULT, ALLOY_INGREDIENTS, EXPERIENCE, COOKING_TIME, CATEGORY, GROUP
    ).constructor(RESULT, ALLOY_INGREDIENTS);

    // overgeared:shaped_alloy_smelting, overgeared:shaped_nether_alloy_smelting
    private static final RecipeKey<TinyMap<Character, Ingredient>> SHAPED_KEY = INGREDIENT.asPatternKey().inputKey("key");
    public static final RecipeSchema SHAPED_ALLOY_SMELTING = new RecipeSchema(
            RESULT, PATTERN, SHAPED_KEY, EXPERIENCE, COOKING_TIME, CATEGORY, GROUP
    ).constructor(RESULT, PATTERN, SHAPED_KEY);

    // overgeared:casting, overgeared:cast_smelting, overgeared:cast_blasting
    /** Material amounts by material id, e.g. {@code {copper: 27}}. */
    private static final RecipeKey<TinyMap<String, Integer>> CAST_INPUT = NumberComponent.INT.asMap(STRING).otherKey("input");
    private static final RecipeKey<String> TOOL_TYPE = STRING.otherKey("tool_type");
    public static final RecipeSchema CASTING = new RecipeSchema(
            RESULT, CAST_INPUT, TOOL_TYPE, EXPERIENCE, COOKING_TIME,
            flag("need_polishing"),
            CATEGORY, GROUP
    ).constructor(RESULT, CAST_INPUT, TOOL_TYPE);

    // overgeared:cooling, overgeared:grinding
    private static final RecipeKey<ItemStack> OUTPUT = ItemStackComponent.ITEM_STACK.outputKey("output");
    private static final RecipeKey<Ingredient> INPUT = INGREDIENT.inputKey("input");
    public static final RecipeSchema COOLING = new RecipeSchema(OUTPUT, INPUT);

    // overgeared:item_to_tooltype
    public static final RecipeSchema ITEM_TO_TOOLTYPE = new RecipeSchema(INGREDIENT.inputKey("item"), STRING.otherKey("tooltype"));

    // overgeared:crafting_shapeless: ingredients may set "remainder" and "durability_decrease"
    private static final RecipeKey<List<IngredientWithRemainder>> SHAPELESS_INGREDIENTS =
            REMAINDER_INGREDIENT.instance().asList().inputKey("ingredients");
    public static final RecipeSchema CRAFTING_SHAPELESS = new RecipeSchema(
            RESULT, SHAPELESS_INGREDIENTS, CATEGORY, GROUP
    ).constructor(RESULT, SHAPELESS_INGREDIENTS);

    // overgeared:nbt_smelting, overgeared:nbt_blasting (keep the input's components), overgeared:campfire_heating
    public static final RecipeSchema NBT_COOKING = new RecipeSchema(
            RESULT, INGREDIENT_KEY, EXPERIENCE, COOKING_TIME, CATEGORY, GROUP
    ).constructor(RESULT, INGREDIENT_KEY);

    private OvergearedRecipeSchemas() {
    }

    private static RecipeKey<Boolean> flag(String name) {
        return BooleanComponent.BOOLEAN.otherKey(name).defaultOptional();
    }

    private static RecipeKey<Ingredient> optionalIngredient(String name) {
        return IngredientComponent.OPTIONAL_INGREDIENT.inputKey(name).defaultOptional();
    }

    public static void registerComponents(RecipeComponentTypeRegistry registry) {
        registry.register(FORGING_INGREDIENT);
        registry.register(REMAINDER_INGREDIENT);
    }

    public static void registerSchemas(RecipeSchemaRegistry registry) {
        RecipeNamespace ns = registry.namespace(OvergearedMod.MOD_ID);
        Map<String, RecipeSchema> schemas = Map.ofEntries(
                Map.entry("forging", FORGING),
                Map.entry("rock_knapping", ROCK_KNAPPING),
                Map.entry("fletching", FLETCHING),
                Map.entry("alloy_smelting", ALLOY_SMELTING),
                Map.entry("nether_alloy_smelting", NETHER_ALLOY_SMELTING),
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
                Map.entry("campfire_heating", NBT_COOKING));
        schemas.forEach(ns::register);
        // Recipes with no fields of their own
        ns.special("crafting_cloning");
        ns.special("crafting_cast");
        ns.special("crafting_initial_cast");
    }
}
