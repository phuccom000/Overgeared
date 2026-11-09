package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.List;

/**
 * Shared layout for the explanation categories: one input, one output and a block of wrapped text.
 */
public abstract class ExplanationCategory implements IRecipeCategory<ExplanationRecipe> {
    private static final Identifier BACKGROUND_LOCATION =
            OvergearedMod.loc("textures/gui/explanation_jei.png");

    private final RecipeType<ExplanationRecipe> type;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final Component description;

    protected ExplanationCategory(IGuiHelper guiHelper, RecipeType<ExplanationRecipe> type, ItemStack icon,
                                  String titleKey, String descriptionKey) {
        this.type = type;
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 150, 120)
                .setTextureSize(150, 120)
                .build();
        this.icon = guiHelper.createDrawableItemStack(icon);
        this.title = Component.translatable(titleKey);
        this.description = Component.translatable(descriptionKey);
    }

    /** What goes in the input slot. */
    protected abstract Ingredient getInput();

    @Override
    public RecipeType<ExplanationRecipe> getRecipeType() {
        return type;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ExplanationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 18).addIngredients(getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 18).addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(ExplanationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics,
                     double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(description, 140);
        int color = 0xFF555555; // ChatFormatting.DARK_GRAY
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.text(font, lines.get(i), 5, 43 + i * font.lineHeight, color, false);
        }
    }
}
