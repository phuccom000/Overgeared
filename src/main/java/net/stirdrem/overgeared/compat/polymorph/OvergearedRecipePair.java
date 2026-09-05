package net.stirdrem.overgeared.compat.polymorph;

import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


/** One selectable option in the anvil's recipe-choice list: a recipe id plus its output preview. */
public record OvergearedRecipePair(ResourceLocation resourceLocation, ItemStack output) implements IRecipePair {
    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    @Override
    public int compareTo(IRecipePair other) {
        ResourceLocation thisItem = BuiltInRegistries.ITEM.getKey(output.getItem());
        ResourceLocation otherItem = BuiltInRegistries.ITEM.getKey(other.getOutput().getItem());
        int compare = thisItem.compareTo(otherItem);
        if (compare != 0) return compare;
        return resourceLocation.compareTo(other.getResourceLocation());
    }
}
