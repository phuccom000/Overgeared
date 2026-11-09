package net.stirdrem.overgeared.recipe.ingredients;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Replacements for Ingredient helpers removed in 1.21.2. */
public final class Ingredients {
    /** An item tag with no values (data/overgeared/tags/item/empty.json). */
    public static final TagKey<Item> EMPTY_TAG = TagKey.create(Registries.ITEM, OvergearedMod.loc("empty"));

    /**
     * Stands in for the removed {@code Ingredient.EMPTY}: matches nothing and {@link Ingredient#isEmpty()}
     * is true. It is written as the empty tag {@code #overgeared:empty}, so it survives JSON and network
     * round trips (where it comes back as that real, empty tag).
     */
    public static final Ingredient EMPTY = Ingredient.of(new EmptyHolderSet());

    private Ingredients() {
    }

    /** One stack of each item the ingredient accepts (what {@code Ingredient#getItems()} used to return). */
    public static List<ItemStack> stacks(Ingredient ingredient) {
        if (ingredient.isEmpty()) return List.of();
        return ingredient.items().map(holder -> new ItemStack(holder)).toList();
    }

    public static ItemStack[] stackArray(Ingredient ingredient) {
        return stacks(ingredient).toArray(ItemStack[]::new);
    }

    private static final class EmptyHolderSet implements HolderSet<Item> {
        @Override
        public Stream<Holder<Item>> stream() {
            return Stream.empty();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public Either<TagKey<Item>, List<Holder<Item>>> unwrap() {
            return Either.left(EMPTY_TAG);
        }

        @Override
        public Optional<Holder<Item>> getRandomElement(RandomSource random) {
            return Optional.empty();
        }

        @Override
        public Holder<Item> get(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public boolean contains(Holder<Item> value) {
            return false;
        }

        @Override
        public boolean canSerializeIn(HolderOwner<Item> owner) {
            return true;
        }

        @Override
        public Optional<TagKey<Item>> unwrapKey() {
            return Optional.of(EMPTY_TAG);
        }

        @Override
        public Iterator<Holder<Item>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public String toString() {
            return "EmptySet(" + EMPTY_TAG + ")";
        }
    }
}
