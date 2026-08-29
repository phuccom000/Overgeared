package net.stirdrem.overgeared.datapack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.List;

public class RockInteractionData {

    public record ToolEntry(Ingredient ingredient, ItemStack dropItem, float dropChance, float breakChance) {
    }

    private final Block inputBlock;
    private final List<ToolEntry> tools;
    private final Block resultBlock;

    public RockInteractionData(Block inputBlock, List<ToolEntry> tools, Block resultBlock) {
        this.inputBlock = inputBlock;
        this.tools = tools;
        this.resultBlock = resultBlock;
    }

    public boolean matches(BlockState state, ItemStack stack) {
        if (!state.isOf(inputBlock)) return false;
        return tools.stream().anyMatch(t -> t.ingredient.test(stack));
    }

    public ToolEntry getTool(ItemStack stack) {
        return tools.stream().filter(t -> t.ingredient.test(stack)).findFirst().orElse(null);
    }

    public Block getResultBlock() {
        return resultBlock;
    }
}
