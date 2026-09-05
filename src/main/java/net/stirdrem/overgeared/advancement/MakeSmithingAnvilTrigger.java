package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.Overgeared;

import org.jetbrains.annotations.Nullable;

public class MakeSmithingAnvilTrigger
        extends SimpleCriterionTrigger<MakeSmithingAnvilTrigger.Conditions> {

    public static final ResourceLocation ID = new ResourceLocation(Overgeared.MOD_ID, "make_smithing_anvil");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Conditions createInstance(
            JsonObject json,
            ContextAwarePredicate playerPredicate,
            DeserializationContext context
    ) {
        String tier = null;

        if (json.has("tier")) {
            tier = json.get("tier").getAsString();
        }

        return new Conditions(playerPredicate, tier);
    }

    public void trigger(ServerPlayer player, String tierUsed) {
        this.trigger(player, instance -> instance.matches(tierUsed));
    }

    // ---------------- Conditions ----------------

    public static class Conditions extends AbstractCriterionTriggerInstance {

        @Nullable
        private final String tier;

        public Conditions(ContextAwarePredicate playerPredicate,
                           @Nullable String tier) {
            super(ID, playerPredicate);
            this.tier = tier;
        }

        public boolean matches(String tierUsed) {
            // No condition = always match
            if (this.tier == null) {
                return true;
            }
            return this.tier.equals(tierUsed);
        }
    }
}
