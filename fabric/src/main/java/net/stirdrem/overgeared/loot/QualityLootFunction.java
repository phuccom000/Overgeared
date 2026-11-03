package net.stirdrem.overgeared.loot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.stirdrem.overgeared.util.LootQualityHelper;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.util.ModTags;

/**
 * Applied globally to every loot table via LootTableEvents.MODIFY (see ModLootModifiers) -
 * Fabric has no direct equivalent of Forge's global loot modifiers, but applying a LootFunction
 * to every table's builder runs it against every stack that table generates, which is the same
 * per-stack post-process semantics as the original QualityLootModifier.
 */
public class QualityLootFunction implements LootItemFunction {
    public static final QualityLootFunction INSTANCE = new QualityLootFunction();

    private static final LootItemFunctionType TYPE = new LootItemFunctionType(new Serializer<>() {
        @Override
        public void serialize(com.google.gson.JsonObject json, LootItemFunction object, com.google.gson.JsonSerializationContext context) {
        }

        @Override
        public LootItemFunction deserialize(com.google.gson.JsonObject json, com.google.gson.JsonDeserializationContext context) {
            return INSTANCE;
        }
    });

    @Override
    public ItemStack apply(ItemStack generated, LootContext context) {
        if (!Overgeared.getConfig().ENABLE_LOOT_QUALITY()) return generated;
        if (!Overgeared.getConfig().ENABLE_MOB_DROP_QUALITY() && isMobDrop(context)) return generated;
        if (!isEligibleItem(generated)) return generated;

        LootQualityHelper.applyRolled(generated, context.getRandom());
        return generated;
    }

    /** Mob death loot tables are the only ones that carry a damage source. */
    private static boolean isMobDrop(LootContext context) {
        return context.hasParam(LootContextParams.DAMAGE_SOURCE);
    }

    private static boolean isEligibleItem(ItemStack stack) {
        Item item = stack.getItem();

        if (!item.canBeDepleted()) return false;

        return !stack.is(ModTags.Items.QUALITY_BLACKLIST);
    }

    @Override
    public LootItemFunctionType getType() {
        return TYPE;
    }
}
