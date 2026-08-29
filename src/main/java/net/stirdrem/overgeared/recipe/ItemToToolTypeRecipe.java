package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class ItemToToolTypeRecipe implements Recipe<SimpleInventory> {

    private final Identifier id;
    private final Ingredient input;
    private final String toolType;

    public ItemToToolTypeRecipe(Identifier id, Ingredient input, String toolType) {
        this.id = id;
        this.input = input;
        this.toolType = toolType;
    }

    public Ingredient getInput() {
        return input;
    }

    public String getToolType() {
        return toolType;
    }

    @Override
    public boolean matches(SimpleInventory container, World world) {
        return input.test(container.getStack(0));
    }

    @Override
    public ItemStack craft(SimpleInventory container, DynamicRegistryManager registryAccess) {
        return ItemStack.EMPTY; // purely data-driven recipe
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ITEM_TO_TOOLTYPE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE;
    }

    public List<ItemStack> getItems() {
        return List.of(input.getMatchingStacks());
    }

    // ----------------------------------------------------
    // Serializer
    // ----------------------------------------------------
    public static class Serializer implements RecipeSerializer<ItemToToolTypeRecipe> {

        @Override
        public ItemToToolTypeRecipe read(Identifier id, JsonObject json) {
            // Allow "item" to be either an object or an array
            if (!json.has("item")) {
                throw new JsonSyntaxException("Missing 'item' for item_to_tooltype recipe");
            }

            JsonElement itemElement = json.get("item");
            Ingredient input = Ingredient.fromJson(itemElement);
            String toolType = json.get("tooltype").getAsString();

            return new ItemToToolTypeRecipe(id, input, toolType);
        }

        @Override
        public ItemToToolTypeRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient input = Ingredient.fromPacket(buf);
            String toolType = buf.readString();
            return new ItemToToolTypeRecipe(id, input, toolType);
        }

        @Override
        public void write(PacketByteBuf buf, ItemToToolTypeRecipe recipe) {
            recipe.input.write(buf);
            buf.writeString(recipe.toolType);
        }
    }
}
