package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
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

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "nether_alloy_smelting");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/nether_furnace_jei.png");

    // JEI recipe type — placed under vanilla smelting tab
    public static final RecipeType<INetherAlloyRecipe> ALLOY_SMELTING_TYPE =
            new RecipeType<>(UID, INetherAlloyRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 100; // full cycle in ticks
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;
    private final int textureWidth = 143;
    private final int textureHeight = 54;

    public NetherAlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 120, textureHeight)
                .setTextureSize(textureWidth, textureHeight)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.NETHER_ALLOY_FURNACE.get()));

        arrowStatic = helper.drawableBuilder(TEXTURE, 120, 14, 23, 16).setTextureSize(textureWidth, textureHeight).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 120, 0, 14, 13).setTextureSize(textureWidth, textureHeight).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                50, // burn time
                IDrawableAnimated.StartDirection.TOP,
                true
        );
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
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(INetherAlloyRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 60, 19);
        flameAnimated.draw(guiGraphics, 64, 39);

        // Draw experience with formatting to avoid trailing .0
        String expText;
        if (exp == exp.intValue()) {
            expText = exp.intValue() + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        // Calculate X position to right-align the text
        int textWidth = Minecraft.getInstance().font.width(expText);
        int xPos = this.background.getWidth() - textWidth; // 5 pixels from right edge

        guiGraphics.drawString(Minecraft.getInstance().font, expText, xPos, textureHeight - 9, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, INetherAlloyRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredientsList();
        boolean isShaped = recipe.isShaped();

        if (isShaped) {
            // Get the dimensions of the shaped recipe
            int width = recipe.getWidth();
            int height = recipe.getHeight();

            // Calculate starting position to center the pattern in the 3x3 grid
            int startX = (3 - width) * 18 / 2 + 1;
            int startY = (3 - height) * 18 / 2 + 1;

            // Fill the 3x3 grid
            for (int gridRow = 0; gridRow < 3; gridRow++) {
                for (int gridCol = 0; gridCol < 3; gridCol++) {

                    // Calculate position relative to the centered pattern
                    int patternRow = gridRow - ((3 - height) / 2);
                    int patternCol = gridCol - ((3 - width) / 2);

                    // Check if this grid position is within the recipe pattern
                    boolean isInPattern = patternRow >= 0 && patternRow < height &&
                            patternCol >= 0 && patternCol < width;

                    var slot = builder.addSlot(RecipeIngredientRole.INPUT,
                            1 + gridCol * 18,
                            1 + gridRow * 18);

                    if (isInPattern) {
                        // Get the ingredient from the pattern
                        int index = patternRow * width + patternCol;
                        if (index < ingredients.size()) {
                            Ingredient ingredient = ingredients.get(index);
                            if (!ingredient.isEmpty()) {
                                slot.addIngredients(ingredient);
                            }
                            // If ingredient is empty, slot stays empty (shows pattern hole)
                        }
                    }
                    // If not in pattern, slot stays empty
                }
            }
        } else
            // Always draw a full 3x3 grid
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int index = row * 3 + col;
                    Ingredient ingredient = index < ingredients.size()
                            ? ingredients.get(index)
                            : Ingredient.EMPTY;

                    var slot = builder.addSlot(RecipeIngredientRole.INPUT,
                            1 + col * 18,
                            1 + row * 18);
                    if (!ingredient.isEmpty()) {
                        slot.addIngredients(ingredient);
                    }
                }
            }

        // Output slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 20)
                .addItemStack(recipe.getResultItem(null));
    }
}