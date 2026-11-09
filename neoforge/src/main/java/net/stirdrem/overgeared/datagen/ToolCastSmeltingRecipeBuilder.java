package net.stirdrem.overgeared.datagen;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.CastSmeltingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ToolCastSmeltingRecipeBuilder implements RecipeBuilder {

    private final ItemLike result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new HashMap<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private String toolType;
    @Nullable
    private Boolean needPolishing = null;
    @Nullable
    private String group = "misc";
    @Nullable
    private String category = "misc";

    public ToolCastSmeltingRecipeBuilder(ItemLike result, float experience, int cookTime) {
        this.result = result;
        this.experience = experience;
        this.cookTime = cookTime;
    }

    public static ToolCastSmeltingRecipeBuilder cast(ItemLike result, float xp, int time) {
        return new ToolCastSmeltingRecipeBuilder(result, xp, time);
    }

    public ToolCastSmeltingRecipeBuilder toolType(String type) {
        this.toolType = type;
        return this;
    }

    public ToolCastSmeltingRecipeBuilder material(String material, int amount) {
        this.materialInput.put(material, amount);
        return this;
    }

    public ToolCastSmeltingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public ToolCastSmeltingRecipeBuilder category(String category) {
        this.category = category;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(result.asItem()));
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> idKey) {
        Identifier id = idKey.identifier();
        ensureValid(id);
        // The recipe is saved with a suffix; the advancement unlocks (and is named after) that recipe
        Identifier recipeId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_from_cast_smelting");
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);

        Advancement.Builder advBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(output.lookup(Registries.RECIPE).getOrThrow(recipeKey)))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advBuilder::addCriterion);

        // Convert Integer map to Double map for recipe
        Map<String, Double> requiredMaterials = new HashMap<>();
        materialInput.forEach((mat, amt) -> requiredMaterials.put(mat, (double) amt));

        // Determine cooking book category
        CookingBookCategory bookCategory = CookingBookCategory.MISC;
        
        // Create the actual CastSmeltingRecipe instance
        CastSmeltingRecipe recipe = new CastSmeltingRecipe(
                group == null ? "" : group,
                bookCategory,
                materialInput,
                toolType,
                new ItemStackTemplate(result.asItem()),
                needPolishing != null ? needPolishing : true,
                experience,
                cookTime
        );
        output.accept(recipeKey, recipe, advBuilder.build(recipeId.withPrefix("recipes/misc/")));
    }

    private void ensureValid(Identifier id) {
        if (toolType == null)
            throw new IllegalStateException("Tool type missing for " + id);

        if (materialInput.isEmpty())
            throw new IllegalStateException("No material input for " + id);

        if (criteria.isEmpty())
            throw new IllegalStateException("No unlock criteria for " + id);
    }
}
