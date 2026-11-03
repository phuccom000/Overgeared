package net.stirdrem.overgeared.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> implements ItemLike {
    protected DeferredBlock(ResourceKey<Block> key) {
        super(key);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }

    public ItemStack toStack() {
        return new ItemStack(this);
    }
}
