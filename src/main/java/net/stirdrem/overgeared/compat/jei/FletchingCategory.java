package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.Overgeared;

import java.util.Optional;

public class FletchingCategory implements IRecipeCategory<FletchingJeiRecipe> {

    public static final Identifier UID = Overgeared.id("fletching");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/fletching_table_jei.png");

    public static final IRecipeType<FletchingJeiRecipe> FLETCHING_RECIPE_TYPE =
            IRecipeType.create(UID, FletchingJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FletchingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 29, 16, 118, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.FLETCHING_TABLE));
    }

    @Override
    public IRecipeType<FletchingJeiRecipe> getRecipeType() {
        return FLETCHING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.fletching");
    }

    @Override
    public int getWidth() {
        return this.background.getWidth();
    }

    @Override
    public int getHeight() {
        return this.background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public Identifier getIdentifier(FletchingJeiRecipe recipe) {
        return recipe.id();
    }

    @Override
    public void draw(FletchingJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FletchingJeiRecipe recipe, IFocusGroup focuses) {
        addOptional(builder.addSlot(RecipeIngredientRole.INPUT, 37, 1), recipe.tip());
        addOptional(builder.addSlot(RecipeIngredientRole.INPUT, 19, 19), recipe.shaft());
        addOptional(builder.addSlot(RecipeIngredientRole.INPUT, 1, 37), recipe.feather());

        IRecipeSlotBuilder potionSlot = builder.addSlot(RecipeIngredientRole.INPUT, 63, 37);
        if (!recipe.potionStacks().isEmpty()) {
            potionSlot.addItemStacks(recipe.potionStacks());
        } else {
            addOptional(potionSlot, recipe.potion());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 97, 19)
                .add(recipe.output());
    }

    private static void addOptional(IRecipeSlotBuilder slot, Optional<Ingredient> ingredient) {
        ingredient.ifPresent(slot::add);
    }
}
