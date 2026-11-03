package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.stirdrem.overgeared.recipe.GrindingRecipe;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

/**
 * EMI recipe display for Grinding recipes.
 */
public class GrindingEmiRecipe implements EmiRecipe {

    private static final int SLOT_SIZE = 18;

    private final ResourceLocation id;
    private final GrindingRecipe recipe;
    private final EmiIngredient inputs;
    private final EmiStack outputs;

    public GrindingEmiRecipe(RecipeHolder<GrindingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();

        this.inputs = EmiIngredient.of(recipe.getInput());

        this.outputs = EmiStack.of(recipe.getResultItem(null));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.GRINDING_CATEGORY;
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

        // Grindstone slot
        widgets.addSlot(EmiIngredient.of(ModTags.Blocks.GRINDSTONES), 45, y)
                .drawBack(true);

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, y);

        // Output slot
        widgets.addSlot(outputs, 100, y)
                .drawBack(true)
                .recipeContext(this);
    }

}
