package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.stirdrem.overgeared.Overgeared;

import org.jetbrains.annotations.Nullable;

public class BlueprintQualityTrigger
        extends AbstractCriterion<BlueprintQualityTrigger.Conditions> {

    public static final Identifier ID = new Identifier(Overgeared.MOD_ID, "blueprint_quality");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(
            JsonObject json,
            LootContextPredicate playerPredicate,
            AdvancementEntityPredicateDeserializer context
    ) {
        @Nullable String quality = null;

        if (JsonHelper.hasString(json, "quality")) {
            quality = JsonHelper.getString(json, "quality");
        }

        return new Conditions(playerPredicate, quality);
    }

    /**
     * Call when forging completes
     */
    public void trigger(ServerPlayerEntity player, String forgedQuality) {
        this.trigger(player, inst -> inst.matches(forgedQuality));
    }

    // ─────────────────────────────────────────────────────────────

    public static class Conditions extends AbstractCriterionConditions {

        @Nullable
        private final String requiredQuality;

        public Conditions(LootContextPredicate playerPredicate,
                           @Nullable String requiredQuality) {
            super(ID, playerPredicate);
            this.requiredQuality = requiredQuality;
        }

        public boolean matches(String forgedQuality) {
            // No condition → always match
            if (this.requiredQuality == null) {
                return true;
            }
            return this.requiredQuality.equals(forgedQuality);
        }
    }
}
