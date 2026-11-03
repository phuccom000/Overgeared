package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.stirdrem.overgeared.recipe.CoolingRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * EMI recipe display for Cooling recipes.
 */
public class CoolingEmiRecipe implements EmiRecipe {

    private static final int SLOT_SIZE = 18;

    private final ResourceLocation id;
    private final CoolingRecipe recipe;
    private final EmiIngredient inputs;
    private final EmiStack outputs;

    public CoolingEmiRecipe(RecipeHolder<CoolingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();

        this.inputs = EmiIngredient.of(recipe.getIngredient());

        this.outputs = EmiStack.of(recipe.getResultItem(null));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.COOLING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(inputs);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(outputs);
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 22;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int y = 2;

        // Input slot
        widgets.addSlot(inputs, 2, y)
                .drawBack(true);

        // Plus icon
        widgets.addTexture(EmiTexture.PLUS, 26, y + 2);

        // Water slot
        widgets.addSlot(EmiStack.of(Fluids.WATER), 45, y)
                .drawBack(true);

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, y);

        // Output slot
        widgets.addSlot(outputs, 100, y)
                .drawBack(true)
                .recipeContext(this);
    }

}
