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
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.CoolingRecipe;

public class CoolingRecipeCategory implements IRecipeCategory<CoolingRecipe> {
    public static final RecipeType<CoolingRecipe> TYPE = RecipeType.create(OvergearedMod.MOD_ID, "cooling", CoolingRecipe.class);
    public static final Identifier UID = Identifier.tryBuild(OvergearedMod.MOD_ID, "cooling");
    private static final Identifier TEXTURE = Identifier.tryBuild(OvergearedMod.MOD_ID, "textures/gui/cooling_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public CoolingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, 76, 18).setTextureSize(76, 18).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.WATER_BUCKET));
        this.title = Component.translatable("gui.overgeared.jei.category.cooling");
    }

    @Override
    public RecipeType<CoolingRecipe> getRecipeType() {
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
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CoolingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 1).addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(CoolingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }
}
