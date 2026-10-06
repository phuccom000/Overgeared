package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.ExplanationRecipe;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SteelAnvilCategory implements IRecipeCategory<ExplanationRecipe> {
    private static final Identifier BACKGROUND_LOCATION = Overgeared.id("textures/gui/explanation_jei.png");

    public static final Identifier UID = Overgeared.id("steel_anvil");

    public static final IRecipeType<ExplanationRecipe> STEEL_ANVIL_GET =
            IRecipeType.create(UID, ExplanationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public SteelAnvilCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 150, 120)
                .setTextureSize(150, 120)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SMITHING_ANVIL));
        this.title = Component.translatable("jei.overgeared.category.steel_anvil");
    }

    @Override
    public IRecipeType<ExplanationRecipe> getRecipeType() {
        return STEEL_ANVIL_GET;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ExplanationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 18)
                .addItemStacks(hammerItems());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 18)
                .add(recipe.getResultItem());
    }

    // Resolved lazily: item tags are only bound once the client has received them.
    private static List<ItemStack> hammerItems() {
        TagKey<Item> hammerTag = ModTags.Items.SMITHING_HAMMERS;
        return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(hammerTag).spliterator(), false)
                .map(ItemStack::new)
                .collect(Collectors.toList());
    }

    @Override
    public void draw(ExplanationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        int textWidth = 140;
        int textX = 5;
        int textY = 43;

        renderWrappedText(
                guiGraphics,
                Component.translatable("jei.overgeared.steel_anvil.description"),
                textX, textY,
                textWidth,
                0xFF555555, // ChatFormatting.DARK_GRAY
                false
        );
    }

    public static void renderWrappedText(GuiGraphicsExtractor guiGraphics, Component text, int x, int y, int width, int color, boolean shadow) {
        var font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(text, width);

        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.text(font, lines.get(i), x, y + (i * font.lineHeight), color, shadow);
        }
    }
}
