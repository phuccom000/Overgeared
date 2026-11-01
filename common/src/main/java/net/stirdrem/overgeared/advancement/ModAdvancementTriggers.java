package net.stirdrem.overgeared.advancement;

import net.minecraft.advancements.CriterionTrigger;

import java.util.function.Consumer;

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

    /**
     * Registers all triggers. Vanilla's {@code CriteriaTriggers.register} isn't public on every loader,
     * so each loader passes its own: Forge {@code CriteriaTriggers::register}, Fabric {@code CriterionRegistry::register}.
     */
    public static void register(Consumer<CriterionTrigger<?>> registrar) {
        registrar.accept(MAKE_SMITHING_ANVIL);
        registrar.accept(KNAPPING);
        registrar.accept(FORGING_QUALITY);
        registrar.accept(BLUEPRINT_QUALITY);
        registrar.accept(MAX_LEVEL_BLUEPRINT);
    }
}
