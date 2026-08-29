package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.config.ServerConfig;

public class OvergearedShapelessRecipe extends ShapelessRecipe {

    private final DefaultedList<IngredientWithRemainder> ingredientsWithRemainder;

    public OvergearedShapelessRecipe(Identifier id, String group, CraftingRecipeCategory category,
                                      ItemStack result, DefaultedList<IngredientWithRemainder> ingredientsWithRemainder) {
        super(id, group, category, result, convertToBaseIngredients(ingredientsWithRemainder));
        this.ingredientsWithRemainder = ingredientsWithRemainder;
    }

    // Convert our custom ingredients to base Minecraft ingredients for parent class
    private static DefaultedList<Ingredient> convertToBaseIngredients(DefaultedList<IngredientWithRemainder> customIngredients) {
        DefaultedList<Ingredient> baseIngredients = DefaultedList.of();
        for (IngredientWithRemainder ingredient : customIngredients) {
            baseIngredients.add(ingredient.getIngredient());
        }
        return baseIngredients;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory container) {
        DefaultedList<ItemStack> remainingItems = DefaultedList.ofSize(container.size(), ItemStack.EMPTY);

        // Track which ingredients have been processed
        boolean[] ingredientProcessed = new boolean[ingredientsWithRemainder.size()];

        for (int slot = 0; slot < container.size(); slot++) {
            ItemStack slotStack = container.getStack(slot);
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
    public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        ItemStack result = super.craft(container, registryAccess);

        if (!ServerConfig.ENABLE_MINIGAME.get()) {
            // When minigame is disabled
            boolean hasUnpolishedQualityItem = false;
            boolean unquenched = false;
            String foundQuality = null;
            String creator = null;
            for (int i = 0; i < container.size(); i++) {
                ItemStack ingredient = container.getStack(i);
                if (ingredient.hasNbt()) {
                    NbtCompound tag = ingredient.getNbt();

                    if (tag.contains("Polished") && !tag.getBoolean("Polished")) {
                        hasUnpolishedQualityItem = true;
                        break;
                    }
                    if (tag.contains("Heated") && tag.getBoolean("Heated")) {
                        unquenched = true;
                        break;
                    }
                    if (tag.contains("ForgingQuality")) {
                        if (!tag.getString("ForgingQuality").equals("none"))
                            foundQuality = tag.getString("ForgingQuality");
                    }
                    if (tag.contains("Creator")) {
                        creator = tag.getString("Creator");
                    }
                }
            }

            // Prevent crafting if any unpolished quality items exist
            if (hasUnpolishedQualityItem || unquenched) {
                return ItemStack.EMPTY;
            }
            NbtCompound resultTag = result.getOrCreateNbt();
            ForgingQuality quality = ForgingQuality.fromString(foundQuality);
            resultTag.putString("ForgingQuality", quality.getDisplayName());
            if (creator != null)
                resultTag.putString("Creator", creator);
            result.setNbt(resultTag);
            return result;
        }

        // Original minigame-enabled logic
        NbtCompound resultTag = result.getOrCreateNbt();
        String foundQuality = null;
        boolean isPolished = true;
        boolean unquenched = false;
        String creator = null;
        for (int i = 0; i < container.size(); i++) {
            ItemStack ingredient = container.getStack(i);
            if (ingredient.hasNbt()) {
                NbtCompound tag = ingredient.getNbt();
                if (tag.contains("ForgingQuality")) {
                    if (!tag.getString("ForgingQuality").equals("none"))
                        foundQuality = tag.getString("ForgingQuality");
                }
                if (tag.contains("Polished") && !tag.getBoolean("Polished")) {
                    isPolished = false;
                }
                if (tag.contains("Heated") && tag.getBoolean("Heated")) {
                    unquenched = true;
                }
                if (tag.contains("Creator")) {
                    creator = tag.getString("Creator");
                }
            }
        }
        if (foundQuality == null || foundQuality.equals("none")) {
            // If no quality found
            if (!isPolished || unquenched) {
                // Either polished OR unquenched (or both) → set to POOR
                resultTag.putString("ForgingQuality", ForgingQuality.POOR.getDisplayName());
                result.setNbt(resultTag);
            }
            return result;
        } else {
            ForgingQuality quality = ForgingQuality.fromString(foundQuality);

            if (!isPolished) {
                quality = quality.getLowerQuality();
            }
            if (unquenched) {
                quality = quality.getLowerQuality();
            }

            resultTag.putString("ForgingQuality", quality.getDisplayName());
            if (creator != null)
                resultTag.putString("Creator", creator);
            result.setNbt(resultTag);
            return result;
        }
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    // Custom ingredient class with remainder support
    public static class IngredientWithRemainder {
        private final Ingredient ingredient;
        private final boolean remainder;
        private final int durabilityDecrease;

        public IngredientWithRemainder(Ingredient ingredient, boolean remainder, int durabilityDecrease) {
            this.ingredient = ingredient;
            this.remainder = remainder;
            this.durabilityDecrease = durabilityDecrease;
        }

        public static IngredientWithRemainder fromNetwork(PacketByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromPacket(buffer);
            boolean remainder = buffer.readBoolean();
            int durabilityDecrease = buffer.readInt();
            return new IngredientWithRemainder(ingredient, remainder, durabilityDecrease);
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public boolean hasRemainder() {
            return remainder;
        }

        public int getDurabilityDecrease() {
            return durabilityDecrease;
        }

        public ItemStack getRemainder(ItemStack original) {
            if (!remainder) {
                return ItemStack.EMPTY;
            }

            ItemStack remainderStack = original.copy();
            remainderStack.setCount(1);

            // Handle durability decrease for damageable items
            if (durabilityDecrease > 0 && remainderStack.isDamageable()) {
                int newDamage = remainderStack.getDamage() + durabilityDecrease;
                if (newDamage >= remainderStack.getMaxDamage()) {
                    return ItemStack.EMPTY; // Item breaks
                }
                remainderStack.setDamage(newDamage);
            }

            return remainderStack;
        }

        public void toNetwork(PacketByteBuf buffer) {
            ingredient.write(buffer);
            buffer.writeBoolean(remainder);
            buffer.writeInt(durabilityDecrease);
        }
    }

    public static class Type implements RecipeType<OvergearedShapelessRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_shapeless";
    }

    public static class Serializer implements RecipeSerializer<OvergearedShapelessRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public OvergearedShapelessRecipe read(Identifier recipeId, JsonObject json) {
            String group = json.has("group") ? json.get("group").getAsString() : "";
            CraftingRecipeCategory category = CraftingRecipeCategory.CODEC.byId(
                    JsonHelper.getString(json, "category", "misc"), CraftingRecipeCategory.MISC);
            // Parse result
            JsonObject resultJson = json.getAsJsonObject("result");
            ItemStack result = ShapedRecipe.outputFromJson(resultJson);

            // Parse ingredients with remainder support
            JsonArray ingredientsJson = json.getAsJsonArray("ingredients");
            DefaultedList<IngredientWithRemainder> ingredients = DefaultedList.of();

            for (JsonElement element : ingredientsJson) {
                JsonObject ingredientJson = element.getAsJsonObject();

                // Parse base ingredient
                Ingredient ingredient = Ingredient.fromJson(ingredientJson);

                // Parse remainder properties
                boolean remainder = ingredientJson.has("remainder") && ingredientJson.get("remainder").getAsBoolean();
                int durabilityDecrease = ingredientJson.has("durability_decrease") ? ingredientJson.get("durability_decrease").getAsInt() : 0;

                ingredients.add(new IngredientWithRemainder(ingredient, remainder, durabilityDecrease));
            }

            return new OvergearedShapelessRecipe(recipeId, group, category, result, ingredients);
        }

        @Override
        public OvergearedShapelessRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            String group = buffer.readString();
            CraftingRecipeCategory category = buffer.readEnumConstant(CraftingRecipeCategory.class);
            ItemStack result = buffer.readItemStack();

            int ingredientCount = buffer.readVarInt();
            DefaultedList<IngredientWithRemainder> ingredients = DefaultedList.of();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(IngredientWithRemainder.fromNetwork(buffer));
            }

            return new OvergearedShapelessRecipe(recipeId, group, category, result, ingredients);
        }

        @Override
        public void write(PacketByteBuf buffer, OvergearedShapelessRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            buffer.writeItemStack(recipe.getOutput(null));

            buffer.writeVarInt(recipe.ingredientsWithRemainder.size());
            for (IngredientWithRemainder ingredient : recipe.ingredientsWithRemainder) {
                ingredient.toNetwork(buffer);
            }
        }
    }
}
