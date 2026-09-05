package net.stirdrem.overgeared.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilScreen;

/** Client-side half of the anvil's Polymorph integration - only touched when Polymorph is loaded. */
public final class PolymorphClientIntegration {
    private PolymorphClientIntegration() {
    }

    public static void register() {
        PolymorphApi.client().registerWidget(containerScreen ->
                containerScreen instanceof AbstractSmithingAnvilScreen<?>
                        ? new AnvilRecipesWidget(containerScreen)
                        : null);
    }
}
