package net.stirdrem.overgeared.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ForgingQualityTrigger extends SimpleCriterionTrigger<ForgingQualityTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    /**
     * Call when forging completes
     */
    public void trigger(ServerPlayer player, String forgedQuality) {
        this.trigger(player, inst -> inst.matches(forgedQuality));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<String> requiredQuality)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("quality").forGetter(TriggerInstance::requiredQuality))
                .apply(instance, TriggerInstance::new));

        public boolean matches(String forgedQuality) {
            // No condition → always match
            if (requiredQuality.isEmpty()) return true;
            return requiredQuality.get().equals(forgedQuality);
        }
    }
}
