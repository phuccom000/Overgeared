package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.INetherAlloyRecipe;

import java.util.List;

public class NetherAlloySmeltingRecipeCategory implements IRecipeCategory<INetherAlloyRecipe> {
    public static final ResourceLocation UID = OvergearedMod.loc("nether_alloy_smelting");
    public static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/nether_furnace_jei.png");

    public static final RecipeType<INetherAlloyRecipe> ALLOY_SMELTING_TYPE =
            new RecipeType<>(UID, INetherAlloyRecipe.class);

    private static final int TEX_W = 143;
    private static final int TEX_H = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableAnimated flameAnimated;

    public NetherAlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 120, TEX_H).setTextureSize(TEX_W, TEX_H).build();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.NETHER_ALLOY_FURNACE.get()));
        this.arrowAnimated = helper.createAnimatedDrawable(
                helper.drawableBuilder(TEXTURE, 120, 14, 23, 16).setTextureSize(TEX_W, TEX_H).build(),
                100, IDrawableAnimated.StartDirection.LEFT, false);
        this.flameAnimated = helper.createAnimatedDrawable(
                helper.drawableBuilder(TEXTURE, 120, 0, 14, 13).setTextureSize(TEX_W, TEX_H).build(),
                50, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public RecipeType<INetherAlloyRecipe> getRecipeType() {
        return ALLOY_SMELTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.nether_alloy_smelting");
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
    public void draw(INetherAlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        arrowAnimated.draw(guiGraphics, 60, 19);
        flameAnimated.draw(guiGraphics, 64, 39);
        JeiUtils.drawExperience(guiGraphics, recipe.getExperience(), background.getWidth(), TEX_H - 9);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, INetherAlloyRecipe recipe, IFocusGroup focuses) {
        // Shaped recipes store a full 3x3 grid (with EMPTY holes); shapeless store up to 9 ingredients.
        // Both map index i -> grid cell i.
        List<Ingredient> ingredients = recipe.getIngredientsList();
        for (int i = 0; i < 9; i++) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, 1 + (i % 3) * 18, 1 + (i / 3) * 18);
            if (i < ingredients.size() && !ingredients.get(i).isEmpty()) {
                slot.addIngredients(ingredients.get(i));
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 20).addItemStack(recipe.getResultItem(JeiUtils.registries()));
    }
}
