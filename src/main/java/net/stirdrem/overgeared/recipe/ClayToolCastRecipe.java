package net.stirdrem.overgeared.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

public class ClayToolCastRecipe extends SpecialCraftingRecipe {

    private static final int[] CLAY_SLOTS = {1, 3, 5, 7}; // N, E, S, W around center

    // store the world between matches() and craft()
    private World lastWorld = null;

    public ClayToolCastRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inv, World world) {
        if (inv.size() != 9) return false;

        // store world reference for craft()
        this.lastWorld = world;

        ItemStack center = inv.getStack(4);
        if (center.isEmpty()) return false;

        // Must be mapped to a tool type in config
        String toolType = ConfigHelper.getToolTypeForItem(world, center);
        if ("none".equals(toolType)) return false;

        boolean clayPattern = true;
        boolean netherPattern = true;

        for (int slot : CLAY_SLOTS) {
            ItemStack stack = inv.getStack(slot);
            clayPattern &= stack.isOf(Items.CLAY_BALL);
            netherPattern &= stack.isOf(Items.NETHER_BRICK);
        }

        // Must be exclusively clay or exclusively nether bricks
        if (!clayPattern && !netherPattern) return false;

        // Other slots must be empty
        for (int i = 0; i < 9; i++) {
            if (i == 4 || i == 1 || i == 3 || i == 5 || i == 7) continue;
            if (!inv.getStack(i).isEmpty()) return false;
        }

        return true;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager registryAccess) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;

        ItemStack center = inv.getStack(4);
        if (center.isEmpty()) return ItemStack.EMPTY;

        // use the last known world from matches(), fallback to overworld if null
        World world = (lastWorld != null)
                ? lastWorld
                : Overgeared.getServer().getOverworld();

        String toolType = ConfigHelper.getToolTypeForItem(world, center);
        if ("none".equals(toolType) || toolType.isBlank()) return ItemStack.EMPTY;

        // detect if nether bricks were used
        boolean netherPattern = true;
        for (int slot : CLAY_SLOTS) {
            ItemStack stack = inv.getStack(slot);
            netherPattern &= stack.isOf(Items.NETHER_BRICK);
        }

        // Determine which cast item to create
        ItemStack result = netherPattern
                ? new ItemStack(ModItems.NETHER_TOOL_CAST)
                : new ItemStack(ModItems.UNFIRED_TOOL_CAST);

        // Extract forging quality from the center item
        NbtCompound centerTag = center.getNbt();
        String quality = "none";
        if (centerTag != null && centerTag.contains("ForgingQuality")) {
            quality = centerTag.getString("ForgingQuality");
            if (quality.isEmpty()) quality = "none";
        }

        int maxAmount = ConfigHelper.getMaxMaterialAmount(toolType);
        if (maxAmount <= 0) maxAmount = 9;

        // downgrade the quality one level
        if (!quality.equals("none"))
            quality = BlueprintQuality.getPrevious(BlueprintQuality.fromString(quality)).getId();

        // Attach all relevant NBT data
        NbtCompound tag = result.getOrCreateNbt();
        tag.putString("ToolType", toolType);
        if (!quality.equalsIgnoreCase("none"))
            tag.putString("Quality", quality);
        tag.putInt("Amount", 0);
        tag.putInt("MaxAmount", maxAmount);
        tag.put("Materials", new NbtCompound());

        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(RecipeInputInventory inv) {
        DefaultedList<ItemStack> remaining = DefaultedList.ofSize(inv.size(), ItemStack.EMPTY);

        // Keep the center item (slot 4)
        ItemStack centerItem = inv.getStack(4);
        if (!centerItem.isEmpty()) {
            remaining.set(4, centerItem.copyWithCount(1));
        }

        // Clay balls / nether bricks are consumed
        return remaining;
    }

    @Override
    public boolean fits(int w, int h) {
        return w * h >= 9;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CLAY_TOOL_CAST;
    }
}
