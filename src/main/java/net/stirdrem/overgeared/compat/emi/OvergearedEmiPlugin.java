package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.util.ModTags;

import java.util.*;

/**
 * EMI integration for displaying Forging recipes.
 * This class is only loaded when EMI is present (handled by @EmiEntrypoint).
 */
@EmiEntrypoint
public class OvergearedEmiPlugin implements EmiPlugin {

    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.SMITHING_ANVIL.get());

    public static final EmiRecipeCategory FORGING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("forging"),
            WORKSTATION,
            new EmiTexture(OvergearedMod.loc("textures/gui/smithing_anvil_jei.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.smithing_anvil");
        }
    };

    public static final EmiStack KNAPPING_WORKSTATION = EmiStack.of(ModItems.ROCK.get());
    public static final EmiRecipeCategory KNAPPING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("rock_knapping"),
            KNAPPING_WORKSTATION,
            new EmiTexture(OvergearedMod.loc("textures/gui/rock_knapping_gui.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.rock_knapping");
        }
    };

    public static final EmiStack ALLOY_WORKSTATION = EmiStack.of(ModBlocks.ALLOY_FURNACE.get());
    public static final EmiRecipeCategory ALLOY_SMELTING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("alloy_smelting"),
            ALLOY_WORKSTATION,
            new EmiTexture(OvergearedMod.loc("textures/gui/brick_alloy_furnace.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.jei.category.alloy_smelting");
        }
    };

    public static final EmiStack NETHER_ALLOY_WORKSTATION = EmiStack.of(ModBlocks.NETHER_ALLOY_FURNACE.get());
    public static final EmiRecipeCategory NETHER_ALLOY_SMELTING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("nether_alloy_smelting"),
            NETHER_ALLOY_WORKSTATION,
            new EmiTexture(OvergearedMod.loc("textures/gui/nether_alloy_furnace.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.jei.category.nether_alloy_smelting");
        }
    };

    public static final EmiStack FLETCHING_WORKSTATION = EmiStack.of(net.minecraft.world.level.block.Blocks.FLETCHING_TABLE);
    public static final EmiRecipeCategory FLETCHING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("fletching"),
            FLETCHING_WORKSTATION,
            new EmiTexture(OvergearedMod.loc("textures/gui/fletching_table.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.jei.category.fletching");
        }
    };

    public static final EmiIngredient COOLING_WORKSTATION = EmiIngredient.of(List.of(
            EmiStack.of(Fluids.WATER),
            EmiStack.of(Blocks.WATER_CAULDRON)
    ));
    public static final EmiRecipeCategory COOLING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("cooling"),
            EmiStack.of(Fluids.WATER),
            new EmiTexture(OvergearedMod.loc("textures/gui/cooling.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.jei.category.cooling");
        }
    };

    public static final EmiStack GRINDING_WORKSTATION = EmiStack.of(Blocks.GRINDSTONE);
    public static final EmiRecipeCategory GRINDING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("grinding"),
            GRINDING_WORKSTATION,
            new EmiTexture(OvergearedMod.loc("textures/gui/grinding.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.jei.category.grinding");
        }
    };

    public static final EmiStack CASTING_WORKSTATION = EmiStack.of(ModBlocks.CASTING_FURNACE.get());
    public static final EmiRecipeCategory CASTING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("casting"),
            CASTING_WORKSTATION,
            new EmiTexture(OvergearedMod.loc("textures/gui/casting_smelter.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.jei.category.casting");
        }
    };
    public static final EmiStack FLINT = EmiStack.of(ModItems.ROCK);
    public static final EmiRecipeCategory ROCK_GETTING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("flint_knapping"),
            FLINT,
            new EmiTexture(OvergearedMod.loc("textures/gui/flint.png"), 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("jei.overgeared.category.flint_knapping");
        }
    };

    // Priority for sorting recipes by category
    private static final Map<String, Integer> CATEGORY_PRIORITY = Map.of(
            "tool_head", 0,
            "tools", 1,
            "armor", 2,
            "plate", 3,
            "misc", 4
    );

    @Override
    public void register(EmiRegistry registry) {
        OvergearedMod.LOGGER.info("Registering EMI plugin for Overgeared recipes.");

        // Register the forging category
        registry.addCategory(FORGING_CATEGORY);

        // Register all smithing anvil blocks as workstations (ordered by tier: Stone -> Iron -> A -> B)
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.STONE_SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.TIER_A_SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.TIER_B_SMITHING_ANVIL.get()));

        // Register Knapping
        registry.addCategory(KNAPPING_CATEGORY);
        //registry.addWorkstation(KNAPPING_CATEGORY, KNAPPING_WORKSTATION);

        for (RecipeHolder<RockKnappingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.KNAPPING.get())) {
            registry.addRecipe(new KnappingEmiRecipe(holder));
        }

        // Register Alloy Smelting
        registry.addCategory(ALLOY_SMELTING_CATEGORY);
        registry.addWorkstation(ALLOY_SMELTING_CATEGORY, ALLOY_WORKSTATION);

        for (RecipeHolder<AlloySmeltingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ALLOY_SMELTING.get())) {
            registry.addRecipe(new AlloySmeltingEmiRecipe(holder));
        }
        for (RecipeHolder<ShapedAlloySmeltingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SHAPED_ALLOY_SMELTING.get())) {
            registry.addRecipe(new ShapedAlloySmeltingEmiRecipe(holder));
        }

        // Register Nether Alloy Smelting
        registry.addCategory(NETHER_ALLOY_SMELTING_CATEGORY);
        registry.addWorkstation(NETHER_ALLOY_SMELTING_CATEGORY, NETHER_ALLOY_WORKSTATION);

        for (RecipeHolder<NetherAlloySmeltingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.NETHER_ALLOY_SMELTING.get())) {
            registry.addRecipe(new NetherAlloySmeltingEmiRecipe(holder));
        }
        for (RecipeHolder<ShapedNetherAlloySmeltingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING.get())) {
            registry.addRecipe(new ShapedNetherAlloySmeltingEmiRecipe(holder));
        }

        // Register Fletching
        registry.addCategory(FLETCHING_CATEGORY);
        registry.addWorkstation(FLETCHING_CATEGORY, FLETCHING_WORKSTATION);

        for (RecipeHolder<FletchingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FLETCHING.get())) {
            registry.addRecipe(new FletchingEmiRecipe(holder));
        }

        registry.addCategory(COOLING_CATEGORY);
        registry.addWorkstation(COOLING_CATEGORY, COOLING_WORKSTATION);

        for (RecipeHolder<CoolingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE.get())) {
            registry.addRecipe(new CoolingEmiRecipe(holder));
        }

        registry.addCategory(GRINDING_CATEGORY);
        registry.addWorkstation(GRINDING_CATEGORY, GRINDING_WORKSTATION);

        for (RecipeHolder<GrindingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())) {
            registry.addRecipe(new GrindingEmiRecipe(holder));
        }

        // Register Casting
        registry.addCategory(CASTING_CATEGORY);
        registry.addWorkstation(CASTING_CATEGORY, CASTING_WORKSTATION);

        for (RecipeHolder<CastingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CASTING.get())) {
            registry.addRecipe(new CastingEmiRecipe(holder));
        }

        if (ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get())
            registry.addRecipe(new DragonBreathEmiRecipe());

        registry.addCategory(KNAPPING_CATEGORY);
        registry.addWorkstation(KNAPPING_CATEGORY, FLINT);
        if (ServerConfig.GET_ROCK_USING_FLINT.get())
            registry.addRecipe(new FlintKnappingEmiRecipe());

        // Register Clay + Nether Tool Cast recipes (one of each per tool type)
        for (RecipeHolder<ItemToToolTypeRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ITEM_TO_TOOLTYPE.get())) {
            ItemToToolTypeRecipe recipe = holder.value();
            registry.addRecipe(new ToolCastEmiRecipe(recipe.toolType(), recipe.input(), false));
            registry.addRecipe(new ToolCastEmiRecipe(recipe.toolType(), recipe.input(), true));
        }

        // Collect and sort all forging recipes
        List<RecipeHolder<ForgingRecipe>> allRecipes = new ArrayList<>(
                registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FORGING.get())
        );

        // Sort recipes by category priority, then alphabetically by output name
        allRecipes.sort((a, b) -> {
            String catA = categorizeRecipe(a.value());
            String catB = categorizeRecipe(b.value());

            int priorityA = CATEGORY_PRIORITY.getOrDefault(catA, 999);
            int priorityB = CATEGORY_PRIORITY.getOrDefault(catB, 999);

            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }

            // Fallback: alphabetical by display name
            return a.value().getResultItem(null).getDisplayName().getString()
                    .compareToIgnoreCase(b.value().getResultItem(null).getDisplayName().getString());
        });

        // Add sorted recipes
        for (RecipeHolder<ForgingRecipe> holder : allRecipes) {
            registry.addRecipe(new ForgingEmiRecipe(holder));
        }

        OvergearedMod.LOGGER.info("EMI plugin registered successfully.");
    }

    /**
     * Categorize a recipe for sorting purposes.
     */
    private static String categorizeRecipe(ForgingRecipe recipe) {
        ItemStack output = recipe.getResultItem(null);
        if (output.is(Tags.Items.ARMORS)) return "armor";
        if (output.is(ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (output.is(Tags.Items.TOOLS)) return "tools";
        if (output.is(ModItems.IRON_PLATE.get()) || output.is(ModItems.STEEL_PLATE.get()) || output.is(ModItems.COPPER_PLATE.get())) {
            return "plate";
        }
        return "misc";
    }

}
