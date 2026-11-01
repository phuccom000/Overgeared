package net.stirdrem.overgeared.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class AbstractShapedAlloyRecipe
        implements Recipe<SimpleContainer> {

    protected final ResourceLocation id;
    protected final String group;
    protected final CraftingBookCategory category;

    protected final int width;
    protected final int height;
    protected final int gridSize; // 2 or 3

    protected final NonNullList<Ingredient> patterns;

    protected final ItemStack output;
    protected final float experience;
    protected final int cookingTime;

    protected AbstractShapedAlloyRecipe(
            ResourceLocation id,
            String group,
            CraftingBookCategory category,
            int width,
            int height,
            int gridSize,
            NonNullList<Ingredient> patterns,
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
    public boolean matches(SimpleContainer inv, Level level) {
        if (level.isClientSide) return false;

        for (int offY = 0; offY <= gridSize - height; offY++) {
            for (int offX = 0; offX <= gridSize - width; offX++) {
                if (matchesAt(inv, offX, offY)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean matchesAt(SimpleContainer inv, int offX, int offY) {
        // Match pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int recipeIndex = y * width + x;
                int invIndex = (y + offY) * gridSize + (x + offX);

                if (!patterns.get(recipeIndex).test(inv.getItem(invIndex))) {
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

            if (!inside && !inv.getItem(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // -----------------------
    // Boilerplate
    // -----------------------

    @Override
    public ItemStack assemble(SimpleContainer inv, net.minecraft.core.RegistryAccess access) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w >= gridSize && h >= gridSize;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess access) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public CraftingBookCategory category() {
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
