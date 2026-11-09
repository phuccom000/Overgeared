package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.Overgeared;

import java.util.HashMap;
import java.util.Map;

public class KnappingResourceReloadListener extends OvergearedJsonReloadListener {

    private static final Gson GSON = new Gson();

    /* ---------- TEXTURES ---------- */
    private static final Map<Item, Identifier> ITEM_TEXTURES = new HashMap<>();
    private static final Map<TagKey<Item>, Identifier> TAG_TEXTURES = new HashMap<>();

    /* ---------- SOUNDS ---------- */
    private static final Map<Item, SoundEvent> ITEM_SOUNDS = new HashMap<>();
    private static final Map<TagKey<Item>, SoundEvent> TAG_SOUNDS = new HashMap<>();

    /* ---------- FALLBACKS ---------- */
    public static final Identifier FALLBACK_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/block/stone.png");

    public static final SoundEvent FALLBACK_SOUND =
            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("minecraft", "block.stone.break"));

    public KnappingResourceReloadListener() {
        super("knapping_resources");
    }
    public Identifier getFabricId() {
        return Overgeared.id("knapping_resources_listener");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsons,
                          ResourceManager resourceManager,
                          ProfilerFiller profiler) {

        ITEM_TEXTURES.clear();
        TAG_TEXTURES.clear();
        ITEM_SOUNDS.clear();
        TAG_SOUNDS.clear();

        for (Map.Entry<Identifier, JsonElement> entry : jsons.entrySet()) {
            JsonObject root = GsonHelper.convertToJsonObject(entry.getValue(), "root");

            if (!root.has("knapping")) continue;

            JsonArray array = GsonHelper.getAsJsonArray(root, "knapping");

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();

                /* ---------- TEXTURE ---------- */
                Identifier texture = obj.has("texture")
                        ? Identifier.tryParse(GsonHelper.getAsString(obj, "texture"))
                        : null;

                /* ---------- SOUND ---------- */
                SoundEvent sound = null;
                if (obj.has("sound")) {
                    Identifier soundId =
                            Identifier.tryParse(GsonHelper.getAsString(obj, "sound"));

                    sound = BuiltInRegistries.SOUND_EVENT.getValue(soundId);

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
                            Identifier.tryParse(GsonHelper.getAsString(obj, "item"));

                    Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);

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
                            Identifier.tryParse(GsonHelper.getAsString(obj, "tag"));

                    TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);

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
            if (stack.is(entry.getKey())) {
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
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return FALLBACK_SOUND;
    }
}
