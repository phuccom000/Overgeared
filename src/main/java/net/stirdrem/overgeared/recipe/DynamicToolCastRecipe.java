package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.ConfigHelper;
import net.stirdrem.overgeared.util.ModTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicToolCastRecipe extends CustomRecipe {

    public DynamicToolCastRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack cast = ItemStack.EMPTY;
        CastData castData = null;
        int materialCount = 0;
        int totalMaterialValue = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            // Check for Cast
            if (stack.getItem() instanceof ToolCastItem) {
                // Only one cast allowed
                if (!cast.isEmpty()) return false;

                castData = stack.get(ModComponents.CAST_DATA);
                if (castData == null) return false;

                // Cannot add materials if cast has output
                if (castData.hasOutput()) return false;

                // Must have a valid tool type
                if (castData.toolType().isBlank()) return false;

                cast = stack;
                continue;
            }

            // Check for valid material
            if (ConfigHelper.isValidMaterial(stack)) {
                materialCount++;
                totalMaterialValue += ConfigHelper.getMaterialValue(stack);
                continue;
            }

            // If Item is Invalid
            return false;
        }

        if (cast.isEmpty() || materialCount == 0) return false;

        // Check overflow
        if (castData != null) {
            int existingAmount = castData.amount();
            int maxAmount = ConfigHelper.getMaxMaterialAmount(castData.toolType());

            if (maxAmount > 0 && existingAmount + totalMaterialValue > maxAmount) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;

        ItemStack cast = ItemStack.EMPTY;
        CastData castData = null;
        Map<String, Integer> materialTotals = new HashMap<>();
        List<ItemStack> addedInputs = new ArrayList<>();
        int addedAmount = 0;

        // Scan grid and add materials
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue; // Skip Empty Slots

            // Find the cast
            if (stack.getItem() instanceof ToolCastItem) {
                cast = stack.copy();
                castData = cast.get(ModComponents.CAST_DATA);
                if (castData == null) return ItemStack.EMPTY;

                // Copy existing materials from cast
                materialTotals.putAll(castData.materials());
            }
            // Process materials
            if (ConfigHelper.isValidMaterial(stack)) {
                String material = ConfigHelper.getMaterialForItem(stack);
                int value = ConfigHelper.getMaterialValue(stack);

                // Add to material totals
                materialTotals.put(material, materialTotals.getOrDefault(material, 0) + value);
                addedAmount += value;

                // Track input item (single item for display)
                ItemStack singleItem = stack.copyWithCount(1);
                addedInputs.add(singleItem);
            }
        }

        if (cast.isEmpty() || castData == null) return ItemStack.EMPTY;

        // Calculate new total
        int existingAmount = castData.amount();
        int totalAmount = existingAmount + addedAmount;
        int maxAmount = ConfigHelper.getMaxMaterialAmount(castData.toolType());

        // Block if exceeds max
        if (maxAmount > 0 && totalAmount > maxAmount) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> allInputs = new ArrayList<>(castData.input());
        for (ItemStack addedInput : addedInputs) {
            // Try to merge with existing stacks
            boolean merged = false;
            for (int i = 0; i < allInputs.size(); i++) {
                ItemStack existing = allInputs.get(i);
                if (ItemStack.isSameItemSameComponents(existing, addedInput)) {
                    ItemStack combined = existing.copy();
                    combined.grow(1);
                    allInputs.set(i, combined);
                    merged = true;
                    break;
                }
            }

            // If not merged, add as new entry
            if (!merged) {
                allInputs.add(addedInput.copy());
            }
        }

        // Create updated cast data
        CastData newCastData = new CastData(
                castData.quality(),
                castData.toolType(),
                materialTotals,
                totalAmount,
                maxAmount > 0 ? maxAmount : castData.maxAmount(),
                allInputs,
                castData.output(),
                castData.heated()
        );

        cast.set(ModComponents.CAST_DATA, newCastData);
        return cast;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_DYNAMIC_TOOL_CAST.get();
    }

}
