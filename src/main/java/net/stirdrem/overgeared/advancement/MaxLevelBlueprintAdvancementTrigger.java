package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.Overgeared;

public class MaxLevelBlueprintAdvancementTrigger extends SimpleCriterionTrigger<MaxLevelBlueprintAdvancementTrigger.Conditions> {

    public static final ResourceLocation ID = new ResourceLocation(Overgeared.MOD_ID, "max_level_blueprint");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Conditions createInstance(JsonObject json,
                                             ContextAwarePredicate playerPredicate,
                                             DeserializationContext context) {
        return new Conditions(playerPredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    // ---------------- Conditions ----------------

    public static class Conditions extends AbstractCriterionTriggerInstance {

        public Conditions(ContextAwarePredicate playerPredicate) {
            super(ID, playerPredicate);
        }

        public static Conditions instance() {
            return new Conditions(ContextAwarePredicate.ANY);
        }
    }
}
