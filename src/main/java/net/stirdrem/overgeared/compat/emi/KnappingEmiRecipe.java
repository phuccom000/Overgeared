package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.datapack.KnappingResourceReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;


import java.util.List;

public class KnappingEmiRecipe implements EmiRecipe {
    private static final ResourceLocation STONE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");
    private final ResourceLocation id;
    private final RockKnappingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public KnappingEmiRecipe(RecipeHolder<RockKnappingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();
        this.outputs = List.of(EmiStack.of(recipe.output()));
        // Recipe always consumes 1 Rock
        this.inputs = List.of(EmiStack.of(ModItems.ROCK.get()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.KNAPPING_CATEGORY;
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
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int startX = 4;
        int startY = 4;

        // Input Slot
        widgets.addSlot(inputs.get(0), startX, startY + EmiLayoutConstants.SLOT_SIZE);

        // Grid starts after input slot + padding
        int gridStartX = startX + 24;

        boolean[][] pattern = recipe.pattern();


        // Draw stones based on exact grid position
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (pattern[r][c]) {
                    widgets.addTexture(resolveUnchippedTexture(recipe),
                            gridStartX + c * EmiLayoutConstants.SLOT_SIZE,
                            startY + r * EmiLayoutConstants.SLOT_SIZE,
                            EmiLayoutConstants.SLOT_SIZE, EmiLayoutConstants.SLOT_SIZE,
                            0, 0,
                            16, 16,
                            16, 16
                    );
                }
            }
        }

        // Arrow matches grid width + padding
        int gridSize = 3 * EmiLayoutConstants.SLOT_SIZE;
        widgets.addTexture(EmiTexture.EMPTY_ARROW, gridStartX + gridSize + 4, startY + EmiLayoutConstants.SLOT_SIZE);

        // Output
        widgets.addSlot(outputs.getFirst(), gridStartX + gridSize + 32, startY + EmiLayoutConstants.SLOT_SIZE - 4).large(true).recipeContext(this);
    }

    private ResourceLocation resolveUnchippedTexture(RockKnappingRecipe recipe) {
        ItemStack[] stacks = recipe.getIngredient().getItems();

        for (ItemStack stack : stacks) {
            ResourceLocation tex = KnappingResourceReloadListener.getTexture(stack);
            if (tex != null) {
                return tex;
            }
        }

        // Fallback if no datapack entry exists
        return STONE_TEXTURE;
    }

}
