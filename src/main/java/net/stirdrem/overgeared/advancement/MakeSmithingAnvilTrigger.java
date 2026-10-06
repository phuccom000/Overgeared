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

public class MakeSmithingAnvilTrigger
        extends SimpleCriterionTrigger<MakeSmithingAnvilTrigger.Conditions> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "make_smithing_anvil");

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, String tierUsed) {
        this.trigger(player, instance -> instance.matches(tierUsed));
    }

    // ---------------- Conditions ----------------

    public record Conditions(Optional<Holder<LootItemCondition>> player,
                             Optional<String> tier) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(i -> i.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.STRING.optionalFieldOf("tier").forGetter(Conditions::tier)
        ).apply(i, Conditions::new));

        public boolean matches(String tierUsed) {
            // No condition = always match
            return this.tier.isEmpty() || this.tier.get().equals(tierUsed);
        }
    }
}
