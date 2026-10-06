package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.recipe.CoolingRecipe;

public class CoolingRecipeCategory implements IRecipeCategory<RecipeHolder<CoolingRecipe>> {
    public static final Identifier UID = Overgeared.id("cooling");

    public static final IRecipeHolderType<CoolingRecipe> TYPE = IRecipeHolderType.create(UID);

    private static final Identifier TEXTURE = Overgeared.id("textures/gui/cooling_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public CoolingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, 76, 18).setTextureSize(76, 18).build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.WATER_BUCKET));
        this.title = Component.translatable("gui.overgeared.jei.category.cooling");
    }

    @Override
    public IRecipeHolderType<CoolingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public void draw(RecipeHolder<CoolingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CoolingRecipe> holder, IFocusGroup focuses) {
        CoolingRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .add(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 1)
                .add(recipe.getOutput());
    }
}
