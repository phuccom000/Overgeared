package net.stirdrem.overgeared.recipe.nbtcooking;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public abstract class AbstractNBTCookingRecipe extends AbstractCookingRecipe {

    protected final NbtCompound resultTag;

    public AbstractNBTCookingRecipe(
            RecipeType<?> type,
            Identifier id,
            String group,
            CookingRecipeCategory category,
            Ingredient ingredient,
            ItemStack result,
            float experience,
            int cookingTime,
            NbtCompound resultTag
    ) {
        super(type, id, group, category, ingredient, result, experience, cookingTime);
        this.resultTag = resultTag;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryAccess) {
        ItemStack result = super.craft(inventory, registryAccess).copy();

        if (resultTag != null && !resultTag.isEmpty()) {
            NbtCompound tag = result.getOrCreateNbt();
            tag.copyFrom(resultTag); // merge custom NBT
            result.setNbt(tag);
        }

        return result;
    }

    public NbtCompound getResultTag() {
        return resultTag;
    }
}
