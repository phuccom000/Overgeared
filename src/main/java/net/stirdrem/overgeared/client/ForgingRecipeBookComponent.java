package net.stirdrem.overgeared.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;

import javax.annotation.Nonnull;
import java.util.List;

public class ForgingRecipeBookComponent extends RecipeBookComponent {
    protected static final ResourceLocation RECIPE_BOOK_BUTTONS = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/recipe_book_buttons.png");

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 182, 28, 18, RECIPE_BOOK_BUTTONS);
    }

    public void hide() {
        this.setVisible(false);
    }

    @Override
    @Nonnull
    protected Component getRecipeFilterName() {
        return Component.translatable("overgeared.container.recipe_book.forgeable");
    }

    @Override
    public void init(int pWidth, int pHeight, Minecraft pMinecraft, boolean pWidthTooNarrow, RecipeBookMenu<?> pMenu) {
        super.init(pWidth, pHeight, pMinecraft, pWidthTooNarrow, pMenu);
    }

    @Override
    public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
        if (!(recipe instanceof ForgingRecipe forgingRecipe))
            return;
        if (!(this.menu instanceof AbstractSmithingAnvilMenu forgingMenu))
            return;
        AnvilTier anvilTier = forgingMenu.getBlockEntity().getAnvilTier();
        AnvilTier requiredTier = AnvilTier.valueOf(forgingRecipe.getAnvilTier().toUpperCase());
        int gridWidth = 3;
        int gridHeight = 3;

        if (!anvilTier.isAtLeast(requiredTier)) {
            // Show red message in action bar
            this.minecraft.player.displayClientMessage(
                    Component.literal("Anvil tier not high enough")
                            .withStyle(ChatFormatting.RED),
                    false // true = action bar, false = chat
            );

            return; // stop ghost placement
        }
        this.ghostRecipe.clear();
        this.ghostRecipe.setRecipe(recipe);

        int resultIndex = this.menu.getResultSlotIndex();
        if (resultIndex >= 0 && resultIndex < slots.size()) {

            Slot resultSlot = slots.get(resultIndex);
            ItemStack result = forgingRecipe.getResultItem(this.minecraft.level.registryAccess());

            this.ghostRecipe.addIngredient(
                    Ingredient.of(result),
                    resultSlot.x,
                    resultSlot.y);
        }

        List<Integer> inputSlots = forgingMenu.getInputSlots();

        int recipeWidth = forgingRecipe.getWidth();
        int recipeHeight = forgingRecipe.getHeight();

        NonNullList<Ingredient> ingredients = forgingRecipe.getIngredients();

        int offsetX = (gridWidth - recipeWidth) / 2;
        int offsetY = (gridHeight - recipeHeight) / 2;

        for (int row = 0; row < recipeHeight; row++) {
            for (int col = 0; col < recipeWidth; col++) {

                int recipeIndex = col + row * recipeWidth;
                if (recipeIndex >= ingredients.size())
                    continue;

                Ingredient ingredient = ingredients.get(recipeIndex);
                if (ingredient.isEmpty())
                    continue;

                // Apply centering offset into 3x3 grid
                int gridX = col + offsetX;
                int gridY = row + offsetY;

                int gridIndex = gridX + gridY * gridWidth; // 3 if 3x3
                if (gridIndex >= inputSlots.size())
                    continue;

                int slotIndex = inputSlots.get(gridIndex);
                if (slotIndex >= slots.size())
                    continue;

                Slot slot = slots.get(slotIndex);

                this.ghostRecipe.addIngredient(
                        ingredient,
                        slot.x,
                        slot.y
                );
            }
        }
    }

}