package net.stirdrem.overgeared.datapack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
        if (!state.is(inputBlock)) return false;
        return tools.stream().anyMatch(t -> t.ingredient.test(stack));
    }

    public ToolEntry getTool(ItemStack stack) {
        return tools.stream().filter(t -> t.ingredient.test(stack)).findFirst().orElse(null);
    }

    public Block getResultBlock() {
        return resultBlock;
    }
}
