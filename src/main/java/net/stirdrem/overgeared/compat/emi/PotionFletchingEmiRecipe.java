package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

import java.util.List;

/**
 * EMI recipe entry for arrow and potion conversion in the fletching table (Path 1).
 *   - Arrow in the TIP slot, shaft and feather empty, potion in potion slot
 *   - One entry per: arrow type × potion × variant (TIPPED / LINGERING)
 *   - Includes vanilla arrow (→ tipped_arrow / lingering_arrow)
 *   - Includes upgrade arrows (iron/steel/diamond → same item + POTION_CONTENTS)
 */
public class PotionFletchingEmiRecipe implements EmiRecipe {

    public enum Variant { TIPPED, LINGERING }

    private static final int SLOT_SIZE = 18;

    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public PotionFletchingEmiRecipe(Item arrowItem, Holder<Potion> potion,
                                    ResourceLocation potionKey, Variant variant) {
        String arrowPath = arrowItem.builtInRegistryHolder().key().location().getPath();
        this.id = OvergearedMod.loc("fletching/" + variant.name().toLowerCase()
                + "_conv/" + arrowPath + "/" + potionKey.getPath());

        // Input: 1x base arrow (goes in tip slot)
        ItemStack arrowInput = new ItemStack(arrowItem, 1);

        // Input: specific potion
        Item potionItem = variant == Variant.TIPPED ? Items.POTION : Items.LINGERING_POTION;
        ItemStack potionStack = PotionContents.createItemStack(potionItem, potion);

        this.inputs = List.of(EmiStack.of(arrowInput), EmiStack.of(potionStack));

        // Output depends on arrow type and variant
        PotionContents contents = potionStack.get(DataComponents.POTION_CONTENTS);
        ItemStack output;

        if (arrowItem == Items.ARROW) {
            // Vanilla arrow → tipped_arrow or lingering_arrow (different items)
            if (variant == Variant.TIPPED) {
                output = new ItemStack(Items.TIPPED_ARROW, 1);
            } else {
                output = new ItemStack(ModItems.LINGERING_ARROW.get(), 1);
            }
        } else {
            // Upgrade arrow → same item with potion contents
            output = new ItemStack(arrowItem, 1);
            if (variant == Variant.LINGERING) {
                output.set(ModComponents.LINGERING_STATUS, true);
            }
        }
        if (contents != null) output.set(DataComponents.POTION_CONTENTS, contents);

        // only shows THIS recipe, not all tipped/lingering recipes for this arrow.
        EmiStack outputStack = EmiStack.of(output).comparison(Comparison.compareComponents());
        this.outputs = List.of(outputStack);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.FLETCHING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int offsetX = 4;
        int offsetY = 4;

        // Arrow in shaft slot (middle) for aesthetics, tip and feather empty
        widgets.addSlot(EmiStack.EMPTY,  offsetX + 36, offsetY);
        widgets.addSlot(inputs.get(0),   offsetX + 18, offsetY + SLOT_SIZE);
        widgets.addSlot(EmiStack.EMPTY,  offsetX,      offsetY + SLOT_SIZE * 2);

        // Potion in potion slot (bottom-right)
        widgets.addSlot(inputs.get(1), offsetX + 36, offsetY + SLOT_SIZE * 2);

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, offsetX + 54, offsetY + SLOT_SIZE);

        // Output
        widgets.addSlot(outputs.getFirst(), offsetX + 82, offsetY + SLOT_SIZE - 4)
                .large(true).recipeContext(this);
    }
}
