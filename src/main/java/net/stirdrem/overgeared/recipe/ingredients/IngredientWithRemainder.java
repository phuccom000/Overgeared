package net.stirdrem.overgeared.recipe.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientWithRemainder {
    public static final IngredientWithRemainder EMPTY = new IngredientWithRemainder(Ingredient.EMPTY, false, 0);
    private final Ingredient ingredient;
    private final boolean remainder;
    private final int durabilityDecrease;

    public IngredientWithRemainder(Ingredient ingredient, boolean remainder, int durabilityDecrease) {
        this.ingredient = ingredient;
        this.remainder = remainder;
        this.durabilityDecrease = durabilityDecrease;
    }

    public static final Codec<IngredientWithRemainder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(o -> o.ingredient),
            Codec.BOOL.optionalFieldOf("remainder", false).forGetter(o -> o.remainder),
            Codec.INT.optionalFieldOf("durability_decrease", 0).forGetter(o -> o.durabilityDecrease)
    ).apply(instance, IngredientWithRemainder::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithRemainder> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, o -> o.ingredient,
            ByteBufCodecs.BOOL, o -> o.remainder,
            ByteBufCodecs.INT, o -> o.durabilityDecrease,
            IngredientWithRemainder::new
    );

    public Ingredient getIngredient() {
        return ingredient;
    }

    public boolean hasRemainder() {
        return remainder;
    }

    public int getDurabilityDecrease() {
        return durabilityDecrease;
    }

    public ItemStack getRemainder(ItemStack original) {
        if (!remainder) return ItemStack.EMPTY;

        ItemStack stack = original.copy();
        stack.setCount(1);

        if (durabilityDecrease > 0 && stack.isDamageableItem()) {
            int newDamage = stack.getDamageValue() + durabilityDecrease;
            if (newDamage >= stack.getMaxDamage()) {
                return ItemStack.EMPTY;
            }
            stack.setDamageValue(newDamage);
        }

        return stack;
    }
}
