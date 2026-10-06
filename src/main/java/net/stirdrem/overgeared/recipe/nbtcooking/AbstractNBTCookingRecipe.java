package net.stirdrem.overgeared.recipe.nbtcooking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.stirdrem.overgeared.recipe.CookingCodecs;
import net.stirdrem.overgeared.recipe.RecipeCodecs;

/**
 * Shared contract of the {@code overgeared:nbt_add_*} cooking recipes (vanilla smelting / blasting / campfire
 * recipes that add data to their result).
 * <p>
 * 26.3 port: was an abstract class extending AbstractCookingRecipe; vanilla cooking classes now have their own
 * per-type subclasses, so each NBT recipe extends the matching vanilla class and implements this interface.
 * <p>
 * JSON: vanilla cooking fields ({@code cookingtime} defaults to 200) plus optional {@code nbt}: a 1.20.1 NBT
 * object whose known Overgeared keys (Heated, Polished, ForgingQuality, Creator, ...) are converted to data
 * components (unknown keys go to {@code minecraft:custom_data}). New datapacks can instead put
 * {@code "components"} directly in {@code result}.
 */
public interface AbstractNBTCookingRecipe extends CookingCodecs.ResultAccess {

    /** The raw legacy NBT from the JSON (possibly empty). */
    CompoundTag getResultTag();

    /** {@link #getResultTag()} converted to components; applied on top of the result in assemble. */
    default DataComponentPatch getResultPatch() {
        return RecipeCodecs.legacyNbtToPatch(getResultTag());
    }

    static ItemStack applyResultTag(ItemStackTemplate result, CompoundTag tag) {
        ItemStack out = result.create();
        DataComponentPatch patch = RecipeCodecs.legacyNbtToPatch(tag);
        if (!patch.isEmpty()) out.applyComponents(patch);
        return out;
    }

    @FunctionalInterface
    interface Factory<T> {
        T create(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient,
                 ItemStackTemplate result, float experience, int cookingTime, CompoundTag tag);
    }

    static <T extends AbstractCookingRecipe & AbstractNBTCookingRecipe> MapCodec<T> mapCodec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(o -> new Recipe.CommonInfo(o.showNotification())),
                AbstractCookingRecipe.CookingBookInfo.MAP_CODEC.forGetter(o -> new AbstractCookingRecipe.CookingBookInfo(o.category(), o.group())),
                RecipeCodecs.INGREDIENT.fieldOf("ingredient").forGetter(AbstractCookingRecipe::input),
                RecipeCodecs.RESULT.fieldOf("result").forGetter(CookingCodecs.ResultAccess::resultTemplate),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(AbstractCookingRecipe::experience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(AbstractCookingRecipe::cookingTime),
                RecipeCodecs.legacyNbtField("nbt").forGetter(AbstractNBTCookingRecipe::getResultTag)
        ).apply(i, factory::create));
    }

    static <T extends AbstractCookingRecipe & AbstractNBTCookingRecipe> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                Recipe.CommonInfo.STREAM_CODEC, o -> new Recipe.CommonInfo(o.showNotification()),
                AbstractCookingRecipe.CookingBookInfo.STREAM_CODEC, o -> new AbstractCookingRecipe.CookingBookInfo(o.category(), o.group()),
                Ingredient.CONTENTS_STREAM_CODEC, AbstractCookingRecipe::input,
                ItemStackTemplate.STREAM_CODEC, CookingCodecs.ResultAccess::resultTemplate,
                ByteBufCodecs.FLOAT, AbstractCookingRecipe::experience,
                ByteBufCodecs.VAR_INT, AbstractCookingRecipe::cookingTime,
                ByteBufCodecs.COMPOUND_TAG, AbstractNBTCookingRecipe::getResultTag,
                factory::create);
    }
}
