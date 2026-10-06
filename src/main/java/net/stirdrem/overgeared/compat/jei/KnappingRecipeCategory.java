package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.datapack.KnappingResourceReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

import java.util.List;

public class KnappingRecipeCategory implements IRecipeCategory<RecipeHolder<RockKnappingRecipe>> {
    public static final Identifier UID = Overgeared.id("rock_knapping");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/rock_knapping_jei.png");
    private static final Identifier CHIPPED_TEXTURE = Overgeared.id("textures/gui/blank.png");

    public static final IRecipeHolderType<RockKnappingRecipe> KNAPPING_RECIPE_TYPE = IRecipeHolderType.create(UID);

    private final IDrawable background;
    private final IDrawable icon;

    public KnappingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 7, 16, 138, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.ROCK));
    }

    @Override
    public IRecipeHolderType<RockKnappingRecipe> getRecipeType() {
        return KNAPPING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.rock_knapping");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<RockKnappingRecipe> holder, IFocusGroup focuses) {
        RockKnappingRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .add(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19)
                .add(recipe.getResultItem());
    }

    @Override
    public void draw(RecipeHolder<RockKnappingRecipe> holder,
                     IRecipeSlotsView recipeSlotsView,
                     GuiGraphicsExtractor guiGraphics,
                     double mouseX,
                     double mouseY) {
        RockKnappingRecipe recipe = holder.value();
        background.draw(guiGraphics);

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

                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, posX, posY, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private Identifier resolveUnchippedTexture(RockKnappingRecipe recipe) {
        List<Holder<Item>> items = recipe.getIngredient().items().toList();

        for (Holder<Item> item : items) {
            Identifier tex = KnappingResourceReloadListener.getTexture(new ItemStack(item));
            if (tex != null) {
                return tex;
            }
        }

        return Identifier.tryParse("textures/block/stone.png");
    }
}
