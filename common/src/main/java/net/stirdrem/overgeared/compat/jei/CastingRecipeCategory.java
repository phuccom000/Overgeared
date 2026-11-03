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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CastingRecipeCategory implements IRecipeCategory<CastingRecipe> {
    public static final ResourceLocation UID = OvergearedMod.loc("casting");
    public static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/casting_furnace_jei.png");

    public static final RecipeType<CastingRecipe> CASTING_TYPE =
            new RecipeType<>(UID, CastingRecipe.class);

    private static final int TEX_W = 112;
    private static final int TEX_H = 43;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableAnimated flameAnimated;

    public CastingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 89, TEX_H).setTextureSize(TEX_W, TEX_H).build();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.CAST_FURNACE.get()));
        this.arrowAnimated = helper.createAnimatedDrawable(
                helper.drawableBuilder(TEXTURE, 89, 14, 22, 16).setTextureSize(TEX_W, TEX_H).build(),
                200, IDrawableAnimated.StartDirection.LEFT, false);
        this.flameAnimated = helper.createAnimatedDrawable(
                helper.drawableBuilder(TEXTURE, 89, 0, 14, 13).setTextureSize(TEX_W, TEX_H).build(),
                100, IDrawableAnimated.StartDirection.TOP, true);
    }

    @Override
    public RecipeType<CastingRecipe> getRecipeType() {
        return CASTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.casting");
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
    public void draw(CastingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        arrowAnimated.draw(guiGraphics, 29, 9);
        flameAnimated.draw(guiGraphics, 33, 29);
        JeiUtils.drawExperience(guiGraphics, recipe.getExperience(), background.getWidth(), 35);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {
        Map<String, Integer> requiredMaterials = recipe.getRequiredMaterials();

        // Material input: every configured item for each required material, sized to fill the cast
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(buildMaterialStacks(requiredMaterials));

        // Tool cast input: empty clay / nether cast for this tool type
        int total = requiredMaterials.values().stream().mapToInt(Integer::intValue).sum();
        CastData castData = new CastData("", recipe.getToolType(), Map.of(), 0, total,
                List.of(), ItemStack.EMPTY, false);

        ItemStack clayCast = new ItemStack(ModItems.CLAY_TOOL_CAST.get());
        clayCast.set(ModComponents.CAST_DATA.get(), castData);
        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST.get());
        netherCast.set(ModComponents.CAST_DATA.get(), castData);

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addItemStacks(List.of(clayCast, netherCast));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 10)
                .addItemStack(recipe.getResultItem(JeiUtils.registries()));
    }

    private static List<ItemStack> buildMaterialStacks(Map<String, Integer> requiredMaterials) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : requiredMaterials.entrySet()) {
            String requiredMaterialId = entry.getKey();
            int amountNeeded = entry.getValue();

            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR || !ConfigHelper.isValidMaterial(item)) continue;
                if (!requiredMaterialId.equalsIgnoreCase(ConfigHelper.getMaterialForItem(item))) continue;

                int value = ConfigHelper.getMaterialValue(item);
                if (value <= 0) continue;

                int count = (int) Math.ceil((double) amountNeeded / value);
                stacks.add(new ItemStack(item, Math.max(1, count)));
            }
        }
        return stacks;
    }
}
