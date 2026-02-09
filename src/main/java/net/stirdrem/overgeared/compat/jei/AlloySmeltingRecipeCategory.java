package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.IAlloyRecipe;

import java.util.List;


public class AlloySmeltingRecipeCategory implements IRecipeCategory<IAlloyRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "alloy_smelting");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/furnace_jei.png");

    // JEI recipe type — placed under vanilla smelting tab
    public static final RecipeType<IAlloyRecipe> ALLOY_SMELTING_TYPE =
            new RecipeType<>(UID, IAlloyRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 200; // full cycle in ticks
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;

    public AlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 107, 43)
                .setTextureSize(130, 43)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.ALLOY_FURNACE.get()));

        arrowStatic = helper.drawableBuilder(TEXTURE, 107, 14, 22, 16).setTextureSize(130, 43).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 107, 0, 14, 13).setTextureSize(130, 43).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                100, // burn time
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<IAlloyRecipe> getRecipeType() {
        return ALLOY_SMELTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.alloy_smelting");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }


    @Override
    public void draw(IAlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 47, 9);
        flameAnimated.draw(guiGraphics, 51, 29);

        // Draw experience with formatting to avoid trailing .0
        String expText;
        if (exp == exp.intValue()) {
            expText = exp.intValue() + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        // Calculate X position to right-align the text
        int textWidth = Minecraft.getInstance().font.width(expText);
        int xPos = this.background.getWidth() - textWidth; // 5 pixels from right edge

        guiGraphics.drawString(Minecraft.getInstance().font, expText, xPos, 35, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IAlloyRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredientsList();
        boolean isShaped = recipe.isShaped();

        if (isShaped) {
            // Get the dimensions of the shaped recipe
            int width = recipe.getWidth();
            int height = recipe.getHeight();

            // Fill the 2x2 grid, showing empty slots for pattern holes
            for (int gridRow = 0; gridRow < 2; gridRow++) {
                for (int gridCol = 0; gridCol < 2; gridCol++) {

                    // Calculate position relative to the pattern
                    // Since it's a 2x2 grid, pattern must be 1x1, 1x2, 2x1, or 2x2

                    // Check if this grid position is within the recipe pattern
                    boolean isInPattern = gridRow < height && gridCol < width;

                    var slot = builder.addSlot(RecipeIngredientRole.INPUT,
                            1 + gridCol * 18,
                            1 + gridRow * 18);

                    if (isInPattern) {
                        // Get the ingredient from the pattern
                        int index = gridRow * width + gridCol;
                        if (index < ingredients.size()) {
                            Ingredient ingredient = ingredients.get(index);
                            if (!ingredient.isEmpty()) {
                                slot.addIngredients(ingredient);
                            }
                            // If ingredient is empty, slot stays empty (shows pattern hole)
                        }
                    }
                    // If not in pattern, slot stays empty
                }
            }
        } else {
            // Always draw a 2x2 grid
            for (int i = 0; i < 4; i++) {
                Ingredient ingredient = i < ingredients.size()
                        ? ingredients.get(i)
                        : Ingredient.EMPTY;

                int x = (i % 2) * 18 + 1;
                int y = (i / 2) * 18 + 1;

                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                        .addIngredients(ingredient);
            }
        }
        // Output slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 10)
                .addItemStack(recipe.getResultItem(null));
        
    }
}