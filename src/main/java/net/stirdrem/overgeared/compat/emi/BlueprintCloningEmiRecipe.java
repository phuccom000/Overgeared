package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

import java.util.List;

/**
 * EMI recipe display for Blueprint Cloning (crafting table).
 * One entry per quality tier: shows [quality blueprint] + [empty blueprint] → [downgraded blueprint x2].
 */
public class BlueprintCloningEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public BlueprintCloningEmiRecipe(BlueprintQuality quality) {
        this.id = OvergearedMod.loc("blueprint_cloning/" + quality.getId());

        // Input: blueprint at this quality tier
        ItemStack inputStack = new ItemStack(ModItems.BLUEPRINT.get());
        BlueprintData inputData = BlueprintData.createDefault().withQuality(quality.getId());
        inputStack.set(ModComponents.BLUEPRINT_DATA, inputData);

        // Output: blueprint at the downgraded quality (x2).
        BlueprintQuality outputQuality = BlueprintQuality.getPrevious(quality);
        if (outputQuality == null || outputQuality == BlueprintQuality.NONE) outputQuality = quality;

        ItemStack outputStack = new ItemStack(ModItems.BLUEPRINT.get(), 2);
        BlueprintData outputData = BlueprintData.createDefault().withQuality(outputQuality.getId());
        outputStack.set(ModComponents.BLUEPRINT_DATA, outputData);

        this.inputs = List.of(
                EmiStack.of(inputStack),
                EmiIngredient.of(Ingredient.of(ModItems.EMPTY_BLUEPRINT.get()))
        );
        this.outputs = List.of(EmiStack.of(outputStack));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.CRAFTING;
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

    @Override
    public int getDisplayWidth() {
        return 116;
    }

    @Override
    public int getDisplayHeight() {
        return 54;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addSlot(inputs.get(1), 18, 0);
        widgets.addSlot(EmiStack.EMPTY, 36, 0);
        widgets.addSlot(EmiStack.EMPTY, 0, 18);
        widgets.addSlot(EmiStack.EMPTY, 18, 18);
        widgets.addSlot(EmiStack.EMPTY, 36, 18);
        widgets.addSlot(EmiStack.EMPTY, 0, 36);
        widgets.addSlot(EmiStack.EMPTY, 18, 36);
        widgets.addSlot(EmiStack.EMPTY, 36, 36);

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);

        // Output
        widgets.addSlot(outputs.getFirst(), 90, 14).large(true).recipeContext(this);
    }
}
