package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DraftingTableEmiRecipe implements EmiRecipe {

    private final EmiIngredient input;
    private final EmiStack output;

    public DraftingTableEmiRecipe() {
        this.input = EmiIngredient.of(Ingredient.of(ModItems.EMPTY_BLUEPRINT.get()));
        this.output = EmiStack.of(ModItems.BLUEPRINT.get());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.DRAFTING_TABLE_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return OvergearedMod.loc("explanation/drafting_table");
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 150;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int startY = 10;

        int slotSize = 18;
        int arrowWidth = 24;
        int gap = 6;

        int totalWidth = slotSize + gap + arrowWidth + gap + slotSize;
        int startX = (getDisplayWidth() - totalWidth) / 2;

        int inputX = startX;
        int arrowX = inputX + slotSize + gap;
        int outputX = arrowX + arrowWidth + gap;

        // Input slot (empty blueprint)
        widgets.addSlot(input, inputX, startY).drawBack(true);

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowX, startY + 1);

        // Output slot (blueprint)
        widgets.addSlot(output, outputX, startY).drawBack(true).recipeContext(this);

        // Explanation text
        int textWidth = getDisplayWidth() - 20;
        int textX = 10;
        int textY = startY + 30;

        Component text = Component.translatable("jei.overgeared.drafting_table.description");

        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(text, textWidth);

        for (FormattedCharSequence line : lines) {
            widgets.addText(line, textX, textY, ChatFormatting.DARK_GRAY.getColor(), false);
            textY += 10;
        }
    }
}
