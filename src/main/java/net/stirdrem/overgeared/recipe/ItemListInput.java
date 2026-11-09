package net.stirdrem.overgeared.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe input for Overgeared's own recipe types: a fixed list of stacks, indexed the same way
 * as the Container the 1.20.1 recipes used to receive (slot i of the container = index i here).
 */
public record ItemListInput(List<ItemStack> items) implements RecipeInput {

    public static ItemListInput of(ItemStack... stacks) {
        return new ItemListInput(List.of(stacks));
    }

    /** Snapshot of every slot in {@code container}. */
    public static ItemListInput of(Container container) {
        return of(container, 0, container.getContainerSize());
    }

    /** Snapshot of slots {@code [from, to)} of {@code container}, re-indexed from 0. */
    public static ItemListInput of(Container container, int from, int to) {
        List<ItemStack> stacks = new ArrayList<>(to - from);
        for (int i = from; i < to; i++) stacks.add(container.getItem(i));
        return new ItemListInput(stacks);
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < items.size() ? items.get(index) : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return items.size();
    }
}
