package net.stirdrem.overgeared.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ModItems;

public class BlueprintCloningRecipe extends SpecialCraftingRecipe {
    public BlueprintCloningRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inv, World world) {
        int blueprintCount = 0;
        ItemStack emptyBlueprint = ItemStack.EMPTY;

        for (int j = 0; j < inv.size(); ++j) {
            ItemStack stack = inv.getStack(j);
            if (!stack.isEmpty()) {
                if (stack.isOf(ModItems.EMPTY_BLUEPRINT)) {
                    if (!emptyBlueprint.isEmpty()) {
                        return false; // Only 1 empty blueprint allowed
                    }
                    emptyBlueprint = stack;
                } else {
                    if (!stack.isOf(ModItems.BLUEPRINT)) {
                        return false;
                    }

                    ++blueprintCount;
                }
            }
        }

        return !emptyBlueprint.isEmpty() && blueprintCount > 0;
    }


    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager registryAccess) {
        ItemStack source = ItemStack.EMPTY;

        for (int j = 0; j < inv.size(); ++j) {
            ItemStack stack = inv.getStack(j);
            if (!stack.isEmpty() && stack.isOf(ModItems.BLUEPRINT)) {
                if (!source.isEmpty()) return ItemStack.EMPTY; // only 1 blueprint source allowed
                source = stack;
            }
        }

        if (source.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = source.copyWithCount(2);

        // Reduce quality
        if (source.hasNbt() && source.getNbt().contains("Quality")) {
            String currentId = source.getNbt().getString("Quality");
            BlueprintQuality current = BlueprintQuality.fromString(currentId);
            BlueprintQuality downgraded = BlueprintQuality.getPrevious(current);

            if (downgraded != null) {
                result.getOrCreateNbt().putString("Quality", downgraded.getId());
            }
        }

        return result;
    }


    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRAFTING_BLUEPRINTCLONING;
    }

}
