package net.stirdrem.overgeared.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import net.stirdrem.overgeared.AnvilTier;

import java.util.List;

/**
 * KubeJS integration, listed in {@code kubejs.plugins.txt}. Adds recipe schemas for Overgeared's recipe types
 * ({@link OvergearedRecipeSchemas}) and the startup event {@code OvergearedEvents.anvilTiers}:
 * <pre>
 * // kubejs/startup_scripts/anvil_tiers.js
 * OvergearedEvents.anvilTiers(event => {
 *   event.add('mythril').aliases('above_c').hammerTag('overgeared:mythril_smithing_hammers')
 *   event.get('tier_a').enabled(true)
 *   event.get('stone').blueprint(true)
 *   event.remove('tier_b')
 * })
 * </pre>
 */
public class OvergearedKubeJSPlugin implements KubeJSPlugin {
    public static final EventGroup GROUP = EventGroup.of("OvergearedEvents");
    public static final EventHandler ANVIL_TIERS = GROUP.startup("anvilTiers", () -> AnvilTiersKubeEvent.class);

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(GROUP);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        OvergearedRecipeSchemas.registerComponents(registry);
    }

    /** {@code event.recipes.overgeared.<type>(...)} for each Overgeared recipe type; see {@link OvergearedRecipeSchemas}. */
    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        OvergearedRecipeSchemas.registerSchemas(registry);
    }

    /** Runs the {@code anvilTiers} startup scripts on the tiers read from config. */
    public static void modifyAnvilTiers(List<AnvilTier.Definition> tiers) {
        if (ANVIL_TIERS.hasListeners()) {
            ANVIL_TIERS.post(new AnvilTiersKubeEvent(tiers));
        }
    }
}
