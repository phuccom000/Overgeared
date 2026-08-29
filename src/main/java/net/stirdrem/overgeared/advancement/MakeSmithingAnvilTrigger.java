package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;

import org.jetbrains.annotations.Nullable;

public class MakeSmithingAnvilTrigger
        extends AbstractCriterion<MakeSmithingAnvilTrigger.Conditions> {

    public static final Identifier ID = new Identifier(Overgeared.MOD_ID, "make_smithing_anvil");

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
        String tier = null;

        if (json.has("tier")) {
            tier = json.get("tier").getAsString();
        }

        return new Conditions(playerPredicate, tier);
    }

    public void trigger(ServerPlayerEntity player, String tierUsed) {
        this.trigger(player, instance -> instance.matches(tierUsed));
    }

    // ---------------- Conditions ----------------

    public static class Conditions extends AbstractCriterionConditions {

        @Nullable
        private final String tier;

        public Conditions(LootContextPredicate playerPredicate,
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
