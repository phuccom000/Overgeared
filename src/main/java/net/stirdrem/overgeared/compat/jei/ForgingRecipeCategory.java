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
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ForgingRecipeCategory implements IRecipeCategory<RecipeHolder<ForgingRecipe>> {
    public static final Identifier UID = Overgeared.id("forging");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/smithing_anvil_jei.png");

    public static final Identifier RESULT_BIG = Overgeared.id("textures/gui/result_big.png");

    public static final Identifier RESULT_TWOSLOT = Overgeared.id("textures/gui/twoslot.png");

    public static final IRecipeHolderType<ForgingRecipe> FORGING_RECIPE_TYPE = IRecipeHolderType.create(UID);

    private final IDrawable background;
    private final IDrawable icon;
    private static final int imageWidth = 138;
    private static final int imageHeight = 54;

    private final int animationTime = 200;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated arrowAnimated;

    public ForgingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 7, 16, imageWidth, imageHeight);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SMITHING_ANVIL));
        arrowStatic = helper.createDrawable(TEXTURE, 176, 0, 24, 17);
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);
    }


    @Override
    public IRecipeHolderType<ForgingRecipe> getRecipeType() {
        return FORGING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Override
    public int getWidth() {
        return imageWidth;
    }

    @Override
    public int getHeight() {
        return imageHeight;
    }

    @Override
    public void draw(RecipeHolder<ForgingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        ForgingRecipe recipe = holder.value();
        background.draw(guiGraphics);

        String hitsText = Component.translatable("tooltip.overgeared.recipe.hits", recipe.getRemainingHits()).getString();

        String tierRaw = recipe.getAnvilTier();
        AnvilTier tierName = AnvilTier.fromDisplayName(tierRaw);

        MutableComponent tierText =
                Component.translatable("tooltip.overgeared.recipe.tier")
                        .append(Component.literal(" "));

        if (tierName != null) {
            tierText = tierText.append(
                    Component.translatable(tierName.getLang())
            );
        } else {
            tierText = tierText.append(
                    Component.literal(tierRaw)
            );
        }

        if (recipe.hasQuality() || !recipe.needsMinigame()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RESULT_BIG, 112, 14, 0, 0, 26, 26, 26, 26);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RESULT_TWOSLOT, 116, 9, 0, 0, 18, 36, 18, 36);
        }

        guiGraphics.text(Minecraft.getInstance().font, hitsText, 79, 1, 0xFF808080, false);
        guiGraphics.text(Minecraft.getInstance().font, tierText, 79, 47, 0xFF808080, false);

        arrowAnimated.draw(guiGraphics, 82, 19);

    }


    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RecipeHolder<ForgingRecipe> holder,
            IFocusGroup focuses
    ) {
        try {
            setupRecipe(builder, holder.value(), focuses);
        } catch (Exception e) {

            Overgeared.LOGGER.error(
                    "JEI failed recipe: {}",
                    holder.id().identifier(),
                    e
            );

        }
    }

    public void setupRecipe(IRecipeLayoutBuilder builder, ForgingRecipe recipe, IFocusGroup focuses) {

        int gridWidth = 3;
        int gridHeight = 3;

        int recipeWidth = recipe.width;
        int recipeHeight = recipe.height;

        List<ForgingRecipe.ForgingIngredient> ingredients =
                recipe.getForgingIngredients();

        int offsetX = (gridWidth - recipeWidth) / 2;
        int offsetY = getOffsetY(gridHeight, gridWidth, recipeHeight, recipeWidth);


        for (int y = 0; y < recipeHeight; y++) {
            for (int x = 0; x < recipeWidth; x++) {

                int slotX = 23 + (x + offsetX) * 18;
                int slotY = 1 + (y + offsetY) * 18;

                int index = y * recipeWidth + x;

                if (index >= ingredients.size())
                    continue;


                ForgingRecipe.ForgingIngredient forgingIngredient =
                        ingredients.get(index);

                if (forgingIngredient.isEmpty() || forgingIngredient.ingredient().isEmpty())
                    continue;

                Ingredient ingredient = forgingIngredient.ingredient().get();

                if (!forgingIngredient.requiresHeated()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                            .add(ingredient);
                    continue;
                }

                // Heated ingredients are shown with the overgeared:heated component set.
                List<ItemStack> stacks = new ArrayList<>();

                ingredient.items().forEach(item -> {
                    ItemStack copy = new ItemStack(item);
                    if (copy.isEmpty())
                        return;
                    copy.set(ModComponents.HEATED, true);
                    stacks.add(copy);
                });


                if (!stacks.isEmpty()) {
                    builder.addSlot(
                            RecipeIngredientRole.INPUT,
                            slotX,
                            slotY
                    ).addItemStacks(stacks);
                }
            }
        }


        // blueprint

        List<ItemStack> blueprints =
                createBlueprintStacksForRecipe(recipe);

        if (!blueprints.isEmpty()) {
            builder.addSlot(
                    RecipeIngredientRole.CRAFTING_STATION,
                    1,
                    19
            ).addItemStacks(blueprints);
        }


        ItemStack result = recipe.getResultItem();


        if (recipe.hasQuality() || !recipe.needsMinigame()) {

            builder.addSlot(
                    RecipeIngredientRole.OUTPUT,
                    117,
                    19
            ).add(result);

        } else {

            builder.addSlot(
                    RecipeIngredientRole.OUTPUT,
                    117,
                    10
            ).add(result);


            ItemStack failed = recipe.getFailedResultItem();

            if (!failed.isEmpty()) {

                failed.set(ModComponents.FAILED_RESULT, true);


                builder.addSlot(
                        RecipeIngredientRole.OUTPUT,
                        117,
                        28
                ).add(failed);
            }
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
            offsetY = gridHeight - recipeHeight;
        }

        return offsetY;
    }

    private List<ItemStack> createBlueprintStacksForRecipe(ForgingRecipe recipe) {
        Set<String> types = recipe.getBlueprintTypes();
        boolean required = recipe.requiresBlueprint();
        List<ItemStack> stacks = new ArrayList<>();

        for (String type : types) {
            ItemStack stack = new ItemStack(ModItems.BLUEPRINT);
            // 1.20.1 NBT {ToolType, Required} -> BLUEPRINT_DATA.toolType + BLUEPRINT_REQUIRED
            stack.set(ModComponents.BLUEPRINT_DATA, BlueprintData.createDefault().withToolType(type));
            stack.set(ModComponents.BLUEPRINT_REQUIRED, required);
            stacks.add(stack);
        }

        return stacks;
    }

}
