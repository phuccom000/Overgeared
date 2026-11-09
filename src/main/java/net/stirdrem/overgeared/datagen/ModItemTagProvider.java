package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

    public ModItemTagProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {

        // ---------------------------------------------------------------------
        // Repair materials for ModToolTiers.STEEL / ModArmorMaterials.STEEL
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.STEEL_TOOL_MATERIALS)
                .add(ModItems.STEEL_INGOT);
        getOrCreateTagBuilder(ModTags.Items.REPAIRS_STEEL_ARMOR)
                .add(ModItems.STEEL_INGOT);

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
                .add(ModItems.STEEL_NUGGET);

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
                        ModItems.STONE_SPEAR_HEAD,
                        ModItems.STONE_HOE_HEAD,

                        // Copper
                        ModItems.COPPER_HAMMER_HEAD,
                        ModItems.COPPER_SWORD_BLADE,
                        ModItems.COPPER_PICKAXE_HEAD,
                        ModItems.COPPER_AXE_HEAD,
                        ModItems.COPPER_HOE_HEAD,
                        ModItems.COPPER_SHOVEL_HEAD,
                        ModItems.COPPER_SPEAR_HEAD,

                        // Iron
                        ModItems.IRON_SWORD_BLADE,
                        ModItems.IRON_PICKAXE_HEAD,
                        ModItems.IRON_AXE_HEAD,
                        ModItems.IRON_SHOVEL_HEAD,
                        ModItems.IRON_SPEAR_HEAD,
                        ModItems.IRON_HOE_HEAD,

                        // Golden
                        ModItems.GOLDEN_SWORD_BLADE,
                        ModItems.GOLDEN_PICKAXE_HEAD,
                        ModItems.GOLDEN_AXE_HEAD,
                        ModItems.GOLDEN_SHOVEL_HEAD,
                        ModItems.GOLDEN_SPEAR_HEAD,
                        ModItems.GOLDEN_HOE_HEAD,

                        // Steel
                        ModItems.STEEL_HAMMER_HEAD,
                        ModItems.STEEL_SWORD_BLADE,
                        ModItems.STEEL_PICKAXE_HEAD,
                        ModItems.STEEL_AXE_HEAD,
                        ModItems.STEEL_SHOVEL_HEAD,
                        ModItems.STEEL_SPEAR_HEAD,
                        ModItems.STEEL_HOE_HEAD,

                        // Arrow heads
                        ModItems.IRON_ARROW_HEAD,
                        ModItems.STEEL_ARROW_HEAD,
                        ModItems.DIAMOND_SHARD
                );

        // ---------------------------------------------------------------------
        // Tools
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(commonTag("tools")) // 26.3: vanilla minecraft:tools is gone; c:tools is the convention tag
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
                ModItems.STEEL_HELMET
                
        );

        getOrCreateTagBuilder(
                commonTag("armors/chestplates")
        ).add(
                ModItems.STEEL_CHESTPLATE
                
        );

        getOrCreateTagBuilder(
                commonTag("armors/leggings")
        ).add(
                ModItems.STEEL_LEGGINGS
                
        );

        getOrCreateTagBuilder(
                commonTag("armors/boots")
        ).add(
                ModItems.STEEL_BOOTS
                
        );

        // ---------------------------------------------------------------------
        // Tools
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(commonTag("tools")) // 26.3: vanilla minecraft:tools is gone; c:tools is the convention tag
                .add(
                        ModItems.STEEL_AXE,
                        ModItems.STEEL_PICKAXE,
                        ModItems.STEEL_HOE,
                        ModItems.STEEL_SHOVEL,
                        ModItems.STEEL_SWORD,
                        ModItems.STEEL_SPEAR
                        
                );

        getOrCreateTagBuilder(ItemTags.HOES)
                .add(
                        ModItems.STEEL_HOE
                );

        getOrCreateTagBuilder(ItemTags.AXES)
                .add(
                        ModItems.STEEL_AXE
                );

        getOrCreateTagBuilder(ItemTags.PICKAXES)
                .add(
                        ModItems.STEEL_PICKAXE
                );

        getOrCreateTagBuilder(ItemTags.SHOVELS)
                .add(
                        ModItems.STEEL_SHOVEL
                );

        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(
                        ModItems.STEEL_SWORD
                );

        // 26.x spears; vanilla's spear enchantment tags (lunge, melee, durability) include #minecraft:spears
        getOrCreateTagBuilder(ItemTags.SPEARS)
                .add(
                        ModItems.STEEL_SPEAR
                );

        // ---------------------------------------------------------------------
        // Enchantability: 26.x decides which enchantments an item accepts by tag.
        // Steel armor joins the vanilla armor-slot tags (Protection, Unbreaking, Mending, ...);
        // hammers and tongs were DiggerItems in 1.20.1 and keep the same enchantments.
        // ---------------------------------------------------------------------
        getOrCreateTagBuilder(ItemTags.HEAD_ARMOR).add(ModItems.STEEL_HELMET);
        getOrCreateTagBuilder(ItemTags.CHEST_ARMOR).add(ModItems.STEEL_CHESTPLATE);
        getOrCreateTagBuilder(ItemTags.LEG_ARMOR).add(ModItems.STEEL_LEGGINGS);
        getOrCreateTagBuilder(ItemTags.FOOT_ARMOR).add(ModItems.STEEL_BOOTS);

        getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(ModTags.Items.SMITHING_HAMMERS)
                .addTag(ModTags.Items.TONGS);
        getOrCreateTagBuilder(ItemTags.MINING_ENCHANTABLE)
                .addTag(ModTags.Items.SMITHING_HAMMERS)
                .addTag(ModTags.Items.TONGS);
        getOrCreateTagBuilder(ItemTags.MINING_LOOT_ENCHANTABLE)
                .addTag(ModTags.Items.SMITHING_HAMMERS)
                .addTag(ModTags.Items.TONGS);

        // ---------------------------------------------------------------------
        // Common tool tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(
                commonTag("tools/hoes")
        ).add(
                ModItems.STEEL_HOE
                
        );

        getOrCreateTagBuilder(
                commonTag("tools/axes")
        ).add(
                ModItems.STEEL_AXE
        );

        getOrCreateTagBuilder(
                commonTag("tools/pickaxes")
        ).add(
                ModItems.STEEL_PICKAXE
        );

        getOrCreateTagBuilder(
                commonTag("tools/shovels")
        ).add(
                ModItems.STEEL_SHOVEL
                
        );

        getOrCreateTagBuilder(
                commonTag("tools/swords")
        ).add(
                ModItems.STEEL_SWORD
                
        );

        // ---------------------------------------------------------------------
        // Trimmable armor
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(
                        ModItems.STEEL_HELMET,
                        ModItems.STEEL_CHESTPLATE,
                        ModItems.STEEL_LEGGINGS,
                        ModItems.STEEL_BOOTS
                        
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
                        Items.WOODEN_SPEAR,

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
        return TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                Identifier.tryBuild("c", path)
        );
    }

    /** 26.3 port: Fabric's tag builders are ResourceKey based now; this keeps the old value-based call sites. */
    private ValueTagAppender<Item> getOrCreateTagBuilder(TagKey<Item> tag) {
        return new ValueTagAppender<>(builder(tag), item -> item.builtInRegistryHolder().key());
    }

    /** Value-based wrapper around a key-based {@link TagAppender} (shared with ModBlockTagProvider). */
    public static final class ValueTagAppender<T> {
        private final TagAppender<T> delegate;
        private final Function<T, ResourceKey<T>> keyGetter;

        public ValueTagAppender(TagAppender<T> delegate, Function<T, ResourceKey<T>> keyGetter) {
            this.delegate = delegate;
            this.keyGetter = keyGetter;
        }

        @SafeVarargs
        public final ValueTagAppender<T> add(T... values) {
            for (T value : values) delegate.add(keyGetter.apply(value));
            return this;
        }

        public ValueTagAppender<T> addTag(TagKey<T> tag) {
            delegate.addTag(tag);
            return this;
        }

        public ValueTagAppender<T> addOptionalTag(TagKey<T> tag) {
            delegate.addOptionalTag(tag);
            return this;
        }

        public ValueTagAppender<T> addOptional(ResourceKey<T> key) {
            delegate.addOptional(key);
            return this;
        }
    }
}
