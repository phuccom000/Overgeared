package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.screen.*;
import net.stirdrem.overgeared.util.ModTags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        ItemStack output = recipe.getResultItem();
        Item item = output.getItem();

        // 26.3 port: ArmorItem / TieredItem no longer exist; use the equippable / tool / weapon components.
        Equippable equippable = output.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) return "armor";
        if (output.is(ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (output.has(DataComponents.TOOL) || output.has(DataComponents.WEAPON)
                || item instanceof ProjectileWeaponItem) return "tools";

        if (item == ModItems.IRON_PLATE
                || item == ModItems.STEEL_PLATE
                || item == ModItems.COPPER_PLATE) {
            return "plate";
        }

        return "misc";
    }

    @Override
    public Identifier getPluginUid() {
        return Overgeared.id("jei_plugin");
    }

    // ----------------------------
    // CATEGORIES
    // ----------------------------
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new ForgingRecipeCategory(gui));
        registration.addRecipeCategories(new KnappingRecipeCategory(gui));
        registration.addRecipeCategories(new FlintKnappingCategory(gui));
        registration.addRecipeCategories(new StoneAnvilCategory(gui));
        registration.addRecipeCategories(new SteelAnvilCategory(gui));
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

        if (mc.level == null) {
            Overgeared.LOGGER.warn("JEI registerRecipes: mc.level is null");
            return;
        }

        // Vanilla no longer syncs recipes to clients; every Overgeared serializer is synchronized through
        // Fabric's RecipeSynchronization, so RecipeLookup sees them on the client level.
        Level level = mc.level;

        // ----------------------------
        // FORGING RECIPES
        // ----------------------------
        List<RecipeHolder<ForgingRecipe>> all = new ArrayList<>(RecipeLookup.all(level, ModRecipeTypes.FORGING));

        List<RecipeHolder<ForgingRecipe>> combined = new ArrayList<>();
        combined.addAll(filterByTier(all, AnvilTier.STONE));
        combined.addAll(filterByTier(all, AnvilTier.IRON));
        combined.addAll(filterByTier(all, AnvilTier.ABOVE_A));
        combined.addAll(filterByTier(all, AnvilTier.ABOVE_B));

        combined.sort(Comparator
                .comparing((RecipeHolder<ForgingRecipe> r) -> CATEGORY_PRIORITY.getOrDefault(categorizeRecipe(r.value()), 999))
                .thenComparing(r -> safeName(r.value()))
        );

        registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, combined);

        // ----------------------------
        // CASTING
        // ----------------------------
        registration.addRecipes(
                CastingRecipeCategory.CASTING_TYPE,
                RecipeLookup.all(level, ModRecipeTypes.CASTING).stream()
                        .sorted(Comparator.comparing((RecipeHolder<CastingRecipe> r) ->
                                BuiltInRegistries.ITEM.getKey(
                                        r.value().getResultItem().getItem()
                                ).toString()
                        ))
                        .toList()
        );

        // ----------------------------
        // KNAPPING
        // ----------------------------
        registration.addRecipes(
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE,
                List.copyOf(RecipeLookup.all(level, ModRecipeTypes.KNAPPING))
        );

        // ----------------------------
        // ALLOY
        // ----------------------------
        List<IAlloyRecipe> alloy = new ArrayList<>();
        alloy.addAll(RecipeLookup.allValues(level, ModRecipeTypes.ALLOY_SMELTING));
        alloy.addAll(RecipeLookup.allValues(level, ModRecipeTypes.SHAPED_ALLOY_SMELTING));

        registration.addRecipes(AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, alloy);

        // ----------------------------
        // NETHER ALLOY
        // ----------------------------
        List<INetherAlloyRecipe> nether = new ArrayList<>();
        nether.addAll(RecipeLookup.allValues(level, ModRecipeTypes.NETHER_ALLOY_SMELTING));
        nether.addAll(RecipeLookup.allValues(level, ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING));

        registration.addRecipes(NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, nether);

        // ----------------------------
        // COOLING + GRINDING
        // ----------------------------
        registration.addRecipes(CoolingRecipeCategory.TYPE,
                List.copyOf(RecipeLookup.all(level, ModRecipeTypes.COOLING_RECIPE)));

        registration.addRecipes(GrindingRecipeCategory.TYPE,
                List.copyOf(RecipeLookup.all(level, ModRecipeTypes.GRINDING_RECIPE)));

        // ----------------------------
        // BREWING
        // ----------------------------
        if (ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) {
            registration.addRecipes(RecipeTypes.BREWING, dragonBreathRecipe());
        }

        // ----------------------------
        // FLETCHING
        // ----------------------------
        if (ServerConfig.ENABLE_FLETCHING_RECIPES.get()) {
            List<FletchingJeiRecipe> base = RecipeLookup.all(level, ModRecipeTypes.FLETCHING).stream()
                    .map(FletchingJeiRecipe::of)
                    .toList();

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
    private List<RecipeHolder<ForgingRecipe>> filterByTier(List<RecipeHolder<ForgingRecipe>> list, AnvilTier tier) {
        return list.stream()
                .filter(r -> r.value().getAnvilTier().equalsIgnoreCase(tier.getDisplayName()))
                .toList();
    }

    private String safeName(ForgingRecipe r) {
        return r.getResultItem().getHoverName().getString();
    }

    // ----------------------------
    // BREWING RECIPE
    // ----------------------------
    // 26.3 port: vanilla brewing is now data-driven (net.minecraft.world.item.crafting.BrewingRecipe), but JEI 31
    // still exposes its brewing category through IJeiBrewingRecipe, so the display recipe is kept as before.
    private List<IJeiBrewingRecipe> dragonBreathRecipe() {
        ItemStack input = PotionContents.createItemStack(Items.POTION, Potions.THICK);
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
    // POTION CONVERSION
    // ----------------------------
    private List<FletchingJeiRecipe> generatePotionConversions() {

        List<FletchingJeiRecipe> list = new ArrayList<>();

        Item[] arrows = {
                Items.ARROW,
                ModItems.IRON_UPGRADE_ARROW,
                ModItems.STEEL_UPGRADE_ARROW,
                ModItems.DIAMOND_UPGRADE_ARROW
        };

        List<Holder<Potion>> potions = BuiltInRegistries.POTION.listElements()
                .<Holder<Potion>>map(h -> h)
                .toList();

        int id = 0;

        for (Item arrow : arrows) {
            for (Holder<Potion> potion : potions) {

                ItemStack potionStack = PotionContents.createItemStack(Items.POTION, potion);

                ItemStack output;

                if (arrow == Items.ARROW) {
                    output = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
                } else {
                    // 1.20.1 PotionUtils.setPotion -> minecraft:potion_contents
                    output = PotionContents.createItemStack(arrow, potion);
                }

                list.add(new FletchingJeiRecipe(
                        Overgeared.id("potion_conv_" + (id++)),
                        Optional.empty(),
                        Optional.of(Ingredient.of(arrow)),
                        Optional.empty(),
                        Optional.of(Ingredient.of(Items.POTION)),
                        List.of(potionStack),
                        output
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
        // 1.20.1 useNbtForSubtypes -> the components that replaced the arrow NBT (Potion, LingeringPotion)
        for (Item arrow : List.of(ModItems.LINGERING_ARROW, ModItems.IRON_UPGRADE_ARROW,
                ModItems.STEEL_UPGRADE_ARROW, ModItems.DIAMOND_UPGRADE_ARROW)) {
            registration.registerFromDataComponentTypes(arrow,
                    DataComponents.POTION_CONTENTS, ModComponents.LINGERING_STATUS);
        }
    }

    // ----------------------------
    // TRANSFERS
    // ----------------------------
    // The anvil screen handlers add slots in order hammer, [blueprint], 3x3 grid, result, then
    // player inventory/hotbar. Blueprint forging is on by default (ServerConfig.ENABLE_BLUEPRINT_FORGING),
    // so the grid starts at slot 2 with the blueprint slot present.
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

        registration.addRecipeTransferHandler(new AnvilRecipeTransferInfo<>(SteelSmithingAnvilScreenHandler.class, ModMenuTypes.STEEL_SMITHING_ANVIL_MENU));

        registration.addRecipeTransferHandler(new AnvilRecipeTransferInfo<>(StoneSmithingAnvilScreenHandler.class, ModMenuTypes.STONE_SMITHING_ANVIL_MENU));

        registration.addRecipeTransferHandler(new AnvilRecipeTransferInfo<>(TierASmithingAnvilScreenHandler.class, ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU));

        registration.addRecipeTransferHandler(new AnvilRecipeTransferInfo<>(TierBSmithingAnvilScreenHandler.class, ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU));

        registration.addRecipeTransferHandler(FletchingStationScreenHandler.class,
                ModMenuTypes.FLETCHING_STATION_MENU,
                FletchingCategory.FLETCHING_RECIPE_TYPE, 0, 4, 5, 36);
    }

    // ----------------------------
    // CATALYSTS (UNCHANGED)
    // ----------------------------
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addCraftingStation(AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, ModBlocks.ALLOY_FURNACE);

        registration.addCraftingStation(GrindingRecipeCategory.TYPE, Blocks.GRINDSTONE);

        registration.addCraftingStation(CoolingRecipeCategory.TYPE, Items.WATER_BUCKET);

        registration.addCraftingStation(CoolingRecipeCategory.TYPE, Blocks.WATER_CAULDRON);

        registration.addCraftingStation(NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, ModBlocks.NETHER_ALLOY_FURNACE);

        registration.addCraftingStation(ForgingRecipeCategory.FORGING_RECIPE_TYPE, ModBlocks.STONE_SMITHING_ANVIL);

        registration.addCraftingStation(ForgingRecipeCategory.FORGING_RECIPE_TYPE, ModBlocks.SMITHING_ANVIL);

        if (ServerConfig.ENABLE_TIER_A.get())
            registration.addCraftingStation(ForgingRecipeCategory.FORGING_RECIPE_TYPE, ModBlocks.TIER_A_SMITHING_ANVIL);

        if (ServerConfig.ENABLE_TIER_B.get())
            registration.addCraftingStation(ForgingRecipeCategory.FORGING_RECIPE_TYPE, ModBlocks.TIER_B_SMITHING_ANVIL);

        registration.addCraftingStation(FletchingCategory.FLETCHING_RECIPE_TYPE, Blocks.FLETCHING_TABLE);

        registration.addCraftingStation(CastingRecipeCategory.CASTING_TYPE, ModBlocks.CAST_FURNACE);
    }
}
