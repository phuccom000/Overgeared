package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared logic of the shapeless alloy smelter recipes. Input: {@link ItemListInput} whose first
 * {@code slotCount} slots are the alloy input slots (empties ignored, order irrelevant).
 * <p>
 * JSON: {@code ingredients} (list, at most {@code slotCount}), {@code result}, optional {@code group},
 * {@code category} (crafting book category), {@code experience} (0), {@code cookingtime} (200).
 */
public abstract class AbstractShapelessAlloyRecipe implements Recipe<ItemListInput> {
    protected final String group;
    protected final CraftingBookCategory category;
    protected final List<Ingredient> inputs;
    protected final ItemStackTemplate output;
    protected final float experience;
    protected final int cookingTime;
    private final int slotCount;
    private PlacementInfo placementInfo;

    protected AbstractShapelessAlloyRecipe(int slotCount, String group, CraftingBookCategory category, List<Ingredient> inputs,
                                           ItemStackTemplate output, float experience, int cookingTime) {
        this.slotCount = slotCount;
        this.group = group;
        this.category = category;
        this.inputs = List.copyOf(inputs);
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(ItemListInput inv, Level world) {
        // Don't bother checking on client side for performance reasons
        if (world.isClientSide()) return false;

        List<Ingredient> remainingIngredients = new ArrayList<>(inputs);
        List<ItemStack> remainingItems = new ArrayList<>();

        for (int i = 0; i < slotCount; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                remainingItems.add(stack);
            }
        }

        if (remainingItems.size() != remainingIngredients.size()) {
            return false;
        }

        for (ItemStack stack : remainingItems) {
            boolean matched = false;
            for (int i = 0; i < remainingIngredients.size(); i++) {
                if (remainingIngredients.get(i).test(stack)) {
                    remainingIngredients.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        return remainingIngredients.isEmpty();
    }

    @Override
    public ItemStack assemble(ItemListInput container) {
        return output.create();
    }

    public ItemStack getResultItem() {
        return output.create();
    }

    public ItemStackTemplate result() {
        return output;
    }

    @Override
    public String group() {
        return group;
    }

    /** @deprecated 1.20.1 name; use {@link #group()}. */
    @Deprecated
    public String getGroup() {
        return group;
    }

    public CraftingBookCategory category() {
        return category;
    }

    public float getExperience() {
        return experience;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public boolean isShaped() {
        return false;
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    /** The raw ingredient list (all present). */
    public List<Ingredient> getInputs() {
        return inputs;
    }

    public List<Optional<Ingredient>> getIngredientsList() {
        return inputs.stream().map(Optional::of).toList();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.create(inputs);
        }
        return placementInfo;
    }

    @FunctionalInterface
    public interface Factory<T extends AbstractShapelessAlloyRecipe> {
        T create(String group, CraftingBookCategory category, List<Ingredient> inputs, ItemStackTemplate output, float experience, int cookingTime);
    }

    public static <T extends AbstractShapelessAlloyRecipe> MapCodec<T> mapCodec(int maxIngredients, Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(r -> r.category),
                RecipeCodecs.INGREDIENT.listOf(1, maxIngredients).fieldOf("ingredients").forGetter(r -> r.inputs),
                RecipeCodecs.RESULT.fieldOf("result").forGetter(r -> r.output),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(r -> r.cookingTime)
        ).apply(i, factory::create));
    }

    public static <T extends AbstractShapelessAlloyRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                CraftingBookCategory.STREAM_CODEC, r -> r.category,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.inputs,
                ItemStackTemplate.STREAM_CODEC, r -> r.output,
                ByteBufCodecs.FLOAT, r -> r.experience,
                ByteBufCodecs.VAR_INT, r -> r.cookingTime,
                factory::create);
    }
}
