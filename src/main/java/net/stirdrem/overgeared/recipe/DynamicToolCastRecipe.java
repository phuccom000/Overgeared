package net.stirdrem.overgeared.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DynamicToolCastRecipe extends SpecialCraftingRecipe {

    public DynamicToolCastRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inv, World world) {
        ItemStack cast = ItemStack.EMPTY;
        int existingAmount = 0;
        int addedAmount = 0;
        int maxAmount = 0;
        boolean foundMaterial = false;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            // === CAST ===
            if (stack.isOf(ModItems.CLAY_TOOL_CAST) || stack.isOf(ModItems.NETHER_TOOL_CAST)) {
                if (!cast.isEmpty()) return false; // only one cast allowed
                if (!stack.hasNbt()) return false;

                NbtCompound tag = stack.getNbt();
                String toolType = tag.getString("ToolType");
                if (toolType.isBlank()) return false;

                cast = stack;
                existingAmount = tag.getInt("Amount");
                maxAmount = ConfigHelper.getMaxMaterialAmount(toolType);
                continue;
            }

            // === MATERIAL ===
            String material = ConfigHelper.getMaterialForItem(stack);
            if (!material.equals("none")) {
                foundMaterial = true;
                addedAmount += ConfigHelper.getMaterialValue(stack);
                continue;
            }

            // === INVALID ITEM ===
            return false;
        }

        if (cast.isEmpty() || !foundMaterial) return false;

        // Overflow check
        if (maxAmount > 0 && existingAmount + addedAmount > maxAmount) {
            return false;
        }

        return true;
    }


    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager registryAccess) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;

        ItemStack cast = ItemStack.EMPTY;
        HashMap<String, Integer> materialTotals = new HashMap<>();
        List<ItemStack> inputItems = new ArrayList<>(); // Store the actual ItemStacks for comparison
        int newAmount = 0;
        int maxAmount = 0;
        String toolType = "none";

        // Scan grid
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);

            // Find cast
            if (stack.isOf(ModItems.CLAY_TOOL_CAST) || stack.isOf(ModItems.NETHER_TOOL_CAST)) {
                cast = stack.copy();
                NbtCompound tag = cast.getOrCreateNbt();
                toolType = tag.getString("ToolType");
                maxAmount = ConfigHelper.getMaxMaterialAmount(toolType);
            }
            // Process materials
            else if (!stack.isEmpty()) {
                String material = ConfigHelper.getMaterialForItem(stack);
                if (!material.equals("none")) {
                    int value = ConfigHelper.getMaterialValue(stack);
                    materialTotals.put(material, materialTotals.getOrDefault(material, 0) + value);
                    newAmount += value;

                    // Store the actual ItemStack for comparison
                    ItemStack singleItem = stack.copy();
                    singleItem.setCount(1);
                    inputItems.add(singleItem);
                }
            }
        }

        if (cast.isEmpty()) return ItemStack.EMPTY;

        // === Load existing values ===
        NbtCompound castTag = cast.getOrCreateNbt();
        NbtCompound existingMatTag = castTag.getCompound("Materials");
        int existingAmount = castTag.getInt("Amount");

        // Sum total after adding
        int totalAmount = existingAmount + newAmount;

        // Block if exceeds
        if (maxAmount > 0 && totalAmount > maxAmount) return ItemStack.EMPTY;

        // Merge materials
        for (String mat : existingMatTag.getKeys()) {
            int oldVal = existingMatTag.getInt(mat);
            materialTotals.put(mat, materialTotals.getOrDefault(mat, 0) + oldVal);
        }

        // Write merged data back
        NbtCompound newMatTag = new NbtCompound();
        for (var entry : materialTotals.entrySet()) {
            newMatTag.putInt(entry.getKey(), entry.getValue());
        }

        // Add complete item data to the "input" list, merging duplicates
        addItemStacksToInputList(castTag, inputItems);

        castTag.put("Materials", newMatTag);
        castTag.putInt("Amount", totalAmount);
        castTag.putString("ToolType", toolType);

        return cast;
    }

    /**
     * Adds complete item stack data to the "input" NBT list, merging duplicates
     */
    private void addItemStacksToInputList(NbtCompound castTag, List<ItemStack> newInputItems) {
        // Get or create the "input" list
        NbtList inputList;
        if (castTag.contains("input", NbtElement.LIST_TYPE)) {
            inputList = castTag.getList("input", NbtElement.COMPOUND_TYPE);
        } else {
            inputList = new NbtList();
        }

        // Convert existing input list to a list of ItemStacks for comparison
        List<ItemStack> existingItems = new ArrayList<>();
        for (NbtElement inputTag : inputList) {
            if (inputTag instanceof NbtCompound compound) {
                ItemStack existingItem = ItemStack.fromNbt(compound);
                if (!existingItem.isEmpty()) {
                    existingItems.add(existingItem);
                }
            }
        }

        // Merge new items with existing items
        for (ItemStack newItem : newInputItems) {
            boolean merged = false;

            // Try to merge with existing items
            for (int i = 0; i < existingItems.size(); i++) {
                ItemStack existingItem = existingItems.get(i);

                // Check if items are identical (same item, same NBT)
                if (areItemStacksIdentical(existingItem, newItem)) {
                    // Increase count of existing item
                    existingItem.setCount(existingItem.getCount() + newItem.getCount());
                    merged = true;
                    break;
                }
            }

            // If not merged, add as new entry
            if (!merged) {
                existingItems.add(newItem.copy());
            }
        }

        // Convert back to NBT list
        NbtList mergedInputList = new NbtList();
        for (ItemStack item : existingItems) {
            NbtCompound itemTag = new NbtCompound();
            item.writeNbt(itemTag); // Save with updated count
            mergedInputList.add(itemTag);
        }

        // Save the updated list back to the cast
        castTag.put("input", mergedInputList);
    }

    /**
     * Checks if two ItemStacks are identical (same item and same NBT)
     */
    private boolean areItemStacksIdentical(ItemStack stack1, ItemStack stack2) {
        // Check if items are the same
        if (!ItemStack.areItemsEqual(stack1, stack2)) {
            return false;
        }

        // Check if NBT tags are the same
        NbtCompound tag1 = stack1.getNbt();
        NbtCompound tag2 = stack2.getNbt();

        if (tag1 == null && tag2 == null) {
            return true;
        }

        if (tag1 == null || tag2 == null) {
            return false;
        }

        return tag1.equals(tag2);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRAFTING_DYNAMIC_TOOL_CAST;
    }
}
