package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

/**
 * {@code overgeared:crafting_initial_cast}: a tool (mapped to a tool type via item_to_tooltype recipes) in the
 * centre of a 3x3 grid, surrounded N/E/S/W by 4 clay balls (-> unfired tool cast) or 4 nether bricks (-> nether
 * tool cast), corners empty. The tool is returned. The cast gets CAST_DATA with the tool type, the tool's quality
 * downgraded one step, amount 0 and the configured max amount.
 * <p>
 * Input indexes are those of the (trimmed) {@link CraftingInput}; the pattern fills the whole 3x3 box so they
 * equal the crafting grid slots (4 = centre).
 */
public class ClayToolCastRecipe extends CustomRecipe {
    public static final ClayToolCastRecipe INSTANCE = new ClayToolCastRecipe();

    private static final int[] CLAY_SLOTS = {1, 3, 5, 7}; // N, W, E, S around center

    // store the world between matches() and assemble()
    private Level lastWorld = null;

    public ClayToolCastRecipe() {
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        if (inv.width() != 3 || inv.height() != 3) return false;

        this.lastWorld = world;

        ItemStack center = inv.getItem(4);
        if (center.isEmpty()) return false;

        String toolType = ConfigHelper.getToolTypeForItem(world, center);
        if ("none".equals(toolType)) return false;

        boolean clayPattern = true;
        boolean netherPattern = true;

        for (int slot : CLAY_SLOTS) {
            ItemStack stack = inv.getItem(slot);
            clayPattern &= stack.is(Items.CLAY_BALL);
            netherPattern &= stack.is(Items.NETHER_BRICK);
        }

        if (!clayPattern && !netherPattern) return false;

        for (int i = 0; i < 9; i++) {
            if (i == 4 || i == 1 || i == 3 || i == 5 || i == 7) continue;
            if (!inv.getItem(i).isEmpty()) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;
        if (inv.width() != 3 || inv.height() != 3) return ItemStack.EMPTY;

        ItemStack center = inv.getItem(4);
        if (center.isEmpty()) return ItemStack.EMPTY;

        Level world = lastWorld != null ? lastWorld : Overgeared.getServer().overworld();

        String toolType = ConfigHelper.getToolTypeForItem(world, center);
        if ("none".equals(toolType) || toolType.isBlank()) return ItemStack.EMPTY;

        boolean netherPattern = true;
        for (int slot : CLAY_SLOTS) {
            netherPattern &= inv.getItem(slot).is(Items.NETHER_BRICK);
        }

        ItemStack result = netherPattern
                ? new ItemStack(ModItems.NETHER_TOOL_CAST)
                : new ItemStack(ModItems.UNFIRED_TOOL_CAST);

        ForgingQuality forgingQuality = ForgingQuality.get(center);
        String quality = forgingQuality == null ? "none" : forgingQuality.getDisplayName();

        int maxAmount = ConfigHelper.getMaxMaterialAmount(toolType);
        if (maxAmount <= 0) maxAmount = 9;

        if (!quality.equals("none")) {
            // The cast stores one tier below the pattern head; Poor has no lower tier and stays Poor.
            BlueprintQuality lower = BlueprintQuality.getPrevious(BlueprintQuality.fromString(quality));
            quality = lower != null ? lower.getId() : BlueprintQuality.POOR.getId();
        }

        result.set(ModComponents.CAST_DATA, CastData.EMPTY
                .withToolType(toolType)
                .withQuality(quality.equalsIgnoreCase("none") ? "" : quality)
                .withAmount(0)
                .withMaxAmount(maxAmount));

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        // Keep the center item (slot 4); clay balls / nether bricks are consumed
        if (inv.size() == 9) {
            ItemStack centerItem = inv.getItem(4);
            if (!centerItem.isEmpty()) {
                remaining.set(4, centerItem.copyWithCount(1));
            }
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<ClayToolCastRecipe> getSerializer() {
        return ModRecipes.CLAY_TOOL_CAST;
    }

    public static final MapCodec<ClayToolCastRecipe> MAP_CODEC = MapCodec.unit(ClayToolCastRecipe::new); // new instance per recipe: 26.3 recipes are registry values and must be distinct
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayToolCastRecipe> STREAM_CODEC = StreamCodec.of((buf, recipe) -> { }, buf -> new ClayToolCastRecipe());
    public static final RecipeSerializer<ClayToolCastRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
