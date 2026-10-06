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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.CastRecipeHelper;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CastingRecipeCategory implements IRecipeCategory<RecipeHolder<CastingRecipe>> {

    public static final Identifier UID = Overgeared.id("casting");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/casting_furnace_jei.png");

    public static final IRecipeHolderType<CastingRecipe> CASTING_TYPE = IRecipeHolderType.create(UID);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 200;
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;

    public CastingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 89, 43)
                .setTextureSize(112, 43)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.CAST_FURNACE));

        arrowStatic = helper.drawableBuilder(TEXTURE, 89, 14, 22, 16).setTextureSize(112, 43).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 89, 0, 14, 13).setTextureSize(112, 43).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                100,
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public IRecipeHolderType<CastingRecipe> getRecipeType() {
        return CASTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.casting");
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
    public void draw(RecipeHolder<CastingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        CastingRecipe recipe = holder.value();
        background.draw(guiGraphics);
        float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 29, 9);
        flameAnimated.draw(guiGraphics, 33, 29);

        String expText;
        if (exp == (int) exp) {
            expText = (int) exp + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        int textWidth = Minecraft.getInstance().font.width(expText);
        int xPos = this.background.getWidth() - textWidth;

        guiGraphics.text(Minecraft.getInstance().font, expText, xPos, 35, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CastingRecipe> holder, IFocusGroup focuses) {
        CastingRecipe recipe = holder.value();

        // -------------------------
        // MATERIAL INPUT SLOT
        // -------------------------
        // The per-material stacks are flattened into one combined stack list.
        List<ItemStack> materialStacks = new ArrayList<>();

        Map<String, Double> requiredMaterials = recipe.getRequiredMaterials();

        for (var entry : requiredMaterials.entrySet()) {
            String materialId = entry.getKey();
            double requiredAmount = entry.getValue();

            for (Item item : ConfigHelper.getItemListForMaterial(materialId)) {
                int value = ConfigHelper.getMaterialValue(item);
                if (value <= 0) continue;

                int count = (int) Math.ceil(requiredAmount / value);
                materialStacks.add(new ItemStack(item, count));
            }
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(materialStacks);

        // -------------------------
        // TOOL CAST SLOT
        // -------------------------
        // 1.20.1 NBT {ToolType, Amount, MaxAmount} -> CAST_DATA (with the required materials)
        ItemStack firedCast = recipe.getDisplayCast();
        ItemStack netherCast = CastRecipeHelper.displayCast(ModItems.NETHER_TOOL_CAST,
                recipe.getToolType(), requiredMaterials);

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addItemStacks(List.of(firedCast, netherCast));

        // -------------------------
        // OUTPUT
        // -------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 10)
                .add(recipe.getResultItem());
    }

}
