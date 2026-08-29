package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

        // ---------------------------------------------------------------------
        // Tongs
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.TONGS)
                .add(
                        ModItems.IRON_TONGS,
                        ModItems.STEEL_TONGS,
                        ModItems.WOODEN_TONGS
                );

        // ---------------------------------------------------------------------
        // Common material tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(commonTag("ingots"))
                .add(ModItems.STEEL_INGOT);
        getOrCreateTagBuilder(commonTag("nuggets"))
                .add(ModItems.STEEL_NUGGET)
                .add(ModItems.COPPER_NUGGET);

        // ---------------------------------------------------------------------
        // Tool parts
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.TOOL_PARTS)
                .add(
                        // Stone
                        ModItems.STONE_HAMMER_HEAD,
                        ModItems.STONE_SWORD_BLADE,
                        ModItems.STONE_PICKAXE_HEAD,
                        ModItems.STONE_AXE_HEAD,
                        ModItems.STONE_SHOVEL_HEAD,
                        ModItems.STONE_HOE_HEAD,

                        // Copper
                        ModItems.COPPER_HAMMER_HEAD,
                        ModItems.COPPER_SWORD_BLADE,
                        ModItems.COPPER_PICKAXE_HEAD,
                        ModItems.COPPER_AXE_HEAD,
                        ModItems.COPPER_HOE_HEAD,
                        ModItems.COPPER_SHOVEL_HEAD,

                        // Iron
                        ModItems.IRON_SWORD_BLADE,
                        ModItems.IRON_PICKAXE_HEAD,
                        ModItems.IRON_AXE_HEAD,
                        ModItems.IRON_SHOVEL_HEAD,
                        ModItems.IRON_HOE_HEAD,

                        // Golden
                        ModItems.GOLDEN_SWORD_BLADE,
                        ModItems.GOLDEN_PICKAXE_HEAD,
                        ModItems.GOLDEN_AXE_HEAD,
                        ModItems.GOLDEN_SHOVEL_HEAD,
                        ModItems.GOLDEN_HOE_HEAD,

                        // Steel
                        ModItems.STEEL_HAMMER_HEAD,
                        ModItems.STEEL_SWORD_BLADE,
                        ModItems.STEEL_PICKAXE_HEAD,
                        ModItems.STEEL_AXE_HEAD,
                        ModItems.STEEL_SHOVEL_HEAD,
                        ModItems.STEEL_HOE_HEAD,

                        // Arrow heads
                        ModItems.IRON_ARROW_HEAD,
                        ModItems.STEEL_ARROW_HEAD,
                        ModItems.DIAMOND_SHARD
                );

        // ---------------------------------------------------------------------
        // Tools
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.TOOLS)
                .add(
                        ModItems.WOODEN_TONGS,
                        ModItems.IRON_TONGS,
                        ModItems.STEEL_TONGS,
                        ModItems.SMITHING_HAMMER,
                        ModItems.COPPER_SMITHING_HAMMER
                );

        // ---------------------------------------------------------------------
        // Heated metals
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.HEATED_METALS)
                .add(
                        ModItems.HEATED_STEEL_INGOT,
                        ModItems.HEATED_IRON_INGOT,
                        ModItems.HEATED_CRUDE_STEEL,
                        ModItems.HEATED_COPPER_INGOT,
                        ModItems.HEATED_SILVER_INGOT,
                        ModItems.HEATED_NETHERITE_ALLOY
                );

        // ---------------------------------------------------------------------
        // Smithing hammers
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.SMITHING_HAMMERS)
                .add(
                        ModItems.SMITHING_HAMMER,
                        ModItems.COPPER_SMITHING_HAMMER
                )
                .addTag(ModTags.Items.STONE_SMITHING_HAMMERS)
                .addTag(ModTags.Items.IRON_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        getOrCreateTagBuilder(ModTags.Items.STONE_SMITHING_HAMMERS)
                .add(ModItems.COPPER_SMITHING_HAMMER)
                .addTag(ModTags.Items.IRON_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        getOrCreateTagBuilder(ModTags.Items.IRON_SMITHING_HAMMERS)
                .add(ModItems.SMITHING_HAMMER)
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        getOrCreateTagBuilder(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        getOrCreateTagBuilder(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        // ---------------------------------------------------------------------
        // Common / Forge-compatible material tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(
                commonTag("ingots/steel")
        ).add(ModItems.STEEL_INGOT);

        getOrCreateTagBuilder(
                commonTag("nuggets/steel")
        ).add(ModItems.STEEL_NUGGET);

        getOrCreateTagBuilder(
                commonTag("nuggets/copper")
        ).add(ModItems.COPPER_NUGGET);

        getOrCreateTagBuilder(
                commonTag("plates/copper")
        ).add(ModItems.COPPER_PLATE);

        getOrCreateTagBuilder(
                commonTag("plates/iron")
        ).add(ModItems.IRON_PLATE);

        getOrCreateTagBuilder(
                commonTag("plates/steel")
        ).add(ModItems.STEEL_PLATE);

        // ---------------------------------------------------------------------
        // Armor
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(
                commonTag("armors/helmets")
        ).add(
                ModItems.STEEL_HELMET,
                ModItems.COPPER_HELMET
        );

        getOrCreateTagBuilder(
                commonTag("armors/chestplates")
        ).add(
                ModItems.STEEL_CHESTPLATE,
                ModItems.COPPER_CHESTPLATE
        );

        getOrCreateTagBuilder(
                commonTag("armors/leggings")
        ).add(
                ModItems.STEEL_LEGGINGS,
                ModItems.COPPER_LEGGINGS
        );

        getOrCreateTagBuilder(
                commonTag("armors/boots")
        ).add(
                ModItems.STEEL_BOOTS,
                ModItems.COPPER_BOOTS
        );

        // ---------------------------------------------------------------------
        // Tools
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.TOOLS)
                .add(
                        ModItems.STEEL_AXE,
                        ModItems.STEEL_PICKAXE,
                        ModItems.STEEL_HOE,
                        ModItems.STEEL_SHOVEL,
                        ModItems.STEEL_SWORD,
                        ModItems.COPPER_AXE,
                        ModItems.COPPER_PICKAXE,
                        ModItems.COPPER_HOE,
                        ModItems.COPPER_SHOVEL,
                        ModItems.COPPER_SWORD
                );

        getOrCreateTagBuilder(ItemTags.HOES)
                .add(
                        ModItems.COPPER_HOE,
                        ModItems.STEEL_HOE
                );

        getOrCreateTagBuilder(ItemTags.AXES)
                .add(
                        ModItems.COPPER_AXE,
                        ModItems.STEEL_AXE
                );

        getOrCreateTagBuilder(ItemTags.PICKAXES)
                .add(
                        ModItems.COPPER_PICKAXE,
                        ModItems.STEEL_PICKAXE
                );

        getOrCreateTagBuilder(ItemTags.SHOVELS)
                .add(
                        ModItems.COPPER_SHOVEL,
                        ModItems.STEEL_SHOVEL
                );

        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(
                        ModItems.COPPER_SWORD,
                        ModItems.STEEL_SWORD
                );

        // ---------------------------------------------------------------------
        // Common tool tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(
                commonTag("tools/hoes")
        ).add(
                ModItems.STEEL_HOE,
                ModItems.COPPER_HOE
        );

        getOrCreateTagBuilder(
                commonTag("tools/axes")
        ).add(
                ModItems.COPPER_AXE,
                ModItems.STEEL_AXE
        );

        getOrCreateTagBuilder(
                commonTag("tools/pickaxes")
        ).add(
                ModItems.COPPER_PICKAXE,
                ModItems.STEEL_PICKAXE
        );

        getOrCreateTagBuilder(
                commonTag("tools/shovels")
        ).add(
                ModItems.STEEL_SHOVEL,
                ModItems.COPPER_SHOVEL
        );

        getOrCreateTagBuilder(
                commonTag("tools/swords")
        ).add(
                ModItems.STEEL_SWORD,
                ModItems.COPPER_SWORD
        );

        // ---------------------------------------------------------------------
        // Trimmable armor
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(
                        ModItems.STEEL_HELMET,
                        ModItems.STEEL_CHESTPLATE,
                        ModItems.STEEL_LEGGINGS,
                        ModItems.STEEL_BOOTS,

                        ModItems.COPPER_HELMET,
                        ModItems.COPPER_CHESTPLATE,
                        ModItems.COPPER_LEGGINGS,
                        ModItems.COPPER_BOOTS
                );

        // ---------------------------------------------------------------------
        // Arrows
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.ARROWS)
                .add(
                        ModItems.LINGERING_ARROW,
                        ModItems.IRON_UPGRADE_ARROW,
                        ModItems.STEEL_UPGRADE_ARROW,
                        ModItems.DIAMOND_UPGRADE_ARROW
                );

        // ---------------------------------------------------------------------
        // Other custom tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.HOT_ITEMS)
                .add(Items.LAVA_BUCKET);

        getOrCreateTagBuilder(ModTags.Items.KNAPPABLE)
                .add(ModItems.ROCK);

        getOrCreateTagBuilder(ModTags.Items.TOOL_CAST)
                .add(
                        ModItems.CLAY_TOOL_CAST,
                        ModItems.NETHER_TOOL_CAST
                );

        getOrCreateTagBuilder(ModTags.Items.QUALITY_BLACKLIST)
                .add(
                        Items.WOODEN_SWORD,
                        Items.WOODEN_PICKAXE,
                        Items.WOODEN_AXE,
                        Items.WOODEN_SHOVEL,
                        Items.WOODEN_HOE,

                        Items.LEATHER_HELMET,
                        Items.LEATHER_CHESTPLATE,
                        Items.LEATHER_LEGGINGS,
                        Items.LEATHER_BOOTS,

                        Items.FLINT_AND_STEEL,
                        Items.ELYTRA
                );
    }

    /**
     * Creates a common item tag.
     * <p>
     * Example:
     * commonTag("ingots/steel")
     * -> c:ingots/steel
     */
    private TagKey<Item> commonTag(String path) {
        return TagKey.of(
                net.minecraft.registry.RegistryKeys.ITEM,
                Identifier.of("c", path)
        );
    }
}
