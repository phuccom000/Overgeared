package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.recipe.FletchingRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * EMI recipe display for Fletching recipes.
 * Shows the diagonal slot layout matching the in-game GUI.
 * Displays the base arrow recipe - potion variants are applied at runtime with any potion.
 */
public class FletchingEmiRecipe implements EmiRecipe {
    
    private static final int SLOT_SIZE = 18;
    
    private final ResourceLocation id;
    private final FletchingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public FletchingEmiRecipe(RecipeHolder<FletchingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();
        
        // Build inputs list: tip, shaft, feather
        List<EmiIngredient> inputList = new ArrayList<>();
        inputList.add(EmiIngredient.of(recipe.getTip()));
        inputList.add(EmiIngredient.of(recipe.getShaft()));
        inputList.add(EmiIngredient.of(recipe.getFeather()));
        this.inputs = inputList;
        
        // Just show the default result - potion variants are runtime behavior
        this.outputs = List.of(EmiStack.of(recipe.getDefaultResult()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.FLETCHING_CATEGORY;
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
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Match in-game GUI diagonal layout
        int offsetX = 4;
        int offsetY = 4;
<<<<<<< HEAD
        
        // Tip slot (top-right of diagonal)
        int tipX = offsetX + 36;
        int tipY = offsetY;
        widgets.addSlot(EmiIngredient.of(recipe.getTip()), tipX, tipY);
        
        // Shaft slot (middle-left)
        int shaftX = offsetX + 18;
        int shaftY = offsetY + SLOT_SIZE;
        widgets.addSlot(EmiIngredient.of(recipe.getShaft()), shaftX, shaftY);
        
        // Feather slot (bottom-left)
        int featherX = offsetX;
        int featherY = offsetY + SLOT_SIZE * 2;
        widgets.addSlot(EmiIngredient.of(recipe.getFeather()), featherX, featherY);
        
        // Arrow 
=======

        if (variant == Variant.BASE) {
            // Tip slot (top-right of diagonal)
            int tipX = offsetX + 36;
            int tipY = offsetY;
            widgets.addSlot(EmiIngredient.of(recipe.getTip()), tipX, tipY);

            // Shaft slot (middle-left)
            int shaftX = offsetX + 18;
            int shaftY = offsetY + SLOT_SIZE;
            widgets.addSlot(EmiIngredient.of(recipe.getShaft()), shaftX, shaftY);

            // Feather slot (bottom-left)
            int featherX = offsetX;
            int featherY = offsetY + SLOT_SIZE * 2;
            widgets.addSlot(EmiIngredient.of(recipe.getFeather()), featherX, featherY);
        } else {
            EmiIngredient arrow = EmiStack.of(recipe.getDefaultResult().copyWithCount(1));
            EmiIngredient potion = variant == Variant.TIPPED
                    ? (recipe.hasPotion() ? EmiIngredient.of(recipe.getPotion())
                            : EmiStack.of(PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION)))
                    : (recipe.hasPotion() ? EmiIngredient.of(recipe.getPotion())
                            : EmiStack.of(PotionContents.createItemStack(Items.LINGERING_POTION, Potions.NIGHT_VISION)));

            int shaftX = offsetX + 18;
            int shaftY = offsetY + SLOT_SIZE;
            widgets.addSlot(arrow, shaftX, shaftY);

            int potionX = offsetX + 36;
            int potionY = offsetY + SLOT_SIZE * 2;
            widgets.addSlot(potion, potionX, potionY);
        }

        // Arrow
>>>>>>> f185c2b (fix: add missing EMI entries for blueprint cloning, tipped/lingering arrows and forging blueprint usage)
        int arrowX = offsetX + 54;
        int arrowY = offsetY + SLOT_SIZE;
        widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowX, arrowY);
        
        // Output - just the base result
        int outputX = arrowX + 28;
        int outputY = offsetY + SLOT_SIZE - 4;
        widgets.addSlot(outputs.getFirst(), outputX, outputY).large(true).recipeContext(this);
    }
}
