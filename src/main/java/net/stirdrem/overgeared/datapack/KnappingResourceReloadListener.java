package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.stirdrem.overgeared.Overgeared;

import java.util.HashMap;
import java.util.Map;

public class KnappingResourceReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = new Gson();

    /* ---------- TEXTURES ---------- */
    private static final Map<Item, Identifier> ITEM_TEXTURES = new HashMap<>();
    private static final Map<TagKey<Item>, Identifier> TAG_TEXTURES = new HashMap<>();

    /* ---------- SOUNDS ---------- */
    private static final Map<Item, SoundEvent> ITEM_SOUNDS = new HashMap<>();
    private static final Map<TagKey<Item>, SoundEvent> TAG_SOUNDS = new HashMap<>();

    /* ---------- FALLBACKS ---------- */
    public static final Identifier FALLBACK_TEXTURE =
            new Identifier("minecraft", "textures/block/stone.png");

    public static final SoundEvent FALLBACK_SOUND =
            SoundEvent.of(new Identifier("minecraft", "block.stone.break"));

    public KnappingResourceReloadListener() {
        super(GSON, "knapping_resources");
    }

    @Override
    public Identifier getFabricId() {
        return Overgeared.id("knapping_resources_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons,
                          ResourceManager resourceManager,
                          Profiler profiler) {

        ITEM_TEXTURES.clear();
        TAG_TEXTURES.clear();
        ITEM_SOUNDS.clear();
        TAG_SOUNDS.clear();

        for (Map.Entry<Identifier, JsonElement> entry : jsons.entrySet()) {
            JsonObject root = JsonHelper.asObject(entry.getValue(), "root");

            if (!root.has("knapping")) continue;

            JsonArray array = JsonHelper.getArray(root, "knapping");

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();

                /* ---------- TEXTURE ---------- */
                Identifier texture = obj.has("texture")
                        ? Identifier.tryParse(JsonHelper.getString(obj, "texture"))
                        : null;

                /* ---------- SOUND ---------- */
                SoundEvent sound = null;
                if (obj.has("sound")) {
                    Identifier soundId =
                            Identifier.tryParse(JsonHelper.getString(obj, "sound"));

                    sound = Registries.SOUND_EVENT.get(soundId);

                    if (sound == null) {
                        Overgeared.LOGGER.warn(
                                "Unknown sound '{}' in {}",
                                soundId, entry.getKey()
                        );
                        continue;
                    }
                }

                /* ---------- ITEM ---------- */
                if (obj.has("item")) {
                    Identifier itemId =
                            Identifier.tryParse(JsonHelper.getString(obj, "item"));

                    Item item = Registries.ITEM.get(itemId);

                    if (item == null) {
                        Overgeared.LOGGER.warn(
                                "Unknown item '{}' in {}",
                                itemId, entry.getKey()
                        );
                        continue;
                    }

                    if (texture != null) ITEM_TEXTURES.put(item, texture);
                    if (sound != null) ITEM_SOUNDS.put(item, sound);
                }

                /* ---------- TAG ---------- */
                if (obj.has("tag")) {
                    Identifier tagId =
                            Identifier.tryParse(JsonHelper.getString(obj, "tag"));

                    TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagId);

                    if (texture != null) TAG_TEXTURES.put(tag, texture);
                    if (sound != null) TAG_SOUNDS.put(tag, sound);
                }
            }
        }

        Overgeared.LOGGER.info(
                "Loaded {} item textures, {} tag textures, {} item sounds, {} tag sounds",
                ITEM_TEXTURES.size(),
                TAG_TEXTURES.size(),
                ITEM_SOUNDS.size(),
                TAG_SOUNDS.size()
        );
    }

    /* ============================================================ */
    /* ====================== RESOLUTION API ====================== */
    /* ============================================================ */

    public static Identifier getTexture(ItemStack stack) {
        Item item = stack.getItem();

        Identifier tex = ITEM_TEXTURES.get(item);
        if (tex != null) return tex;

        for (var entry : TAG_TEXTURES.entrySet()) {
            if (stack.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }

        return FALLBACK_TEXTURE;
    }

    public static SoundEvent getSound(ItemStack stack) {
        Item item = stack.getItem();

        SoundEvent snd = ITEM_SOUNDS.get(item);
        if (snd != null) return snd;

        for (var entry : TAG_SOUNDS.entrySet()) {
            if (stack.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }

        return FALLBACK_SOUND;
    }
}
