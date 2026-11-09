package net.stirdrem.overgeared.recipe;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;
import net.minecraft.world.item.ItemStackTemplate;
import java.util.List;
import net.stirdrem.overgeared.recipe.ingredients.Ingredients;

import net.stirdrem.overgeared.recipe.ingredients.IngredientCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class FletchingRecipe implements OvergearedRecipe<RecipeInput> {
    private final Ingredient tip, shaft, feather, potion;
    private final ItemStackTemplate result;
    private final @Nullable ItemStackTemplate resultTipped;
    private final @Nullable ItemStackTemplate resultLingering;
    private final String tippedTag;
    private final String lingeringTag;

    public FletchingRecipe(Ingredient tip, Ingredient shaft, Ingredient feather, Ingredient potion,
                           ItemStackTemplate result, @Nullable ItemStackTemplate resultTipped, @Nullable ItemStackTemplate resultLingering,
                           String tippedTag, String lingeringTag) {
        this.tip = tip;
        this.shaft = shaft;
        this.feather = feather;
        this.potion = potion != null ? potion : Ingredients.EMPTY;
        this.result = result;
        this.resultTipped = resultTipped;
        this.resultLingering = resultLingering;
        this.tippedTag = tippedTag != null ? tippedTag : "Potion";
        this.lingeringTag = lingeringTag != null ? lingeringTag : "LingeringPotion";
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        // Tip, Shaft, Feather: STRICT - if ingredient is empty, the slot MUST be empty.
        boolean tipMatches = tip.isEmpty() ? recipeInput.getItem(0).isEmpty() : tip.test(recipeInput.getItem(0));
        boolean shaftMatches = shaft.isEmpty() ? recipeInput.getItem(1).isEmpty() : shaft.test(recipeInput.getItem(1));
        boolean featherMatches = feather.isEmpty() ? recipeInput.getItem(2).isEmpty() : feather.test(recipeInput.getItem(2));
        
        // Potion: LENIENT - if ingredient is empty, the slot is OPTIONAL (applied after crafting).
        // This allows potions to be used as optional modifiers without being part of the recipe match.
        boolean potionMatches = potion.isEmpty() || potion.test(recipeInput.getItem(3));
        
        return tipMatches && shaftMatches && featherMatches && potionMatches;
    }

    @Override
    public ItemStack assemble(RecipeInput container) {
        return getDefaultResult();
    }

    public Ingredient getTip() {
        return tip;
    }

    public Ingredient getShaft() {
        return shaft;
    }

    public Ingredient getFeather() {
        return feather;
    }

    public Ingredient getPotion() {
        return potion;
    } // <-- getter for potion

    /**
     * Specific potion stacks for recipe viewers. Ingredients can no longer hold item components, so
     * the viewer-only "tip an arrow with this potion" entries carry the exact potion here.
     */
    private List<ItemStack> potionDisplay = List.of();

    public FletchingRecipe withPotionDisplay(ItemStack... stacks) {
        this.potionDisplay = List.of(stacks);
        return this;
    }

    public List<ItemStack> getPotionDisplay() {
        return potionDisplay;
    }

    public boolean hasPotion() {
        return potion != null && !potion.isEmpty();
    }

    public ItemStack getDefaultResult() {
        return result.create();
    }

    public ItemStack getTippedResult() {
        return resultTipped == null ? ItemStack.EMPTY : resultTipped.create();
    }

    public ItemStack getLingeringResult() {
        return resultLingering == null ? ItemStack.EMPTY : resultLingering.create();
    }


    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return getDefaultResult();
    }

    @Override
    public RecipeSerializer<FletchingRecipe> getSerializer() {
        return ModRecipeSerializers.FLETCHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<FletchingRecipe> getType() {
        return ModRecipeTypes.FLETCHING.get();
    }

    public boolean hasTippedResult() {
        return resultTipped != null;
    }

    public boolean hasLingeringResult() {
        return resultLingering != null;
    }

    public String getTippedTag() {
        return tippedTag;
    }

    public String getLingeringTag() {
        return lingeringTag;
    }

    public static class Serializer {
        public static final MapCodec<FletchingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCodecs.LENIENT.optionalFieldOf("tip", Ingredients.EMPTY).forGetter(r -> r.tip),
                IngredientCodecs.LENIENT.optionalFieldOf("shaft", Ingredients.EMPTY).forGetter(r -> r.shaft),
                IngredientCodecs.LENIENT.optionalFieldOf("feather", Ingredients.EMPTY).forGetter(r -> r.feather),
                IngredientCodecs.LENIENT.optionalFieldOf("potion", Ingredients.EMPTY).forGetter(r -> r.potion),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                ItemStackTemplate.CODEC.optionalFieldOf("result_tipped").forGetter(r -> Optional.ofNullable(r.resultTipped)),
                ItemStackTemplate.CODEC.optionalFieldOf("result_lingering").forGetter(r -> Optional.ofNullable(r.resultLingering)),
                Codec.STRING.optionalFieldOf("tipped_tag", "Potion").forGetter(r -> r.tippedTag),
                Codec.STRING.optionalFieldOf("lingering_tag", "LingeringPotion").forGetter(r -> r.lingeringTag)
        ).apply(instance, (tip, shaft, feather, potion, result, tipped, lingering, tippedTag, lingeringTag) ->
                new FletchingRecipe(tip, shaft, feather, potion, result, tipped.orElse(null), lingering.orElse(null), tippedTag, lingeringTag)));

        public static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        private static FletchingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient tip = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient shaft = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient feather = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient potion = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            ItemStackTemplate resultTipped = ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).decode(buffer).orElse(null);
            ItemStackTemplate resultLingering = ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).decode(buffer).orElse(null);
            String tippedTag = buffer.readUtf();
            String lingeringTag = buffer.readUtf();

            return new FletchingRecipe(tip, shaft, feather, potion, result, resultTipped, resultLingering, tippedTag, lingeringTag);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, FletchingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tip);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.shaft);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.feather);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.potion);
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).encode(buffer, Optional.ofNullable(recipe.resultTipped));
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).encode(buffer, Optional.ofNullable(recipe.resultLingering));
            buffer.writeUtf(recipe.tippedTag);
            buffer.writeUtf(recipe.lingeringTag);
        }

        public MapCodec<FletchingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
