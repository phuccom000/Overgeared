package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
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
import java.util.function.Consumer;

public class ModRecipeProvider extends FabricRecipeProvider {
    /* private static final List<ItemConvertible> RUBY_SMELTABLES = List.of(ModItems.RAW_RUBY,
             ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get(), ModBlocks.NETHER_RUBY_ORE.get(), ModBlocks.END_STONE_RUBY_ORE.get());*/
    private static final List<ItemLike> STEEL_SMELTABLES = List.of(
            ModItems.CRUDE_STEEL.get());

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
            ModItems.IRON_HOE_HEAD.get(),
            ModItems.IRON_PICKAXE_HEAD.get(),
            ModItems.IRON_SWORD_BLADE.get(),
            ModItems.IRON_AXE_HEAD.get(),
            ModItems.IRON_SHOVEL_HEAD.get(),
            ModItems.IRON_ARROW_HEAD.get()

    );
    private static final List<ItemLike> STEEL_HEADS = List.of(
            ModItems.STEEL_HOE_HEAD.get(),
            ModItems.STEEL_PICKAXE_HEAD.get(),
            ModItems.STEEL_SWORD_BLADE.get(),
            ModItems.STEEL_AXE_HEAD.get(),
            ModItems.STEEL_SHOVEL_HEAD.get(),
            ModItems.STEEL_HOE.get(),
            ModItems.STEEL_PICKAXE.get(),
            ModItems.STEEL_SWORD.get(),
            ModItems.STEEL_AXE.get(),
            ModItems.STEEL_SHOVEL.get(),
            ModItems.STEEL_HELMET.get(),
            ModItems.STEEL_CHESTPLATE.get(),
            ModItems.STEEL_LEGGINGS.get(),
            ModItems.STEEL_BOOTS.get(),
            ModItems.STEEL_ARROW_HEAD.get());

    private static final List<ItemLike> COPPER_HEADS = List.of(
            ModItems.COPPER_HOE_HEAD.get(),
            ModItems.COPPER_PICKAXE_HEAD.get(),
            ModItems.COPPER_SWORD_BLADE.get(),
            ModItems.COPPER_AXE_HEAD.get(),
            ModItems.COPPER_SHOVEL_HEAD.get(),
            ModItems.COPPER_HOE.get(),
            ModItems.COPPER_PICKAXE.get(),
            ModItems.COPPER_SWORD.get(),
            ModItems.COPPER_AXE.get(),
            ModItems.COPPER_SHOVEL.get(),
            ModItems.COPPER_HELMET.get(),
            ModItems.COPPER_CHESTPLATE.get(),
            ModItems.COPPER_LEGGINGS.get(),
            ModItems.COPPER_BOOTS.get()

    );
    private static final List<ItemLike> GOLDEN_HEADS = List.of(
            ModItems.GOLDEN_HOE_HEAD.get(),
            ModItems.GOLDEN_PICKAXE_HEAD.get(),
            ModItems.GOLDEN_SWORD_BLADE.get(),
            ModItems.GOLDEN_AXE_HEAD.get(),
            ModItems.GOLDEN_SHOVEL_HEAD.get()

    );

    public ModRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter) {
        oreBlasting(exporter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_CRUDE_STEEL.get(), 0, 100,
                "steel_ingot");
        oreBlasting(exporter, COPPER_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_COPPER_INGOT.get(), 0, 70,
                "copper_ingot");
        oreBlasting(exporter, IRON_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT.get(), 0, 100,
                "iron_ingot");
        oreSmelting(exporter, COPPER_SMELTABLES, RecipeCategory.MISC, ModItems.HEATED_COPPER_INGOT.get(), 0, 140,
                "copper_ingot");
        oreBlasting(exporter, IRON_SOURCE, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT.get(), 0.7f, 100,
                "iron_ingot");
        oreBlasting(exporter, COPPER_SOURCE, RecipeCategory.MISC, ModItems.HEATED_COPPER_INGOT.get(), 0.7f, 100,
                "copper_ingot");
        oreSmelting(exporter, IRON_SOURCE, RecipeCategory.MISC, Items.IRON_INGOT, 0.7f, 200, "iron_ingot");
        oreSmelting(exporter, COPPER_SOURCE, RecipeCategory.MISC, Items.COPPER_INGOT, 0.7f, 200, "copper_ingot");
        oreSmelting(exporter, IRON_HEADS, RecipeCategory.MISC, Items.IRON_NUGGET, 0.1f, 200, null);
        oreBlasting(exporter, IRON_HEADS, RecipeCategory.MISC, Items.IRON_NUGGET, 0.1f, 100, null);
        oreSmelting(exporter, GOLDEN_HEADS, RecipeCategory.MISC, Items.GOLD_NUGGET, 0.1f, 200, null);
        oreBlasting(exporter, GOLDEN_HEADS, RecipeCategory.MISC, Items.GOLD_NUGGET, 0.1f, 100, null);
        oreBlasting(exporter, STEEL_HEADS, RecipeCategory.MISC, ModItems.STEEL_NUGGET.get(), 0.1f, 200, null);
        oreSmelting(exporter, COPPER_HEADS, RecipeCategory.MISC, ModItems.COPPER_NUGGET.get(), 0.1f, 200, null);
        oreBlasting(exporter, COPPER_HEADS, RecipeCategory.MISC, ModItems.COPPER_NUGGET.get(), 0.1f, 100, null);
        /*offerSmelting(exporter, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY,
                0.7f, 200, "ruby");
        offerBlasting(exporter, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY,
                0.7f, 100, "ruby");

        offerReversibleCompactingRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, ModItems.RUBY, RecipeCategory.DECORATIONS,
                ModBlocks.RUBY_BLOCK.get());

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.RAW_RUBY, 1)
                .pattern("SSS")
                .pattern("SRS")
                .pattern("SSS")
                .input('S', Items.STONE)
                .input('R', ModItems.RUBY)
                .criterion(hasItem(Items.STONE), conditionsFromItem(Items.STONE))
                .criterion(hasItem(ModItems.RUBY), conditionsFromItem(ModItems.RUBY))
                .offerTo(exporter, new Identifier(getRecipeName(ModItems.RAW_RUBY)));*/

        nineBlockStorageRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, ModItems.STEEL_INGOT.get(), RecipeCategory.DECORATIONS,
                ModBlocks.STEEL_BLOCK.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_INGOT.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_NUGGET.get())
                .unlockedBy("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .unlockedBy(getHasName(ModItems.STEEL_NUGGET.get()), has(ModItems.STEEL_NUGGET.get()))
                .save(exporter, Overgeared.MOD_ID + ":steel_ingot_from_nuggets");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.COPPER_INGOT)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.COPPER_NUGGET.get())
                .unlockedBy("has_copper_ingot",
                        has(Items.COPPER_INGOT))
                .unlockedBy(getHasName(ModItems.COPPER_NUGGET.get()), has(ModItems.COPPER_NUGGET.get()))
                .save(exporter, Overgeared.MOD_ID + ":copper_ingot_from_nuggets");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WOODEN_TONGS.get())
                .pattern(" # ")
                .pattern("###")
                .pattern(" # ")
                .define('#', Items.STICK)
                .unlockedBy("has_hot_item", has(ModTags.Items.HOT_ITEMS))
                .unlockedBy("has_heated_metal", has(ModTags.Items.HEATED_METALS))
                .save(exporter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.EMPTY_BLUEPRINT.get())
                .requires(Items.PAPER)
                .requires(Items.PAPER)
                .requires(Items.PAPER)
                .requires(Items.BLUE_DYE)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(exporter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.DRAFTING_TABLE.get())
                .requires(Blocks.CRAFTING_TABLE)
                .requires(ModItems.EMPTY_BLUEPRINT.get())
                .unlockedBy(getHasName(Blocks.CRAFTING_TABLE), has(Items.CRAFTING_TABLE))
                .unlockedBy(getHasName(ModItems.EMPTY_BLUEPRINT.get()),
                        has(ModItems.EMPTY_BLUEPRINT.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_AXE)
                .input(ModItems.STONE_AXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STONE_AXE_HEAD.get()),
                        has(ModItems.STONE_AXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_PICKAXE)
                .input(ModItems.STONE_PICKAXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STONE_PICKAXE_HEAD.get()),
                        has(ModItems.STONE_PICKAXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_SHOVEL)
                .input(ModItems.STONE_SHOVEL_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STONE_SHOVEL_HEAD.get()),
                        has(ModItems.STONE_SHOVEL_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_HOE)
                .input(ModItems.STONE_HOE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STONE_HOE_HEAD.get()),
                        has(ModItems.STONE_HOE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.STONE_SWORD)
                .input(ModItems.STONE_SWORD_BLADE.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STONE_SWORD_BLADE.get()),
                        has(ModItems.STONE_SWORD_BLADE.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_AXE)
                .input(ModItems.IRON_AXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_AXE_HEAD.get()), has(ModItems.IRON_AXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
                .input(ModItems.IRON_PICKAXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_PICKAXE_HEAD.get()),
                        has(ModItems.IRON_PICKAXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
                .input(ModItems.IRON_SHOVEL_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_SHOVEL_HEAD.get()),
                        has(ModItems.IRON_SHOVEL_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_HOE)
                .input(ModItems.IRON_HOE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_HOE_HEAD.get()), has(ModItems.IRON_HOE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.IRON_SWORD)
                .input(ModItems.IRON_SWORD_BLADE.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_SWORD_BLADE.get()),
                        has(ModItems.IRON_SWORD_BLADE.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_AXE.get())
                .input(ModItems.STEEL_AXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_AXE_HEAD.get()),
                        has(ModItems.STEEL_AXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_PICKAXE.get())
                .input(ModItems.STEEL_PICKAXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_PICKAXE_HEAD.get()),
                        has(ModItems.STEEL_PICKAXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_SHOVEL.get())
                .input(ModItems.STEEL_SHOVEL_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_SHOVEL_HEAD.get()),
                        has(ModItems.STEEL_SHOVEL_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_HOE.get())
                .input(ModItems.STEEL_HOE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_HOE_HEAD.get()),
                        has(ModItems.STEEL_HOE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.STEEL_SWORD.get())
                .input(ModItems.STEEL_SWORD_BLADE.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_SWORD_BLADE.get()),
                        has(ModItems.STEEL_SWORD_BLADE.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_AXE.get())
                .input(ModItems.COPPER_AXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.COPPER_AXE_HEAD.get()),
                        has(ModItems.COPPER_AXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_PICKAXE.get())
                .input(ModItems.COPPER_PICKAXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.COPPER_PICKAXE_HEAD.get()),
                        has(ModItems.COPPER_PICKAXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_SHOVEL.get())
                .input(ModItems.COPPER_SHOVEL_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.COPPER_SHOVEL_HEAD.get()),
                        has(ModItems.COPPER_SHOVEL_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_HOE.get())
                .input(ModItems.COPPER_HOE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.COPPER_HOE_HEAD.get()),
                        has(ModItems.COPPER_HOE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.COPPER_SWORD.get())
                .input(ModItems.COPPER_SWORD_BLADE.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.COPPER_SWORD_BLADE.get()),
                        has(ModItems.COPPER_SWORD_BLADE.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
                .input(ModItems.GOLDEN_AXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_AXE_HEAD.get()),
                        has(ModItems.GOLDEN_AXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
                .input(ModItems.GOLDEN_PICKAXE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_PICKAXE_HEAD.get()),
                        has(ModItems.GOLDEN_PICKAXE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
                .input(ModItems.GOLDEN_SHOVEL_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_SHOVEL_HEAD.get()),
                        has(ModItems.GOLDEN_SHOVEL_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
                .input(ModItems.GOLDEN_HOE_HEAD.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_HOE_HEAD.get()),
                        has(ModItems.GOLDEN_HOE_HEAD.get()))
                .save(exporter);

        OvergearedShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.GOLDEN_SWORD)
                .input(ModItems.GOLDEN_SWORD_BLADE.get())
                .input(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_SWORD_BLADE.get()),
                        has(ModItems.GOLDEN_SWORD_BLADE.get()))
                .save(exporter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STEEL_NUGGET.get(), 9)
                .requires(ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.STEEL_INGOT.get()), has(ModItems.STEEL_INGOT.get()))
                .save(exporter, Overgeared.MOD_ID + ":steel_nugget_from_ingot");

        /*
         * ShapedRecipeJsonBuilder.create(RecipeCategory.MISC,
         * ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 2)
         * .pattern("axa")
         * .pattern("aba")
         * .pattern("aaa")
         * .input('a', ModItems.STEEL_INGOT.get())
         * .input('b', Items.DIAMOND)
         * .input('x', ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get())
         * .criterion(hasItem(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
         * conditionsFromItem(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()))
         * .offerTo(exporter);
         */
        /*
         * ShapedForgingRecipeBuilder.shaped(ForgingBookCategory.MISC,
         * ModBlocks.STEEL_BLOCK.get(), 5)
         * .pattern("###")
         * .pattern("###")
         * .pattern("###")
         * .input('#', ModItems.STEEL_INGOT.get())
         * .criterion("has_steel_ingot",
         * conditionsFromItem(ItemTags.create(Identifier.of("forge", "ingots/steel"))))
         * .offerTo(exporter, Overgeared.MOD_ID + ":" +
         * getItemName(ModBlocks.STEEL_BLOCK.get()) + "_from_forging_" +
         * getItemName(ModItems.STEEL_INGOT.get()));
         */

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.IRON_PLATE.get(), 3)
                .tier(AnvilTier.stone())
                .setNeedQuenching(false)
                .setQuality(false)
                .pattern("#")
                .input('#', Items.IRON_INGOT)
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.COPPER_PLATE.get(), 3)
                .tier(AnvilTier.stone())
                .setNeedQuenching(false)
                .setQuality(false)
                .pattern("#")
                .input('#', Items.COPPER_INGOT)
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter);

        /*
         * ShapedForgingRecipeBuilder.shaped(ForgingBookCategory.MISC,
         * ModItems.STEEL_PLATE.get(), 4)
         * .tier(AnvilTier.iron())
         * .setQuality(false)
         * .pattern("#")
         * .input('#', ModItems.STEEL_INGOT.get())
         * .criterion(hasItem(ModItems.STEEL_INGOT.get()),
         * conditionsFromItem(ModItems.STEEL_INGOT.get()))
         * .offerTo(exporter);
         */

        // Iron Tools
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_PICKAXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.PICKAXE.getId())
                .pattern("###")
                .input('#', ModItems.HEATED_IRON_INGOT.get())
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_SWORD_BLADE.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.SWORD.getId())
                .pattern("#")
                .pattern("#")
                .input('#', ModItems.HEATED_IRON_INGOT.get())
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_SHOVEL_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.SHOVEL.getId())
                .pattern("#")
                .input('#', ModItems.HEATED_IRON_INGOT.get())
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_HOE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.HOE.getId())
                .pattern("##")
                .input('#', ModItems.HEATED_IRON_INGOT.get())
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_AXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern("# ")
                .input('#', ModItems.HEATED_IRON_INGOT.get())
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_AXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern(" #")
                .input('#', ModItems.HEATED_IRON_INGOT.get())
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter, new ResourceLocation(Overgeared.MOD_ID, "iron_axe_head_2"));

        // Copper Tools

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_PICKAXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.PICKAXE.getId())
                .pattern("###")
                .input('#', ModItems.HEATED_COPPER_INGOT.get())
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_HAMMER_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setQuality(true)
                .pattern("# ")
                .pattern(" #")
                .input('#', ModItems.HEATED_COPPER_INGOT.get())
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_SWORD_BLADE.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.SWORD.getId())
                .pattern("#")
                .pattern("#")
                .input('#', ModItems.HEATED_COPPER_INGOT.get())
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_SHOVEL_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.SHOVEL.getId())
                .pattern("#")
                .input('#', ModItems.HEATED_COPPER_INGOT.get())
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_HOE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.HOE.getId())
                .pattern("##")
                .input('#', ModItems.HEATED_COPPER_INGOT.get())
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_AXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern("# ")
                .input('#', ModItems.HEATED_COPPER_INGOT.get())
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.COPPER_AXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern(" #")
                .input('#', ModItems.HEATED_COPPER_INGOT.get())
                .criterion(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .offerTo(exporter, new ResourceLocation(Overgeared.MOD_ID, "copper_axe_head_2"));


        // Steel Tools
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_PICKAXE_HEAD.get(), 4)
                .setBlueprint(ToolType.PICKAXE.getId())
                .pattern("###")
                .input('#', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_HAMMER_HEAD.get(), 4)
                .setQuality(true)
                .setPolishing(false)
                .pattern("# ")
                .pattern(" #")
                .input('#', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_SWORD_BLADE.get(), 4)
                .setBlueprint(ToolType.SWORD.getId())
                .pattern("#")
                .pattern("#")
                .input('#', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_SHOVEL_HEAD.get(), 4)
                .setBlueprint(ToolType.SHOVEL.getId())
                .pattern("#")
                .input('#', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_HOE_HEAD.get(), 4)
                .setBlueprint(ToolType.HOE.getId())
                .pattern("##")
                .input('#', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_AXE_HEAD.get(), 4)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern("# ")
                .input('#', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_AXE_HEAD.get(), 4)
                .setBlueprint(ToolType.AXE.getId())
                .pattern("##")
                .pattern(" #")
                .input('#', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter, new ResourceLocation(Overgeared.MOD_ID, "steel_axe_head_2"));

        // Gold Tools
        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_PICKAXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.PICKAXE.getId())
                .setNeedQuenching(false)
                .pattern("###")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_SWORD_BLADE.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.SWORD.getId())
                .setNeedQuenching(false)
                .pattern("#")
                .pattern("#")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_SHOVEL_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.SHOVEL.getId())
                .setNeedQuenching(false)
                .pattern("#")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_HOE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.HOE.getId())
                .setNeedQuenching(false)
                .pattern("##")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_AXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.AXE.getId())
                .setNeedQuenching(false)
                .pattern("##")
                .pattern("# ")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.GOLDEN_AXE_HEAD.get(), 3)
                .tier(AnvilTier.stone())
                .setBlueprint(ToolType.AXE.getId())
                .setNeedQuenching(false)
                .pattern("##")
                .pattern(" #")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter, new ResourceLocation(Overgeared.MOD_ID, "golden_axe_head_2"));

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.IRON_TONG.get(), 2)
                .tier(AnvilTier.stone())
                .setQuality(false)
                .pattern("  x")
                .pattern(" xx")
                .pattern("x  ")
                .input('x', ModItems.HEATED_IRON_INGOT.get())
                .criterion("has_iron_ingot", has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.IRON_TONGS.get())
                .requires(ModItems.IRON_TONG.get())
                .requires(ModItems.IRON_TONG.get())
                .unlockedBy("has_iron_ingot",
                        has(Items.IRON_INGOT))
                .save(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.TOOL_HEADS, ModItems.STEEL_TONG.get(), 2)
                .setQuality(false)
                .pattern("  x")
                .pattern(" xx")
                .pattern("x  ")
                .input('x', ModItems.HEATED_STEEL_INGOT.get())
                .criterion("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_TONGS.get())
                .requires(ModItems.STEEL_TONG.get())
                .requires(ModItems.STEEL_TONG.get())
                .unlockedBy("has_steel_ingot",
                        has(ModItems.STEEL_INGOT.get()))
                .save(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.HEATED_STEEL_INGOT.get(), 3)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("#")
                .input('#', ModItems.HEATED_CRUDE_STEEL.get())
                .criterion(getHasName(ModItems.CRUDE_STEEL.get()), has(ModItems.CRUDE_STEEL.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.BUCKET, 3)
                .tier(AnvilTier.stone())
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern(" # ")
                .input('#', Items.IRON_INGOT)
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.SHEARS, 3)
                .tier(AnvilTier.stone())
                .needsMinigame(true)
                .failedResult(Items.IRON_INGOT)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern(" #")
                .pattern("# ")
                .input('#', Items.IRON_INGOT)
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Items.NETHERITE_INGOT, 10)
                .tier(AnvilTier.iron())
                .needsMinigame(true)
                .failedResult(Items.NETHERITE_SCRAP, 4)
                .qualityDifficulty(ForgingQuality.MASTER)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("#")
                .input('#', ModItems.HEATED_NETHERITE_ALLOY.get())
                .criterion(getHasName(Items.NETHERITE_SCRAP), has(Items.NETHERITE_SCRAP))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, Blocks.CAULDRON, 5)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .pattern("###")
                .input('#', ModItems.STEEL_PLATE.get())
                .criterion(getHasName(ModItems.STEEL_PLATE.get()), has(ModItems.STEEL_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_HELMET, 3)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_CHESTPLATE, 5)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_LEGGINGS, 4)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.IRON_BOOTS, 3)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.IRON_PLATES)
                .criterion(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_HELMET.get(), 3)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', ModItems.STEEL_PLATE.get())
                .criterion("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_CHESTPLATE.get(), 5)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', ModItems.STEEL_PLATE.get())
                .criterion("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_LEGGINGS.get(), 4)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', ModItems.STEEL_PLATE.get())
                .criterion("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.STEEL_BOOTS.get(), 3)
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', ModItems.STEEL_PLATE.get())
                .criterion("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_HELMET.get(), 3)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", has(ModItems.COPPER_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_CHESTPLATE.get(), 5)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", has(ModItems.COPPER_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_LEGGINGS.get(), 4)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", has(ModItems.COPPER_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, ModItems.COPPER_BOOTS.get(), 3)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', ModTags.Items.COPPER_PLATES)
                .criterion("has_copper_plate", has(ModItems.COPPER_PLATE.get()))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_HELMET, 3)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_CHESTPLATE, 5)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_LEGGINGS, 4)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.ARMORS, Items.GOLDEN_BOOTS, 3)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setNeedQuenching(false)
                .pattern("# #")
                .pattern("# #")
                .input('#', Items.GOLD_INGOT)
                .criterion(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.IRON_ARROW_HEAD.get(), 2)
                .tier(AnvilTier.stone())
                .setPolishing(false)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern(" ##")
                .pattern("# #")
                .input('#', Items.IRON_NUGGET)
                .criterion(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .offerTo(exporter);

        ShapedForgingRecipeBuilder.create(ForgingBookCategory.MISC, ModItems.STEEL_ARROW_HEAD.get(), 3)
                .tier(AnvilTier.iron())
                .setPolishing(false)
                .setQuality(false)
                .setNeedQuenching(false)
                .pattern("###")
                .pattern(" ##")
                .pattern("# #")
                .input('#', ModItems.STEEL_NUGGET.get())
                .criterion(getHasName(ModItems.STEEL_INGOT.get()), has(ModItems.STEEL_INGOT.get()))
                .offerTo(exporter);

        // Steel Axe to Diamond Axe
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_AXE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_AXE)
                .unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_axe"));

        // Steel Pickaxe to Diamond Pickaxe
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_PICKAXE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_PICKAXE)
                .unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_pickaxe"));

        // Steel Shovel to Diamond Shovel
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_SHOVEL.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_SHOVEL).unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_shovel"));

        // Steel Hoe to Diamond Hoe
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_HOE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_HOE).unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_hoe"));

        // Steel Sword to Diamond Sword
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_SWORD.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_SWORD).unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_sword"));

        // Steel Helmet to Diamond Helmet
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_HELMET.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_HELMET).unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_helmet"));

        // Steel Chestplate to Diamond Chestplate
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_CHESTPLATE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_CHESTPLATE).unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_chestplate"));

        // Steel Leggings to Diamond Leggings
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_LEGGINGS.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_LEGGINGS).unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_leggings"));

        // Steel Boots to Diamond Boots
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_BOOTS.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_BOOTS).unlocks("has_diamond", has(Items.DIAMOND))
                .save(exporter, ResourceLocation.tryBuild("minecraft", "diamond_boots"));

        /*
         * FletchingRecipeBuilder.fletching(
         * Ingredient.ofItems(Items.FLINT),
         * Ingredient.ofItems(Items.STICK),
         * Ingredient.ofItems(Items.FEATHER),
         * Items.ARROW,
         * 4
         * ).withTippedResult(Items.TIPPED_ARROW)
         * .withLingeringResult("Potion", ModItems.LINGERING_ARROW.get())
         * .criterion("has_flint", conditionsFromItem(Items.FLINT)) // Add this unlock condition
         * .offerTo(exporter);
         */

        FletchingRecipeBuilder.fletching(
                        Ingredient.of(ModItems.IRON_ARROW_HEAD.get()),
                        Ingredient.of(Items.STICK),
                        Ingredient.of(Items.FEATHER),
                        ModItems.IRON_UPGRADE_ARROW.get(),
                        4).withTippedResult(ModItems.IRON_UPGRADE_ARROW.get())
                .withLingeringResult(ModItems.IRON_UPGRADE_ARROW.get())
                .criterion("has_iron_ingot", has(Items.IRON_INGOT)) // Add this unlock condition
                .offerTo(exporter);

        FletchingRecipeBuilder.fletching(
                        Ingredient.of(ModItems.STEEL_ARROW_HEAD.get()),
                        Ingredient.of(Items.STICK),
                        Ingredient.of(Items.FEATHER),
                        ModItems.STEEL_UPGRADE_ARROW.get(),
                        4).withTippedResult(ModItems.STEEL_UPGRADE_ARROW.get())
                .withLingeringResult(ModItems.STEEL_UPGRADE_ARROW.get())
                .criterion("has_steel_ingot", has(ModItems.STEEL_INGOT.get())) // Add this unlock
                // condition
                .offerTo(exporter);
        FletchingRecipeBuilder.fletching(
                        Ingredient.of(ModItems.DIAMOND_SHARD.get()),
                        Ingredient.of(Items.STICK),
                        Ingredient.of(Items.FEATHER),
                        ModItems.DIAMOND_UPGRADE_ARROW.get(),
                        4)
                .withTippedResult(ModItems.DIAMOND_UPGRADE_ARROW.get())
                .withLingeringResult(ModItems.DIAMOND_UPGRADE_ARROW.get())
                .criterion("has_diamond", has(Items.DIAMOND)) // Add this unlock condition
                .offerTo(exporter);
        FletchingRecipeBuilder.fletching(
                        Ingredient.of(Items.GLOWSTONE_DUST),
                        Ingredient.of(Items.ARROW),
                        Ingredient.EMPTY,
                        Items.SPECTRAL_ARROW,
                        1)
                .criterion("has_arrow", has(Items.ARROW)) // Add this unlock condition
                .offerTo(exporter);

        // ===== CAST SMELTING =====

        // COPPER
        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_HAMMER_HEAD.get(), 0.5F, 150)
                .toolType("hammer").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_hammer_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_SWORD_BLADE.get(), 0.5F, 150)
                .toolType("sword").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_PICKAXE_HEAD.get(), 0.5F, 150)
                .toolType("pickaxe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_AXE_HEAD.get(), 0.5F, 150)
                .toolType("axe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_SHOVEL_HEAD.get(), 0.5F, 150)
                .toolType("shovel").material("copper", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.COPPER_HOE_HEAD.get(), 0.5F, 150)
                .toolType("hoe").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_hoe_head"));

        // IRON
        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_SWORD_BLADE.get(), 0.7F, 150)
                .toolType("sword").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_PICKAXE_HEAD.get(), 0.7F, 150)
                .toolType("pickaxe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_AXE_HEAD.get(), 0.7F, 150)
                .toolType("axe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_SHOVEL_HEAD.get(), 0.7F, 150)
                .toolType("shovel").material("iron", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.IRON_HOE_HEAD.get(), 0.7F, 150)
                .toolType("hoe").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_hoe_head"));

        // GOLDEN
        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_SWORD_BLADE.get(), 1.0F, 150)
                .toolType("sword").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_PICKAXE_HEAD.get(), 1.0F, 150)
                .toolType("pickaxe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_AXE_HEAD.get(), 1.0F, 150)
                .toolType("axe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_SHOVEL_HEAD.get(), 1.0F, 150)
                .toolType("shovel").material("gold", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.GOLDEN_HOE_HEAD.get(), 1.0F, 150)
                .toolType("hoe").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_hoe_head"));

        // STEEL
        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_HAMMER_HEAD.get(), 0.9F, 150)
                .toolType("hammer").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_hammer_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_SWORD_BLADE.get(), 0.9F, 150)
                .toolType("sword").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_sword_blade"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_PICKAXE_HEAD.get(), 0.9F, 150)
                .toolType("pickaxe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_pickaxe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_AXE_HEAD.get(), 0.9F, 150)
                .toolType("axe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_axe_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_SHOVEL_HEAD.get(), 0.9F, 150)
                .toolType("shovel").material("steel", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_shovel_head"));

        ToolCastSmeltingRecipeBuilder.cast(ModItems.STEEL_HOE_HEAD.get(), 0.9F, 150)
                .toolType("hoe").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_hoe_head"));

        // ===== CAST BLASTING =====

        // COPPER
        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_HAMMER_HEAD.get(), 0.5F, 75)
                .toolType("hammer").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_hammer_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_SWORD_BLADE.get(), 0.5F, 75)
                .toolType("sword").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_PICKAXE_HEAD.get(), 0.5F, 75)
                .toolType("pickaxe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_AXE_HEAD.get(), 0.5F, 75)
                .toolType("axe").material("copper", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_SHOVEL_HEAD.get(), 0.5F, 75)
                .toolType("shovel").material("copper", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.COPPER_HOE_HEAD.get(), 0.5F, 75)
                .toolType("hoe").material("copper", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("copper_hoe_head"));

        // IRON
        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_SWORD_BLADE.get(), 0.7F, 75)
                .toolType("sword").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_PICKAXE_HEAD.get(), 0.7F, 75)
                .toolType("pickaxe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_AXE_HEAD.get(), 0.7F, 75)
                .toolType("axe").material("iron", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_SHOVEL_HEAD.get(), 0.7F, 75)
                .toolType("shovel").material("iron", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.IRON_HOE_HEAD.get(), 0.7F, 75)
                .toolType("hoe").material("iron", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("iron_hoe_head"));

        // GOLDEN
        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_SWORD_BLADE.get(), 1.0F, 75)
                .toolType("sword").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_PICKAXE_HEAD.get(), 1.0F, 75)
                .toolType("pickaxe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_AXE_HEAD.get(), 1.0F, 75)
                .toolType("axe").material("gold", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_SHOVEL_HEAD.get(), 1.0F, 75)
                .toolType("shovel").material("gold", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.GOLDEN_HOE_HEAD.get(), 1.0F, 75)
                .toolType("hoe").material("gold", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("golden_hoe_head"));

        // STEEL
        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_HAMMER_HEAD.get(), 0.9F, 75)
                .toolType("hammer").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_hammer_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_SWORD_BLADE.get(), 0.9F, 75)
                .toolType("sword").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_sword_blade"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_PICKAXE_HEAD.get(), 0.9F, 75)
                .toolType("pickaxe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_pickaxe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_AXE_HEAD.get(), 0.9F, 75)
                .toolType("axe").material("steel", 27).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_axe_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_SHOVEL_HEAD.get(), 0.9F, 75)
                .toolType("shovel").material("steel", 9).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_shovel_head"));

        ToolCastBlastingRecipeBuilder.cast(ModItems.STEEL_HOE_HEAD.get(), 0.9F, 75)
                .toolType("hoe").material("steel", 18).needsPolishing(true)
                .criterion("has_cast", has(ModItems.UNFIRED_TOOL_CAST.get()))
                .offerTo(exporter, rl("steel_hoe_head"));
        // Axe
        CastingRecipeBuilder.casting(ModItems.COPPER_AXE_HEAD.get(), 0.4f, 150)
                .toolType("axe")
                .material("copper", 27)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Pickaxe
        CastingRecipeBuilder.casting(ModItems.COPPER_PICKAXE_HEAD.get(), 0.4f, 150)
                .toolType("pickaxe")
                .material("copper", 27)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Shovel
        CastingRecipeBuilder.casting(ModItems.COPPER_SHOVEL_HEAD.get(), 0.3f, 120)
                .toolType("shovel")
                .material("copper", 9)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Hoe
        CastingRecipeBuilder.casting(ModItems.COPPER_HOE_HEAD.get(), 0.3f, 100)
                .toolType("hoe")
                .material("copper", 18)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        // Sword
        CastingRecipeBuilder.casting(ModItems.COPPER_SWORD_BLADE.get(), 0.5f, 160)
                .toolType("sword")
                .material("copper", 18)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.COPPER_HAMMER_HEAD.get(), 0.5f, 160)
                .toolType("hammer")
                .material("copper", 18)
                .needsPolishing(false)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_AXE_HEAD.get(), 0.6f, 180)
                .toolType("axe")
                .material("iron", 27)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_PICKAXE_HEAD.get(), 0.6f, 180)
                .toolType("pickaxe")
                .material("iron", 27)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_SHOVEL_HEAD.get(), 0.5f, 140)
                .toolType("shovel")
                .material("iron", 9)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_HOE_HEAD.get(), 0.5f, 120)
                .toolType("hoe")
                .material("iron", 18)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.IRON_SWORD_BLADE.get(), 0.7f, 190)
                .toolType("sword")
                .material("iron", 18)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);
        CastingRecipeBuilder.casting(ModItems.STEEL_AXE_HEAD.get(), 0.8f, 220)
                .toolType("axe")
                .material("steel", 27)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_PICKAXE_HEAD.get(), 0.8f, 220)
                .toolType("pickaxe")
                .material("steel", 27)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_SHOVEL_HEAD.get(), 0.7f, 180)
                .toolType("shovel")
                .material("steel", 9)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_HOE_HEAD.get(), 0.7f, 160)
                .toolType("hoe")
                .material("steel", 18)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_SWORD_BLADE.get(), 0.9f, 240)
                .toolType("sword")
                .material("steel", 18)
                .needsPolishing(true)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.STEEL_HAMMER_HEAD.get(), 0.9f, 240)
                .toolType("hammer")
                .material("steel", 18)
                .needsPolishing(false)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_AXE_HEAD.get(), 0.3f, 100)
                .toolType("axe")
                .material("gold", 27)
                .needsPolishing(false)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_PICKAXE_HEAD.get(), 0.3f, 100)
                .toolType("pickaxe")
                .material("gold", 27)
                .needsPolishing(false)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_SHOVEL_HEAD.get(), 0.2f, 80)
                .toolType("shovel")
                .material("gold", 9)
                .needsPolishing(false)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_HOE_HEAD.get(), 0.2f, 70)
                .toolType("hoe")
                .material("gold", 18)
                .needsPolishing(false)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

        CastingRecipeBuilder.casting(ModItems.GOLDEN_SWORD_BLADE.get(), 0.4f, 110)
                .toolType("sword")
                .material("gold", 18)
                .needsPolishing(false)
                .criterion("has_cast", has(ModTags.Items.TOOL_CAST))
                .offerTo(exporter);

    }

    private ResourceLocation rl(String path) {
        return new ResourceLocation(Overgeared.MOD_ID, path);
    }

}