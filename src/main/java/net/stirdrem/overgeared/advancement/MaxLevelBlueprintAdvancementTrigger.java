package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;

public class MaxLevelBlueprintAdvancementTrigger extends AbstractCriterion<MaxLevelBlueprintAdvancementTrigger.Conditions> {

    public static final Identifier ID = new Identifier(Overgeared.MOD_ID, "max_level_blueprint");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json,
                                             LootContextPredicate playerPredicate,
                                             AdvancementEntityPredicateDeserializer context) {
        return new Conditions(playerPredicate);
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, instance -> true);
    }

    // ---------------- Conditions ----------------

    public static class Conditions extends AbstractCriterionConditions {

        public Conditions(LootContextPredicate playerPredicate) {
            super(ID, playerPredicate);
        }

        public static Conditions instance() {
            return new Conditions(LootContextPredicate.EMPTY);
        }
    }
}
