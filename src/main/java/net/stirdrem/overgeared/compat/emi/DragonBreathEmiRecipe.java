package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.List;

public class DragonBreathEmiRecipe implements EmiRecipe {
    private final EmiIngredient input;
    private final EmiIngredient ingredient;
    private final EmiStack output, input3, output3;
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/brewing_stand.png");
    private static final EmiStack BLAZE_POWDER = EmiStack.of(Items.BLAZE_POWDER);

    public DragonBreathEmiRecipe() {
        ItemStack potionItemStack = PotionContents.createItemStack(Items.POTION, Potions.THICK);
        ItemStack chorus = new ItemStack(Items.CHORUS_FRUIT);
        this.input = EmiIngredient.of(Ingredient.of(potionItemStack));   // input potion
        this.ingredient = EmiIngredient.of(Ingredient.of(chorus)); // catalyst
        this.output = EmiStack.of(Items.DRAGON_BREATH); // result
        ItemStack threePotions = potionItemStack.copy(); // copy so original isn’t modified
        threePotions.setCount(3); // set amount to 3

        this.input3 = EmiStack.of(threePotions);
        this.output3 = output.copy().setAmount(3);
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input3, ingredient);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output3);
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, "brewing/dragon_breath");
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return VanillaEmiRecipeCategories.BREWING; // use vanilla brewing category
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 61;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        Component text = Component.translatable("emi.cooking.time", 20);

        widgets.addTexture(BACKGROUND, 0, 0, 103, 61, 16, 14);
        widgets.addAnimatedTexture(BACKGROUND, 81, 2, 9, 28, 176, 0, 1000 * 20, false, false, false).tooltip((mx, my) -> {
            return List.of(ClientTooltipComponent.create(text.getVisualOrderText()));
        });
        widgets.addAnimatedTexture(BACKGROUND, 47, 0, 12, 29, 185, 0, 700, false, true, false);
        widgets.addTexture(BACKGROUND, 44, 30, 18, 4, 176, 29);
        widgets.addSlot(BLAZE_POWDER, 0, 2).drawBack(false);
        widgets.addSlot(input, 39, 36).drawBack(false);
        widgets.addSlot(ingredient, 62, 2).drawBack(false);
        widgets.addSlot(output, 85, 36).drawBack(false).recipeContext(this);
    }
}
