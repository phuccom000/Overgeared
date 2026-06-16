package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.screen.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JEIOvergearedModPlugin implements IModPlugin {

    private static final Map<String, Integer> CATEGORY_PRIORITY = Map.of(
            "tool_head", 0,
            "tools", 1,
            "armor", 2,
            "plate", 3,
            "misc", 4
    );

    // ----------------------------
    // SAFER CATEGORY LOGIC
    // ----------------------------
    private static String categorizeRecipe(ForgingRecipe recipe) {
        ItemStack output = recipe.getResultItem(null);
        Item item = output.getItem();

        if (output.is(net.minecraftforge.common.Tags.Items.ARMORS)) return "armor";
        if (output.is(net.stirdrem.overgeared.util.ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (output.is(net.minecraftforge.common.Tags.Items.TOOLS)) return "tools";

        if (item == ModItems.IRON_PLATE.get()
                || item == ModItems.STEEL_PLATE.get()
                || item == ModItems.COPPER_PLATE.get()) {
            return "plate";
        }

        return "misc";
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(OvergearedMod.MOD_ID, "jei_plugin");
    }

    // ----------------------------
    // CATEGORIES
    // ----------------------------
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        RegistryAccess registryAccess = Minecraft.getInstance().getConnection() != null
                ? Minecraft.getInstance().getConnection().registryAccess()
                : RegistryAccess.EMPTY;

        registration.addRecipeCategories(new ForgingRecipeCategory(gui));
        registration.addRecipeCategories(new KnappingRecipeCategory(gui));
        registration.addRecipeCategories(new FlintKnappingCategory(gui));
        registration.addRecipeCategories(new StoneAnvilCategory(gui, registryAccess));
        registration.addRecipeCategories(new SteelAnvilCategory(gui, registryAccess));
        registration.addRecipeCategories(new FletchingCategory(gui));
        registration.addRecipeCategories(new AlloySmeltingRecipeCategory(gui));
        registration.addRecipeCategories(new NetherAlloySmeltingRecipeCategory(gui));
        registration.addRecipeCategories(new CoolingRecipeCategory(gui));
        registration.addRecipeCategories(new GrindingRecipeCategory(gui));
        registration.addRecipeCategories(new CastingRecipeCategory(gui));
    }

    // ----------------------------
    // RECIPES
    // ----------------------------
    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level == null || mc.getConnection() == null) return;

        RecipeManager manager = level.getRecipeManager();
        RegistryAccess registryAccess = level.registryAccess();

        // ----------------------------
        // FORGING RECIPES
        // ----------------------------
        List<ForgingRecipe> all = manager.getAllRecipesFor(ForgingRecipe.Type.INSTANCE);

        List<ForgingRecipe> combined = new ArrayList<>();
        combined.addAll(filterByTier(all, AnvilTier.STONE));
        combined.addAll(filterByTier(all, AnvilTier.IRON));
        combined.addAll(filterByTier(all, AnvilTier.ABOVE_A));
        combined.addAll(filterByTier(all, AnvilTier.ABOVE_B));

        combined.sort(Comparator
                .comparing((ForgingRecipe r) -> CATEGORY_PRIORITY.getOrDefault(categorizeRecipe(r), 999))
                .thenComparing(r -> safeName(r, registryAccess))
        );

        registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, combined);

        // ----------------------------
        // CASTING
        // ----------------------------
        registration.addRecipes(
                CastingRecipeCategory.CASTING_TYPE,
                manager.getAllRecipesFor(ModRecipeTypes.CASTING.get()).stream()
                        .sorted(Comparator.comparing(r ->
                                BuiltInRegistries.ITEM.getKey(
                                        r.getResultItem(registryAccess).getItem()
                                ).toString()
                        ))
                        .toList()
        );

        // ----------------------------
        // KNAPPING
        // ----------------------------
        registration.addRecipes(
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE,
                manager.getAllRecipesFor(RockKnappingRecipe.Type.INSTANCE)
        );

        // ----------------------------
        // ALLOY
        // ----------------------------
        List<IAlloyRecipe> alloy = new ArrayList<>();
        alloy.addAll(manager.getAllRecipesFor(AlloySmeltingRecipe.Type.INSTANCE));
        alloy.addAll(manager.getAllRecipesFor(ShapedAlloySmeltingRecipe.Type.INSTANCE));

        registration.addRecipes(AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, alloy);

        // ----------------------------
        // NETHER ALLOY
        // ----------------------------
        List<INetherAlloyRecipe> nether = new ArrayList<>();
        nether.addAll(manager.getAllRecipesFor(NetherAlloySmeltingRecipe.Type.INSTANCE));
        nether.addAll(manager.getAllRecipesFor(ShapedNetherAlloySmeltingRecipe.Type.INSTANCE));

        registration.addRecipes(NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, nether);

        // ----------------------------
        // COOLING + GRINDING
        // ----------------------------
        registration.addRecipes(CoolingRecipeCategory.TYPE,
                manager.getAllRecipesFor(CoolingRecipe.Type.INSTANCE));

        registration.addRecipes(GrindingRecipeCategory.TYPE,
                manager.getAllRecipesFor(GrindingRecipe.Type.INSTANCE));

        // ----------------------------
        // BREWING FIXED
        // ----------------------------
        if (ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) {
            registration.addRecipes(RecipeTypes.BREWING, dragonBreathRecipe());
        }

        // ----------------------------
        // FLETCHING
        // ----------------------------
        if (ServerConfig.ENABLE_FLETCHING_RECIPES.get()) {
            List<FletchingRecipe> base = manager.getAllRecipesFor(FletchingRecipe.Type.INSTANCE);

            registration.addRecipes(FletchingCategory.FLETCHING_RECIPE_TYPE, base);

            if (ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get()) {
                registration.addRecipes(
                        FletchingCategory.FLETCHING_RECIPE_TYPE,
                        generatePotionConversions()
                );
            }
        }
    }

    // ----------------------------
    // SAFE TIER FILTER
    // ----------------------------
    private List<ForgingRecipe> filterByTier(List<ForgingRecipe> list, AnvilTier tier) {
        return list.stream()
                .filter(r -> r.getAnvilTier().equalsIgnoreCase(tier.getDisplayName()))
                .toList();
    }

    private String safeName(ForgingRecipe r, RegistryAccess access) {
        return r.getResultItem(access).getHoverName().getString();
    }

    // ----------------------------
    // BREWING RECIPE
    // ----------------------------
    private List<IJeiBrewingRecipe> dragonBreathRecipe() {
        ItemStack input = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.THICK).copy();
        ItemStack ingredient = new ItemStack(Items.CHORUS_FRUIT);
        ItemStack output = new ItemStack(Items.DRAGON_BREATH);

        return List.of(new JeiBetterBrewingRecipe(
                List.of(input),
                List.of(ingredient),
                output,
                new ResourceLocation(OvergearedMod.MOD_ID, "dragon_breath_brewing")
        ));
    }

    // ----------------------------
    // POTION CONVERSION (FIXED + SAFE)
    // ----------------------------
    private List<FletchingRecipe> generatePotionConversions() {

        List<FletchingRecipe> list = new ArrayList<>();

        ItemStack[] arrows = {
                new ItemStack(Items.ARROW),
                new ItemStack(ModItems.IRON_UPGRADE_ARROW.get()),
                new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get()),
                new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get())
        };

        List<Potion> potions = ForgeRegistries.POTIONS.getValues().stream()
                .filter(p -> p != Potions.EMPTY)
                .toList();

        int id = 0;

        for (ItemStack arrow : arrows) {
            for (Potion potion : potions) {

                ItemStack potionStack = PotionUtils.setPotion(
                        new ItemStack(Items.POTION),
                        potion
                ).copy();

                ItemStack output;

                if (arrow.is(Items.ARROW)) {
                    output = PotionUtils.setPotion(
                            new ItemStack(Items.TIPPED_ARROW),
                            potion
                    );
                } else {
                    output = arrow.copy();
                    PotionUtils.setPotion(output, potion);
                }

                list.add(new FletchingRecipe(
                        new ResourceLocation(OvergearedMod.MOD_ID, "potion_conv_" + (id++)),
                        Ingredient.EMPTY,
                        Ingredient.of(arrow),
                        Ingredient.EMPTY,
                        Ingredient.of(potionStack),
                        output,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        "Potion",
                        "LingeringPotion"
                ));
            }
        }

        return list;
    }

    // ----------------------------
    // GUI HANDLERS (UNCHANGED)
    // ----------------------------
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SteelSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(NetherAlloySmelterScreen.class, 90, 35, 22, 15,
                NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);

        registration.addRecipeClickArea(AlloySmelterScreen.class, 86, 35, 22, 15,
                AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);

        registration.addRecipeClickArea(FletchingStationScreen.class, 90, 35, 22, 15,
                FletchingCategory.FLETCHING_RECIPE_TYPE);

        registration.addRecipeClickArea(TierASmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(TierBSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(StoneSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(RockKnappingScreen.class, 90, 35, 22, 15,
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE);
    }

    // ----------------------------
    // SUBTYPES
    // ----------------------------
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(ModItems.LINGERING_ARROW.get());
        registration.useNbtForSubtypes(ModItems.IRON_UPGRADE_ARROW.get());
        registration.useNbtForSubtypes(ModItems.STEEL_UPGRADE_ARROW.get());
        registration.useNbtForSubtypes(ModItems.DIAMOND_UPGRADE_ARROW.get());
    }

    // ----------------------------
    // TRANSFERS (UNCHANGED)
    // ----------------------------
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

        registration.addRecipeTransferHandler(SteelSmithingAnvilMenu.class,
                ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 38, 9, 0, 36);

        registration.addRecipeTransferHandler(StoneSmithingAnvilMenu.class,
                ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 37, 9, 0, 36);

        registration.addRecipeTransferHandler(TierASmithingAnvilMenu.class,
                ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 38, 9, 0, 36);

        registration.addRecipeTransferHandler(TierBSmithingAnvilMenu.class,
                ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 38, 9, 0, 36);

        registration.addRecipeTransferHandler(FletchingStationMenu.class,
                ModMenuTypes.FLETCHING_STATION_MENU.get(),
                FletchingCategory.FLETCHING_RECIPE_TYPE, 0, 4, 5, 36);
    }

    // ----------------------------
    // CATALYSTS (UNCHANGED)
    // ----------------------------
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ALLOY_FURNACE.get()),
                AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(Blocks.GRINDSTONE),
                GrindingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(Items.WATER_BUCKET),
                CoolingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(Blocks.WATER_CAULDRON),
                CoolingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NETHER_ALLOY_FURNACE.get()),
                NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SMITHING_ANVIL.get()),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TIER_A_SMITHING_ANVIL.get()),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TIER_B_SMITHING_ANVIL.get()),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(Blocks.FLETCHING_TABLE),
                FletchingCategory.FLETCHING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CAST_FURNACE.get()),
                CastingRecipeCategory.CASTING_TYPE);
    }
}