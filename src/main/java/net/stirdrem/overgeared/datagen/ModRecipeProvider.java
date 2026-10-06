package net.stirdrem.overgeared.datagen;

import java.util.Optional;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.recipe.ForgingBookCategory;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    /* private static final List<ItemConvertible> RUBY_SMELTABLES = List.of(ModItems.RAW_RUBY,
             ModBlocks.RUBY_ORE, ModBlocks.DEEPSLATE_RUBY_ORE, ModBlocks.NETHER_RUBY_ORE, ModBlocks.END_STONE_RUBY_ORE);*/
    private static final List<ItemLike> STEEL_SMELTABLES = List.of(
            ModItems.CRUDE_STEEL);

    private static final List<ItemLike> COPPER_SMELTABLES = List.of(
            Items.COPPER_INGOT);
    private static final List<ItemLike> IRON_SMELTABLES = List.of(
            Items.IRON_INGOT);

    private static final List<ItemLike> IRON_SOURCE = List.of(
            Items.RAW_IRON,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.IRON_ORE);

    private static final List<ItemLike> COPPER_SOURCE = List.of(
            Items.RAW_COPPER,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.COPPER_ORE);

    private static final List<ItemLike> IRON_HEADS = List.of(
            ModItems.IRON_HOE_HEAD,
            ModItems.IRON_PICKAXE_HEAD,
            ModItems.IRON_SWORD_BLADE,
            ModItems.IRON_AXE_HEAD,
            ModItems.IRON_SHOVEL_HEAD,
            ModItems.IRON_SPEAR_HEAD,
            ModItems.IRON_ARROW_HEAD

    );
    private static final List<ItemLike> STEEL_HEADS = List.of(
            ModItems.STEEL_HOE_HEAD,
            ModItems.STEEL_PICKAXE_HEAD,
            ModItems.STEEL_SWORD_BLADE,
            ModItems.STEEL_AXE_HEAD,
            ModItems.STEEL_SHOVEL_HEAD,
            ModItems.STEEL_SPEAR_HEAD,
            ModItems.STEEL_HOE,
            ModItems.STEEL_PICKAXE,
            ModItems.STEEL_SWORD,
            ModItems.STEEL_AXE,
            ModItems.STEEL_SHOVEL,
            ModItems.STEEL_SPEAR,
            ModItems.STEEL_HELMET,
            ModItems.STEEL_CHESTPLATE,
            ModItems.STEEL_LEGGINGS,
            ModItems.STEEL_BOOTS,
            ModItems.STEEL_ARROW_HEAD);

    private static final List<ItemLike> COPPER_HEADS = List.of(
            ModItems.COPPER_HOE_HEAD,
            ModItems.COPPER_PICKAXE_HEAD,
            ModItems.COPPER_SWORD_BLADE,
            ModItems.COPPER_AXE_HEAD,
            ModItems.COPPER_SHOVEL_HEAD,
            ModItems.COPPER_SPEAR_HEAD
            // Copper tools/armor are vanilla items since 26.x - vanilla's own
            // copper_nugget_from_smelting/blasting recipes already cover them.
    );
    private static final List<ItemLike> GOLDEN_HEADS = List.of(
            ModItems.GOLDEN_HOE_HEAD,
            ModItems.GOLDEN_PICKAXE_HEAD,
            ModItems.GOLDEN_SWORD_BLADE,
            ModItems.GOLDEN_AXE_HEAD,
            ModItems.GOLDEN_SHOVEL_HEAD,
            ModItems.GOLDEN_SPEAR_HEAD

    );

    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Overgeared Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes,
                                                  BootstrapContext<Advancement> advancements) {
        return new Recipes(recipes, advancements);
    }

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, path);
    }

    private static class Recipes extends RecipeProvider {
        Recipes(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
            super(recipes, advancements);
        }

        /**
         * Steel -> diamond upgrade at the smithing table, saved under the vanilla id
         * (minecraft:diamond_*) so it replaces vanilla's crafting-table recipe, like the iron/gold
         * overrides. Accepted directly because Fabric's datagen rewrites ids passed through
         * SmithingTransformRecipeBuilder into the mod's namespace.
         */
        private void diamondUpgrade(Item steel, Item diamond, RecipeCategory category) {
            ResourceKey<Recipe<?>> id = ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(diamond));
            SmithingTransformRecipe recipe = new SmithingTransformRecipe(
                    new Recipe.CommonInfo(true),
                    Optional.of(Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE)),
                    Ingredient.of(steel),
                    Optional.of(Ingredient.of(Items.DIAMOND)),
                    new ItemStackTemplate(diamond));
            RecipeBuilderSupport.Unlocks unlocks = new RecipeBuilderSupport.Unlocks();
            unlocks.add("has_diamond", has(Items.DIAMOND));
            output.accept(id, recipe, unlocks.build(output, id, category.getFolderName()));
        }

        @Override
        public void buildRecipes() {
            oreBlasting(STEEL_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.HEATED_CRUDE_STEEL, 0, 100,
                    "steel_ingot");
            oreBlasting(COPPER_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.HEATED_COPPER_INGOT, 0, 70,
                    "copper_ingot");
            oreBlasting(IRON_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.HEATED_IRON_INGOT, 0, 100,
                    "iron_ingot");
            oreSmelting(COPPER_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.HEATED_COPPER_INGOT, 0, 140,
                    "copper_ingot");
            oreBlasting(IRON_SOURCE, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.HEATED_IRON_INGOT, 0.7f, 100,
                    "iron_ingot");
            oreBlasting(COPPER_SOURCE, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.HEATED_COPPER_INGOT, 0.7f, 100,
                    "copper_ingot");
            oreSmelting(IRON_SOURCE, RecipeCategory.MISC, CookingBookCategory.MISC, Items.IRON_INGOT, 0.7f, 200, "iron_ingot");
            oreSmelting(COPPER_SOURCE, RecipeCategory.MISC, CookingBookCategory.MISC, Items.COPPER_INGOT, 0.7f, 200, "copper_ingot");
            oreSmelting(IRON_HEADS, RecipeCategory.MISC, CookingBookCategory.MISC, Items.IRON_NUGGET, 0.1f, 200, null);
            oreBlasting(IRON_HEADS, RecipeCategory.MISC, CookingBookCategory.MISC, Items.IRON_NUGGET, 0.1f, 100, null);
            oreSmelting(GOLDEN_HEADS, RecipeCategory.MISC, CookingBookCategory.MISC, Items.GOLD_NUGGET, 0.1f, 200, null);
            oreBlasting(GOLDEN_HEADS, RecipeCategory.MISC, CookingBookCategory.MISC, Items.GOLD_NUGGET, 0.1f, 100, null);
            oreBlasting(STEEL_HEADS, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.STEEL_NUGGET, 0.1f, 200, null);
            oreSmelting(COPPER_HEADS, RecipeCategory.MISC, CookingBookCategory.MISC, Items.COPPER_NUGGET, 0.1f, 200, null);
            oreBlasting(COPPER_HEADS, RecipeCategory.MISC, CookingBookCategory.MISC, Items.COPPER_NUGGET, 0.1f, 100, null);
            /*offerSmelting(output, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY,
                    0.7f, 200, "ruby");
            offerBlasting(output, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY,
                    0.7f, 100, "ruby");

            offerReversibleCompactingRecipes(output, RecipeCategory.BUILDING_BLOCKS, ModItems.RUBY, RecipeCategory.DECORATIONS,
                    ModBlocks.RUBY_BLOCK);

            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.RAW_RUBY, 1)
                    .pattern("SSS")
                    .pattern("SRS")
                    .pattern("SSS")
                    .input('S', Items.STONE)
                    .input('R', ModItems.RUBY)
                    .criterion(hasItem(Items.STONE), conditionsFromItem(Items.STONE))
                    .criterion(hasItem(ModItems.RUBY), conditionsFromItem(ModItems.RUBY))
                    .offerTo(output, new Identifier(getRecipeName(ModItems.RAW_RUBY)));*/

            nineBlockStorageRecipes(RecipeCategory.BUILDING_BLOCKS, ModItems.STEEL_INGOT, RecipeCategory.DECORATIONS,
                    ModBlocks.STEEL_BLOCK);

            shaped(RecipeCategory.MISC, ModItems.STEEL_INGOT)
                    .pattern("###")
                    .pattern("###")
                    .pattern("###")
                    .define('#', ModItems.STEEL_NUGGET)
                    .unlockedBy("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .unlockedBy(getHasName(ModItems.STEEL_NUGGET), has(ModItems.STEEL_NUGGET))
                    .save(output, Overgeared.MOD_ID + ":steel_ingot_from_nuggets");

            // Copper ingot <-> nugget recipes are vanilla (copper_ingot_from_nuggets / copper_nugget) since 26.x.

            shaped(RecipeCategory.MISC, ModItems.WOODEN_TONGS)
                    .pattern(" # ")
                    .pattern("###")
                    .pattern(" # ")
                    .define('#', Items.STICK)
                    .unlockedBy("has_hot_item", has(ModTags.Items.HOT_ITEMS))
                    .unlockedBy("has_heated_metal", has(ModTags.Items.HEATED_METALS))
                    .save(output);

            shapeless(RecipeCategory.MISC, ModItems.EMPTY_BLUEPRINT)
                    .requires(Items.PAPER)
                    .requires(Items.PAPER)
                    .requires(Items.PAPER)
                    .requires(Items.DYE.blue())
                    .unlockedBy("has_paper", has(Items.PAPER))
                    .save(output);

            shapeless(RecipeCategory.MISC, ModBlocks.DRAFTING_TABLE)
                    .requires(Blocks.CRAFTING_TABLE)
                    .requires(ModItems.EMPTY_BLUEPRINT)
                    .unlockedBy(getHasName(Blocks.CRAFTING_TABLE), has(Items.CRAFTING_TABLE))
                    .unlockedBy(getHasName(ModItems.EMPTY_BLUEPRINT),
                            has(ModItems.EMPTY_BLUEPRINT))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_AXE)
                    .input(ModItems.STONE_AXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STONE_AXE_HEAD),
                            has(ModItems.STONE_AXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_PICKAXE)
                    .input(ModItems.STONE_PICKAXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STONE_PICKAXE_HEAD),
                            has(ModItems.STONE_PICKAXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_SHOVEL)
                    .input(ModItems.STONE_SHOVEL_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STONE_SHOVEL_HEAD),
                            has(ModItems.STONE_SHOVEL_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.STONE_SPEAR)
                    .input(ModItems.STONE_SPEAR_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STONE_SPEAR_HEAD),
                            has(ModItems.STONE_SPEAR_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_HOE)
                    .input(ModItems.STONE_HOE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STONE_HOE_HEAD),
                            has(ModItems.STONE_HOE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_SWORD)
                    .input(ModItems.STONE_SWORD_BLADE)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STONE_SWORD_BLADE),
                            has(ModItems.STONE_SWORD_BLADE))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_AXE)
                    .input(ModItems.IRON_AXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.IRON_AXE_HEAD), has(ModItems.IRON_AXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
                    .input(ModItems.IRON_PICKAXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.IRON_PICKAXE_HEAD),
                            has(ModItems.IRON_PICKAXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
                    .input(ModItems.IRON_SHOVEL_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.IRON_SHOVEL_HEAD),
                            has(ModItems.IRON_SHOVEL_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.IRON_SPEAR)
                    .input(ModItems.IRON_SPEAR_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.IRON_SPEAR_HEAD),
                            has(ModItems.IRON_SPEAR_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_HOE)
                    .input(ModItems.IRON_HOE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.IRON_HOE_HEAD), has(ModItems.IRON_HOE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_SWORD)
                    .input(ModItems.IRON_SWORD_BLADE)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.IRON_SWORD_BLADE),
                            has(ModItems.IRON_SWORD_BLADE))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_AXE)
                    .input(ModItems.STEEL_AXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STEEL_AXE_HEAD),
                            has(ModItems.STEEL_AXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_PICKAXE)
                    .input(ModItems.STEEL_PICKAXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STEEL_PICKAXE_HEAD),
                            has(ModItems.STEEL_PICKAXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_SHOVEL)
                    .input(ModItems.STEEL_SHOVEL_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STEEL_SHOVEL_HEAD),
                            has(ModItems.STEEL_SHOVEL_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.COMBAT, ModItems.STEEL_SPEAR)
                    .input(ModItems.STEEL_SPEAR_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STEEL_SPEAR_HEAD),
                            has(ModItems.STEEL_SPEAR_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_HOE)
                    .input(ModItems.STEEL_HOE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STEEL_HOE_HEAD),
                            has(ModItems.STEEL_HOE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_SWORD)
                    .input(ModItems.STEEL_SWORD_BLADE)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.STEEL_SWORD_BLADE),
                            has(ModItems.STEEL_SWORD_BLADE))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.COPPER_AXE)
                    .input(ModItems.COPPER_AXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.COPPER_AXE_HEAD),
                            has(ModItems.COPPER_AXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.COPPER_PICKAXE)
                    .input(ModItems.COPPER_PICKAXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.COPPER_PICKAXE_HEAD),
                            has(ModItems.COPPER_PICKAXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.COPPER_SHOVEL)
                    .input(ModItems.COPPER_SHOVEL_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.COPPER_SHOVEL_HEAD),
                            has(ModItems.COPPER_SHOVEL_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.COPPER_SPEAR)
                    .input(ModItems.COPPER_SPEAR_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.COPPER_SPEAR_HEAD),
                            has(ModItems.COPPER_SPEAR_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.COPPER_HOE)
                    .input(ModItems.COPPER_HOE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.COPPER_HOE_HEAD),
                            has(ModItems.COPPER_HOE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.COPPER_SWORD)
                    .input(ModItems.COPPER_SWORD_BLADE)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.COPPER_SWORD_BLADE),
                            has(ModItems.COPPER_SWORD_BLADE))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
                    .input(ModItems.GOLDEN_AXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.GOLDEN_AXE_HEAD),
                            has(ModItems.GOLDEN_AXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
                    .input(ModItems.GOLDEN_PICKAXE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.GOLDEN_PICKAXE_HEAD),
                            has(ModItems.GOLDEN_PICKAXE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
                    .input(ModItems.GOLDEN_SHOVEL_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.GOLDEN_SHOVEL_HEAD),
                            has(ModItems.GOLDEN_SHOVEL_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.COMBAT, Items.GOLDEN_SPEAR)
                    .input(ModItems.GOLDEN_SPEAR_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.GOLDEN_SPEAR_HEAD),
                            has(ModItems.GOLDEN_SPEAR_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
                    .input(ModItems.GOLDEN_HOE_HEAD)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.GOLDEN_HOE_HEAD),
                            has(ModItems.GOLDEN_HOE_HEAD))
                    .save(output);

            OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_SWORD)
                    .input(ModItems.GOLDEN_SWORD_BLADE)
                    .input(Items.STICK)
                    .unlockedBy(getHasName(ModItems.GOLDEN_SWORD_BLADE),
                            has(ModItems.GOLDEN_SWORD_BLADE))
                    .save(output);

            shapeless(RecipeCategory.MISC, ModItems.STEEL_NUGGET, 9)
                    .requires(ModItems.STEEL_INGOT)
                    .unlockedBy(getHasName(ModItems.STEEL_INGOT), has(ModItems.STEEL_INGOT))
                    .save(output, Overgeared.MOD_ID + ":steel_nugget_from_ingot");

            /*
             * ShapedRecipeJsonBuilder.create(RecipeCategory.MISC,
             * ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE, 2)
             * .pattern("axa")
             * .pattern("aba")
             * .pattern("aaa")
             * .input('a', ModItems.STEEL_INGOT)
             * .input('b', Items.DIAMOND)
             * .input('x', ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE)
             * .criterion(hasItem(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
             * conditionsFromItem(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE))
             * .offerTo(output);
             */
            /*
             * ShapedForgingRecipeBuilder.shaped(ForgingBookCategory.MISC,
             * ModBlocks.STEEL_BLOCK, 5)
             * .pattern("###")
             * .pattern("###")
             * .pattern("###")
             * .input('#', ModItems.STEEL_INGOT)
             * .criterion("has_steel_ingot",
             * conditionsFromItem(ItemTags.create(Identifier.of("forge", "ingots/steel"))))
             * .offerTo(output, Overgeared.MOD_ID + ":" +
             * getItemName(ModBlocks.STEEL_BLOCK) + "_from_forging_" +
             * getItemName(ModItems.STEEL_INGOT));
             */

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.IRON_PLATE, 3)
                    .tier(AnvilTier.STONE)
                    .setNeedQuenching(false)
                    .setQuality(false)
                    .pattern("#")
                    .input('#', Items.IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.COPPER_PLATE, 3)
                    .tier(AnvilTier.STONE)
                    .setNeedQuenching(false)
                    .setQuality(false)
                    .pattern("#")
                    .input('#', Items.COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            /*
             * ShapedForgingRecipeBuilder.shaped(ForgingBookCategory.MISC,
             * ModItems.STEEL_PLATE, 4)
             * .tier(AnvilTier.IRON)
             * .setQuality(false)
             * .pattern("#")
             * .input('#', ModItems.STEEL_INGOT)
             * .criterion(hasItem(ModItems.STEEL_INGOT),
             * conditionsFromItem(ModItems.STEEL_INGOT))
             * .offerTo(output);
             */

            // Iron Tools
            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_PICKAXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.PICKAXE.getId())
                    .pattern("###")
                    .input('#', ModItems.HEATED_IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_SWORD_BLADE, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SWORD.getId())
                    .pattern("#")
                    .pattern("#")
                    .input('#', ModItems.HEATED_IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_SHOVEL_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SHOVEL.getId())
                    .pattern("#")
                    .input('#', ModItems.HEATED_IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_SPEAR_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SPEAR.getId())
                    .pattern("n")
                    .pattern("#")
                    .input('#', ModItems.HEATED_IRON_INGOT)
                    .input('n', Items.IRON_NUGGET)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_HOE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.HOE.getId())
                    .pattern("##")
                    .input('#', ModItems.HEATED_IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_AXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.AXE.getId())
                    .pattern("##")
                    .pattern("# ")
                    .input('#', ModItems.HEATED_IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_AXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.AXE.getId())
                    .pattern("##")
                    .pattern(" #")
                    .input('#', ModItems.HEATED_IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output, Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "iron_axe_head_2"));

            // Copper Tools

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_PICKAXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.PICKAXE.getId())
                    .pattern("###")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_HAMMER_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setQuality(true)
                    .pattern("# ")
                    .pattern(" #")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_SWORD_BLADE, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SWORD.getId())
                    .pattern("#")
                    .pattern("#")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_SHOVEL_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SHOVEL.getId())
                    .pattern("#")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_SPEAR_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SPEAR.getId())
                    .pattern("n")
                    .pattern("#")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .input('n', Items.COPPER_NUGGET)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_HOE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.HOE.getId())
                    .pattern("##")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_AXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.AXE.getId())
                    .pattern("##")
                    .pattern("# ")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_AXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.AXE.getId())
                    .pattern("##")
                    .pattern(" #")
                    .input('#', ModItems.HEATED_COPPER_INGOT)
                    .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                    .offerTo(output, Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "copper_axe_head_2"));


            // Steel Tools
            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_PICKAXE_HEAD, 4)
                    .setBlueprint(ToolType.PICKAXE.getId())
                    .pattern("###")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_HAMMER_HEAD, 4)
                    .setQuality(true)
                    .setPolishing(false)
                    .pattern("# ")
                    .pattern(" #")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_SWORD_BLADE, 4)
                    .setBlueprint(ToolType.SWORD.getId())
                    .pattern("#")
                    .pattern("#")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_SHOVEL_HEAD, 4)
                    .setBlueprint(ToolType.SHOVEL.getId())
                    .pattern("#")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_SPEAR_HEAD, 4)
                    .setBlueprint(ToolType.SPEAR.getId())
                    .pattern("n")
                    .pattern("#")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .input('n', ModItems.STEEL_NUGGET)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_HOE_HEAD, 4)
                    .setBlueprint(ToolType.HOE.getId())
                    .pattern("##")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_AXE_HEAD, 4)
                    .setBlueprint(ToolType.AXE.getId())
                    .pattern("##")
                    .pattern("# ")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);
            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_AXE_HEAD, 4)
                    .setBlueprint(ToolType.AXE.getId())
                    .pattern("##")
                    .pattern(" #")
                    .input('#', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output, Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "steel_axe_head_2"));

            // Gold Tools
            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_PICKAXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.PICKAXE.getId())
                    .setNeedQuenching(false)
                    .pattern("###")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_SWORD_BLADE, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SWORD.getId())
                    .setNeedQuenching(false)
                    .pattern("#")
                    .pattern("#")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_SHOVEL_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SHOVEL.getId())
                    .setNeedQuenching(false)
                    .pattern("#")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_SPEAR_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.SPEAR.getId())
                    .setNeedQuenching(false)
                    .pattern("n")
                    .pattern("#")
                    .input('#', Items.GOLD_INGOT)
                    .input('n', Items.GOLD_NUGGET)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_HOE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.HOE.getId())
                    .setNeedQuenching(false)
                    .pattern("##")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_AXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.AXE.getId())
                    .setNeedQuenching(false)
                    .pattern("##")
                    .pattern("# ")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_AXE_HEAD, 3)
                    .tier(AnvilTier.STONE)
                    .setBlueprint(ToolType.AXE.getId())
                    .setNeedQuenching(false)
                    .pattern("##")
                    .pattern(" #")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output, Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "golden_axe_head_2"));

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_TONG, 2)
                    .tier(AnvilTier.STONE)
                    .setQuality(false)
                    .pattern("  x")
                    .pattern(" xx")
                    .pattern("x  ")
                    .input('x', ModItems.HEATED_IRON_INGOT)
                    .criterion("has_iron_ingot", has(Items.IRON_INGOT))
                    .offerTo(output);

            shapeless(RecipeCategory.TOOLS, ModItems.IRON_TONGS)
                    .requires(ModItems.IRON_TONG)
                    .requires(ModItems.IRON_TONG)
                    .unlockedBy("has_iron_ingot",
                            has(Items.IRON_INGOT))
                    .save(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_TONG, 2)
                    .setQuality(false)
                    .pattern("  x")
                    .pattern(" xx")
                    .pattern("x  ")
                    .input('x', ModItems.HEATED_STEEL_INGOT)
                    .criterion("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            shapeless(RecipeCategory.TOOLS, ModItems.STEEL_TONGS)
                    .requires(ModItems.STEEL_TONG)
                    .requires(ModItems.STEEL_TONG)
                    .unlockedBy("has_steel_ingot",
                            has(ModItems.STEEL_INGOT))
                    .save(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.HEATED_STEEL_INGOT, 3)
                    .setQuality(false)
                    .setNeedQuenching(false)
                    .pattern("#")
                    .input('#', ModItems.HEATED_CRUDE_STEEL)
                    .criterion(getHasName(ModItems.CRUDE_STEEL), has(ModItems.CRUDE_STEEL))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.BUCKET, 3)
                    .tier(AnvilTier.STONE)
                    .setQuality(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern(" # ")
                    .input('#', Items.IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.SHEARS, 3)
                    .tier(AnvilTier.STONE)
                    .needsMinigame(true)
                    .failedResult(Items.IRON_INGOT)
                    .setQuality(false)
                    .setNeedQuenching(false)
                    .pattern(" #")
                    .pattern("# ")
                    .input('#', Items.IRON_INGOT)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.NETHERITE_INGOT, 10)
                    .tier(AnvilTier.IRON)
                    .needsMinigame(true)
                    .failedResult(Items.NETHERITE_SCRAP, 4)
                    .qualityDifficulty(ForgingQuality.MASTER)
                    .setQuality(false)
                    .setNeedQuenching(false)
                    .pattern("#")
                    .input('#', ModItems.HEATED_NETHERITE_ALLOY)
                    .criterion(getHasName(Items.NETHERITE_SCRAP), has(Items.NETHERITE_SCRAP))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Blocks.CAULDRON, 5)
                    .setQuality(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("# #")
                    .pattern("###")
                    .input('#', ModItems.STEEL_PLATE)
                    .criterion(getHasName(ModItems.STEEL_PLATE), has(ModItems.STEEL_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_HELMET, 3)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .input('#', ModTags.Items.IRON_PLATES)
                    .criterion(getHasName(ModItems.IRON_PLATE), has(ModItems.IRON_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_CHESTPLATE, 5)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("###")
                    .pattern("###")
                    .input('#', ModTags.Items.IRON_PLATES)
                    .criterion(getHasName(ModItems.IRON_PLATE), has(ModItems.IRON_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_LEGGINGS, 4)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', ModTags.Items.IRON_PLATES)
                    .criterion(getHasName(ModItems.IRON_PLATE), has(ModItems.IRON_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_BOOTS, 3)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', ModTags.Items.IRON_PLATES)
                    .criterion(getHasName(ModItems.IRON_PLATE), has(ModItems.IRON_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_HELMET, 3)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .input('#', ModItems.STEEL_PLATE)
                    .criterion("has_steel_plate", has(ModItems.STEEL_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_CHESTPLATE, 5)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("###")
                    .pattern("###")
                    .input('#', ModItems.STEEL_PLATE)
                    .criterion("has_steel_plate", has(ModItems.STEEL_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_LEGGINGS, 4)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', ModItems.STEEL_PLATE)
                    .criterion("has_steel_plate", has(ModItems.STEEL_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_BOOTS, 3)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', ModItems.STEEL_PLATE)
                    .criterion("has_steel_plate", has(ModItems.STEEL_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.COPPER_HELMET, 3)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .input('#', ModTags.Items.COPPER_PLATES)
                    .criterion("has_copper_plate", has(ModItems.COPPER_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.COPPER_CHESTPLATE, 5)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("###")
                    .pattern("###")
                    .input('#', ModTags.Items.COPPER_PLATES)
                    .criterion("has_copper_plate", has(ModItems.COPPER_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.COPPER_LEGGINGS, 4)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', ModTags.Items.COPPER_PLATES)
                    .criterion("has_copper_plate", has(ModItems.COPPER_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.COPPER_BOOTS, 3)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', ModTags.Items.COPPER_PLATES)
                    .criterion("has_copper_plate", has(ModItems.COPPER_PLATE))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_HELMET, 3)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_CHESTPLATE, 5)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("###")
                    .pattern("###")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_LEGGINGS, 4)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_BOOTS, 3)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setNeedQuenching(false)
                    .pattern("# #")
                    .pattern("# #")
                    .input('#', Items.GOLD_INGOT)
                    .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.IRON_ARROW_HEAD, 2)
                    .tier(AnvilTier.STONE)
                    .setPolishing(false)
                    .setQuality(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern(" ##")
                    .pattern("# #")
                    .input('#', Items.IRON_NUGGET)
                    .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                    .offerTo(output);

            ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.STEEL_ARROW_HEAD, 3)
                    .tier(AnvilTier.IRON)
                    .setPolishing(false)
                    .setQuality(false)
                    .setNeedQuenching(false)
                    .pattern("###")
                    .pattern(" ##")
                    .pattern("# #")
                    .input('#', ModItems.STEEL_NUGGET)
                    .criterion(getHasName(ModItems.STEEL_INGOT), has(ModItems.STEEL_INGOT))
                    .offerTo(output);

            // Steel Axe to Diamond Axe
            diamondUpgrade(ModItems.STEEL_AXE, Items.DIAMOND_AXE, RecipeCategory.TOOLS);

            // Steel Pickaxe to Diamond Pickaxe
            diamondUpgrade(ModItems.STEEL_PICKAXE, Items.DIAMOND_PICKAXE, RecipeCategory.TOOLS);

            // Steel Shovel to Diamond Shovel
            diamondUpgrade(ModItems.STEEL_SHOVEL, Items.DIAMOND_SHOVEL, RecipeCategory.TOOLS);

            // Steel Spear to Diamond Spear
            diamondUpgrade(ModItems.STEEL_SPEAR, Items.DIAMOND_SPEAR, RecipeCategory.COMBAT);

            // Steel Hoe to Diamond Hoe
            diamondUpgrade(ModItems.STEEL_HOE, Items.DIAMOND_HOE, RecipeCategory.TOOLS);

            // Steel Sword to Diamond Sword
            diamondUpgrade(ModItems.STEEL_SWORD, Items.DIAMOND_SWORD, RecipeCategory.COMBAT);

            // Steel Helmet to Diamond Helmet
            diamondUpgrade(ModItems.STEEL_HELMET, Items.DIAMOND_HELMET, RecipeCategory.COMBAT);

            // Steel Chestplate to Diamond Chestplate
            diamondUpgrade(ModItems.STEEL_CHESTPLATE, Items.DIAMOND_CHESTPLATE, RecipeCategory.COMBAT);

            // Steel Leggings to Diamond Leggings
            diamondUpgrade(ModItems.STEEL_LEGGINGS, Items.DIAMOND_LEGGINGS, RecipeCategory.COMBAT);

            // Steel Boots to Diamond Boots
            diamondUpgrade(ModItems.STEEL_BOOTS, Items.DIAMOND_BOOTS, RecipeCategory.COMBAT);

            /*
             * FletchingRecipeBuilder.fletching(
             * Ingredient.ofItems(Items.FLINT),
             * Ingredient.ofItems(Items.STICK),
             * Ingredient.ofItems(Items.FEATHER),
             * Items.ARROW,
             * 4
             * ).withTippedResult(Items.TIPPED_ARROW)
             * .withLingeringResult("Potion", ModItems.LINGERING_ARROW)
             * .criterion("has_flint", conditionsFromItem(Items.FLINT)) // Add this unlock condition
             * .offerTo(output);
             */

            FletchingRecipeBuilder.fletching(
                            Ingredient.of(ModItems.IRON_ARROW_HEAD),
                            Ingredient.of(Items.STICK),
                            Ingredient.of(Items.FEATHER),
                            ModItems.IRON_UPGRADE_ARROW,
                            4).withTippedResult(ModItems.IRON_UPGRADE_ARROW)
                    .withLingeringResult(ModItems.IRON_UPGRADE_ARROW)
                    .criterion("has_iron_ingot", has(Items.IRON_INGOT)) // Add this unlock condition
                    .offerTo(output);

            FletchingRecipeBuilder.fletching(
                            Ingredient.of(ModItems.STEEL_ARROW_HEAD),
                            Ingredient.of(Items.STICK),
                            Ingredient.of(Items.FEATHER),
                            ModItems.STEEL_UPGRADE_ARROW,
                            4).withTippedResult(ModItems.STEEL_UPGRADE_ARROW)
                    .withLingeringResult(ModItems.STEEL_UPGRADE_ARROW)
                    .criterion("has_steel_ingot", has(ModItems.STEEL_INGOT)) // Add this unlock
                    // condition
                    .offerTo(output);
            FletchingRecipeBuilder.fletching(
                            Ingredient.of(ModItems.DIAMOND_SHARD),
                            Ingredient.of(Items.STICK),
                            Ingredient.of(Items.FEATHER),
                            ModItems.DIAMOND_UPGRADE_ARROW,
                            4)
                    .withTippedResult(ModItems.DIAMOND_UPGRADE_ARROW)
                    .withLingeringResult(ModItems.DIAMOND_UPGRADE_ARROW)
                    .criterion("has_diamond", has(Items.DIAMOND)) // Add this unlock condition
                    .offerTo(output);
            FletchingRecipeBuilder.fletching(
                            Ingredient.of(Items.GLOWSTONE_DUST),
                            Ingredient.of(Items.ARROW),
                            null, // no feather: any item / empty slot accepted
                            Items.SPECTRAL_ARROW,
                            1)
                    .criterion("has_arrow", has(Items.ARROW)) // Add this unlock condition
                    .offerTo(output);

            // ===== CAST SMELTING =====

            // COPPER
            ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_HAMMER_HEAD, 0.5F, 150)
                    .toolType("hammer").material("copper", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_hammer_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_SWORD_BLADE, 0.5F, 150)
                    .toolType("sword").material("copper", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_sword_blade"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_PICKAXE_HEAD, 0.5F, 150)
                    .toolType("pickaxe").material("copper", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_pickaxe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_AXE_HEAD, 0.5F, 150)
                    .toolType("axe").material("copper", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_axe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_SHOVEL_HEAD, 0.5F, 150)
                    .toolType("shovel").material("copper", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_shovel_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_SPEAR_HEAD, 0.5F, 150)
                    .toolType("spear").material("copper", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_spear_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_HOE_HEAD, 0.5F, 150)
                    .toolType("hoe").material("copper", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_hoe_head"));

            // IRON
            ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_SWORD_BLADE, 0.7F, 150)
                    .toolType("sword").material("iron", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_sword_blade"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_PICKAXE_HEAD, 0.7F, 150)
                    .toolType("pickaxe").material("iron", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_pickaxe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_AXE_HEAD, 0.7F, 150)
                    .toolType("axe").material("iron", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_axe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_SHOVEL_HEAD, 0.7F, 150)
                    .toolType("shovel").material("iron", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_shovel_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_SPEAR_HEAD, 0.7F, 150)
                    .toolType("spear").material("iron", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_spear_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_HOE_HEAD, 0.7F, 150)
                    .toolType("hoe").material("iron", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_hoe_head"));

            // GOLDEN
            ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_SWORD_BLADE, 1.0F, 150)
                    .toolType("sword").material("gold", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_sword_blade"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_PICKAXE_HEAD, 1.0F, 150)
                    .toolType("pickaxe").material("gold", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_pickaxe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_AXE_HEAD, 1.0F, 150)
                    .toolType("axe").material("gold", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_axe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_SHOVEL_HEAD, 1.0F, 150)
                    .toolType("shovel").material("gold", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_shovel_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_SPEAR_HEAD, 1.0F, 150)
                    .toolType("spear").material("gold", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_spear_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_HOE_HEAD, 1.0F, 150)
                    .toolType("hoe").material("gold", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_hoe_head"));

            // STEEL
            ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_HAMMER_HEAD, 0.9F, 150)
                    .toolType("hammer").material("steel", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_hammer_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_SWORD_BLADE, 0.9F, 150)
                    .toolType("sword").material("steel", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_sword_blade"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_PICKAXE_HEAD, 0.9F, 150)
                    .toolType("pickaxe").material("steel", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_pickaxe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_AXE_HEAD, 0.9F, 150)
                    .toolType("axe").material("steel", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_axe_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_SHOVEL_HEAD, 0.9F, 150)
                    .toolType("shovel").material("steel", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_shovel_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_SPEAR_HEAD, 0.9F, 150)
                    .toolType("spear").material("steel", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_spear_head"));

            ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_HOE_HEAD, 0.9F, 150)
                    .toolType("hoe").material("steel", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_hoe_head"));

            // ===== CAST BLASTING =====

            // COPPER
            ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_HAMMER_HEAD, 0.5F, 75)
                    .toolType("hammer").material("copper", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_hammer_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_SWORD_BLADE, 0.5F, 75)
                    .toolType("sword").material("copper", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_sword_blade"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_PICKAXE_HEAD, 0.5F, 75)
                    .toolType("pickaxe").material("copper", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_pickaxe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_AXE_HEAD, 0.5F, 75)
                    .toolType("axe").material("copper", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_axe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_SHOVEL_HEAD, 0.5F, 75)
                    .toolType("shovel").material("copper", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_shovel_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_SPEAR_HEAD, 0.5F, 75)
                    .toolType("spear").material("copper", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_spear_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_HOE_HEAD, 0.5F, 75)
                    .toolType("hoe").material("copper", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("copper_hoe_head"));

            // IRON
            ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_SWORD_BLADE, 0.7F, 75)
                    .toolType("sword").material("iron", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_sword_blade"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_PICKAXE_HEAD, 0.7F, 75)
                    .toolType("pickaxe").material("iron", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_pickaxe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_AXE_HEAD, 0.7F, 75)
                    .toolType("axe").material("iron", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_axe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_SHOVEL_HEAD, 0.7F, 75)
                    .toolType("shovel").material("iron", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_shovel_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_SPEAR_HEAD, 0.7F, 75)
                    .toolType("spear").material("iron", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_spear_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_HOE_HEAD, 0.7F, 75)
                    .toolType("hoe").material("iron", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("iron_hoe_head"));

            // GOLDEN
            ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_SWORD_BLADE, 1.0F, 75)
                    .toolType("sword").material("gold", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_sword_blade"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_PICKAXE_HEAD, 1.0F, 75)
                    .toolType("pickaxe").material("gold", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_pickaxe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_AXE_HEAD, 1.0F, 75)
                    .toolType("axe").material("gold", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_axe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_SHOVEL_HEAD, 1.0F, 75)
                    .toolType("shovel").material("gold", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_shovel_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_SPEAR_HEAD, 1.0F, 75)
                    .toolType("spear").material("gold", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_spear_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_HOE_HEAD, 1.0F, 75)
                    .toolType("hoe").material("gold", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("golden_hoe_head"));

            // STEEL
            ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_HAMMER_HEAD, 0.9F, 75)
                    .toolType("hammer").material("steel", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_hammer_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_SWORD_BLADE, 0.9F, 75)
                    .toolType("sword").material("steel", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_sword_blade"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_PICKAXE_HEAD, 0.9F, 75)
                    .toolType("pickaxe").material("steel", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_pickaxe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_AXE_HEAD, 0.9F, 75)
                    .toolType("axe").material("steel", 27).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_axe_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_SHOVEL_HEAD, 0.9F, 75)
                    .toolType("shovel").material("steel", 9).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_shovel_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_SPEAR_HEAD, 0.9F, 75)
                    .toolType("spear").material("steel", 10).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_spear_head"));

            ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_HOE_HEAD, 0.9F, 75)
                    .toolType("hoe").material("steel", 18).needsPolishing(true)
                    .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST))
                    .offerTo(output, rl("steel_hoe_head"));
            // Axe
            CastingRecipeBuilder.casting(ModItems.COPPER_AXE_HEAD, 0.4f, 150)
                    .toolType("axe")
                    .material("copper", 27)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            // Pickaxe
            CastingRecipeBuilder.casting(ModItems.COPPER_PICKAXE_HEAD, 0.4f, 150)
                    .toolType("pickaxe")
                    .material("copper", 27)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            // Shovel
            CastingRecipeBuilder.casting(ModItems.COPPER_SHOVEL_HEAD, 0.3f, 120)
                    .toolType("shovel")
                    .material("copper", 9)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            // Spear
            CastingRecipeBuilder.casting(ModItems.COPPER_SPEAR_HEAD, 0.3f, 120)
                    .toolType("spear")
                    .material("copper", 10)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            // Hoe
            CastingRecipeBuilder.casting(ModItems.COPPER_HOE_HEAD, 0.3f, 100)
                    .toolType("hoe")
                    .material("copper", 18)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            // Sword
            CastingRecipeBuilder.casting(ModItems.COPPER_SWORD_BLADE, 0.5f, 160)
                    .toolType("sword")
                    .material("copper", 18)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.COPPER_HAMMER_HEAD, 0.5f, 160)
                    .toolType("hammer")
                    .material("copper", 18)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.IRON_AXE_HEAD, 0.6f, 180)
                    .toolType("axe")
                    .material("iron", 27)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.IRON_PICKAXE_HEAD, 0.6f, 180)
                    .toolType("pickaxe")
                    .material("iron", 27)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.IRON_SHOVEL_HEAD, 0.5f, 140)
                    .toolType("shovel")
                    .material("iron", 9)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.IRON_SPEAR_HEAD, 0.5f, 140)
                    .toolType("spear")
                    .material("iron", 10)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.IRON_HOE_HEAD, 0.5f, 120)
                    .toolType("hoe")
                    .material("iron", 18)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.IRON_SWORD_BLADE, 0.7f, 190)
                    .toolType("sword")
                    .material("iron", 18)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);
            CastingRecipeBuilder.casting(ModItems.STEEL_AXE_HEAD, 0.8f, 220)
                    .toolType("axe")
                    .material("steel", 27)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.STEEL_PICKAXE_HEAD, 0.8f, 220)
                    .toolType("pickaxe")
                    .material("steel", 27)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.STEEL_SHOVEL_HEAD, 0.7f, 180)
                    .toolType("shovel")
                    .material("steel", 9)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.STEEL_SPEAR_HEAD, 0.7f, 180)
                    .toolType("spear")
                    .material("steel", 10)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.STEEL_HOE_HEAD, 0.7f, 160)
                    .toolType("hoe")
                    .material("steel", 18)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.STEEL_SWORD_BLADE, 0.9f, 240)
                    .toolType("sword")
                    .material("steel", 18)
                    .needsPolishing(true)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.STEEL_HAMMER_HEAD, 0.9f, 240)
                    .toolType("hammer")
                    .material("steel", 18)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.GOLDEN_AXE_HEAD, 0.3f, 100)
                    .toolType("axe")
                    .material("gold", 27)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.GOLDEN_PICKAXE_HEAD, 0.3f, 100)
                    .toolType("pickaxe")
                    .material("gold", 27)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.GOLDEN_SHOVEL_HEAD, 0.2f, 80)
                    .toolType("shovel")
                    .material("gold", 9)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.GOLDEN_SPEAR_HEAD, 0.2f, 80)
                    .toolType("spear")
                    .material("gold", 10)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.GOLDEN_HOE_HEAD, 0.2f, 70)
                    .toolType("hoe")
                    .material("gold", 18)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

            CastingRecipeBuilder.casting(ModItems.GOLDEN_SWORD_BLADE, 0.4f, 110)
                    .toolType("sword")
                    .material("gold", 18)
                    .needsPolishing(false)
                    .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                    .offerTo(output);

        }
    }
}
