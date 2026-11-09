package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

/**
 * {@code overgeared:crafting_cast}: a fired clay / nether tool cast (with a tool type in CAST_DATA) plus casting
 * materials -> the cast with the materials added to CAST_DATA (materials, amount, input stacks). Fails when the
 * total would exceed the tool type's configured max amount.
 */
public class DynamicToolCastRecipe extends CustomRecipe {
    public static final DynamicToolCastRecipe INSTANCE = new DynamicToolCastRecipe();

    public DynamicToolCastRecipe() {
    }

    private static boolean isCast(ItemStack stack) {
        return stack.is(ModItems.CLAY_TOOL_CAST) || stack.is(ModItems.NETHER_TOOL_CAST);
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        ItemStack cast = ItemStack.EMPTY;
        int existingAmount = 0;
        int addedAmount = 0;
        int maxAmount = 0;
        boolean foundMaterial = false;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (isCast(stack)) {
                if (!cast.isEmpty()) return false; // only one cast allowed
                CastData data = stack.get(ModComponents.CAST_DATA);
                if (data == null || data.toolType().isBlank()) return false;

                cast = stack;
                existingAmount = data.amount();
                maxAmount = ConfigHelper.getMaxMaterialAmount(data.toolType());
                continue;
            }

            String material = ConfigHelper.getMaterialForItem(stack);
            if (!material.equals("none")) {
                foundMaterial = true;
                addedAmount += ConfigHelper.getMaterialValue(stack);
                continue;
            }

            return false;
        }

        if (cast.isEmpty() || !foundMaterial) return false;

        return maxAmount <= 0 || existingAmount + addedAmount <= maxAmount;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;

        ItemStack cast = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isCast(stack)) {
                cast = stack.copyWithCount(1);
                break;
            }
        }
        if (cast.isEmpty()) return ItemStack.EMPTY;

        CastData data = cast.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);
        int maxAmount = ConfigHelper.getMaxMaterialAmount(data.toolType());

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || isCast(stack)) continue;
            String material = ConfigHelper.getMaterialForItem(stack);
            if (material.equals("none")) continue;
            data = data.withAddedMaterial(material, ConfigHelper.getMaterialValue(stack), stack);
        }

        if (maxAmount > 0 && data.amount() > maxAmount) return ItemStack.EMPTY;

        cast.set(ModComponents.CAST_DATA, data);
        return cast;
    }

    @Override
    public RecipeSerializer<DynamicToolCastRecipe> getSerializer() {
        return ModRecipes.CRAFTING_DYNAMIC_TOOL_CAST;
    }

    public static final MapCodec<DynamicToolCastRecipe> MAP_CODEC = MapCodec.unit(DynamicToolCastRecipe::new); // new instance per recipe: 26.3 recipes are registry values and must be distinct
    public static final StreamCodec<RegistryFriendlyByteBuf, DynamicToolCastRecipe> STREAM_CODEC = StreamCodec.of((buf, recipe) -> { }, buf -> new DynamicToolCastRecipe());
    public static final RecipeSerializer<DynamicToolCastRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
