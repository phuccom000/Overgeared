package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.stirdrem.overgeared.Overgeared;

import org.jetbrains.annotations.Nullable;

public class BlueprintQualityTrigger
        extends SimpleCriterionTrigger<BlueprintQualityTrigger.Conditions> {

    public static final ResourceLocation ID = new ResourceLocation(Overgeared.MOD_ID, "blueprint_quality");

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
        @Nullable String quality = null;

        if (GsonHelper.isStringValue(json, "quality")) {
            quality = GsonHelper.getAsString(json, "quality");
        }

        return new Conditions(playerPredicate, quality);
    }

    /**
     * Call when forging completes
     */
    public void trigger(ServerPlayer player, String forgedQuality) {
        this.trigger(player, inst -> inst.matches(forgedQuality));
    }

    // ─────────────────────────────────────────────────────────────

    public static class Conditions extends AbstractCriterionTriggerInstance {

        @Nullable
        private final String requiredQuality;

        public Conditions(ContextAwarePredicate playerPredicate,
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
