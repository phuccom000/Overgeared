package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.recipe.ForgingBookCategory;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends FabricRecipeProvider {
    /* private static final List<ItemConvertible> RUBY_SMELTABLES = List.of(ModItems.RAW_RUBY,
             ModBlocks.RUBY_ORE, ModBlocks.DEEPSLATE_RUBY_ORE, ModBlocks.NETHER_RUBY_ORE, ModBlocks.END_STONE_RUBY_ORE);*/
    private static final List<ItemConvertible> STEEL_SMELTABLES = List.of(
            ModItems.CRUDE_STEEL);

    private static final List<ItemConvertible> COPPER_SMELTABLES = List.of(
            Items.COPPER_INGOT);
    private static final List<ItemConvertible> IRON_SMELTABLES = List.of(
            Items.IRON_INGOT);

    private static final List<ItemConvertible> IRON_SOURCE = List.of(
            Items.RAW_IRON,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.IRON_ORE);

    private static final List<ItemConvertible> COPPER_SOURCE = List.of(
            Items.RAW_COPPER,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.COPPER_ORE);

    private static final List<ItemConvertible> IRON_HEADS = List.of(
            ModItems.IRON_HOE_HEAD,
            ModItems.IRON_PICKAXE_HEAD,
            ModItems.IRON_SWORD_BLADE,
            ModItems.IRON_AXE_HEAD,
            ModItems.IRON_SHOVEL_HEAD,
            ModItems.IRON_ARROW_HEAD

    );
    private static final List<ItemConvertible> STEEL_HEADS = List.of(
            ModItems.STEEL_HOE_HEAD,
            ModItems.STEEL_PICKAXE_HEAD,
            ModItems.STEEL_SWORD_BLADE,
            ModItems.STEEL_AXE_HEAD,
            ModItems.STEEL_SHOVEL_HEAD,
            ModItems.STEEL_HOE,
            ModItems.STEEL_PICKAXE,
            ModItems.STEEL_SWORD,
            ModItems.STEEL_AXE,
            ModItems.STEEL_SHOVEL,
            ModItems.STEEL_HELMET,
            ModItems.STEEL_CHESTPLATE,
            ModItems.STEEL_LEGGINGS,
            ModItems.STEEL_BOOTS,
            ModItems.STEEL_ARROW_HEAD);

    private static final List<ItemConvertible> COPPER_HEADS = List.of(
            ModItems.COPPER_HOE_HEAD,
            ModItems.COPPER_PICKAXE_HEAD,
            ModItems.COPPER_SWORD_BLADE,
            ModItems.COPPER_AXE_HEAD,
            ModItems.COPPER_SHOVEL_HEAD,
            ModItems.COPPER_HOE,
            ModItems.COPPER_PICKAXE,
            ModItems.COPPER_SWORD,
            ModItems.COPPER_AXE,
            ModItems.COPPER_SHOVEL,
            ModItems.COPPER_HELMET,
            ModItems.COPPER_CHESTPLATE,
            ModItems.COPPER_LEGGINGS,
            ModItems.COPPER_BOOTS

    );
    private static final List<ItemConvertible> GOLDEN_HEADS = List.of(
            ModItems.GOLDEN_HOE_HEAD,
            ModItems.GOLDEN_PICKAXE_HEAD,
            ModItems.GOLDEN_SWORD_BLADE,
            ModItems.GOLDEN_AXE_HEAD,
            ModItems.GOLDEN_SHOVEL_HEAD

    );

    public ModRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        offerBlasting(exporter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_CRUDE_STEEL, 0, 100,
                "steel_ingot");
        offerBlasting(exporter, COPPER_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_COPPER_INGOT, 0, 70,
                "copper_ingot");
        offerBlasting(exporter, IRON_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT, 0, 100,
                "iron_ingot");
        offerSmelting(exporter, COPPER_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_COPPER_INGOT, 0, 140,
                "copper_ingot");
        offerBlasting(exporter, IRON_SOURCE, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT, 0.7f, 100,
                "iron_ingot");
        offerBlasting(exporter, COPPER_SOURCE, RecipeCategory.MISC, ModItems.HEATED_COPPER_INGOT, 0.7f, 100,
                "copper_ingot");
        offerSmelting(exporter, IRON_SOURCE, RecipeCategory.MISC, Items.IRON_INGOT, 0.7f, 200, "iron_ingot");
        offerSmelting(exporter, COPPER_SOURCE, RecipeCategory.MISC, Items.COPPER_INGOT, 0.7f, 200, "copper_ingot");
        offerSmelting(exporter, IRON_HEADS, RecipeCategory.MISC, Items.IRON_NUGGET, 0.1f, 200, null);
        offerBlasting(exporter, IRON_HEADS, RecipeCategory.MISC, Items.IRON_NUGGET, 0.1f, 100, null);
        offerSmelting(exporter, GOLDEN_HEADS, RecipeCategory.MISC, Items.GOLD_NUGGET, 0.1f, 200, null);
        offerBlasting(exporter, GOLDEN_HEADS, RecipeCategory.MISC, Items.GOLD_NUGGET, 0.1f, 100, null);
        offerBlasting(exporter, STEEL_HEADS, RecipeCategory.MISC, ModItems.STEEL_NUGGET, 0.1f, 200, null);
        offerSmelting(exporter, COPPER_HEADS, RecipeCategory.MISC, ModItems.COPPER_NUGGET, 0.1f, 200, null);
        offerBlasting(exporter, COPPER_HEADS, RecipeCategory.MISC, ModItems.COPPER_NUGGET, 0.1f, 100, null);
        /*offerSmelting(exporter, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY,
                0.7f, 200, "ruby");
        offerBlasting(exporter, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY,
                0.7f, 100, "ruby");

        offerReversibleCompactingRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, ModItems.RUBY, RecipeCategory.DECORATIONS,
                ModBlocks.RUBY_BLOCK);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.RAW_RUBY, 1)
                .pattern("SSS")
                .pattern("SRS")
                .pattern("SSS")
                .input('S', Items.STONE)
                .input('R', ModItems.RUBY)
                .criterion(hasItem(Items.STONE), conditionsFromItem(Items.STONE))
                .criterion(hasItem(ModItems.RUBY), conditionsFromItem(ModItems.RUBY))
                .offerTo(exporter, new Identifier(getRecipeName(ModItems.RAW_RUBY)));*/

        offerReversibleCompactingRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, ModItems.STEEL_INGOT, RecipeCategory.DECORATIONS,
                ModBlocks.STEEL_BLOCK);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.STEEL_INGOT)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .input('#', ModItems.STEEL_NUGGET)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .criterion(hasItem(ModItems.STEEL_NUGGET), conditionsFromItem(ModItems.STEEL_NUGGET))
                .offerTo(exporter, Overgeared.MOD_ID + ":steel_ingot_from_nuggets");

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Items.COPPER_INGOT)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .input('#', ModItems.COPPER_NUGGET)
                .criterion("has_copper_ingot",
                        conditionsFromItem(Items.COPPER_INGOT))
                .criterion(hasItem(ModItems.COPPER_NUGGET), conditionsFromItem(ModItems.COPPER_NUGGET))
                .offerTo(exporter, Overgeared.MOD_ID + ":copper_ingot_from_nuggets");

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.WOODEN_TONGS)
                .pattern(" # ")
                .pattern("###")
                .pattern(" # ")
                .input('#', Items.STICK)
                .criterion("has_hot_item", conditionsFromTag(ModTags.Items.HOT_ITEMS))
                .criterion("has_heated_metal", conditionsFromTag(ModTags.Items.HEATED_METALS))
                .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.EMPTY_BLUEPRINT)
                .input(Items.PAPER)
                .input(Items.PAPER)
                .input(Items.PAPER)
                .input(Items.BLUE_DYE)
                .criterion("has_paper", conditionsFromItem(Items.PAPER))
                .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.DRAFTING_TABLE)
                .input(Blocks.CRAFTING_TABLE)
                .input(ModItems.EMPTY_BLUEPRINT)
                .criterion(hasItem(Blocks.CRAFTING_TABLE), conditionsFromItem(Items.CRAFTING_TABLE))
                .criterion(hasItem(ModItems.EMPTY_BLUEPRINT),
                        conditionsFromItem(ModItems.EMPTY_BLUEPRINT))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_AXE)
                .input(ModItems.STONE_AXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STONE_AXE_HEAD),
                        conditionsFromItem(ModItems.STONE_AXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_PICKAXE)
                .input(ModItems.STONE_PICKAXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STONE_PICKAXE_HEAD),
                        conditionsFromItem(ModItems.STONE_PICKAXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_SHOVEL)
                .input(ModItems.STONE_SHOVEL_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STONE_SHOVEL_HEAD),
                        conditionsFromItem(ModItems.STONE_SHOVEL_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_HOE)
                .input(ModItems.STONE_HOE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STONE_HOE_HEAD),
                        conditionsFromItem(ModItems.STONE_HOE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_SWORD)
                .input(ModItems.STONE_SWORD_BLADE)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STONE_SWORD_BLADE),
                        conditionsFromItem(ModItems.STONE_SWORD_BLADE))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_AXE)
                .input(ModItems.IRON_AXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.IRON_AXE_HEAD), conditionsFromItem(ModItems.IRON_AXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
                .input(ModItems.IRON_PICKAXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.IRON_PICKAXE_HEAD),
                        conditionsFromItem(ModItems.IRON_PICKAXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
                .input(ModItems.IRON_SHOVEL_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.IRON_SHOVEL_HEAD),
                        conditionsFromItem(ModItems.IRON_SHOVEL_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_HOE)
                .input(ModItems.IRON_HOE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.IRON_HOE_HEAD), conditionsFromItem(ModItems.IRON_HOE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_SWORD)
                .input(ModItems.IRON_SWORD_BLADE)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.IRON_SWORD_BLADE),
                        conditionsFromItem(ModItems.IRON_SWORD_BLADE))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_AXE)
                .input(ModItems.STEEL_AXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STEEL_AXE_HEAD),
                        conditionsFromItem(ModItems.STEEL_AXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_PICKAXE)
                .input(ModItems.STEEL_PICKAXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STEEL_PICKAXE_HEAD),
                        conditionsFromItem(ModItems.STEEL_PICKAXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_SHOVEL)
                .input(ModItems.STEEL_SHOVEL_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STEEL_SHOVEL_HEAD),
                        conditionsFromItem(ModItems.STEEL_SHOVEL_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_HOE)
                .input(ModItems.STEEL_HOE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STEEL_HOE_HEAD),
                        conditionsFromItem(ModItems.STEEL_HOE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_SWORD)
                .input(ModItems.STEEL_SWORD_BLADE)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.STEEL_SWORD_BLADE),
                        conditionsFromItem(ModItems.STEEL_SWORD_BLADE))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_AXE)
                .input(ModItems.COPPER_AXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.COPPER_AXE_HEAD),
                        conditionsFromItem(ModItems.COPPER_AXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_PICKAXE)
                .input(ModItems.COPPER_PICKAXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.COPPER_PICKAXE_HEAD),
                        conditionsFromItem(ModItems.COPPER_PICKAXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_SHOVEL)
                .input(ModItems.COPPER_SHOVEL_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.COPPER_SHOVEL_HEAD),
                        conditionsFromItem(ModItems.COPPER_SHOVEL_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_HOE)
                .input(ModItems.COPPER_HOE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.COPPER_HOE_HEAD),
                        conditionsFromItem(ModItems.COPPER_HOE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_SWORD)
                .input(ModItems.COPPER_SWORD_BLADE)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.COPPER_SWORD_BLADE),
                        conditionsFromItem(ModItems.COPPER_SWORD_BLADE))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
                .input(ModItems.GOLDEN_AXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.GOLDEN_AXE_HEAD),
                        conditionsFromItem(ModItems.GOLDEN_AXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
                .input(ModItems.GOLDEN_PICKAXE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.GOLDEN_PICKAXE_HEAD),
                        conditionsFromItem(ModItems.GOLDEN_PICKAXE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
                .input(ModItems.GOLDEN_SHOVEL_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.GOLDEN_SHOVEL_HEAD),
                        conditionsFromItem(ModItems.GOLDEN_SHOVEL_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
                .input(ModItems.GOLDEN_HOE_HEAD)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.GOLDEN_HOE_HEAD),
                        conditionsFromItem(ModItems.GOLDEN_HOE_HEAD))
                .offerTo(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_SWORD)
                .input(ModItems.GOLDEN_SWORD_BLADE)
                .input(Items.STICK)
                .criterion(hasItem(ModItems.GOLDEN_SWORD_BLADE),
                        conditionsFromItem(ModItems.GOLDEN_SWORD_BLADE))
                .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.STEEL_NUGGET, 9)
                .input(ModItems.STEEL_INGOT)
                .criterion(hasItem(ModItems.STEEL_INGOT), conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter, Overgeared.MOD_ID + ":steel_nugget_from_ingot");

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
         * .offerTo(exporter);
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
         * .offerTo(exporter, Overgeared.MOD_ID + ":" +
         * getItemName(ModBlocks.STEEL_BLOCK) + "_from_forging_" +
         * getItemName(ModItems.STEEL_INGOT));
         */

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.IRON_PLATE, 3)
                .tier(AnvilTier.STONE)
                .setNeedQuenching(false)
                .setQuality(false)
                .pattern("#")
                .input('#', Items.IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.COPPER_PLATE, 3)
                .tier(AnvilTier.STONE)
                .setNeedQuenching(false)
                .setQuality(false)
                .pattern("#")
                .input('#', Items.COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        /*
         * ShapedForgingRecipeBuilder.shaped(ForgingBookCategory.MISC,
         * ModItems.STEEL_PLATE, 4)
         * .tier(AnvilTier.IRON)
         * .setQuality(false)
         * .pattern("#")
         * .input('#', ModItems.STEEL_INGOT)
         * .criterion(hasItem(ModItems.STEEL_INGOT),
         * conditionsFromItem(ModItems.STEEL_INGOT))
         * .offerTo(exporter);
         */

        // Iron Tools
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_PICKAXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.PICKAXE.getId())
                .pattern("###")
                .input('#', ModItems.HEATED_IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_SWORD_BLADE, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.SWORD.getId())
                .pattern("#")
                .pattern("#")
                .input('#', ModItems.HEATED_IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_SHOVEL_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.SHOVEL.getId())
                .pattern("#")
                .input('#', ModItems.HEATED_IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_HOE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.HOE.getId())
                .pattern("##")
                .input('#', ModItems.HEATED_IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_AXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern("# ")
                .input('#', ModItems.HEATED_IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_AXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern(" #")
                .input('#', ModItems.HEATED_IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter, new Identifier(Overgeared.MOD_ID, "iron_axe_head_2"));

        // Copper Tools

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_PICKAXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.PICKAXE.getId())
                .pattern("###")
                .input('#', ModItems.HEATED_COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_HAMMER_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setQuality(true)
                .pattern("# ")
                .pattern(" #")
                .input('#', ModItems.HEATED_COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_SWORD_BLADE, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.SWORD.getId())
                .pattern("#")
                .pattern("#")
                .input('#', ModItems.HEATED_COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_SHOVEL_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.SHOVEL.getId())
                .pattern("#")
                .input('#', ModItems.HEATED_COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_HOE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.HOE.getId())
                .pattern("##")
                .input('#', ModItems.HEATED_COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_AXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern("# ")
                .input('#', ModItems.HEATED_COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_AXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern(" #")
                .input('#', ModItems.HEATED_COPPER_INGOT)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter, new Identifier(Overgeared.MOD_ID, "copper_axe_head_2"));


        // Steel Tools
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_PICKAXE_HEAD, 4)
                .setBlueprint(ToolType.PICKAXE.getId())
                .pattern("###")
                .input('#', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_HAMMER_HEAD, 4)
                .setQuality(true)
                .setPolishing(false)
                .pattern("# ")
                .pattern(" #")
                .input('#', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_SWORD_BLADE, 4)
                .setBlueprint(ToolType.SWORD.getId())
                .pattern("#")
                .pattern("#")
                .input('#', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_SHOVEL_HEAD, 4)
                .setBlueprint(ToolType.SHOVEL.getId())
                .pattern("#")
                .input('#', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_HOE_HEAD, 4)
                .setBlueprint(ToolType.HOE.getId())
                .pattern("##")
                .input('#', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_AXE_HEAD, 4)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern("# ")
                .input('#', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_AXE_HEAD, 4)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern(" #")
                .input('#', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter, new Identifier(Overgeared.MOD_ID, "steel_axe_head_2"));

        // Gold Tools
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_PICKAXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.PICKAXE.getId())
                .setNeedQuenching(false)
                .pattern("###")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_SWORD_BLADE, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.SWORD.getId())
                .setNeedQuenching(false)
                .pattern("#")
                .pattern("#")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_SHOVEL_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.SHOVEL.getId())
                .setNeedQuenching(false)
                .pattern("#")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_HOE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.HOE.getId())
                .setNeedQuenching(false)
                .pattern("##")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_AXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.AXE.getId())
                .setNeedQuenching(false)
                .pattern("##")
                .pattern("# ")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_AXE_HEAD, 3)
                .tier(AnvilTier.STONE)
                .setBlueprint(ToolType.AXE.getId())
                .setNeedQuenching(false)
                .pattern("##")
                .pattern(" #")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter, new Identifier(Overgeared.MOD_ID, "golden_axe_head_2"));

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_TONG, 2)
                .tier(AnvilTier.STONE)
                .setQuality(false)
                .pattern("  x")
                .pattern(" xx")
                .pattern("x  ")
                .input('x', ModItems.HEATED_IRON_INGOT)
                .criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.IRON_TONGS)
                .input(ModItems.IRON_TONG)
                .input(ModItems.IRON_TONG)
                .criterion("has_iron_ingot",
                        conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_TONG, 2)
                .setQuality(false)
                .pattern("  x")
                .pattern(" xx")
                .pattern("x  ")
                .input('x', ModItems.HEATED_STEEL_INGOT)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_TONGS)
                .input(ModItems.STEEL_TONG)
                .input(ModItems.STEEL_TONG)
                .criterion("has_steel_ingot",
                        conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.HEATED_STEEL_INGOT, 3)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("#")
                .input('#', ModItems.HEATED_CRUDE_STEEL)
                .criterion(hasItem(ModItems.CRUDE_STEEL), conditionsFromItem(ModItems.CRUDE_STEEL))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.BUCKET, 3)
                .tier(AnvilTier.STONE)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern(" # ")
                .input('#', Items.IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.SHEARS, 3)
                .tier(AnvilTier.STONE)
                .needsMinigame(true)
                .failedResult(Items.IRON_INGOT)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern(" #")
                .pattern("# ")
                .input('#', Items.IRON_INGOT)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.NETHERITE_INGOT, 10)
                .tier(AnvilTier.IRON)
                .needsMinigame(true)
                .failedResult(Items.NETHERITE_SCRAP, 4)
                .qualityDifficulty(ForgingQuality.MASTER)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("#")
                .input('#', ModItems.HEATED_NETHERITE_ALLOY)
                .criterion(hasItem(Items.NETHERITE_SCRAP), conditionsFromItem(Items.NETHERITE_SCRAP))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Blocks.CAULDRON, 5)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .pattern("###")
                .input('#', ModItems.STEEL_PLATE)
                .criterion(hasItem(ModItems.STEEL_PLATE), conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_HELMET, 3)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(hasItem(ModItems.IRON_PLATE), conditionsFromItem(ModItems.IRON_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_CHESTPLATE, 5)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(hasItem(ModItems.IRON_PLATE), conditionsFromItem(ModItems.IRON_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_LEGGINGS, 4)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(hasItem(ModItems.IRON_PLATE), conditionsFromItem(ModItems.IRON_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_BOOTS, 3)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(hasItem(ModItems.IRON_PLATE), conditionsFromItem(ModItems.IRON_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_HELMET, 3)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', ModItems.STEEL_PLATE)
                .criterion("has_steel_plate", conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_CHESTPLATE, 5)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', ModItems.STEEL_PLATE)
                .criterion("has_steel_plate", conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_LEGGINGS, 4)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', ModItems.STEEL_PLATE)
                .criterion("has_steel_plate", conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_BOOTS, 3)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', ModItems.STEEL_PLATE)
                .criterion("has_steel_plate", conditionsFromItem(ModItems.STEEL_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_HELMET, 3)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", conditionsFromItem(ModItems.COPPER_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_CHESTPLATE, 5)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", conditionsFromItem(ModItems.COPPER_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_LEGGINGS, 4)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", conditionsFromItem(ModItems.COPPER_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_BOOTS, 3)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", conditionsFromItem(ModItems.COPPER_PLATE))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_HELMET, 3)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_CHESTPLATE, 5)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_LEGGINGS, 4)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_BOOTS, 3)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', Items.GOLD_INGOT)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.IRON_ARROW_HEAD, 2)
                .tier(AnvilTier.STONE)
                .setPolishing(false)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern(" ##")
                .pattern("# #")
                .input('#', Items.IRON_NUGGET)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.STEEL_ARROW_HEAD, 3)
                .tier(AnvilTier.IRON)
                .setPolishing(false)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern(" ##")
                .pattern("# #")
                .input('#', ModItems.STEEL_NUGGET)
                .criterion(hasItem(ModItems.STEEL_INGOT), conditionsFromItem(ModItems.STEEL_INGOT))
                .offerTo(exporter);

        // Steel Axe to Diamond Axe
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_AXE),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_AXE)
                .criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_axe"));

        // Steel Pickaxe to Diamond Pickaxe
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_PICKAXE),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_PICKAXE)
                .criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_pickaxe"));

        // Steel Shovel to Diamond Shovel
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_SHOVEL),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_SHOVEL).criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_shovel"));

        // Steel Hoe to Diamond Hoe
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_HOE),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_HOE).criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_hoe"));

        // Steel Sword to Diamond Sword
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_SWORD),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_SWORD).criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_sword"));

        // Steel Helmet to Diamond Helmet
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_HELMET),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_HELMET).criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_helmet"));

        // Steel Chestplate to Diamond Chestplate
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_CHESTPLATE),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_CHESTPLATE).criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_chestplate"));

        // Steel Leggings to Diamond Leggings
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_LEGGINGS),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_LEGGINGS).criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_leggings"));

        // Steel Boots to Diamond Boots
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModItems.STEEL_BOOTS),
                        Ingredient.ofItems(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_BOOTS).criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, Identifier.of("minecraft", "diamond_boots"));

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
         * .offerTo(exporter);
         */

        FletchingRecipeBuilder.fletching(
                        Ingredient.ofItems(ModItems.IRON_ARROW_HEAD),
                        Ingredient.ofItems(Items.STICK),
                        Ingredient.ofItems(Items.FEATHER),
                        ModItems.IRON_UPGRADE_ARROW,
                        4).withTippedResult(ModItems.IRON_UPGRADE_ARROW)
                .withLingeringResult(ModItems.IRON_UPGRADE_ARROW)
                .criterion("has_iron_ingot", conditionsFromItem(Items.IRON_INGOT)) // Add this unlock condition
                .offerTo(exporter);

        FletchingRecipeBuilder.fletching(
                        Ingredient.ofItems(ModItems.STEEL_ARROW_HEAD),
                        Ingredient.ofItems(Items.STICK),
                        Ingredient.ofItems(Items.FEATHER),
                        ModItems.STEEL_UPGRADE_ARROW,
                        4).withTippedResult(ModItems.STEEL_UPGRADE_ARROW)
                .withLingeringResult(ModItems.STEEL_UPGRADE_ARROW)
                .criterion("has_steel_ingot", conditionsFromItem(ModItems.STEEL_INGOT)) // Add this unlock
                // condition
                .offerTo(exporter);
        FletchingRecipeBuilder.fletching(
                        Ingredient.ofItems(ModItems.DIAMOND_SHARD),
                        Ingredient.ofItems(Items.STICK),
                        Ingredient.ofItems(Items.FEATHER),
                        ModItems.DIAMOND_UPGRADE_ARROW,
                        4)
                .withTippedResult(ModItems.DIAMOND_UPGRADE_ARROW)
                .withLingeringResult(ModItems.DIAMOND_UPGRADE_ARROW)
                .criterion("has_diamond", conditionsFromItem(Items.DIAMOND)) // Add this unlock condition
                .offerTo(exporter);
        FletchingRecipeBuilder.fletching(
                        Ingredient.ofItems(Items.GLOWSTONE_DUST),
                        Ingredient.ofItems(Items.ARROW),
                        Ingredient.EMPTY,
                        Items.SPECTRAL_ARROW,
                        1)
                .criterion("has_arrow", conditionsFromItem(Items.ARROW)) // Add this unlock condition
                .offerTo(exporter);

        // ===== CAST SMELTING =====

        // COPPER
        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_HAMMER_HEAD, 0.5F, 150)
                .toolType("hammer").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_hammer_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_SWORD_BLADE, 0.5F, 150)
                .toolType("sword").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_PICKAXE_HEAD, 0.5F, 150)
                .toolType("pickaxe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_AXE_HEAD, 0.5F, 150)
                .toolType("axe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_SHOVEL_HEAD, 0.5F, 150)
                .toolType("shovel").material("copper", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_HOE_HEAD, 0.5F, 150)
                .toolType("hoe").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_hoe_head"));

        // IRON
        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_SWORD_BLADE, 0.7F, 150)
                .toolType("sword").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_PICKAXE_HEAD, 0.7F, 150)
                .toolType("pickaxe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_AXE_HEAD, 0.7F, 150)
                .toolType("axe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_SHOVEL_HEAD, 0.7F, 150)
                .toolType("shovel").material("iron", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_HOE_HEAD, 0.7F, 150)
                .toolType("hoe").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_hoe_head"));

        // GOLDEN
        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_SWORD_BLADE, 1.0F, 150)
                .toolType("sword").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_PICKAXE_HEAD, 1.0F, 150)
                .toolType("pickaxe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_AXE_HEAD, 1.0F, 150)
                .toolType("axe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_SHOVEL_HEAD, 1.0F, 150)
                .toolType("shovel").material("gold", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_HOE_HEAD, 1.0F, 150)
                .toolType("hoe").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_hoe_head"));

        // STEEL
        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_HAMMER_HEAD, 0.9F, 150)
                .toolType("hammer").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_hammer_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_SWORD_BLADE, 0.9F, 150)
                .toolType("sword").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_PICKAXE_HEAD, 0.9F, 150)
                .toolType("pickaxe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_AXE_HEAD, 0.9F, 150)
                .toolType("axe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_SHOVEL_HEAD, 0.9F, 150)
                .toolType("shovel").material("steel", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_HOE_HEAD, 0.9F, 150)
                .toolType("hoe").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_hoe_head"));

        // ===== CAST BLASTING =====

        // COPPER
        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_HAMMER_HEAD, 0.5F, 75)
                .toolType("hammer").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_hammer_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_SWORD_BLADE, 0.5F, 75)
                .toolType("sword").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_PICKAXE_HEAD, 0.5F, 75)
                .toolType("pickaxe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_AXE_HEAD, 0.5F, 75)
                .toolType("axe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_SHOVEL_HEAD, 0.5F, 75)
                .toolType("shovel").material("copper", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_HOE_HEAD, 0.5F, 75)
                .toolType("hoe").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("copper_hoe_head"));

        // IRON
        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_SWORD_BLADE, 0.7F, 75)
                .toolType("sword").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_PICKAXE_HEAD, 0.7F, 75)
                .toolType("pickaxe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_AXE_HEAD, 0.7F, 75)
                .toolType("axe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_SHOVEL_HEAD, 0.7F, 75)
                .toolType("shovel").material("iron", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_HOE_HEAD, 0.7F, 75)
                .toolType("hoe").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("iron_hoe_head"));

        // GOLDEN
        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_SWORD_BLADE, 1.0F, 75)
                .toolType("sword").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_PICKAXE_HEAD, 1.0F, 75)
                .toolType("pickaxe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_AXE_HEAD, 1.0F, 75)
                .toolType("axe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_SHOVEL_HEAD, 1.0F, 75)
                .toolType("shovel").material("gold", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_HOE_HEAD, 1.0F, 75)
                .toolType("hoe").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("golden_hoe_head"));

        // STEEL
        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_HAMMER_HEAD, 0.9F, 75)
                .toolType("hammer").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_hammer_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_SWORD_BLADE, 0.9F, 75)
                .toolType("sword").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_PICKAXE_HEAD, 0.9F, 75)
                .toolType("pickaxe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_AXE_HEAD, 0.9F, 75)
                .toolType("axe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_SHOVEL_HEAD, 0.9F, 75)
                .toolType("shovel").material("steel", 9).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_HOE_HEAD, 0.9F, 75)
                .toolType("hoe").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", conditionsFromItem(ModItems.UNFIRED_TOOL_CAST))
                .offerTo(exporter, rl("steel_hoe_head"));
        // Axe
        CastingRecipeBuilder.casting(ModItems.COPPER_AXE_HEAD, 0.4f, 150)
                .toolType("axe")
                .material("copper", 27)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Pickaxe
        CastingRecipeBuilder.casting(ModItems.COPPER_PICKAXE_HEAD, 0.4f, 150)
                .toolType("pickaxe")
                .material("copper", 27)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Shovel
        CastingRecipeBuilder.casting(ModItems.COPPER_SHOVEL_HEAD, 0.3f, 120)
                .toolType("shovel")
                .material("copper", 9)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Hoe
        CastingRecipeBuilder.casting(ModItems.COPPER_HOE_HEAD, 0.3f, 100)
                .toolType("hoe")
                .material("copper", 18)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Sword
        CastingRecipeBuilder.casting(ModItems.COPPER_SWORD_BLADE, 0.5f, 160)
                .toolType("sword")
                .material("copper", 18)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.COPPER_HAMMER_HEAD, 0.5f, 160)
                .toolType("hammer")
                .material("copper", 18)
                .needsPolishing(false)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_AXE_HEAD, 0.6f, 180)
                .toolType("axe")
                .material("iron", 27)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_PICKAXE_HEAD, 0.6f, 180)
                .toolType("pickaxe")
                .material("iron", 27)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_SHOVEL_HEAD, 0.5f, 140)
                .toolType("shovel")
                .material("iron", 9)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_HOE_HEAD, 0.5f, 120)
                .toolType("hoe")
                .material("iron", 18)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_SWORD_BLADE, 0.7f, 190)
                .toolType("sword")
                .material("iron", 18)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);
        CastingRecipeBuilder.casting(ModItems.STEEL_AXE_HEAD, 0.8f, 220)
                .toolType("axe")
                .material("steel", 27)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_PICKAXE_HEAD, 0.8f, 220)
                .toolType("pickaxe")
                .material("steel", 27)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_SHOVEL_HEAD, 0.7f, 180)
                .toolType("shovel")
                .material("steel", 9)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_HOE_HEAD, 0.7f, 160)
                .toolType("hoe")
                .material("steel", 18)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_SWORD_BLADE, 0.9f, 240)
                .toolType("sword")
                .material("steel", 18)
                .needsPolishing(true)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_HAMMER_HEAD, 0.9f, 240)
                .toolType("hammer")
                .material("steel", 18)
                .needsPolishing(false)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_AXE_HEAD, 0.3f, 100)
                .toolType("axe")
                .material("gold", 27)
                .needsPolishing(false)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_PICKAXE_HEAD, 0.3f, 100)
                .toolType("pickaxe")
                .material("gold", 27)
                .needsPolishing(false)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_SHOVEL_HEAD, 0.2f, 80)
                .toolType("shovel")
                .material("gold", 9)
                .needsPolishing(false)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_HOE_HEAD, 0.2f, 70)
                .toolType("hoe")
                .material("gold", 18)
                .needsPolishing(false)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_SWORD_BLADE, 0.4f, 110)
                .toolType("sword")
                .material("gold", 18)
                .needsPolishing(false)
                .criterion("has_cast", conditionsFromTag(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

    }

    private Identifier rl(String path) {
        return new Identifier(Overgeared.MOD_ID, path);
    }

}