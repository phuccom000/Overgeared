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
import net.stirdrem.overgeared.recipe.IAlloyRecipe;

import java.util.List;


public class AlloySmeltingRecipeCategory implements IRecipeCategory<IAlloyRecipe> {

    public static final Identifier UID = Overgeared.id("alloy_smelting");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/furnace_jei.png");

    public static final RecipeType<IAlloyRecipe> ALLOY_SMELTING_TYPE =
            new RecipeType<>(UID, IAlloyRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 200;
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;

    public AlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 107, 43)
                .setTextureSize(130, 43)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.ALLOY_FURNACE));

        arrowStatic = helper.drawableBuilder(TEXTURE, 107, 14, 22, 16).setTextureSize(130, 43).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 107, 0, 14, 13).setTextureSize(130, 43).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                100,
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<IAlloyRecipe> getRecipeType() {
        return ALLOY_SMELTING_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("gui.overgeared.jei.category.alloy_smelting");
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
    public void draw(IAlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 47, 9);
        flameAnimated.draw(guiGraphics, 51, 29);

        String expText;
        if (exp == exp.intValue()) {
            expText = exp.intValue() + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(expText);
        int xPos = this.background.getWidth() - textWidth;

        guiGraphics.drawText(MinecraftClient.getInstance().textRenderer, expText, xPos, 35, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IAlloyRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredientsList();
        boolean isShaped = recipe.isShaped();

        if (isShaped) {
            int width = recipe.getWidth();
            int height = recipe.getHeight();

            for (int gridRow = 0; gridRow < 2; gridRow++) {
                for (int gridCol = 0; gridCol < 2; gridCol++) {

                    boolean isInPattern = gridRow < height && gridCol < width;

                    var slot = builder.addSlot(RecipeIngredientRole.INPUT,
                            1 + gridCol * 18,
                            1 + gridRow * 18);

                    if (isInPattern) {
                        int index = gridRow * width + gridCol;
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

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 10)
                .addItemStack(recipe.getOutput(null));
    }
}
