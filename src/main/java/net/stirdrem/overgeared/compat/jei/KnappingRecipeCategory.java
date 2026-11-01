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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.KnappingResourceReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

public class KnappingRecipeCategory implements IRecipeCategory<RockKnappingRecipe> {
    public static final ResourceLocation UID = OvergearedMod.loc("rock_knapping");
    public static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/rock_knapping_jei.png");
    private static final ResourceLocation CHIPPED_TEXTURE = OvergearedMod.loc("textures/gui/blank.png");

    public static final RecipeType<RockKnappingRecipe> KNAPPING_RECIPE_TYPE =
            new RecipeType<>(UID, RockKnappingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public KnappingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 7, 16, 138, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.ROCK.get()));
    }

    @Override
    public RecipeType<RockKnappingRecipe> getRecipeType() {
        return KNAPPING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.rock_knapping");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RockKnappingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19).addItemStack(recipe.getResultItem(JeiUtils.registries()));
    }

    @Override
    public void draw(RockKnappingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        boolean[][] pattern = recipe.getPattern().getPattern();
        int patternHeight = pattern.length;
        int patternWidth = patternHeight > 0 ? pattern[0].length : 0;
        ResourceLocation unchipped = resolveUnchippedTexture(recipe);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                boolean isUnchipped = y < patternHeight && x < patternWidth && pattern[y][x];
                ResourceLocation texture = isUnchipped ? unchipped : CHIPPED_TEXTURE;
                guiGraphics.blit(texture, 25 + x * 16, 3 + y * 16, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private static ResourceLocation resolveUnchippedTexture(RockKnappingRecipe recipe) {
        for (ItemStack stack : recipe.getIngredient().getItems()) {
            ResourceLocation tex = KnappingResourceReloadListener.getTexture(stack);
            if (tex != null) return tex;
        }
        return KnappingResourceReloadListener.FALLBACK_TEXTURE;
    }
}
