package net.stirdrem.overgeared.client;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModRecipeBookTypes {

    @OnlyIn(Dist.CLIENT)
    public static RecipeBookCategories FORGING_SEARCH;
    @OnlyIn(Dist.CLIENT)
    public static RecipeBookCategories FORGING_MAIN;

    private static final Supplier<RecipeBookCategories> SEARCH_CATEGORY = Suppliers.memoize(
            () -> RecipeBookCategories.create("FORGING_SEARCH", new ItemStack(Items.COMPASS)));

    private static final Map<ForgingBookCategory, Supplier<RecipeBookCategories>> CATEGORY_MAP = new EnumMap<>(
            ForgingBookCategory.class);

    static {
        CATEGORY_MAP.put(ForgingBookCategory.TOOLS, Suppliers
                .memoize(() -> RecipeBookCategories.create("FORGING_TOOLS", new ItemStack(Items.IRON_PICKAXE))));
        CATEGORY_MAP.put(ForgingBookCategory.ARMORS, Suppliers
                .memoize(() -> RecipeBookCategories.create("FORGING_ARMORS", new ItemStack(Items.IRON_CHESTPLATE))));
        CATEGORY_MAP.put(ForgingBookCategory.MISC,
                Suppliers.memoize(() -> RecipeBookCategories.create("FORGING_MISC", new ItemStack(Items.ANVIL))));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {

        ImmutableList<RecipeBookCategories> categories = ImmutableList.of(
                SEARCH_CATEGORY.get(),
                CATEGORY_MAP.get(ForgingBookCategory.TOOLS).get(),
                CATEGORY_MAP.get(ForgingBookCategory.ARMORS).get(),
                CATEGORY_MAP.get(ForgingBookCategory.MISC).get());

        event.registerBookCategories(OvergearedMod.FORGING, categories);

        // Register aggregate category (for search tab)
        event.registerAggregateCategory(SEARCH_CATEGORY.get(),
                ImmutableList.copyOf(CATEGORY_MAP.values().stream().map(Supplier::get).toList()));

        // Register how to determine category per recipe
        event.registerRecipeCategoryFinder(ModRecipeTypes.FORGING.get(), recipe -> {
            if (recipe instanceof ForgingRecipe forgingRecipe) {
                return CATEGORY_MAP.get(forgingRecipe.getRecipeBookTab()).get();
            }
            return CATEGORY_MAP.get(ForgingBookCategory.MISC).get();
        });

    }

    public static void register(IEventBus modEventBus) {
    }
}