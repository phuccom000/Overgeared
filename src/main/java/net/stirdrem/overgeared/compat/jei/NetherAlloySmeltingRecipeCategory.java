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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.INetherAlloyRecipe;

import java.util.List;


public class NetherAlloySmeltingRecipeCategory implements IRecipeCategory<INetherAlloyRecipe> {

    public static final Identifier UID = Overgeared.id("nether_alloy_smelting");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/nether_furnace_jei.png");

    public static final RecipeType<INetherAlloyRecipe> ALLOY_SMELTING_TYPE =
            new RecipeType<>(UID, INetherAlloyRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 100;
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;
    private final int textureWidth = 143;
    private final int textureHeight = 54;

    public NetherAlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 120, textureHeight)
                .setTextureSize(textureWidth, textureHeight)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.NETHER_ALLOY_FURNACE));

        arrowStatic = helper.drawableBuilder(TEXTURE, 120, 14, 23, 16).setTextureSize(textureWidth, textureHeight).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 120, 0, 14, 13).setTextureSize(textureWidth, textureHeight).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                50,
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<INetherAlloyRecipe> getRecipeType() {
        return ALLOY_SMELTING_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("gui.overgeared.jei.category.nether_alloy_smelting");
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
    public void draw(INetherAlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 60, 19);
        flameAnimated.draw(guiGraphics, 64, 39);

        String expText;
        if (exp == exp.intValue()) {
            expText = exp.intValue() + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(expText);
        int xPos = this.background.getWidth() - textWidth;

        guiGraphics.drawText(MinecraftClient.getInstance().textRenderer, expText, xPos, textureHeight - 9, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, INetherAlloyRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredientsList();
        boolean isShaped = recipe.isShaped();

        if (isShaped) {

            int width = recipe.getWidth();
            int height = recipe.getHeight();

            int gridWidth = 3;
            int gridHeight = 3;

            int offsetX = (gridWidth - width) / 2;
            int offsetY = getOffsetY(gridHeight, gridWidth, height, width);

            for (int gridRow = 0; gridRow < 3; gridRow++) {
                for (int gridCol = 0; gridCol < 3; gridCol++) {

                    var slot = builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            1 + gridCol * 18,
                            1 + gridRow * 18
                    );

                    int patternRow = gridRow - offsetY;
                    int patternCol = gridCol - offsetX;

                    boolean isInPattern =
                            patternRow >= 0 && patternRow < height &&
                                    patternCol >= 0 && patternCol < width;

                    if (isInPattern) {
                        int index = patternRow * width + patternCol;

                        if (index < ingredients.size()) {
                            Ingredient ingredient = ingredients.get(index);
                            if (!ingredient.isEmpty()) {
                                slot.addIngredients(ingredient);
                            }
                        }
                    }
                }
            }

        } else {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {

                    int index = row * 3 + col;
                    Ingredient ingredient = index < ingredients.size()
                            ? ingredients.get(index)
                            : Ingredient.EMPTY;

                    var slot = builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            1 + col * 18,
                            1 + row * 18
                    );

                    if (!ingredient.isEmpty()) {
                        slot.addIngredients(ingredient);
                    }
                }
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 20)
                .addItemStack(recipe.getOutput(null));
    }

    private static int getOffsetY(int gridHeight, int gridWidth, int recipeHeight, int recipeWidth) {
        int offsetY = (gridHeight - recipeHeight) / 2;

        if (recipeHeight == 1) {
            if (recipeWidth == 2)
                offsetY = 0;
            else if (recipeWidth == 3)
                offsetY = gridHeight - recipeHeight;
        } else if (recipeHeight == 2 && recipeWidth == 3) {
            offsetY = gridHeight - recipeHeight;
        }

        return offsetY;
    }
}
