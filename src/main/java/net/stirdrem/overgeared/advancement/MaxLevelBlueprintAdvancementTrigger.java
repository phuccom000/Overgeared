package net.stirdrem.overgeared.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.stirdrem.overgeared.Overgeared;

import java.util.Optional;

public class MaxLevelBlueprintAdvancementTrigger extends SimpleCriterionTrigger<MaxLevelBlueprintAdvancementTrigger.Conditions> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "max_level_blueprint");

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    // ---------------- Conditions ----------------

    public record Conditions(Optional<Holder<LootItemCondition>> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(i -> i.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player)
        ).apply(i, Conditions::new));

        public static Conditions instance() {
            return new Conditions(Optional.empty());
        }
    }
}
