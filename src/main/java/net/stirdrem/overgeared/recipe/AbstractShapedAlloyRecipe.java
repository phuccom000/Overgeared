package net.stirdrem.overgeared.recipe;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public abstract class AbstractShapedAlloyRecipe
        implements Recipe<SimpleInventory> {

    protected final Identifier id;
    protected final String group;
    protected final CraftingRecipeCategory category;

    protected final int width;
    protected final int height;
    protected final int gridSize; // 2 or 3

    protected final DefaultedList<Ingredient> patterns;

    protected final ItemStack output;
    protected final float experience;
    protected final int cookingTime;

    protected AbstractShapedAlloyRecipe(
            Identifier id,
            String group,
            CraftingRecipeCategory category,
            int width,
            int height,
            int gridSize,
            DefaultedList<Ingredient> patterns,
            ItemStack output,
            float experience,
            int cookingTime
    ) {
        if (width < 1 || height < 1 || width > gridSize || height > gridSize)
            throw new IllegalArgumentException("Pattern must fit inside " + gridSize + "x" + gridSize);

        if (patterns.size() != width * height)
            throw new IllegalArgumentException("Ingredient count does not match pattern size");

        this.id = id;
        this.group = group;
        this.category = category;
        this.width = width;
        this.height = height;
        this.gridSize = gridSize;
        this.patterns = patterns;
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    // -----------------------
    // Matching logic
    // -----------------------

    @Override
    public boolean matches(SimpleInventory inv, World world) {
        if (world.isClient) return false;

        for (int offY = 0; offY <= gridSize - height; offY++) {
            for (int offX = 0; offX <= gridSize - width; offX++) {
                if (matchesAt(inv, offX, offY)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean matchesAt(SimpleInventory inv, int offX, int offY) {
        // Match pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int recipeIndex = y * width + x;
                int invIndex = (y + offY) * gridSize + (x + offX);

                if (!patterns.get(recipeIndex).test(inv.getStack(invIndex))) {
                    return false;
                }
            }
        }

        // Remaining slots must be empty
        for (int i = 0; i < gridSize * gridSize; i++) {
            int x = i % gridSize;
            int y = i / gridSize;

            boolean inside =
                    x >= offX && x < offX + width &&
                            y >= offY && y < offY + height;

            if (!inside && !inv.getStack(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // -----------------------
    // Boilerplate
    // -----------------------

    @Override
    public ItemStack craft(SimpleInventory inv, DynamicRegistryManager access) {
        return output.copy();
    }

    @Override
    public boolean fits(int w, int h) {
        return w >= gridSize && h >= gridSize;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager access) {
        return output.copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public CraftingRecipeCategory category() {
        return category;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Ingredient> getIngredientsList() {
        return patterns;
    }

    public float getExperience() {
        return experience;
    }

    public int getCookingTime() {
        return cookingTime;
    }
}
