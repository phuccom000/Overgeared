package net.stirdrem.overgeared.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Helpers shared by Overgeared's custom recipe builders (26.3 RecipeOutput API). */
final class RecipeBuilderSupport {
    private RecipeBuilderSupport() {
    }

    static ResourceKey<Recipe<?>> key(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    static Identifier defaultId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }

    /** An ingredient that may need the item registry lookup (tags) to be resolved at save time. */
    static Function<HolderGetter<Item>, Ingredient> lazy(TagKey<Item> tag) {
        return items -> Ingredient.of(items.getOrThrow(tag));
    }

    static Function<HolderGetter<Item>, Ingredient> lazy(Ingredient ingredient) {
        return items -> ingredient;
    }

    static HolderGetter<Item> items(RecipeOutput output) {
        return output.lookup(Registries.ITEM);
    }

    /** Collects unlock criteria and builds the standard "unlock recipe" advancement. */
    static final class Unlocks {
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

        void add(String name, Criterion<?> criterion) {
            criteria.put(name, criterion);
        }

        boolean isEmpty() {
            return criteria.isEmpty();
        }

        /** Advancement stored at {@code <ns>:recipes/<folder>/<path>}, like vanilla. */
        AdvancementHolder build(RecipeOutput output, ResourceKey<Recipe<?>> id, String folder) {
            if (criteria.isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + id.identifier());
            }
            Advancement.Builder advancement = output.advancement()
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(output.lookup(Registries.RECIPE).getOrThrow(id)))
                    .rewards(AdvancementRewards.Builder.recipe(id))
                    .requirements(AdvancementRequirements.Strategy.OR);
            criteria.forEach(advancement::addCriterion);
            return advancement.build(id.identifier().withPrefix("recipes/" + folder + "/"));
        }
    }
}
