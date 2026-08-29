package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class NetherAlloySmeltingRecipe implements Recipe<SimpleInventory>, INetherAlloyRecipe {
    private final Identifier id;
    private final String group;
    private final CraftingRecipeCategory category;
    private final List<Ingredient> inputs;
    private final ItemStack output;
    private final float experience;
    private final int cookingTime;

    public NetherAlloySmeltingRecipe(Identifier id, String group, CraftingRecipeCategory category, List<Ingredient> inputs, ItemStack output, float experience, int cookingTime) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.inputs = inputs;
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(SimpleInventory inv, World world) {
        // Don't bother checking on client side for performance reasons
        if (world.isClient) return false;

        // Create a list of non-empty ingredient-item pairs to match
        List<Ingredient> remainingIngredients = new ArrayList<>();
        List<ItemStack> remainingItems = new ArrayList<>();

        // Collect all recipe ingredients (skip empty ingredients if any)
        for (Ingredient ingredient : inputs) {
            if (!ingredient.isEmpty()) {
                remainingIngredients.add(ingredient);
            }
        }

        // Collect all non-empty items from the input slots
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                remainingItems.add(stack);
            }
        }

        // Must have same number of non-empty items as non-empty ingredients
        if (remainingItems.size() != remainingIngredients.size()) {
            return false;
        }

        // Try to match every stack in inventory to an ingredient
        for (ItemStack stack : remainingItems) {
            boolean matched = false;

            for (int i = 0; i < remainingIngredients.size(); i++) {
                if (remainingIngredients.get(i).test(stack)) {
                    remainingIngredients.remove(i); // consume one ingredient
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false; // found a stack that doesn't match any remaining ingredient
            }
        }

        // All ingredients matched successfully
        return remainingIngredients.isEmpty();
    }


    @Override
    public ItemStack craft(SimpleInventory container, DynamicRegistryManager registryAccess) {
        return output.copy();
    }

    @Override
    public boolean fits(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryAccess) {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.NETHER_ALLOY_SMELTING;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.NETHER_ALLOY_SMELTING;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public CraftingRecipeCategory category() {
        return category;
    }

    @Override
    public float getExperience() {
        return experience;
    }

    @Override
    public boolean isShaped() {
        return false;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    @Override
    public List<Ingredient> getIngredientsList() {
        return inputs;
    }

    // ---------------------------------------------------------------------------------------
    // Type & Serializer
    // ---------------------------------------------------------------------------------------
    public static class Type implements RecipeType<NetherAlloySmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "nether_alloy_smelting";
    }

    public static class Serializer implements RecipeSerializer<NetherAlloySmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public NetherAlloySmeltingRecipe read(Identifier id, JsonObject json) {
            String group = json.has("group") ? json.get("group").getAsString() : "";
            CraftingRecipeCategory category = json.has("category")
                    ? CraftingRecipeCategory.CODEC.byId(json.get("category").getAsString(), CraftingRecipeCategory.MISC)
                    : CraftingRecipeCategory.MISC;

            JsonArray ingredients = json.getAsJsonArray("ingredients");
            List<Ingredient> inputList = new ArrayList<>();
            for (int i = 0; i < ingredients.size(); i++) {
                inputList.add(Ingredient.fromJson(ingredients.get(i)));
            }
            if (ingredients.size() > 9) {
                throw new JsonSyntaxException("Alloy smelting recipe cannot have more than 9 ingredients: found " + ingredients.size());
            }

            ItemStack result = ShapedRecipe.outputFromJson(json.getAsJsonObject("result"));
            float experience = json.has("experience") ? json.get("experience").getAsFloat() : 0.0F;
            int cookingTime = json.has("cookingtime") ? json.get("cookingtime").getAsInt() : 200;

            return new NetherAlloySmeltingRecipe(id, group, category, inputList, result, experience, cookingTime);
        }

        @Override
        public NetherAlloySmeltingRecipe read(Identifier id, PacketByteBuf buf) {
            String group = buf.readString();
            CraftingRecipeCategory category = buf.readEnumConstant(CraftingRecipeCategory.class);

            int count = buf.readVarInt();
            List<Ingredient> inputs = new ArrayList<>();
            for (int i = 0; i < count; i++) inputs.add(Ingredient.fromPacket(buf));

            ItemStack result = buf.readItemStack();
            float experience = buf.readFloat();
            int cookingTime = buf.readVarInt();

            return new NetherAlloySmeltingRecipe(id, group, category, inputs, result, experience, cookingTime);
        }

        @Override
        public void write(PacketByteBuf buf, NetherAlloySmeltingRecipe recipe) {
            buf.writeString(recipe.group);
            buf.writeEnumConstant(recipe.category);
            buf.writeVarInt(recipe.inputs.size());
            recipe.inputs.forEach(i -> i.write(buf));
            buf.writeItemStack(recipe.output);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
        }
    }
}
