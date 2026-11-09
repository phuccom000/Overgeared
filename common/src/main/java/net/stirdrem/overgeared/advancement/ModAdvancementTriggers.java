package net.stirdrem.overgeared.advancement;

import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.stirdrem.overgeared.registry.DeferredHolder;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;

public class ModAdvancementTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister
            .create(Registries.TRIGGER_TYPE, OvergearedMod.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, MakeSmithingAnvilTrigger> MAKE_SMITHING_ANVIL = TRIGGERS
            .register("make_smithing_anvil", MakeSmithingAnvilTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, KnappingAdvancementTrigger> KNAPPING = TRIGGERS
            .register("finished_knapping", KnappingAdvancementTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, ForgingQualityTrigger> FORGING_QUALITY = TRIGGERS
            .register("forging_quality", ForgingQualityTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, BlueprintQualityTrigger> BLUEPRINT_QUALITY = TRIGGERS
            .register("blueprint_quality", BlueprintQualityTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MaxLevelBlueprintAdvancementTrigger> MAX_LEVEL_BLUEPRINT = TRIGGERS
            .register("max_level_blueprint", MaxLevelBlueprintAdvancementTrigger::new);

    public static void register() {
        TRIGGERS.register();
    }
}
