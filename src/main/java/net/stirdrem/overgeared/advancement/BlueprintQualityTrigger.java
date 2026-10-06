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

public class BlueprintQualityTrigger
        extends SimpleCriterionTrigger<BlueprintQualityTrigger.Conditions> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "blueprint_quality");

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    /**
     * Call when a blueprint reaches a new quality
     */
    public void trigger(ServerPlayer player, String quality) {
        this.trigger(player, inst -> inst.matches(quality));
    }

    // ─────────────────────────────────────────────────────────────

    public record Conditions(Optional<Holder<LootItemCondition>> player,
                             Optional<String> quality) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(i -> i.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.STRING.optionalFieldOf("quality").forGetter(Conditions::quality)
        ).apply(i, Conditions::new));

        public boolean matches(String quality) {
            // No condition → always match
            return this.quality.isEmpty() || this.quality.get().equals(quality);
        }
    }
}
