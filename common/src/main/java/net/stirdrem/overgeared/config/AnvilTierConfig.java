package net.stirdrem.overgeared.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.platform.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads the smithing anvil tiers from {@code config/overgeared-anvil-tiers.json}. Tiers are registered as blocks,
 * so the list is read once at startup and must match between client and server.
 * <pre>
 * [
 *   {"id": "stone", "blueprint": false},
 *   {"id": "iron"},
 *   {"id": "tier_a", "aliases": ["above_a"], "enabled": false},
 *   {"id": "mythril", "hammer_tag": "overgeared:mythril_smithing_hammers", "base_tag": "overgeared:mythril_anvil_bases"}
 * ]
 * </pre>
 * {@code blueprint} (default true, false for stone) controls whether the anvil has a blueprint slot.
 */
public final class AnvilTierConfig {
    public static final String FILE = Overgeared.MOD_ID + "-anvil-tiers.json";
    /** The main config files of earlier versions, which had the enableTierA/enableTierB options. */
    private static final List<String> OLD_CONFIG_FILES = List.of(Overgeared.MOD_ID + "-common.toml", Overgeared.MOD_ID + "-server.json");
    private static final Pattern ID = Pattern.compile("[a-z0-9_]+");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AnvilTierConfig() {
    }

    /**
     * Writes the default tier file if there is none. Call first thing in the mod entrypoint: the defaults take the
     * old enableTierA/enableTierB options from the main config, which the loader drops once it loads that config.
     */
    public static synchronized void createDefaultFileIfMissing() {
        Path configDir = Services.PLATFORM.getConfigDir();
        Path file = configDir.resolve(FILE);
        if (Files.notExists(file)) {
            write(file, defaults(configDir));
        }
    }

    /** Reads the tiers and hands them to {@link AnvilTier#setTiers}; called by {@link AnvilTier#all()}. */
    public static synchronized void load() {
        createDefaultFileIfMissing();
        List<AnvilTier.Definition> tiers = read(Services.PLATFORM.getConfigDir().resolve(FILE));

        Services.PLATFORM.modifyAnvilTiers(tiers);
        tiers = validate(tiers);
        AnvilTier.setTiers(tiers);
        Overgeared.LOGGER.info("Anvil tiers: {}", tiers.stream().map(t -> t.id).toList());
    }

    private static List<AnvilTier.Definition> read(Path file) {
        try {
            JsonArray array = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonArray();
            List<AnvilTier.Definition> tiers = new ArrayList<>();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                AnvilTier.Definition def = new AnvilTier.Definition(obj.get("id").getAsString());
                if (obj.has("aliases")) {
                    obj.getAsJsonArray("aliases").forEach(a -> def.aliases.add(a.getAsString()));
                }
                if (obj.has("enabled")) def.enabled = obj.get("enabled").getAsBoolean();
                if (obj.has("hammer_tag")) def.hammerTag = parseTag(obj, "hammer_tag");
                if (obj.has("base_tag")) def.baseTag = parseTag(obj, "base_tag");
                if (obj.has("blueprint")) def.blueprint = obj.get("blueprint").getAsBoolean();
                tiers.add(def);
            }
            return tiers;
        } catch (Exception e) {
            Overgeared.LOGGER.error("Could not read {}, using the default anvil tiers", file.getFileName(), e);
            return defaults(null);
        }
    }

    private static ResourceLocation parseTag(JsonObject obj, String key) {
        String value = obj.get(key).getAsString();
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            throw new IllegalArgumentException("Invalid " + key + " '" + value + "'");
        }
        return id;
    }

    /** Drops invalid and duplicate tiers and makes sure the stone and iron tiers exist. */
    private static List<AnvilTier.Definition> validate(List<AnvilTier.Definition> tiers) {
        List<AnvilTier.Definition> valid = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (AnvilTier.Definition def : tiers) {
            if (def.id == null || !ID.matcher(def.id).matches()) {
                Overgeared.LOGGER.error("Skipping anvil tier with invalid id '{}' (use a-z, 0-9 and _)", def.id);
            } else if (!names.add(def.id)) {
                Overgeared.LOGGER.error("Skipping duplicate anvil tier '{}'", def.id);
            } else {
                def.aliases.removeIf(alias -> !names.add(alias.toLowerCase(Locale.ROOT)));
                valid.add(def);
            }
        }
        // Stone and iron have their own blocks and are used as recipe defaults, so they cannot be removed (only disabled)
        if (valid.stream().noneMatch(def -> def.id.equals(AnvilTier.IRON_ID))) {
            Overgeared.LOGGER.warn("Anvil tier 'iron' is required; adding it as the lowest tier");
            valid.add(0, new AnvilTier.Definition(AnvilTier.IRON_ID));
        }
        if (valid.stream().noneMatch(def -> def.id.equals(AnvilTier.STONE_ID))) {
            Overgeared.LOGGER.warn("Anvil tier 'stone' is required; adding it as the lowest tier");
            valid.add(0, new AnvilTier.Definition(AnvilTier.STONE_ID));
        }
        return valid;
    }

    /**
     * The tiers of earlier versions. If {@code configDir} has an old main config, its
     * {@code enableTierA}/{@code enableTierB} options decide whether those tiers start enabled.
     */
    private static List<AnvilTier.Definition> defaults(Path configDir) {
        String old = "";
        if (configDir != null) {
            for (String name : OLD_CONFIG_FILES) {
                Path oldFile = configDir.resolve(name);
                if (Files.exists(oldFile)) {
                    try {
                        old = Files.readString(oldFile, StandardCharsets.UTF_8);
                        break;
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        AnvilTier.Definition stone = new AnvilTier.Definition(AnvilTier.STONE_ID);
        AnvilTier.Definition iron = new AnvilTier.Definition(AnvilTier.IRON_ID);
        AnvilTier.Definition tierA = new AnvilTier.Definition("tier_a");
        tierA.aliases.add("above_a");
        tierA.enabled = isOldOptionOn(old, "enableTierA");
        AnvilTier.Definition tierB = new AnvilTier.Definition("tier_b");
        tierB.aliases.add("above_b");
        tierB.enabled = isOldOptionOn(old, "enableTierB");
        return new ArrayList<>(List.of(stone, iron, tierA, tierB));
    }

    /** Matches {@code name = true} (Forge TOML) and {@code "name": true} (Fabric JSON). */
    private static boolean isOldOptionOn(String config, String name) {
        return Pattern.compile("\\b" + name + "\"?\\s*[=:]\\s*true").matcher(config).find();
    }

    private static void write(Path file, List<AnvilTier.Definition> tiers) {
        JsonArray array = new JsonArray();
        for (AnvilTier.Definition def : tiers) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", def.id);
            if (!def.aliases.isEmpty()) {
                JsonArray aliases = new JsonArray();
                def.aliases.forEach(aliases::add);
                obj.add("aliases", aliases);
            }
            obj.addProperty("enabled", def.enabled);
            obj.addProperty("blueprint", def.blueprint);
            if (def.hammerTag != null) obj.addProperty("hammer_tag", def.hammerTag.toString());
            if (def.baseTag != null) obj.addProperty("base_tag", def.baseTag.toString());
            array.add(obj);
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(array), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Overgeared.LOGGER.error("Could not write {}", file.getFileName(), e);
        }
    }
}
