package net.stirdrem.overgeared;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A smithing anvil tier. {@link #STONE} is built in; every other tier comes from
 * {@code config/overgeared-anvil-tiers.json} (and KubeJS on NeoForge), in ascending order.
 * Each configured tier gets its own {@code overgeared:<id>_smithing_anvil} block.
 */
public final class AnvilTier implements StringRepresentable {
    public static final String IRON_ID = "iron";

    public static final AnvilTier STONE = new AnvilTier("stone", 0, List.of(), true,
            ModTags.Items.STONE_SMITHING_HAMMERS, ModTags.Blocks.ANVIL_BASES);

    private static final Map<String, AnvilTier> BY_NAME = new LinkedHashMap<>();
    private static List<AnvilTier> configured = List.of();
    private static boolean frozen;

    private final String id;
    private final int order;
    private final List<String> aliases;
    private final boolean enabled;
    private final TagKey<Item> hammerTag;
    private final TagKey<Block> baseTag;

    private AnvilTier(String id, int order, List<String> aliases, boolean enabled,
                      TagKey<Item> hammerTag, TagKey<Block> baseTag) {
        this.id = id;
        this.order = order;
        this.aliases = List.copyOf(aliases);
        this.enabled = enabled;
        this.hammerTag = hammerTag;
        this.baseTag = baseTag;
    }

    /** Sets the configured tiers, lowest first. Called once before registration. */
    public static synchronized void setTiers(List<Definition> definitions) {
        if (frozen) {
            throw new IllegalStateException("Anvil tiers are already set");
        }
        frozen = true;
        BY_NAME.clear();
        BY_NAME.put(STONE.id, STONE);
        List<AnvilTier> tiers = new ArrayList<>();
        int order = 1;
        for (Definition def : definitions) {
            AnvilTier tier = new AnvilTier(def.id, order++, def.aliases, def.enabled,
                    TagKey.create(Registries.ITEM, def.hammerTag != null ? def.hammerTag : OvergearedMod.loc(def.id + "_smithing_hammers")),
                    TagKey.create(Registries.BLOCK, def.baseTag != null ? def.baseTag : OvergearedMod.loc(def.id + "_anvil_bases")));
            tiers.add(tier);
            BY_NAME.put(tier.id, tier);
            for (String alias : tier.aliases) {
                BY_NAME.putIfAbsent(alias.toLowerCase(Locale.ROOT), tier);
            }
        }
        configured = Collections.unmodifiableList(tiers);
    }

    /** All configured tiers (not including {@link #STONE}), lowest first. */
    public static List<AnvilTier> configured() {
        return configured;
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

        public Definition(String id) {
            this.id = id;
        }
    }
}
