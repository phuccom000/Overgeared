package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EMI recipe display for Casting recipes.
 * Layout: [Material] [Arrow] [Output]
 * [Cast]     [Fire]  [XP]
 */
public class CastingEmiRecipe implements EmiRecipe {


    private final ResourceLocation id;
    private final CastingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiStack> emiCastStack;

    // Store material requirements for display-time resolution
    private final Map<String, Integer> requiredMaterials;

    public CastingEmiRecipe(RecipeHolder<CastingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();

        // Store required materials for display-time lookup
        this.requiredMaterials = recipe.getRequiredMaterials();

        int maxRequired = requiredMaterials.values().stream().mapToInt(Integer::intValue).sum();

        // Create cast with the tool type and max value set
        this.emiCastStack = createCast(maxRequired);

        // Build inputs list (cast only - materials resolved at display time)
        List<EmiIngredient> inputList = new ArrayList<>();
        inputList.add(EmiIngredient.of(emiCastStack));
        this.inputs = inputList;

        this.outputs = List.of(EmiStack.of(recipe.getResultItem(null)));
    }

    /**
     * Creates a cast ItemStack with the tool type and max value set.
     */
    private List<EmiStack> createCast(int maxValue) {
        CastData castData = new CastData(
                "", // quality
                recipe.getToolType(),
                Map.of(), // empty materials (unfilled)
                0, // current
                maxValue, // max - total required materials
                List.of(), // input
                ItemStack.EMPTY, // output
                false // heated
        );

        List<EmiStack> stacks = new ArrayList<>();

        // Clay cast
        ItemStack clayCast = new ItemStack(ModItems.CLAY_TOOL_CAST.get());
        clayCast.set(ModComponents.CAST_DATA.get(), castData);
        stacks.add(EmiStack.of(clayCast));

        // Nether cast
        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST.get());
        netherCast.set(ModComponents.CAST_DATA.get(), castData);
        stacks.add(EmiStack.of(netherCast));

        return stacks;
    }


    /**
     * Builds material stacks at display time using config-based material values.
     * This allows the values to be resolved after tags and config are loaded.
     */
    private List<EmiStack> buildMaterialStacks() {
        List<EmiStack> validStacks = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String requiredMaterialId = entry.getKey();
            int amountNeeded = entry.getValue();

            // Iterate all registered items and find matching materials
            BuiltInRegistries.ITEM.forEach(item -> {
                // Skip air
                if (item == Items.AIR) return;

                ItemStack stack = new ItemStack(item);

                // Only accept valid materials
                if (!ConfigHelper.isValidMaterial(item)) return;

                // Only accept items that match the required material ID
                String materialId = ConfigHelper.getMaterialForItem(item);
                if (!materialId.equalsIgnoreCase(requiredMaterialId)) return;

                int materialValue = ConfigHelper.getMaterialValue(item);
                if (materialValue <= 0) return;

                int count = (int) Math.ceil((double) amountNeeded / materialValue);

                ItemStack copy = stack.copy();
                copy.setCount(count);

                validStacks.add(EmiStack.of(copy));
            });
        }

        return validStacks;
    }


    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.CASTING_CATEGORY;
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
        return 50;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Layout matching JEI:
        // [Material] [Arrow] [Output]
        // [Cast]     [Fire]  [XP]

        int offsetX = 20;
        int offsetY = 6;

        // Material slot (top-left) - resolve materials at display time
        int materialX = offsetX;
        int materialY = offsetY;

        // Build material stacks at display time using config values
        List<EmiStack> validStacks = buildMaterialStacks();

        if (validStacks.isEmpty()) {
            widgets.addSlot(EmiStack.EMPTY, materialX, materialY);
        } else {
            widgets.addSlot(EmiIngredient.of(validStacks), materialX, materialY);
        }

        // Cast slot (bottom-left) - shows empty tool cast
        int castX = offsetX;
        int castY = offsetY + EmiLayoutConstants.SLOT_SIZE + 2;
        widgets.addSlot(EmiIngredient.of(emiCastStack), castX, castY);

        // Arrow (middle, vertically centered)
        int arrowX = offsetX + EmiLayoutConstants.SLOT_SIZE + 8;
        int arrowY = offsetY;
        widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowX, arrowY);
        widgets.addFillingArrow(arrowX, arrowY, recipe.getCookingTime() * 50);

        // Fire (below arrow)
        int fireX = arrowX + (EmiLayoutConstants.ARROW_WIDTH - EmiLayoutConstants.FIRE_WIDTH) / 2;
        int fireY = arrowY + EmiLayoutConstants.ARROW_HEIGHT + 1;
        widgets.addTexture(EmiTexture.EMPTY_FLAME, fireX, fireY);
        widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, fireX, fireY, 4000, false, true, true);

        // Output (large slot, right side)
        int outputX = arrowX + EmiLayoutConstants.ARROW_WIDTH + EmiLayoutConstants.PAD;
        int outputY = offsetY;
        widgets.addSlot(outputs.getFirst(), outputX, outputY).large(true).recipeContext(this);

        // XP text (next to fire)
        float xp = recipe.getExperience();
        if (xp > 0) {
            widgets.addText(Component.literal(xp + " XP"), fireX + 22, fireY + 10, 0xFF808080, false);
        }
    }
}
