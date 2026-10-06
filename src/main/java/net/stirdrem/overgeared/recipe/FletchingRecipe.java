package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Fletching station recipe. Input: {@link ItemListInput} with slot 0 = tip, 1 = shaft, 2 = feather,
 * 3 = potion. An absent ingredient accepts anything in its slot (1.20.1 behaviour).
 * <p>
 * JSON: {@code material: {tip, shaft, feather}} (each optional), optional {@code potion}, {@code result},
 * optional {@code result_tipped} / {@code result_lingering} (item stack objects that may also carry the legacy
 * {@code "tag"} string).
 */
public class FletchingRecipe implements Recipe<ItemListInput> {
    public static final String DEFAULT_TIPPED_TAG = "Potion";
    public static final String DEFAULT_LINGERING_TAG = "LingeringPotion";

    private final Materials materials;
    private final Optional<Ingredient> potion;
    private final ItemStackTemplate result;
    private final Optional<TaggedResult> resultTipped;
    private final Optional<TaggedResult> resultLingering;
    private final String group;
    private PlacementInfo placementInfo;

    public FletchingRecipe(String group, Materials materials, Optional<Ingredient> potion, ItemStackTemplate result,
                           Optional<TaggedResult> resultTipped, Optional<TaggedResult> resultLingering) {
        this.group = group;
        this.materials = materials;
        this.potion = potion;
        this.result = result;
        this.resultTipped = resultTipped;
        this.resultLingering = resultLingering;
    }

    public FletchingRecipe(String group, Optional<Ingredient> tip, Optional<Ingredient> shaft, Optional<Ingredient> feather,
                           Optional<Ingredient> potion, ItemStackTemplate result,
                           Optional<TaggedResult> resultTipped, Optional<TaggedResult> resultLingering) {
        this(group, new Materials(tip, shaft, feather), potion, result, resultTipped, resultLingering);
    }

    private static boolean test(Optional<Ingredient> ingredient, ItemStack stack) {
        return ingredient.map(i -> i.test(stack)).orElse(true);
    }

    @Override
    public boolean matches(ItemListInput inventory, Level world) {
        return test(materials.tip(), inventory.getItem(0))
                && test(materials.shaft(), inventory.getItem(1))
                && test(materials.feather(), inventory.getItem(2))
                && test(potion, inventory.getItem(3));
    }

    @Override
    public ItemStack assemble(ItemListInput inventory) {
        return getDefaultResult();
    }

    public Optional<Ingredient> getTip() {
        return materials.tip();
    }

    public Optional<Ingredient> getShaft() {
        return materials.shaft();
    }

    public Optional<Ingredient> getFeather() {
        return materials.feather();
    }

    public Optional<Ingredient> getPotion() {
        return potion;
    }

    public boolean hasPotion() {
        return potion.isPresent();
    }

    public ItemStack getDefaultResult() {
        return result.create();
    }

    /** Copy of the tipped-arrow result, or EMPTY. */
    public ItemStack getTippedResult() {
        return resultTipped.map(r -> r.result().create()).orElse(ItemStack.EMPTY);
    }

    /** Copy of the lingering-arrow result, or EMPTY. */
    public ItemStack getLingeringResult() {
        return resultLingering.map(r -> r.result().create()).orElse(ItemStack.EMPTY);
    }

    public ItemStack getResultItem() {
        return getDefaultResult();
    }

    /** @deprecated use {@link #getResultItem()} / {@link #getDefaultResult()}. */
    @Deprecated
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getDefaultResult();
    }

    public boolean hasTippedResult() {
        return resultTipped.isPresent();
    }

    public boolean hasLingeringResult() {
        return resultLingering.isPresent();
    }

    /**
     * 1.20.1 NBT key the potion was stored under. Kept for datapack compatibility only - in 26.3 the potion is
     * stored in {@code minecraft:potion_contents}.
     */
    public String getTippedTag() {
        return resultTipped.flatMap(TaggedResult::tag).orElse(DEFAULT_TIPPED_TAG);
    }

    /** See {@link #getTippedTag()}; lingering arrows use {@code overgeared:lingering_status} in 26.3. */
    public String getLingeringTag() {
        return resultLingering.flatMap(TaggedResult::tag).orElse(DEFAULT_LINGERING_TAG);
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public RecipeSerializer<FletchingRecipe> getSerializer() {
        return ModRecipes.FLETCHING_SERIALIZER;
    }

    @Override
    public RecipeType<FletchingRecipe> getType() {
        return ModRecipeTypes.FLETCHING;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.createFromOptionals(List.of(materials.tip(), materials.shaft(), materials.feather(), potion));
        }
        return placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.FLETCHING;
    }

    public static class Type implements RecipeType<FletchingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "fletching";

        @Override
        public String toString() {
            return ID;
        }
    }

    // ------------------------------------------------------------------------------------------
    // serialization
    // ------------------------------------------------------------------------------------------

    public record Materials(Optional<Ingredient> tip, Optional<Ingredient> shaft, Optional<Ingredient> feather) {
        public static final Materials EMPTY = new Materials(Optional.empty(), Optional.empty(), Optional.empty());
        public static final Codec<Materials> CODEC = RecordCodecBuilder.create(i -> i.group(
                RecipeCodecs.INGREDIENT.optionalFieldOf("tip").forGetter(Materials::tip),
                RecipeCodecs.INGREDIENT.optionalFieldOf("shaft").forGetter(Materials::shaft),
                RecipeCodecs.INGREDIENT.optionalFieldOf("feather").forGetter(Materials::feather)
        ).apply(i, Materials::new));
    }

    /** A result stack plus the optional legacy {@code "tag"} key name. */
    public record TaggedResult(ItemStackTemplate result, Optional<String> tag) {
        private static final Codec<TaggedResult> OBJECT_CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStackTemplate.MAP_CODEC.forGetter(TaggedResult::result),
                Codec.STRING.optionalFieldOf("tag").forGetter(TaggedResult::tag)
        ).apply(i, TaggedResult::new));

        public static final Codec<TaggedResult> CODEC = Codec.withAlternative(OBJECT_CODEC,
                RecipeCodecs.RESULT.xmap(t -> new TaggedResult(t, Optional.empty()), TaggedResult::result));

        public static TaggedResult of(ItemStackTemplate result) {
            return new TaggedResult(result, Optional.empty());
        }
    }

    public static final MapCodec<FletchingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
            Materials.CODEC.optionalFieldOf("material", Materials.EMPTY).forGetter(r -> r.materials),
            RecipeCodecs.INGREDIENT.optionalFieldOf("potion").forGetter(r -> r.potion),
            RecipeCodecs.RESULT.fieldOf("result").forGetter(r -> r.result),
            TaggedResult.CODEC.optionalFieldOf("result_tipped").forGetter(r -> r.resultTipped),
            TaggedResult.CODEC.optionalFieldOf("result_lingering").forGetter(r -> r.resultLingering)
    ).apply(i, FletchingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(MAP_CODEC.codec());

    public static final RecipeSerializer<FletchingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
