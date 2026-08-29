package net.stirdrem.overgeared.util;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.CastingToolTypesReloadListener;
import net.stirdrem.overgeared.datapack.MaterialSettingsReloadListener;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigHelper {

    // -----------------------
    // Tool Types
    // -----------------------

    /**
     * Return the translation key for a tool type ID.
     * Example: "SWORD" -> "tooltype.overgeared.sword"
     */
    public static String getToolTypeDisplayName(String toolType) {
        return "tooltype.overgeared." + toolType.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Get max material amount for a tool type
     * config row: [tool_id, max_amount]
     */
    public static int getMaxMaterialAmount(String toolType) {

        int datapackValue = CastingToolTypesReloadListener.getMaxMaterialAmount(toolType);
        if (datapackValue > 0) {
            return datapackValue;
        }

        for (var e : ServerConfig.CASTING_TOOL_TYPES.get()) {
            List<?> row = e;
            if (row.get(0).equals(toolType)) {
                return ((Number) row.get(1)).intValue();
            }
        }

        return 0;
    }

    // -----------------------
    // Material Types
    // -----------------------

    /**
     * Return the translation key for a material ID.
     * Example: "IRON" -> "material.overgeared.iron"
     */
    public static String getMaterialDisplayName(String materialId) {
        return "material.overgeared." + materialId.toLowerCase(java.util.Locale.ROOT);
    }

    // -----------------------
    // Tool Head → Tool Type
    // -----------------------
    public static String getToolTypeForItem(World world, ItemStack stack) {
        return world.getRecipeManager()
                .listAllOfType(ModRecipeTypes.ITEM_TO_TOOLTYPE)
                .stream()
                .filter(r -> r.getInput().test(stack))
                .map(ItemToToolTypeRecipe::getToolType)
                .findFirst()
                .orElse("none");
    }

    // -----------------------
    // Material from Item (UPDATED - uses datapack)
    // -----------------------

    /**
     * Returns the material name for the given item/stack
     */
    public static String getMaterialForItem(ItemStack stack) {
        return getMaterialForItem(stack.getItem());
    }

    public static String getMaterialForItem(Item item) {
        // First check datapack entries
        Optional<MaterialSettingsReloadListener.MaterialEntry> datapackEntry =
                MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                        .filter(entry -> matchesItemOrTag(item, entry.getItemOrTag()))
                        .findFirst();

        if (datapackEntry.isPresent()) {
            return datapackEntry.get().getMaterialId();
        }

        // Fallback to config for backward compatibility
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            String key = (String) row.get(0);
            if (matchesItemOrTag(item, key)) {
                return (String) row.get(1);
            }
        }
        return "none";
    }

    /**
     * Returns the material value for the given item/stack
     */
    public static int getMaterialValue(ItemStack stack) {
        return getMaterialValue(stack.getItem());
    }

    public static int getMaterialValue(Item item) {
        // First check datapack entries
        Optional<MaterialSettingsReloadListener.MaterialEntry> datapackEntry =
                MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                        .filter(entry -> matchesItemOrTag(item, entry.getItemOrTag()))
                        .findFirst();

        if (datapackEntry.isPresent()) {
            return datapackEntry.get().getMaterialValue();
        }

        // Fallback to config for backward compatibility
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            String key = (String) row.get(0);
            if (matchesItemOrTag(item, key)) {
                return ((Number) row.get(2)).intValue();
            }
        }
        return 0;
    }

    /**
     * Checks if the item/stack is a valid material
     */
    public static boolean isValidMaterial(ItemStack stack) {
        return isValidMaterial(stack.getItem());
    }

    public static boolean isValidMaterial(Item item) {
        // First check datapack entries
        boolean isValidInDatapack = MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                .anyMatch(entry -> matchesItemOrTag(item, entry.getItemOrTag()));

        if (isValidInDatapack) {
            return true;
        }

        // Fallback to config for backward compatibility
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            String key = (String) row.get(0);
            if (matchesItemOrTag(item, key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all material values for an item (useful when an item belongs to multiple materials)
     */
    public static Map<String, Integer> getMaterialValuesForItem(ItemStack stack) {
        return getMaterialValuesForItem(stack.getItem());
    }

    public static Map<String, Integer> getMaterialValuesForItem(Item item) {
        Map<String, Integer> result = new java.util.HashMap<>();

        // Add datapack entries
        MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                .filter(entry -> matchesItemOrTag(item, entry.getItemOrTag()))
                .forEach(entry -> result.put(entry.getMaterialId(), entry.getMaterialValue()));

        // Add config entries (will override datapack if same material ID)
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            String key = (String) row.get(0);
            if (matchesItemOrTag(item, key)) {
                String materialId = (String) row.get(1);
                int value = ((Number) row.get(2)).intValue();
                result.put(materialId, value);
            }
        }

        return result;
    }

    /**
     * Helper: matches an item against an item ID or tag key
     */
    private static boolean matchesItemOrTag(Item item, String key) {

        // Direct item match
        Identifier itemId = Registries.ITEM.getId(item);
        if (itemId != null && itemId.toString().equals(key)) {
            return true;
        }

        // Tag match
        if (key.startsWith("#")) {
            Identifier tagId = new Identifier(key.substring(1));

            // ---- Item tag check ----
            TagKey<Item> itemTag = TagKey.of(RegistryKeys.ITEM, tagId);
            if (Registries.ITEM.getEntry(item).isIn(itemTag)) {
                return true;
            }

            // ---- Block tag check (for BlockItems) ----
            if (item instanceof BlockItem blockItem) {
                TagKey<net.minecraft.block.Block> blockTag = TagKey.of(RegistryKeys.BLOCK, tagId);

                return Registries.BLOCK.getEntry(blockItem.getBlock()).isIn(blockTag);
            }
        }

        return false;
    }


    // -----------------------
    // New utility methods for datapack integration
    // -----------------------

    /**
     * Get all available material IDs from both datapack and config
     */
    public static java.util.Set<String> getAllMaterialIds() {
        java.util.Set<String> materialIds = new java.util.HashSet<>();

        // From datapack
        MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                .map(MaterialSettingsReloadListener.MaterialEntry::getMaterialId)
                .forEach(materialIds::add);

        // From config
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            materialIds.add((String) row.get(1));
        }

        return materialIds;
    }

    /**
     * Get all items for a specific material
     */
    public static List<String> getItemsForMaterial(String materialId) {
        List<String> items = new java.util.ArrayList<>();

        // From datapack
        MaterialSettingsReloadListener.getEntriesForMaterial(materialId).stream()
                .map(MaterialSettingsReloadListener.MaterialEntry::getItemOrTag)
                .forEach(items::add);

        // From config
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            if (row.get(1).equals(materialId)) {
                items.add((String) row.get(0));
            }
        }

        return items;
    }

    public static List<Item> getItemListForMaterial(String materialId) {
        List<Item> items = new java.util.ArrayList<>();

        // --- Datapack entries ---
        MaterialSettingsReloadListener.getEntriesForMaterial(materialId)
                .forEach(entry -> resolveItemOrTag(entry.getItemOrTag(), items));

        // --- Config fallback ---
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            if (row.get(1).equals(materialId)) {
                resolveItemOrTag((String) row.get(0), items);
            }
        }

        return items;
    }

    private static void resolveItemOrTag(String key, List<Item> out) {

        // Tag
        if (key.startsWith("#")) {
            Identifier tagId = new Identifier(key.substring(1));

            // ---- Item tag ----
            TagKey<Item> itemTag = TagKey.of(RegistryKeys.ITEM, tagId);

            for (var entry : Registries.ITEM.iterateEntries(itemTag)) {
                out.add(entry.value());
            }

            // ---- Block tag → BlockItem ----
            TagKey<net.minecraft.block.Block> blockTag = TagKey.of(RegistryKeys.BLOCK, tagId);

            for (var entry : Registries.BLOCK.iterateEntries(blockTag)) {
                Item item = entry.value().asItem();
                if (item != net.minecraft.item.Items.AIR) {
                    out.add(item);
                }
            }

            return;
        }

        // Direct item
        Item item = Registries.ITEM.get(new Identifier(key));
        if (item != net.minecraft.item.Items.AIR) {
            out.add(item);
        }
    }


    public static Map<String, Map<Item, Integer>> getAllMaterialItemValues() {
        Map<String, Map<Item, Integer>> result = new java.util.HashMap<>();

        // Loop all known materials (datapack + config)
        for (String materialId : getAllMaterialIds()) {
            Map<Item, Integer> itemValues = new java.util.HashMap<>();

            // Resolve all items for this material
            for (Item item : getItemListForMaterial(materialId)) {

                // Use existing value resolver (datapack-first)
                int value = getMaterialValue(item);

                if (value > 0) {
                    itemValues.put(item, value);
                }
            }

            if (!itemValues.isEmpty()) {
                result.put(materialId, itemValues);
            }
        }

        return result;
    }


}
