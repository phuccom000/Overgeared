package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.datapack.KnappingResourceReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

public class KnappingRecipeCategory implements IRecipeCategory<RockKnappingRecipe> {
    public static final Identifier UID = Overgeared.id("rock_knapping");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/rock_knapping_jei.png");
    private static final Identifier CHIPPED_TEXTURE = Overgeared.id("textures/gui/blank.png");

    public static final RecipeType<RockKnappingRecipe> KNAPPING_RECIPE_TYPE =
            new RecipeType<>(UID, RockKnappingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public KnappingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 7, 16, 138, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.ROCK));
    }

    @Override
    public RecipeType<RockKnappingRecipe> getRecipeType() {
        return KNAPPING_RECIPE_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("gui.overgeared.rock_knapping");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RockKnappingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19)
                .addItemStack(recipe.getOutput(null));
    }

    @Override
    public void draw(RockKnappingRecipe recipe,
                      IRecipeSlotsView recipeSlotsView,
                      DrawContext guiGraphics,
                      double mouseX,
                      double mouseY) {

        boolean[][] pattern = recipe.getPattern();

        int patternHeight = pattern.length;
        int patternWidth = patternHeight > 0 ? pattern[0].length : 0;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {

                int posX = 25 + x * 16;
                int posY = 3 + y * 16;

                boolean isUnchipped = false;

                if (y < patternHeight && x < patternWidth) {
                    isUnchipped = pattern[y][x];
                }

                Identifier texture = isUnchipped
                        ? resolveUnchippedTexture(recipe)
                        : CHIPPED_TEXTURE;

                guiGraphics.drawTexture(texture, posX, posY, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private Identifier resolveUnchippedTexture(RockKnappingRecipe recipe) {
        ItemStack[] stacks = recipe.getIngredient().getMatchingStacks();

        for (ItemStack stack : stacks) {
            Identifier tex = KnappingResourceReloadListener.getTexture(stack);
            if (tex != null) {
                return tex;
            }
        }

        return Identifier.tryParse("textures/block/stone.png");
    }
}
