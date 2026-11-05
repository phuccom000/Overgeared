package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.screen.*;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * JEI integration. Disabled entirely when EMI is also installed: EMI loads JEI plugins through its
 * JEI compat layer, which would duplicate everything the native EMI plugin already registers.
 */
@JeiPlugin
public class JEIOvergearedModPlugin implements IModPlugin {

    private static final boolean EMI_LOADED = Services.PLATFORM.isModLoaded("emi");

    private static final Map<String, Integer> CATEGORY_PRIORITY = Map.of(
            "tool_head", 0,
            "tools", 1,
            "armor", 2,
            "plate", 3,
            "misc", 4
    );

    private static String categorizeRecipe(ForgingRecipe recipe, HolderLookup.Provider registries) {
        ItemStack output = recipe.getResultItem(registries);
        Item item = output.getItem();

        if (output.is(ModTags.Common.ARMORS)) return "armor";
        if (output.is(ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (output.is(ModTags.Common.TOOLS)) return "tools";

        if (item == ModItems.IRON_PLATE.get()
                || item == ModItems.STEEL_PLATE.get()
                || item == ModItems.COPPER_PLATE.get()) {
            return "plate";
        }

        return "misc";
    }

    @Override
    public ResourceLocation getPluginUid() {
        return OvergearedMod.loc("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (EMI_LOADED) return;
        var gui = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new ForgingRecipeCategory(gui));
        registration.addRecipeCategories(new KnappingRecipeCategory(gui));
        registration.addRecipeCategories(new FlintKnappingCategory(gui));
        registration.addRecipeCategories(new StoneAnvilCategory(gui));
        registration.addRecipeCategories(new SteelAnvilCategory(gui));
        registration.addRecipeCategories(new DraftingTableCategory(gui));
        registration.addRecipeCategories(new FletchingCategory(gui));
        registration.addRecipeCategories(new AlloySmeltingRecipeCategory(gui));
        registration.addRecipeCategories(new NetherAlloySmeltingRecipeCategory(gui));
        registration.addRecipeCategories(new CoolingRecipeCategory(gui));
        registration.addRecipeCategories(new GrindingRecipeCategory(gui));
        registration.addRecipeCategories(new CastingRecipeCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (EMI_LOADED) return;
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        RecipeManager manager = level.getRecipeManager();
        HolderLookup.Provider registries = level.registryAccess();

        // Forging
        List<ForgingRecipe> all = unwrap(manager.getAllRecipesFor(ModRecipeTypes.FORGING.get()));

        List<ForgingRecipe> combined = new ArrayList<>();
        combined.addAll(filterByTier(all, AnvilTier.STONE));
        for (AnvilTier tier : AnvilTier.configured()) {
            combined.addAll(filterByTier(all, tier));
        }

        combined.sort(Comparator
                .comparing((ForgingRecipe r) -> CATEGORY_PRIORITY.getOrDefault(categorizeRecipe(r, registries), 999))
                .thenComparing(r -> r.getResultItem(registries).getHoverName().getString())
        );

        registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, combined);

        // Casting
        registration.addRecipes(CastingRecipeCategory.CASTING_TYPE,
                unwrap(manager.getAllRecipesFor(ModRecipeTypes.CASTING.get())).stream()
                        .sorted(Comparator.comparing(r ->
                                BuiltInRegistries.ITEM.getKey(r.getResultItem(registries).getItem()).toString()))
                        .toList());

        // Knapping
        registration.addRecipes(KnappingRecipeCategory.KNAPPING_RECIPE_TYPE,
                unwrap(manager.getAllRecipesFor(ModRecipeTypes.KNAPPING.get())));

        // Alloy smelting
        List<IAlloyRecipe> alloy = new ArrayList<>();
        alloy.addAll(unwrap(manager.getAllRecipesFor(ModRecipeTypes.ALLOY_SMELTING.get())));
        alloy.addAll(unwrap(manager.getAllRecipesFor(ModRecipeTypes.SHAPED_ALLOY_SMELTING.get())));
        registration.addRecipes(AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, alloy);

        // Nether alloy smelting
        List<INetherAlloyRecipe> nether = new ArrayList<>();
        nether.addAll(unwrap(manager.getAllRecipesFor(ModRecipeTypes.NETHER_ALLOY_SMELTING.get())));
        nether.addAll(unwrap(manager.getAllRecipesFor(ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING.get())));
        registration.addRecipes(NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE, nether);

        // Cooling / Grinding
        registration.addRecipes(CoolingRecipeCategory.TYPE,
                unwrap(manager.getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE.get())));
        registration.addRecipes(GrindingRecipeCategory.TYPE,
                unwrap(manager.getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())));

        // Explanation pages
        if (ServerConfig.GET_ROCK_USING_FLINT.get()) {
            registration.addRecipes(FlintKnappingCategory.FLINT_KNAPPING,
                    List.of(new ExplanationRecipe(new ItemStack(ModItems.ROCK.get()))));
        }
        if (ServerConfig.ENABLE_STONE_TO_ANVIL.get()) {
            registration.addRecipes(StoneAnvilCategory.STONE_ANVIL_GET,
                    List.of(new ExplanationRecipe(new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()))));
        }
        if (ServerConfig.ENABLE_ANVIL_TO_SMITHING.get()) {
            registration.addRecipes(SteelAnvilCategory.STEEL_ANVIL_GET,
                    List.of(new ExplanationRecipe(new ItemStack(ModBlocks.IRON_SMITHING_ANVIL.get()))));
        }

        registration.addRecipes(DraftingTableCategory.DRAFTING,
                List.of(new ExplanationRecipe(new ItemStack(ModItems.BLUEPRINT.get()))));

        // Extra vanilla crafting / anvil entries
        if (ServerConfig.ENABLE_CASTING.get()) {
            registration.addRecipes(RecipeTypes.CRAFTING, ToolCastRecipeMaker.createRecipes(manager, false));
            registration.addRecipes(RecipeTypes.CRAFTING, ToolCastRecipeMaker.createRecipes(manager, true));
        }
        registration.addRecipes(RecipeTypes.CRAFTING, BlueprintCloningRecipeMaker.createRecipes());
        registration.addRecipes(RecipeTypes.ANVIL,
                OvergearedAnvilRecipes.getRepairRecipes(registration.getVanillaRecipeFactory()));

        // Dragon's breath brewing (vanilla brewing category)
        if (ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) {
            registration.addRecipes(RecipeTypes.BREWING, List.of(
                    registration.getVanillaRecipeFactory().createBrewingRecipe(
                            List.of(new ItemStack(Items.CHORUS_FRUIT)),
                            PotionContents.createItemStack(Items.POTION, Potions.THICK),
                            new ItemStack(Items.DRAGON_BREATH),
                            OvergearedMod.loc("dragon_breath_brewing")
                    )
            ));
        }

        // Fletching
        if (ServerConfig.ENABLE_FLETCHING_RECIPES.get()) {
            registration.addRecipes(FletchingCategory.FLETCHING_RECIPE_TYPE,
                    unwrap(manager.getAllRecipesFor(ModRecipeTypes.FLETCHING.get())));
            registration.addRecipes(FletchingCategory.FLETCHING_RECIPE_TYPE, generatePotionConversions());
        }
    }

    private static <T extends Recipe<?>> List<T> unwrap(List<RecipeHolder<T>> holders) {
        return holders.stream().map(RecipeHolder::value).toList();
    }

    private static List<ForgingRecipe> filterByTier(List<ForgingRecipe> list, AnvilTier tier) {
        return list.stream()
                .filter(r -> AnvilTier.fromDisplayName(r.getAnvilTier()) == tier)
                .toList();
    }

    /**
     * Display-only fletching entries for tipping arrows with potions (mirrors the EMI plugin):
     * vanilla arrows always; iron/steel/diamond arrows only when UPGRADE_ARROW_POTION_TOGGLE is on.
     */
    private static List<FletchingRecipe> generatePotionConversions() {
        List<Item> arrows = new ArrayList<>();
        arrows.add(Items.ARROW);
        if (ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get()) {
            arrows.add(ModItems.IRON_UPGRADE_ARROW.get());
            arrows.add(ModItems.STEEL_UPGRADE_ARROW.get());
            arrows.add(ModItems.DIAMOND_UPGRADE_ARROW.get());
        }

        List<Holder.Reference<Potion>> potions = BuiltInRegistries.POTION.holders()
                .filter(p -> !p.value().getEffects().isEmpty())
                .toList();

        List<FletchingRecipe> list = new ArrayList<>();
        for (Item arrow : arrows) {
            for (Holder<Potion> potion : potions) {
                list.add(potionConversion(arrow, potion, false));
                list.add(potionConversion(arrow, potion, true));
            }
        }
        return list;
    }

    private static FletchingRecipe potionConversion(Item arrow, Holder<Potion> potion, boolean lingering) {
        ItemStack potionStack = PotionContents.createItemStack(lingering ? Items.LINGERING_POTION : Items.POTION, potion);

        ItemStack output;
        if (arrow == Items.ARROW) {
            output = new ItemStack(lingering ? ModItems.LINGERING_ARROW.get() : Items.TIPPED_ARROW);
        } else {
            output = new ItemStack(arrow);
            if (lingering) output.set(ModComponents.LINGERING_STATUS.get(), true);
        }
        output.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));

        return new FletchingRecipe(
                Ingredient.EMPTY,
                Ingredient.of(arrow),
                Ingredient.EMPTY,
                Ingredient.of(potionStack),
                output,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                "Potion",
                "LingeringPotion"
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        if (EMI_LOADED) return;
        registration.addRecipeClickArea(StoneSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);
        registration.addRecipeClickArea(TieredSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);
        registration.addRecipeClickArea(AlloySmelterScreen.class, 86, 35, 22, 15,
                AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);
        registration.addRecipeClickArea(NetherAlloySmelterScreen.class, 90, 35, 22, 15,
                NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);
        registration.addRecipeClickArea(FletchingStationScreen.class, 90, 35, 22, 15,
                FletchingCategory.FLETCHING_RECIPE_TYPE);
        registration.addRecipeClickArea(RockKnappingScreen.class, 90, 35, 22, 15,
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (EMI_LOADED) return;
        // Replaces 1.20.1's useNbtForSubtypes: distinguish arrows by potion + lingering status.
        ISubtypeInterpreter<ItemStack> arrowInterpreter = new ISubtypeInterpreter<>() {
            @Override
            public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                Boolean lingering = stack.get(ModComponents.LINGERING_STATUS.get());
                if (contents == null && lingering == null) return null;
                return List.of(
                        contents == null ? PotionContents.EMPTY : contents,
                        lingering != null && lingering
                );
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                String potion = contents == null ? "" : contents.potion()
                        .flatMap(Holder::unwrapKey)
                        .map(k -> k.location().toString())
                        .orElse("");
                return potion + (Boolean.TRUE.equals(stack.get(ModComponents.LINGERING_STATUS.get())) ? ";lingering" : "");
            }
        };

        registration.registerSubtypeInterpreter(ModItems.LINGERING_ARROW.get(), arrowInterpreter);
        registration.registerSubtypeInterpreter(ModItems.IRON_UPGRADE_ARROW.get(), arrowInterpreter);
        registration.registerSubtypeInterpreter(ModItems.STEEL_UPGRADE_ARROW.get(), arrowInterpreter);
        registration.registerSubtypeInterpreter(ModItems.DIAMOND_UPGRADE_ARROW.get(), arrowInterpreter);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (EMI_LOADED) return;
        registration.addRecipeTransferHandler(TieredSmithingAnvilMenu.class,
                ModMenuTypes.TIERED_SMITHING_ANVIL_MENU.get(),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 38, 9, 0, 36);
        registration.addRecipeTransferHandler(StoneSmithingAnvilMenu.class,
                ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE, 37, 9, 0, 36);
        registration.addRecipeTransferHandler(FletchingStationMenu.class,
                ModMenuTypes.FLETCHING_STATION_MENU.get(),
                FletchingCategory.FLETCHING_RECIPE_TYPE, 0, 4, 5, 36);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (EMI_LOADED) return;
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ALLOY_FURNACE.get()),
                AlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NETHER_ALLOY_FURNACE.get()),
                NetherAlloySmeltingRecipeCategory.ALLOY_SMELTING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.GRINDSTONE), GrindingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.WATER_BUCKET), CoolingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.CAULDRON), CoolingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Blocks.FLETCHING_TABLE), FletchingCategory.FLETCHING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CAST_FURNACE.get()), CastingRecipeCategory.CASTING_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ROCK.get()), KnappingRecipeCategory.KNAPPING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRAFTING_TABLE.get()), DraftingTableCategory.DRAFTING);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()),
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);
        for (AnvilTier tier : AnvilTier.configured()) {
            if (tier.isEnabled()) {
                registration.addRecipeCatalyst(new ItemStack(ModBlocks.anvilFor(tier)),
                        ForgingRecipeCategory.FORGING_RECIPE_TYPE);
            }
        }
    }
}
