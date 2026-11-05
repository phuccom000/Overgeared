package net.stirdrem.overgeared.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.platform.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads the smithing anvil tiers from {@code config/overgeared-anvil-tiers.json}. Tiers are registered as blocks,
 * so the list is read once at startup and must match between client and server.
 * <pre>
 * [
 *   {"id": "iron"},
 *   {"id": "tier_a", "aliases": ["above_a"], "enabled": false},
 *   {"id": "mythril", "hammer_tag": "overgeared:mythril_smithing_hammers", "base_tag": "overgeared:mythril_anvil_bases"}
 * ]
 * </pre>
 */
public final class AnvilTierConfig {
    public static final String FILE = OvergearedMod.MOD_ID + "-anvil-tiers.json";
    private static final Pattern ID = Pattern.compile("[a-z0-9_]+");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AnvilTierConfig() {
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE);
        List<AnvilTier.Definition> tiers;
        if (Files.notExists(file)) {
            tiers = defaults(configDir.resolve(ModConfigs.COMMON_FILE));
            write(file, tiers);
        } else {
            tiers = read(file);
        }

        Services.PLATFORM.modifyAnvilTiers(tiers);
        tiers = validate(tiers);
        AnvilTier.setTiers(tiers);
        OvergearedMod.LOGGER.info("Anvil tiers: stone, {}", tiers.stream().map(t -> t.id).toList());
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
                if (obj.has("hammer_tag")) def.hammerTag = ResourceLocation.parse(obj.get("hammer_tag").getAsString());
                if (obj.has("base_tag")) def.baseTag = ResourceLocation.parse(obj.get("base_tag").getAsString());
                tiers.add(def);
            }
            return tiers;
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Could not read {}, using the default anvil tiers", file.getFileName(), e);
            return defaults(null);
        }
    }

    /** Drops invalid and duplicate tiers and makes sure the iron tier exists. */
    private static List<AnvilTier.Definition> validate(List<AnvilTier.Definition> tiers) {
        List<AnvilTier.Definition> valid = new ArrayList<>();
        Set<String> names = new HashSet<>();
        names.add(AnvilTier.STONE.id());
        for (AnvilTier.Definition def : tiers) {
            if (def.id == null || !ID.matcher(def.id).matches()) {
                OvergearedMod.LOGGER.error("Skipping anvil tier with invalid id '{}' (use a-z, 0-9 and _)", def.id);
            } else if (!names.add(def.id)) {
                OvergearedMod.LOGGER.error("Skipping duplicate or reserved anvil tier '{}'", def.id);
            } else {
                def.aliases.removeIf(alias -> !names.add(alias.toLowerCase(java.util.Locale.ROOT)));
                valid.add(def);
            }
        }
        if (valid.stream().noneMatch(def -> def.id.equals(AnvilTier.IRON_ID))) {
            OvergearedMod.LOGGER.warn("Anvil tier 'iron' is required; adding it as the lowest tier");
            valid.add(0, new AnvilTier.Definition(AnvilTier.IRON_ID));
        }
        return valid;
    }

    /**
     * The tiers of earlier versions. {@code oldCommonConfig}, if present, supplies the old
     * {@code enableTierA}/{@code enableTierB} options.
     */
    private static List<AnvilTier.Definition> defaults(Path oldCommonConfig) {
        String old = "";
        if (oldCommonConfig != null && Files.exists(oldCommonConfig)) {
            try {
                old = Files.readString(oldCommonConfig, StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
        }
        AnvilTier.Definition iron = new AnvilTier.Definition(AnvilTier.IRON_ID);
        AnvilTier.Definition tierA = new AnvilTier.Definition("tier_a");
        tierA.aliases.add("above_a");
        tierA.enabled = Pattern.compile("enableTierA\\s*=\\s*true").matcher(old).find();
        AnvilTier.Definition tierB = new AnvilTier.Definition("tier_b");
        tierB.aliases.add("above_b");
        tierB.enabled = Pattern.compile("enableTierB\\s*=\\s*true").matcher(old).find();
        return new ArrayList<>(List.of(iron, tierA, tierB));
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
            if (def.hammerTag != null) obj.addProperty("hammer_tag", def.hammerTag.toString());
            if (def.baseTag != null) obj.addProperty("base_tag", def.baseTag.toString());
            array.add(obj);
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(array), StandardCharsets.UTF_8);
        } catch (IOException e) {
            OvergearedMod.LOGGER.error("Could not write {}", file.getFileName(), e);
        }
    }
}
