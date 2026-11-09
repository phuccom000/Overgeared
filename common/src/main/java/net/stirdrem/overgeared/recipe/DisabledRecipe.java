package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * A recipe that never matches, used to switch off vanilla recipes by overriding their id
 * ({@code {"type": "overgeared:disabled"}}).
 * <p>
 * Recipes are a registry since 26.x and vanilla's recipe advancements refer to them, so removing a
 * vanilla recipe with a {@code false} load condition leaves those references unbound and the data
 * pack fails to load. Replacing it with this keeps the id bound without making anything craftable.
 */
public class DisabledRecipe extends CustomRecipe {
    // A new instance per recipe: registry entries must be distinct objects
    public static final MapCodec<DisabledRecipe> CODEC = MapCodec.unit(DisabledRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, DisabledRecipe> STREAM_CODEC =
            StreamCodec.of((buf, recipe) -> {
            }, buf -> new DisabledRecipe());

    public DisabledRecipe() {
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<DisabledRecipe> getSerializer() {
        return ModRecipeSerializers.DISABLED.get();
    }
}
