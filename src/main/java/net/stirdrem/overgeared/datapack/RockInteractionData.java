package net.stirdrem.overgeared.datapack;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RockInteractionData {

    // 26.3: drops are stored as templates - building an ItemStack needs item components, which are
    // not bound yet while datapack reload listeners run.
    public record ToolEntry(Ingredient ingredient, ItemStackTemplate drop, float dropChance, float breakChance) {
        public ItemStack dropItem() {
            return drop.create();
        }
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
