package net.stirdrem.overgeared.advancement;

import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;

/**
 * Vanilla's Criteria.register is private - Criteria only self-registers vanilla triggers in its
 * static initializer. Fabric API's CriterionRegistry is the supported entry point for mods to
 * register their own Criterion implementations.
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
        CriterionRegistry.register(MAKE_SMITHING_ANVIL);
        CriterionRegistry.register(KNAPPING);
        CriterionRegistry.register(FORGING_QUALITY);
        CriterionRegistry.register(BLUEPRINT_QUALITY);
        CriterionRegistry.register(MAX_LEVEL_BLUEPRINT);
    }
}
