package net.stirdrem.overgeared.compat.jei;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.FletchingRecipe;

public class FletchingCategory implements IRecipeCategory<FletchingRecipe> {
    public static final Identifier UID = OvergearedMod.loc("fletching");
    public static final Identifier TEXTURE = OvergearedMod.loc("textures/gui/fletching_table_jei.png");

    public static final RecipeType<FletchingRecipe> FLETCHING_RECIPE_TYPE =
            new RecipeType<>(UID, FletchingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FletchingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 29, 16, 118, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.FLETCHING_TABLE));
    }

    @Override
    public RecipeType<FletchingRecipe> getRecipeType() {
        return FLETCHING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.fletching");
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
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FletchingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 1).addIngredients(recipe.getTip());
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 19).addIngredients(recipe.getShaft());
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 37).addIngredients(recipe.getFeather());
        var potionSlot = builder.addSlot(RecipeIngredientRole.INPUT, 63, 37);
        if (!recipe.getPotionDisplay().isEmpty()) {
            potionSlot.addItemStacks(recipe.getPotionDisplay());
        } else {
            potionSlot.addIngredients(recipe.getPotion());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 97, 19).addItemStack(recipe.getDefaultResult());
    }

    @Override
    public void draw(FletchingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}
