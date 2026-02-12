package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

import java.util.List;
import java.util.Map;

public class ToolCastEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final String toolType;
    private final EmiIngredient centerInput;
    private final EmiIngredient surroundItem;
    private final EmiStack output;

    public ToolCastEmiRecipe(String toolType, Ingredient toolItems, boolean nether) {
        String prefix = nether ? "nether_tool_cast" : "clay_tool_cast";
        this.id = OvergearedMod.loc(prefix + "/" + toolType);
        this.toolType = toolType;
        this.centerInput = EmiIngredient.of(toolItems);
        this.surroundItem = EmiIngredient.of(Ingredient.of(nether ? Items.NETHER_BRICK : Items.CLAY_BALL));

        Item castItem = nether ? ModItems.NETHER_TOOL_CAST.get() : ModItems.UNFIRED_TOOL_CAST.get();
        ItemStack cast = new ItemStack(castItem);
        CastData castData = new CastData("", toolType, Map.of(), 0, 0, List.of(), ItemStack.EMPTY, false);
        cast.set(ModComponents.CAST_DATA, castData);
        this.output = EmiStack.of(cast);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.CRAFTING;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(centerInput, surroundItem, surroundItem, surroundItem, surroundItem);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 118;
    }

    @Override
    public int getDisplayHeight() {
        return 62;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int gridX = 0;
        int gridY = 2;
        int s = EmiLayoutConstants.SLOT_SIZE;

        widgets.addSlot(EmiStack.EMPTY, gridX, gridY);
        widgets.addSlot(surroundItem, gridX + s, gridY);
        widgets.addSlot(EmiStack.EMPTY, gridX + s * 2, gridY);

        widgets.addSlot(surroundItem, gridX, gridY + s);
        widgets.addSlot(centerInput, gridX + s, gridY + s);
        widgets.addSlot(surroundItem, gridX + s * 2, gridY + s);

        widgets.addSlot(EmiStack.EMPTY, gridX, gridY + s * 2);
        widgets.addSlot(surroundItem, gridX + s, gridY + s * 2);
        widgets.addSlot(EmiStack.EMPTY, gridX + s * 2, gridY + s * 2);

        int arrowX = gridX + s * 3 + 4;
        int arrowY = gridY + s;
        widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowX, arrowY);

        int outX = arrowX + EmiLayoutConstants.ARROW_WIDTH + 4;
        int outY = gridY + s - 4;
        widgets.addSlot(output, outX, outY).large(true).recipeContext(this);
    }
}
