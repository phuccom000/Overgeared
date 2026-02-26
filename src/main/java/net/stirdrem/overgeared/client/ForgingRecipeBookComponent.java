package net.stirdrem.overgeared.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;

import javax.annotation.Nonnull;
import java.util.List;

public class ForgingRecipeBookComponent extends RecipeBookComponent {
    protected static final WidgetSprites RECIPE_BOOK_BUTTONS = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "recipe_book/anvil_enabled"),
            ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "recipe_book/anvil_disabled"),
            ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "recipe_book/anvil_enabled_highlighted"),
            ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "recipe_book/anvil_disabled_highlighted"));

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(RECIPE_BOOK_BUTTONS);
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
    public void setupGhostRecipe(RecipeHolder<?> recipe, List<Slot> slots) {
        if (!(recipe.value() instanceof ForgingRecipe forgingRecipe)) return;
        if (!(this.menu instanceof AbstractSmithingAnvilMenu forgingMenu))
            return;
        ItemStack result = recipe.value().getResultItem(this.minecraft.level.registryAccess());
        AnvilTier anvilTier = forgingMenu.getBlockEntity().getAnvilTier();
        AnvilTier requiredTier = AnvilTier.valueOf(forgingRecipe.getAnvilTier().toUpperCase());

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

            this.ghostRecipe.addIngredient(
                    Ingredient.of(result),
                    resultSlot.x,
                    resultSlot.y);
        }

        List<Integer> inputSlots = forgingMenu.getInputSlots();

        int recipeWidth = forgingRecipe.getWidth();
        int recipeHeight = forgingRecipe.getHeight();

        NonNullList<Ingredient> ingredients = forgingRecipe.getIngredients();

        for (int row = 0; row < recipeHeight; row++) {
            for (int col = 0; col < recipeWidth; col++) {

                int recipeIndex = col + row * recipeWidth;
                if (recipeIndex >= ingredients.size())
                    continue;

                Ingredient ingredient = ingredients.get(recipeIndex);
                if (ingredient.isEmpty())
                    continue;

                int gridIndex = col + row * 3; // 3x3 layout
                if (gridIndex >= inputSlots.size())
                    continue;

                int slotIndex = inputSlots.get(gridIndex);
                if (slotIndex >= slots.size())
                    continue;

                Slot slot = slots.get(slotIndex);

                this.ghostRecipe.addIngredient(
                        ingredient,
                        slot.x,
                        slot.y);
            }
        }
    }

}