package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.screen.*;
import net.stirdrem.overgeared.util.ModTags;

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

        if (item instanceof ArmorItem) return "armor";
        if (output.is(ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (item instanceof TieredItem || item instanceof ProjectileWeaponItem) return "tools";

        if (item == ModItems.IRON_PLATE
                || item == ModItems.STEEL_PLATE
                || item == ModItems.COPPER_PLATE) {
            return "plate";
        }

        return "misc";
    }

    @Override
    public ResourceLocation getPluginUid() {
        return Overgeared.id("jei_plugin");
    }

    // ----------------------------
    // CATEGORIES
    // ----------------------------
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        Minecraft mc = Minecraft.getInstance();
        RegistryAccess registryManager = mc.getConnection() != null
                ? mc.getConnection().registryAccess()
                : RegistryAccess.EMPTY;

        registration.addRecipeCategories(new ForgingRecipeCategory(gui));
        registration.addRecipeCategories(new KnappingRecipeCategory(gui));
        registration.addRecipeCategories(new FlintKnappingCategory(gui));
        registration.addRecipeCategories(new StoneAnvilCategory(gui, registryManager));
        registration.addRecipeCategories(new SteelAnvilCategory(gui, registryManager));
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
        ClientLevel world = mc.level;

        if (world == null || mc.getConnection() == null) return;

        RecipeManager manager = world.getRecipeManager();
        RegistryAccess registryManager = world.registryAccess();

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
                .thenComparing(r -> safeName(r, registryManager))
        );

        registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, combined);

        // ----------------------------
        // CASTING
        // ----------------------------
        registration.addRecipes(
                CastingRecipeCategory.CASTING_TYPE,
                manager.getAllRecipesFor(CastingRecipe.Type.INSTANCE).stream()
                        .sorted(Comparator.comparing(r ->
                                BuiltInRegistries.ITEM.getKey(
                                        r.getResultItem(registryManager).getItem()
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

    private String safeName(ForgingRecipe r, RegistryAccess registryManager) {
        return r.getResultItem(registryManager).getHoverName().getString();
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
                Overgeared.id("dragon_breath_brewing")
        ));
    }

    // ----------------------------
    // POTION CONVERSION (FIXED + SAFE)
    // ----------------------------
    private List<FletchingRecipe> generatePotionConversions() {

        List<FletchingRecipe> list = new ArrayList<>();

        ItemStack[] arrows = {
                new ItemStack(Items.ARROW),
                new ItemStack(ModItems.IRON_UPGRADE_ARROW),
                new ItemStack(ModItems.STEEL_UPGRADE_ARROW),
                new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW)
        };

        List<Potion> potions = BuiltInRegistries.POTION.stream()
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
                        Overgeared.id("potion_conv_" + (id++)),
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
        registration.useNbtForSubtypes(ModItems.LINGERING_ARROW);
        registration.useNbtForSubtypes(ModItems.IRON_UPGRADE_ARROW);
        registration.useNbtForSubtypes(ModItems.STEEL_UPGRADE_ARROW);
        registration.useNbtForSubtypes(ModItems.DIAMOND_UPGRADE_ARROW);
    }

    // ----------------------------
    // TRANSFERS
    // ----------------------------
    // The anvil screen handlers add slots in order hammer, [blueprint], 3x3 grid, result, then
    // player inventory/hotbar. Blueprint forging is on by default (ServerConfig.ENABLE_BLUEPRINT_FORGING),
    // so the grid starts at slot 2 with the blueprint slot present.
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

        registration.addRecipeTransferHandler(SteelSmithingAnvilScreenHandler.class,
                ModMenuTypes.STEEL_SMITHING_ANVIL_MENU,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 2, 9, 12, 36);

        registration.addRecipeTransferHandler(StoneSmithingAnvilScreenHandler.class,
                ModMenuTypes.STONE_SMITHING_ANVIL_MENU,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 2, 9, 12, 36);

        registration.addRecipeTransferHandler(TierASmithingAnvilScreenHandler.class,
                ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 2, 9, 12, 36);

        registration.addRecipeTransferHandler(TierBSmithingAnvilScreenHandler.class,
                ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 2, 9, 12, 36);

        registration.addRecipeTransferHandler(FletchingStationScreenHandler.class,
                ModMenuTypes.FLETCHING_STATION_MENU,
                FletchingCategory.FLETCHING_RECIPE_TYPE, 0, 4, 5, 36);
    }

    // ----------------------------
    // CATALYSTS (UNCHANGED)
    // ----------------------------
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ALLOY_FURNACE),
                AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(Blocks.GRINDSTONE),
                GrindingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(Items.WATER_BUCKET),
                CoolingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(Blocks.WATER_CAULDRON),
                CoolingRecipeCategory.TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NETHER_ALLOY_FURNACE),
                NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.STONE_SMITHING_ANVIL),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SMITHING_ANVIL),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        if (ServerConfig.ENABLE_TIER_A.get())
            registration.addRecipeCatalyst(new ItemStack(ModBlocks.TIER_A_SMITHING_ANVIL),
                    ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        if (ServerConfig.ENABLE_TIER_B.get())
            registration.addRecipeCatalyst(new ItemStack(ModBlocks.TIER_B_SMITHING_ANVIL),
                    ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(Blocks.FLETCHING_TABLE),
                FletchingCategory.FLETCHING_RECIPE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CAST_FURNACE),
                CastingRecipeCategory.CASTING_TYPE);
    }
}
