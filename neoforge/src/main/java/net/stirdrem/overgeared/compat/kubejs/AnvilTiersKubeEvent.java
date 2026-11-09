package net.stirdrem.overgeared.compat.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.Identifier;
import net.stirdrem.overgeared.AnvilTier;

import java.util.List;

/**
 * {@code OvergearedEvents.anvilTiers}: edits the smithing anvil tiers before their blocks are registered.
 * Tiers are ordered lowest first; {@link #add} appends a new highest tier.
 */
public class AnvilTiersKubeEvent implements KubeEvent {
    private final List<AnvilTier.Definition> tiers;

    public AnvilTiersKubeEvent(List<AnvilTier.Definition> tiers) {
        this.tiers = tiers;
    }

    /** Adds a tier above all current ones, or returns the existing tier with that id. */
    public TierBuilder add(String id) {
        TierBuilder existing = get(id);
        if (existing != null) return existing;
        AnvilTier.Definition def = new AnvilTier.Definition(id);
        tiers.add(def);
        return new TierBuilder(def);
    }

    /** The tier with this id, or null. */
    public TierBuilder get(String id) {
        for (AnvilTier.Definition def : tiers) {
            if (def.id.equals(id)) return new TierBuilder(def);
        }
        return null;
    }

    /**
     * Removes a tier. Its block will no longer exist, so worlds lose any placed anvils of it.
     * Stone and iron are always added back; disable them with {@code enabled(false)} instead.
     */
    public boolean remove(String id) {
        return tiers.removeIf(def -> def.id.equals(id));
    }

    public List<String> getIds() {
        return tiers.stream().map(def -> def.id).toList();
    }

    public static class TierBuilder {
        private final AnvilTier.Definition def;

        TierBuilder(AnvilTier.Definition def) {
            this.def = def;
        }

        /** Extra names recipes may use in their {@code tier} field. */
        public TierBuilder aliases(String... aliases) {
            def.aliases.addAll(List.of(aliases));
            return this;
        }

        public TierBuilder enabled(boolean enabled) {
            def.enabled = enabled;
            return this;
        }

        /** Whether the anvil has a blueprint slot. */
        public TierBuilder blueprint(boolean blueprint) {
            def.blueprint = blueprint;
            return this;
        }

        public TierBuilder hammerTag(String tag) {
            def.hammerTag = Identifier.parse(tag);
            return this;
        }

        public TierBuilder baseTag(String tag) {
            def.baseTag = Identifier.parse(tag);
            return this;
        }
    }
}
