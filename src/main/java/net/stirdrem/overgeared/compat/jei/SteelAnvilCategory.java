package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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

    public static final RecipeType<ExplanationRecipe> STEEL_ANVIL_GET =
            new RecipeType<>(UID, ExplanationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Text title;
    private final List<ItemStack> hammerItems;

    public SteelAnvilCategory(IGuiHelper guiHelper, DynamicRegistryManager registryManager) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 150, 120)
                .setTextureSize(150, 120)
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SMITHING_ANVIL));
        this.title = Text.translatable("jei.overgeared.category.steel_anvil");

        TagKey<Item> hammerTag = ModTags.Items.SMITHING_HAMMERS;
        this.hammerItems = StreamSupport.stream(
                        registryManager.get(RegistryKeys.ITEM).iterateEntries(hammerTag).spliterator(), false)
                .map(RegistryEntry::value)
                .map(ItemStack::new)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeType<ExplanationRecipe> getRecipeType() {
        return STEEL_ANVIL_GET;
    }

    @Override
    public Text getTitle() {
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
    public void draw(ExplanationRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext guiGraphics, double mouseX, double mouseY) {
        int textWidth = 140;
        int textX = 5;
        int textY = 43;

        renderWrappedText(
                guiGraphics,
                Text.translatable("jei.overgeared.steel_anvil.description"),
                textX, textY,
                textWidth,
                Formatting.DARK_GRAY.getColorValue(),
                false
        );
    }

    public static void renderWrappedText(DrawContext guiGraphics, Text text, int x, int y, int width, int color, boolean shadow) {
        var font = MinecraftClient.getInstance().textRenderer;
        List<OrderedText> lines = font.wrapLines(text, width);

        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawText(font, lines.get(i), x, y + (i * font.fontHeight), color, shadow);
        }
    }
}
