package net.stirdrem.overgeared;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A smithing anvil tier, read from {@code config/overgeared-anvil-tiers.json} (and KubeJS on NeoForge), lowest first.
 * Each tier gets its own {@code overgeared:<id>_smithing_anvil} block. The {@code stone} and {@code iron} tiers
 * always exist; stone keeps its own fragile anvil block.
 */
public final class AnvilTier implements StringRepresentable {
    public static final String STONE_ID = "stone";
    public static final String IRON_ID = "iron";

    private static final Map<String, AnvilTier> BY_NAME = new LinkedHashMap<>();
    private static List<AnvilTier> all = List.of();
    private static boolean frozen;

    private final String id;
    private final int order;
    private final List<String> aliases;
    private final boolean enabled;
    private final TagKey<Item> hammerTag;
    private final TagKey<Block> baseTag;
    private final boolean blueprint;

    private AnvilTier(String id, int order, List<String> aliases, boolean enabled,
                      TagKey<Item> hammerTag, TagKey<Block> baseTag, boolean blueprint) {
        this.id = id;
        this.order = order;
        this.aliases = List.copyOf(aliases);
        this.enabled = enabled;
        this.hammerTag = hammerTag;
        this.baseTag = baseTag;
        this.blueprint = blueprint;
    }

    /** Sets the configured tiers, lowest first. Called once before registration. */
    public static synchronized void setTiers(List<Definition> definitions) {
        if (frozen) {
            throw new IllegalStateException("Anvil tiers are already set");
        }
        frozen = true;
        BY_NAME.clear();
        List<AnvilTier> tiers = new ArrayList<>();
        int order = 0;
        for (Definition def : definitions) {
            AnvilTier tier = new AnvilTier(def.id, order++, def.aliases, def.enabled,
                    TagKey.create(Registries.ITEM, def.hammerTag != null ? def.hammerTag : OvergearedMod.loc(def.id + "_smithing_hammers")),
                    TagKey.create(Registries.BLOCK, def.baseTag != null ? def.baseTag : OvergearedMod.loc(def.id + "_anvil_bases")),
                    def.blueprint);
            tiers.add(tier);
            BY_NAME.put(tier.id, tier);
            for (String alias : tier.aliases) {
                BY_NAME.putIfAbsent(alias.toLowerCase(Locale.ROOT), tier);
            }
        }
        all = Collections.unmodifiableList(tiers);
    }

    /** All tiers, lowest first. */
    public static List<AnvilTier> all() {
        return all;
    }

    /** The stone tier; always present. */
    public static AnvilTier stone() {
        return BY_NAME.get(STONE_ID);
    }

    /** The default tier of forging recipes; always present. */
    public static AnvilTier iron() {
        return BY_NAME.get(IRON_ID);
    }

    /** Looks a tier up by id or alias, ignoring case. */
    @Nullable
    public static AnvilTier fromDisplayName(@Nullable String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }

    public String id() {
        return id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public String getDisplayName() {
        return id;
    }

    public String getLang() {
        return "gui.overgeared.tier." + id;
    }

    /** The tier's translated name, or its id if no translation exists. */
    public Component displayName() {
        return Component.translatableWithFallback(getLang(), id);
    }

    public List<String> aliases() {
        return aliases;
    }

    /** Disabled tiers are still registered, but hidden from creative tabs and recipe viewers and cannot be built. */
    public boolean isEnabled() {
        return enabled;
    }

    public TagKey<Item> hammerTag() {
        return hammerTag;
    }

    public TagKey<Block> baseTag() {
        return baseTag;
    }

    /** Whether anvils of this tier have a blueprint slot (and forge with blueprints, if blueprint forging is on). */
    public boolean hasBlueprint() {
        return blueprint;
    }

    public boolean isStone() {
        return id.equals(STONE_ID);
    }

    public String blockName() {
        return id + "_smithing_anvil";
    }

    /** @return true if this tier is higher than {@code other} */
    public boolean isEqualOrLowerThan(AnvilTier other) {
        return this.order > other.order;
    }

    public boolean isAtLeast(AnvilTier other) {
        return this.order >= other.order;
    }

    @Override
    public String toString() {
        return id;
    }

    /** A tier as read from config, before it gets its place in the order. */
    public static final class Definition {
        public final String id;
        public final List<String> aliases = new ArrayList<>();
        public boolean enabled = true;
        @Nullable
        public ResourceLocation hammerTag;
        @Nullable
        public ResourceLocation baseTag;
        public boolean blueprint;

        public Definition(String id) {
            this.id = id;
            this.blueprint = !STONE_ID.equals(id);
        }
    }
}
