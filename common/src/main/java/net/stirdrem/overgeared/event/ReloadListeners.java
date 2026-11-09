package net.stirdrem.overgeared.event;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * The server data reload listeners of the mod, for the loaders to register.
 */
public final class ReloadListeners {
    private ReloadListeners() {
    }

    public record Entry(String name, Supplier<PreparableReloadListener> factory) {
        public net.minecraft.resources.Identifier id() {
            return OvergearedMod.loc(name);
        }
    }

    public static final List<Entry> SERVER_DATA = List.of(
            new Entry("blueprint_tooltypes", BlueprintTooltypesReloadListener::new),
            new Entry("grinding_blacklist", GrindingBlacklistReloadListener::new),
            new Entry("durability_blacklist", DurabilityBlacklistReloadListener::new),
            new Entry("casting_tool_types", CastingToolTypesReloadListener::new),
            new Entry("material_settings", MaterialSettingsReloadListener::new),
            new Entry("knapping_resources", KnappingResourceReloadListener::new),
            new Entry("rock_interactions", RockInteractionReloadListener::new),
            new Entry("quality_attributes", QualityAttributeReloadListener::new)
    );
}
