package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/**
 * Abstract base class for alloy smelting EMI recipes.
 * Handles common layout logic for both 2x2 and 3x3 input grids.
 *
 * @param <T> The recipe type (AlloySmeltingRecipe or NetherAlloySmeltingRecipe)
 */
public abstract class AbstractAlloySmeltingEmiRecipe<T extends Recipe<?>> implements EmiRecipe {
    
    protected static final int OFFSET_X = 12;
    protected static final int OFFSET_Y = 6;
    
    protected final ResourceLocation id;
    protected final T recipe;
    protected final List<EmiIngredient> inputs;
    protected final List<EmiStack> outputs;

    protected AbstractAlloySmeltingEmiRecipe(RecipeHolder<T> holder, 
                                              List<Ingredient> ingredients, 
                                              EmiStack output) {
        this.id = holder.id();
        this.recipe = holder.value();
        this.inputs = ingredients.stream()
                .map(EmiIngredient::of)
                .toList();
        this.outputs = List.of(output);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    /**
     * @return The number of columns in the input grid (2 or 3)
     */
    protected abstract int getGridColumns();

    /**
     * @return The cooking time in ticks
     */
    protected abstract int getCookingTime();

    /**
     * @return The experience gained from the recipe
     */
    protected abstract float getExperience();

    @Override
    public int getDisplayWidth() {
        int gridWidth = getGridColumns() * EmiLayoutConstants.SLOT_SIZE;
        // Grid + gap + arrow + gap + large slot + padding
        return OFFSET_X * 2 + gridWidth + 6 + EmiLayoutConstants.ARROW_WIDTH + EmiLayoutConstants.PAD + EmiLayoutConstants.LARGE_SLOT_SIZE;
    }

    @Override
    public int getDisplayHeight() {
        int gridHeight = getGridColumns() * EmiLayoutConstants.SLOT_SIZE;
        return Math.max(gridHeight + OFFSET_Y * 2, 50);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int gridX = OFFSET_X;
        int gridY = OFFSET_Y;
        int gridCols = getGridColumns();
        int gridSize = gridCols * EmiLayoutConstants.SLOT_SIZE;
        
        // Input grid
        int inputCount = inputs.size();
        for (int row = 0; row < gridCols; row++) {
            for (int col = 0; col < gridCols; col++) {
                int index = row * gridCols + col;
                int x = gridX + col * EmiLayoutConstants.SLOT_SIZE;
                int y = gridY + row * EmiLayoutConstants.SLOT_SIZE;
                
                if (index < inputCount) {
                    widgets.addSlot(inputs.get(index), x, y);
                } else {
                    widgets.addSlot(EmiStack.EMPTY, x, y);
                }
            }
        }
        
        // Arrow (centered vertically with grid)
        int arrowX = gridX + gridSize + 6;
        int arrowY = gridY + (gridSize - EmiLayoutConstants.ARROW_HEIGHT) / 2 - 8;
        widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowX, arrowY);
        widgets.addFillingArrow(arrowX, arrowY, getCookingTime() * 50);
        
        // Fire directly under arrow
        int fireX = arrowX + (EmiLayoutConstants.ARROW_WIDTH - EmiLayoutConstants.FIRE_WIDTH) / 2; // Centered under arrow
        int fireY = arrowY + EmiLayoutConstants.ARROW_HEIGHT + 1;
        widgets.addTexture(EmiTexture.EMPTY_FLAME, fireX, fireY);
        widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, fireX, fireY, 4000, false, true, true);
        
        // Output (large slot)
        int outputX = arrowX + EmiLayoutConstants.ARROW_WIDTH + EmiLayoutConstants.PAD;
        int outputY = gridY + (gridSize - EmiLayoutConstants.LARGE_SLOT_SIZE) / 2 - 4;
        widgets.addSlot(outputs.getFirst(), outputX, outputY).large(true).recipeContext(this);
        
        // XP text under output
        float xp = getExperience();
        if (xp > 0) {
            widgets.addText(Component.translatable("emi.cooking.experience", xp), outputX, outputY + 28, 0xFF808080, false);
        }
    }
}
