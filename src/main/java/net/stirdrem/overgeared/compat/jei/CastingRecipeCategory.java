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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CastingRecipeCategory implements IRecipeCategory<CastingRecipe> {

    public static final Identifier UID = Overgeared.id("casting");
    public static final Identifier TEXTURE = Overgeared.id("textures/gui/casting_furnace_jei.png");

    public static final RecipeType<CastingRecipe> CASTING_TYPE =
            new RecipeType<>(UID, CastingRecipe.class);

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
    public RecipeType<CastingRecipe> getRecipeType() {
        return CASTING_TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("gui.overgeared.jei.category.casting");
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
    public void draw(CastingRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 29, 9);
        flameAnimated.draw(guiGraphics, 33, 29);

        String expText;
        if (exp == exp.intValue()) {
            expText = exp.intValue() + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(expText);
        int xPos = this.background.getWidth() - textWidth;

        guiGraphics.drawText(MinecraftClient.getInstance().textRenderer, expText, xPos, 35, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {

        // -------------------------
        // MATERIAL INPUT SLOT
        // -------------------------
        // Yarn's Ingredient has no Ingredient.merge(List<Ingredient>) helper (Forge/NeoForge-only
        // addition), so the per-material stacks are flattened into one combined stack list instead.
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
        NbtCompound tag = new NbtCompound();
        tag.putString("ToolType", recipe.getToolType());

        double total = requiredMaterials.values().stream().mapToDouble(Double::doubleValue).sum();
        tag.putDouble("Amount", total);
        tag.putDouble("MaxAmount", total);

        ItemStack firedCast = new ItemStack(ModItems.CLAY_TOOL_CAST);
        firedCast.setNbt(tag.copy());

        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST);
        netherCast.setNbt(tag.copy());

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addIngredients(Ingredient.ofStacks(firedCast, netherCast));

        // -------------------------
        // OUTPUT
        // -------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 10)
                .addItemStack(recipe.getOutput(null));
    }

}
