package net.stirdrem.overgeared.loot;
import net.minecraft.core.Holder;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddItemModifier extends LootModifier {
    public static final MapCodec<AddItemModifier> CODEC = RecordCodecBuilder.mapCodec((i) -> codecStart(i).and(i.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(AddItemModifier::getItem),
            Codec.INT.fieldOf("count").forGetter(AddItemModifier::getCount)
    )).apply(i, AddItemModifier::new));

    private final Item item;
    private final int count;

    public AddItemModifier(Optional<Holder<LootItemCondition>> condition, int priority, Item item, int count) {
        super(condition, priority);
        this.item = item;
        this.count = count;
    }

    public AddItemModifier(Optional<Holder<LootItemCondition>> condition, int priority, Item item) {
        super(condition, priority);
        this.item = item;
        this.count = 1;
    }

    @Override
    public MapCodec<AddItemModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        loot.add(new ItemStack(item, count));
        return loot;
    }

    // ALI needs to read these three
    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public Optional<Holder<LootItemCondition>> getCondition() {
        return condition;
    }
}