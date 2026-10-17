package net.stirdrem.overgeared.recipe.nbtcooking;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

public abstract class AbstractNBTCookingRecipe extends AbstractCookingRecipe {

    protected final CompoundTag resultTag;

    public AbstractNBTCookingRecipe(
            RecipeType<?> type,
            ResourceLocation id,
            String group,
            CookingBookCategory category,
            Ingredient ingredient,
            ItemStack result,
            float experience,
            int cookingTime,
            CompoundTag resultTag
    ) {
        super(type, id, group, category, ingredient, result, experience, cookingTime);
        this.resultTag = resultTag;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess).copy();

        if (resultTag != null && !resultTag.isEmpty()) {
            CompoundTag tag = result.getOrCreateTag();
            tag.merge(resultTag); // merge custom NBT
            result.setTag(tag);
        }

        return result;
    }

    public CompoundTag getResultTag() {
        return resultTag;
    }
}