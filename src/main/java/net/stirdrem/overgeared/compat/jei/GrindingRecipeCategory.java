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
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.recipe.GrindingRecipe;

public class GrindingRecipeCategory implements IRecipeCategory<RecipeHolder<GrindingRecipe>> {
    public static final Identifier UID = Overgeared.id("grinding");

    public static final IRecipeHolderType<GrindingRecipe> TYPE = IRecipeHolderType.create(UID);

    private static final Identifier TEXTURE = Overgeared.id("textures/gui/grinding_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public GrindingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, 76, 18).setTextureSize(76, 18).build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.GRINDSTONE));
        this.title = Component.translatable("gui.overgeared.jei.category.grinding");
    }

    @Override
    public IRecipeHolderType<GrindingRecipe> getRecipeType() {
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
    public void draw(RecipeHolder<GrindingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<GrindingRecipe> holder, IFocusGroup focuses) {
        GrindingRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .add(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 1)
                .add(recipe.getOutput());
    }
}
