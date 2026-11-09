package net.stirdrem.overgeared.advancement;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Custom criterion triggers, registered into {@link BuiltInRegistries#TRIGGER_TYPES}
 * (vanilla's CriteriaTriggers.register is private).
 */
public class ModAdvancementTriggers {

    public static final MakeSmithingAnvilTrigger MAKE_SMITHING_ANVIL =
            new MakeSmithingAnvilTrigger();
    public static final KnappingAdvancementTrigger KNAPPING =
            new KnappingAdvancementTrigger();
    public static final ForgingQualityTrigger FORGING_QUALITY =
            new ForgingQualityTrigger();
    public static final BlueprintQualityTrigger BLUEPRINT_QUALITY =
            new BlueprintQualityTrigger();
    public static final MaxLevelBlueprintAdvancementTrigger MAX_LEVEL_BLUEPRINT =
            new MaxLevelBlueprintAdvancementTrigger();

    public static void register() {
        register(MakeSmithingAnvilTrigger.ID, MAKE_SMITHING_ANVIL);
        register(KnappingAdvancementTrigger.ID, KNAPPING);
        register(ForgingQualityTrigger.ID, FORGING_QUALITY);
        register(BlueprintQualityTrigger.ID, BLUEPRINT_QUALITY);
        register(MaxLevelBlueprintAdvancementTrigger.ID, MAX_LEVEL_BLUEPRINT);
    }

    private static void register(Identifier id, CriterionTrigger<?> trigger) {
        Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger);
    }
}
