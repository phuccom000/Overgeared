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
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ForgingRecipeCategory implements IRecipeCategory<ForgingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "forging");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/smithing_anvil_jei.png");

    public static final ResourceLocation RESULT_BIG = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/result_big.png");

    public static final ResourceLocation RESULT_TWOSLOT = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/twoslot.png");

    public static final RecipeType<ForgingRecipe> FORGING_RECIPE_TYPE =
            new RecipeType<>(UID, ForgingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private static final int imageWidth = 138;
    private static final int imageHeight = 54;

    private final int animationTime = 200; // full cycle in ticks
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated arrowAnimated;

    public ForgingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 7, 16, imageWidth, imageHeight);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SMITHING_ANVIL.get()));
        arrowStatic = helper.createDrawable(TEXTURE, 176, 0, 24, 17);
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);
    }


    @Override
    public RecipeType<ForgingRecipe> getRecipeType() {
        return FORGING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void draw(ForgingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        String hitsText = Component.translatable("tooltip.overgeared.recipe.hits", recipe.getRemainingHits()).getString();

        String tierRaw = recipe.getAnvilTier();
        AnvilTier tierName = AnvilTier.fromDisplayName(tierRaw);
        Component tierText = Component.translatable("tooltip.overgeared.recipe.tier")
                .append(Component.literal(" "))
                .append(Component.translatable(tierName.getLang()));

        if (recipe.hasQuality() || !recipe.needsMinigame()) {
            guiGraphics.blit(RESULT_BIG, 112, 14, 0, 0, 26, 26, 26, 26);
        } else {
            guiGraphics.blit(RESULT_TWOSLOT, 116, 9, 0, 0, 18, 36, 18, 36);
        }

        // Draw text
        guiGraphics.drawString(Minecraft.getInstance().font, hitsText, 79, 1, 0xFF808080, false);
        guiGraphics.drawString(Minecraft.getInstance().font, tierText, 79, 47, 0xFF808080, false);

        arrowAnimated.draw(guiGraphics, 82, 19);

    }


    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgingRecipe recipe, IFocusGroup focuses) {
        int gridWidth = 3; // Always 3x3 grid
        int gridHeight = 3;
        int recipeWidth = recipe.width;
        int recipeHeight = recipe.height;

        NonNullList<ForgingRecipe.ForgingIngredient> forgingIngredients =
                recipe.getForgingIngredients();

        int offsetX = (gridWidth - recipeWidth) / 2;
        int offsetY = getOffsetY(gridHeight, gridWidth, recipeHeight, recipeWidth);

        for (int gridY = 0; gridY < gridHeight; gridY++) {
            for (int gridX = 0; gridX < gridWidth; gridX++) {

                var slotBuilder = builder.addSlot(
                        RecipeIngredientRole.INPUT,
                        23 + gridX * 18,
                        1 + gridY * 18
                );

                int recipeX = gridX - offsetX;
                int recipeY = gridY - offsetY;

                boolean insideRecipe =
                        recipeX >= 0 && recipeY >= 0 &&
                                recipeX < recipeWidth &&
                                recipeY < recipeHeight;

                if (insideRecipe) {

                    int recipeIndex = recipeY * recipeWidth + recipeX;

                    if (recipeIndex < forgingIngredients.size()) {

                        ForgingRecipe.ForgingIngredient forgingIngredient =
                                forgingIngredients.get(recipeIndex);

                        Ingredient ingredient = forgingIngredient.ingredient();
                        ItemStack[] stacks = ingredient.getItems();

                        if (stacks.length > 0) {
                            List<ItemStack> displayStacks = new ArrayList<>();

                            for (ItemStack stack : stacks) {
                                ItemStack copy = stack.copy();

                                if (forgingIngredient.requiresHeated()) {
                                    copy.getOrCreateTag().putBoolean("Heated", true);
                                }

                                displayStacks.add(copy);
                            }

                            slotBuilder.addItemStacks(displayStacks);
                        } else {
                            slotBuilder.addItemStack(ItemStack.EMPTY);
                        }
                    }

                } else {
                    slotBuilder.addItemStack(ItemStack.EMPTY);
                }
            }
        }

        // Rest of your code (blueprint and output slots) remains the same...
        //BLUEPRINT SLOT
        builder.addSlot(RecipeIngredientRole.CATALYST, 1, 19)
                .addItemStacks(createBlueprintStacksForRecipe(recipe));

        if (recipe.hasQuality() || !recipe.needsMinigame()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19)
                    .addItemStack(recipe.getResultItem(null));
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 10)
                    .addItemStack(recipe.getResultItem(null));
            ItemStack failedStack = recipe.getFailedResultItem(null).copy();
            CompoundTag failedTag = failedStack.getOrCreateTag();
            failedTag.putBoolean("failedResult", true);
            failedStack.setTag(failedTag);

            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 28)
                    .addItemStack(failedStack);
        }
    }

    private static int getOffsetY(int gridHeight, int gridWidth, int recipeHeight, int recipeWidth) {
        int offsetY = (gridHeight - recipeHeight) / 2;

        if (recipeHeight == 1) {
            if (recipeWidth == 2)
                offsetY = 0;
            else if (recipeWidth == 3)
                offsetY = gridHeight - recipeHeight;
        } else if (recipeHeight == 2 && recipeWidth == 3) {
            // Anchor to bottom
            offsetY = gridHeight - recipeHeight;
        }

        return offsetY;
    }

    private List<ItemStack> createBlueprintStacksForRecipe(ForgingRecipe recipe) {
        Set<String> types = recipe.getBlueprintTypes();
        boolean required = recipe.requiresBlueprint();
        List<ItemStack> stacks = new ArrayList<>();

        for (String type : types) {
            ItemStack stack = new ItemStack(ModItems.BLUEPRINT.get());
            CompoundTag tag = new CompoundTag();

            // Set a single string value for ToolType
            tag.putString("ToolType", type);
            tag.putBoolean("Required", required);
            stack.setTag(tag);
            stacks.add(stack);
        }

        return stacks;
    }

}