package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class StoneAnvilCategory implements IRecipeCategory<ExplanationRecipe> {
    private static final ResourceLocation BACKGROUND_LOCATION = Overgeared.id("textures/gui/explanation_jei.png");

    public static final ResourceLocation UID = Overgeared.id("stone_anvil");

    public static final RecipeType<ExplanationRecipe> STONE_ANVIL_GET =
            new RecipeType<>(UID, ExplanationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final List<ItemStack> hammerItems;

    public StoneAnvilCategory(IGuiHelper guiHelper, RegistryAccess registryManager) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 150, 120)
                .setTextureSize(150, 120)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.STONE_SMITHING_ANVIL));
        this.title = Component.translatable("jei.overgeared.category.stone_anvil");

        TagKey<Item> hammerTag = ModTags.Items.SMITHING_HAMMERS;
        this.hammerItems = StreamSupport.stream(
                        registryManager.registryOrThrow(Registries.ITEM).getTagOrEmpty(hammerTag).spliterator(), false)
                .map(Holder::value)
                .map(ItemStack::new)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeType<ExplanationRecipe> getRecipeType() {
        return STONE_ANVIL_GET;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ExplanationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 18)
                .addItemStacks(hammerItems);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 18)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(ExplanationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int textWidth = 140;
        int textX = 5;
        int textY = 43;

        renderWrappedText(
                guiGraphics,
                Component.translatable("jei.overgeared.stone_anvil.description1"),
                textX, textY,
                textWidth,
                ChatFormatting.DARK_GRAY.getColor(),
                false
        );
    }

    public static void renderWrappedText(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color, boolean shadow) {
        var font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(text, width);

        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(font, lines.get(i), x, y + (i * font.lineHeight), color, shadow);
        }
    }
}
