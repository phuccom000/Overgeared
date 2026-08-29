package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.recipe.CoolingRecipe;

public class CoolingRecipeCategory implements IRecipeCategory<CoolingRecipe> {
    public static final RecipeType<CoolingRecipe> TYPE =
            RecipeType.create(Overgeared.MOD_ID, "cooling", CoolingRecipe.class);

    public static final Identifier UID = Overgeared.id("cooling");

    private static final Identifier TEXTURE = Overgeared.id("textures/gui/cooling_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Text title;

    public CoolingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, 76, 18).setTextureSize(76, 18).build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.WATER_BUCKET));
        this.title = Text.translatable("gui.overgeared.jei.category.cooling");
    }

    @Override
    public RecipeType<CoolingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Text getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CoolingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredients(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 1)
                .addItemStack(recipe.getOutput());
    }
}
