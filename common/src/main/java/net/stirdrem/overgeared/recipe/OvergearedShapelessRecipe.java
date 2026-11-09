package net.stirdrem.overgeared.recipe;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.ItemStackTemplate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.recipe.ingredients.IngredientWithRemainder;

public class OvergearedShapelessRecipe extends ShapelessRecipe {
    private final NonNullList<IngredientWithRemainder> ingredientsWithRemainder;
    private final ItemStackTemplate result;

    public OvergearedShapelessRecipe(String group, CraftingBookCategory category, ItemStackTemplate result, NonNullList<IngredientWithRemainder> ingredients) {
        super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(category, group),
                result, convertToBaseIngredients(ingredients));

        this.ingredientsWithRemainder = ingredients;
        this.result = result;
    }

    // Convert our custom ingredients to base Minecraft ingredients for parent class
    private static NonNullList<Ingredient> convertToBaseIngredients(NonNullList<IngredientWithRemainder> customIngredients) {
        NonNullList<Ingredient> baseIngredients = NonNullList.create();
        for (IngredientWithRemainder ingredient : customIngredients) {
            baseIngredients.add(ingredient.getIngredient());
        }
        return baseIngredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        // Track which ingredients have been processed
        boolean[] ingredientProcessed = new boolean[ingredientsWithRemainder.size()];

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack slotStack = input.getItem(slot);
            if (slotStack.isEmpty()) continue;

            // Find matching ingredient with remainder
            for (int ingIndex = 0; ingIndex < ingredientsWithRemainder.size(); ingIndex++) {
                if (!ingredientProcessed[ingIndex] && ingredientsWithRemainder.get(ingIndex).getIngredient().test(slotStack)) {
                    IngredientWithRemainder ingredient = ingredientsWithRemainder.get(ingIndex);

                    if (ingredient.hasRemainder()) {
                        ItemStack remainder = ingredient.getRemainder(slotStack);
                        if (!remainder.isEmpty()) {
                            remainingItems.set(slot, remainder);
                        }
                    }

                    ingredientProcessed[ingIndex] = true;
                    break;
                }
            }
        }

        return remainingItems;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = super.assemble(input);

        if (!ServerConfig.ENABLE_MINIGAME.get()) {
            // When minigame is disabled
            boolean hasUnpolishedQualityItem = false;
            boolean unquenched = false;
            ForgingQuality foundQuality = null;
            String creator = null;

            for (int i = 0; i < input.size(); i++) {
                ItemStack ingredient = input.getItem(i);

                // Check if item is heated (unquenched)
                if (ingredient.getOrDefault(ModComponents.HEATED_COMPONENT.get(), false)) {
                    unquenched = true;
                    break;
                }

                // Check if item is polished
                Boolean polished = ingredient.get(ModComponents.POLISHED.get());
                if (polished != null && !polished) {
                    hasUnpolishedQualityItem = true;
                    break;
                }

                // Get forging quality
                ForgingQuality quality = ingredient.get(ModComponents.FORGING_QUALITY.get());
                if (quality != null && quality != ForgingQuality.NONE) {
                    foundQuality = quality;
                }

                // Get creator
                String itemCreator = ingredient.get(ModComponents.CREATOR.get());
                if (itemCreator != null) {
                    creator = itemCreator;
                }
            }

            // Prevent crafting if any unpolished quality items exist or item is unquenched
            if (hasUnpolishedQualityItem || unquenched) {
                return ItemStack.EMPTY;
            }

            // Set quality on result
            if (foundQuality == null) {
                foundQuality = ForgingQuality.NONE;
            }
            result.set(ModComponents.FORGING_QUALITY.get(), foundQuality);

            if (creator != null) {
                result.set(ModComponents.CREATOR.get(), creator);
            }

            return result;
        }

        // Original minigame-enabled logic
        ForgingQuality foundQuality = null;
        boolean isPolished = true;
        boolean unquenched = false;
        String creator = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack ingredient = input.getItem(i);

            // Get forging quality from component
            ForgingQuality quality = ingredient.get(ModComponents.FORGING_QUALITY.get());
            if (quality != null && quality != ForgingQuality.NONE) {
                foundQuality = quality;
            }

            // Check if polished
            Boolean polished = ingredient.get(ModComponents.POLISHED.get());
            if (polished != null && !polished) {
                isPolished = false;
            }

            // Check if heated (unquenched)
            if (ingredient.getOrDefault(ModComponents.HEATED_COMPONENT.get(), false)) {
                unquenched = true;
            }

            // Get creator
            String itemCreator = ingredient.get(ModComponents.CREATOR.get());
            if (itemCreator != null) {
                creator = itemCreator;
            }
        }

        if (foundQuality == null || foundQuality == ForgingQuality.NONE) {
            // If no quality found
            if (!isPolished || unquenched) {
                // Either not polished OR unquenched (or both) → set to POOR
                result.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.POOR);
            }
            return result;
        } else {
            ForgingQuality quality = foundQuality;

            if (!isPolished) {
                quality = quality.getLowerQuality();
            }
            if (unquenched) {
                quality = quality.getLowerQuality();
            }

            result.set(ModComponents.FORGING_QUALITY.get(), quality);
            if (creator != null) {
                result.set(ModComponents.CREATOR.get(), creator);
            }
            return result;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        // The parent fixes the type parameter; the serializer is really a RecipeSerializer<OvergearedShapelessRecipe>.
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.CRAFTING_SHAPELESS.get();
    }

    private static NonNullList<IngredientWithRemainder> toNonNullList(IngredientWithRemainder[] ingredients) {
        NonNullList<IngredientWithRemainder> list = NonNullList.create();
        java.util.Collections.addAll(list, ingredients);
        return list;
    }

    public static class Serializer {

        public static final MapCodec<OvergearedShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::group),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                IngredientWithRemainder.CODEC.listOf(0, 9).fieldOf("ingredients").flatXmap(list -> {
                        IngredientWithRemainder[] ingredients = list.toArray(IngredientWithRemainder[]::new);
                        int size = 3 * 3;
                        if (ingredients.length == 0) {
                            return DataResult.error(() -> "No ingredients for shapeless recipe");
                        } else {
                            return ingredients.length > size ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: The maximum is: %s".formatted(size)) : DataResult.success(toNonNullList(ingredients));
                        }
                }, DataResult::success).forGetter(r -> r.ingredientsWithRemainder)
        ).apply(instance, OvergearedShapelessRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, OvergearedShapelessRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        public static OvergearedShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);

            int ingredientCount = buffer.readVarInt();
            NonNullList<IngredientWithRemainder> ingredients = NonNullList.create();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(IngredientWithRemainder.STREAM_CODEC.decode(buffer));
            }

            return new OvergearedShapelessRecipe(group, category, result, ingredients);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buffer, OvergearedShapelessRecipe recipe) {
            buffer.writeUtf(recipe.group());
            buffer.writeEnum(recipe.category());
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);

            buffer.writeVarInt(recipe.ingredientsWithRemainder.size());
            for (IngredientWithRemainder ingredient : recipe.ingredientsWithRemainder) {
                IngredientWithRemainder.STREAM_CODEC.encode(buffer, ingredient);
            }
        }

        public MapCodec<OvergearedShapelessRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, OvergearedShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}