package net.stirdrem.overgeared.datagen;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class CastingRecipeBuilder implements RecipeBuilder {

    private final ItemLike result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    private String toolType;
    @Nullable
    private Boolean needPolishing;
    @Nullable
    private String group;

    private CastingRecipeBuilder(ItemLike result, float xp, int cookTime) {
        this.result = result;
        this.experience = xp;
        this.cookTime = cookTime;
    }

    /* ================= FACTORY ================= */

    public static CastingRecipeBuilder casting(ItemLike result, float xp, int cookTime) {
        return new CastingRecipeBuilder(result, xp, cookTime);
    }

    /* ================= FLUENT API ================= */

    public CastingRecipeBuilder toolType(String type) {
        this.toolType = type;
        return this;
    }

    public CastingRecipeBuilder material(String material, int amount) {
        this.materialInput.put(material.toLowerCase(java.util.Locale.ROOT), amount);
        return this;
    }

    public CastingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    @Override
    public CastingRecipeBuilder unlockedBy(String name, net.minecraft.advancements.triggers.Criterion<?> criterion) {
        this.advancement.addCriterion(name, criterion);
        return this;
    }

    @Override
    public CastingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(this.result.asItem()));
    }

    /* ================= SAVE ================= */

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> idKey) {
        Identifier id = idKey.identifier();
        ensureValid(id);
        // The recipe is saved with a suffix; the advancement unlocks (and is named after) that recipe
        Identifier recipeId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_from_casting_furnace");
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);

        Advancement.Builder advBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(output.lookup(Registries.RECIPE).getOrThrow(recipeKey)))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .requirements(AdvancementRequirements.Strategy.OR);
        CastingRecipe recipe = getCastingRecipe();

        output.accept(
                recipeKey,
                recipe,
                advBuilder.build(recipeId.withPrefix("recipes/casting/")));
    }

    private @NotNull CastingRecipe getCastingRecipe() {
        CookingBookCategory bookCategory = CookingBookCategory.MISC;
        // Build recipe instance
        ItemStackTemplate resultStack = new ItemStackTemplate(this.result.asItem());
        return new CastingRecipe(
                this.group == null ? "" : this.group,
                bookCategory,
                this.materialInput,
                this.toolType,
                resultStack,
                Boolean.TRUE.equals(this.needPolishing),
                this.experience,
                this.cookTime);
    }

    /* ================= VALIDATION ================= */

    private void ensureValid(Identifier id) {
        if (toolType == null || toolType.isBlank()) {
            throw new IllegalStateException("Missing tool_type for casting recipe " + id);
        }

        if (materialInput.isEmpty()) {
            throw new IllegalStateException("No material input defined for casting recipe " + id);
        }
    }
}
