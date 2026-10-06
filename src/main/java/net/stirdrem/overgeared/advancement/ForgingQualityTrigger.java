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

public class ForgingQualityTrigger
        extends SimpleCriterionTrigger<ForgingQualityTrigger.Conditions> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "forging_quality");

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    /**
     * Call when forging completes
     */
    public void trigger(ServerPlayer player, String forgedQuality) {
        this.trigger(player, inst -> inst.matches(forgedQuality));
    }

    // ─────────────────────────────────────────────────────────────

    public record Conditions(Optional<Holder<LootItemCondition>> player,
                             Optional<String> quality) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(i -> i.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.STRING.optionalFieldOf("quality").forGetter(Conditions::quality)
        ).apply(i, Conditions::new));

        public boolean matches(String forgedQuality) {
            // No condition → always match
            return this.quality.isEmpty() || this.quality.get().equals(forgedQuality);
        }
    }
}
