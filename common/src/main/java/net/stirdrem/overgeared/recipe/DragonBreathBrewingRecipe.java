package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.stirdrem.overgeared.config.ServerConfig;

/**
 * A brewing recipe that only works while {@code ENABLE_DRAGON_BREATH_RECIPE} is on (thick potion +
 * chorus fruit = dragon's breath, data/overgeared/recipe/dragon_breath_from_brewing.json).
 * Brewing is data driven since 26.x, which replaces the loader-specific brewing hooks.
 */
public class DragonBreathBrewingRecipe extends BrewingRecipe {
    public static final MapCodec<DragonBreathBrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PotionIngredient.MAP_CODEC.fieldOf("input").forGetter(BrewingRecipe::getInput),
            PotionIngredient.MAP_CODEC.fieldOf("reagent").forGetter(BrewingRecipe::getReagent),
            ItemStackTemplate.CODEC.fieldOf("output").forGetter(BrewingRecipe::getOutput)
    ).apply(i, DragonBreathBrewingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DragonBreathBrewingRecipe> STREAM_CODEC = StreamCodec.composite(
            PotionIngredient.STREAM_CODEC, BrewingRecipe::getInput,
            PotionIngredient.STREAM_CODEC, BrewingRecipe::getReagent,
            ItemStackTemplate.STREAM_CODEC, BrewingRecipe::getOutput,
            DragonBreathBrewingRecipe::new
    );

    public DragonBreathBrewingRecipe(PotionIngredient input, PotionIngredient reagent, ItemStackTemplate output) {
        super(input, reagent, output);
    }

    @Override
    public boolean matches(BrewingInput input) {
        return ServerConfig.SERVER_CONFIG.isLoaded() && ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get() && super.matches(input);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<BrewingRecipe> getSerializer() {
        return (RecipeSerializer<BrewingRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.DRAGON_BREATH_BREWING.get();
    }
}
