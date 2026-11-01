package net.stirdrem.overgeared.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {

        // ---------------------------------------------------------------------
        // Tongs
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.TONGS)
                .add(
                        ModItems.IRON_TONGS.get(),
                        ModItems.STEEL_TONGS.get(),
                        ModItems.WOODEN_TONGS.get()
                );

        // ---------------------------------------------------------------------
        // Common material tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(commonTag("ingots"))
                .add(ModItems.STEEL_INGOT.get());
        getOrCreateTagBuilder(commonTag("nuggets"))
                .add(ModItems.STEEL_NUGGET.get())
                .add(ModItems.COPPER_NUGGET.get());

        // ---------------------------------------------------------------------
        // Tool parts
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.TOOL_PARTS)
                .add(
                        // Stone
                        ModItems.STONE_HAMMER_HEAD.get(),
                        ModItems.STONE_SWORD_BLADE.get(),
                        ModItems.STONE_PICKAXE_HEAD.get(),
                        ModItems.STONE_AXE_HEAD.get(),
                        ModItems.STONE_SHOVEL_HEAD.get(),
                        ModItems.STONE_HOE_HEAD.get(),

                        // Copper
                        ModItems.COPPER_HAMMER_HEAD.get(),
                        ModItems.COPPER_SWORD_BLADE.get(),
                        ModItems.COPPER_PICKAXE_HEAD.get(),
                        ModItems.COPPER_AXE_HEAD.get(),
                        ModItems.COPPER_HOE_HEAD.get(),
                        ModItems.COPPER_SHOVEL_HEAD.get(),

                        // Iron
                        ModItems.IRON_SWORD_BLADE.get(),
                        ModItems.IRON_PICKAXE_HEAD.get(),
                        ModItems.IRON_AXE_HEAD.get(),
                        ModItems.IRON_SHOVEL_HEAD.get(),
                        ModItems.IRON_HOE_HEAD.get(),

                        // Golden
                        ModItems.GOLDEN_SWORD_BLADE.get(),
                        ModItems.GOLDEN_PICKAXE_HEAD.get(),
                        ModItems.GOLDEN_AXE_HEAD.get(),
                        ModItems.GOLDEN_SHOVEL_HEAD.get(),
                        ModItems.GOLDEN_HOE_HEAD.get(),

                        // Steel
                        ModItems.STEEL_HAMMER_HEAD.get(),
                        ModItems.STEEL_SWORD_BLADE.get(),
                        ModItems.STEEL_PICKAXE_HEAD.get(),
                        ModItems.STEEL_AXE_HEAD.get(),
                        ModItems.STEEL_SHOVEL_HEAD.get(),
                        ModItems.STEEL_HOE_HEAD.get(),

                        // Arrow heads
                        ModItems.IRON_ARROW_HEAD.get(),
                        ModItems.STEEL_ARROW_HEAD.get(),
                        ModItems.DIAMOND_SHARD.get()
                );

        // ---------------------------------------------------------------------
        // Tools
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.TOOLS)
                .add(
                        ModItems.WOODEN_TONGS.get(),
                        ModItems.IRON_TONGS.get(),
                        ModItems.STEEL_TONGS.get(),
                        ModItems.SMITHING_HAMMER.get(),
                        ModItems.COPPER_SMITHING_HAMMER.get()
                );

        // ---------------------------------------------------------------------
        // Heated metals
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.HEATED_METALS)
                .add(
                        ModItems.HEATED_STEEL_INGOT.get(),
                        ModItems.HEATED_IRON_INGOT.get(),
                        ModItems.HEATED_CRUDE_STEEL.get(),
                        ModItems.HEATED_COPPER_INGOT.get(),
                        ModItems.HEATED_SILVER_INGOT.get(),
                        ModItems.HEATED_NETHERITE_ALLOY.get()
                );

        // ---------------------------------------------------------------------
        // Smithing hammers
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.SMITHING_HAMMERS)
                .add(
                        ModItems.SMITHING_HAMMER.get(),
                        ModItems.COPPER_SMITHING_HAMMER.get()
                )
                .addTag(ModTags.Items.STONE_SMITHING_HAMMERS)
                .addTag(ModTags.Items.IRON_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        getOrCreateTagBuilder(ModTags.Items.STONE_SMITHING_HAMMERS)
                .add(ModItems.COPPER_SMITHING_HAMMER.get())
                .addTag(ModTags.Items.IRON_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        getOrCreateTagBuilder(ModTags.Items.IRON_SMITHING_HAMMERS)
                .add(ModItems.SMITHING_HAMMER.get())
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
        ).add(ModItems.STEEL_INGOT.get());

        getOrCreateTagBuilder(
                commonTag("nuggets/steel")
        ).add(ModItems.STEEL_NUGGET.get());

        getOrCreateTagBuilder(
                commonTag("nuggets/copper")
        ).add(ModItems.COPPER_NUGGET.get());

        getOrCreateTagBuilder(
                commonTag("plates/copper")
        ).add(ModItems.COPPER_PLATE.get());

        getOrCreateTagBuilder(
                commonTag("plates/iron")
        ).add(ModItems.IRON_PLATE.get());

        getOrCreateTagBuilder(
                commonTag("plates/steel")
        ).add(ModItems.STEEL_PLATE.get());

        // ---------------------------------------------------------------------
        // Armor
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(
                commonTag("armors/helmets")
        ).add(
                ModItems.STEEL_HELMET.get(),
                ModItems.COPPER_HELMET.get()
        );

        getOrCreateTagBuilder(
                commonTag("armors/chestplates")
        ).add(
                ModItems.STEEL_CHESTPLATE.get(),
                ModItems.COPPER_CHESTPLATE.get()
        );

        getOrCreateTagBuilder(
                commonTag("armors/leggings")
        ).add(
                ModItems.STEEL_LEGGINGS.get(),
                ModItems.COPPER_LEGGINGS.get()
        );

        getOrCreateTagBuilder(
                commonTag("armors/boots")
        ).add(
                ModItems.STEEL_BOOTS.get(),
                ModItems.COPPER_BOOTS.get()
        );

        // ---------------------------------------------------------------------
        // Tools
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.TOOLS)
                .add(
                        ModItems.STEEL_AXE.get(),
                        ModItems.STEEL_PICKAXE.get(),
                        ModItems.STEEL_HOE.get(),
                        ModItems.STEEL_SHOVEL.get(),
                        ModItems.STEEL_SWORD.get(),
                        ModItems.COPPER_AXE.get(),
                        ModItems.COPPER_PICKAXE.get(),
                        ModItems.COPPER_HOE.get(),
                        ModItems.COPPER_SHOVEL.get(),
                        ModItems.COPPER_SWORD.get()
                );

        getOrCreateTagBuilder(ItemTags.HOES)
                .add(
                        ModItems.COPPER_HOE.get(),
                        ModItems.STEEL_HOE.get()
                );

        getOrCreateTagBuilder(ItemTags.AXES)
                .add(
                        ModItems.COPPER_AXE.get(),
                        ModItems.STEEL_AXE.get()
                );

        getOrCreateTagBuilder(ItemTags.PICKAXES)
                .add(
                        ModItems.COPPER_PICKAXE.get(),
                        ModItems.STEEL_PICKAXE.get()
                );

        getOrCreateTagBuilder(ItemTags.SHOVELS)
                .add(
                        ModItems.COPPER_SHOVEL.get(),
                        ModItems.STEEL_SHOVEL.get()
                );

        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(
                        ModItems.COPPER_SWORD.get(),
                        ModItems.STEEL_SWORD.get()
                );

        // ---------------------------------------------------------------------
        // Common tool tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(
                commonTag("tools/hoes")
        ).add(
                ModItems.STEEL_HOE.get(),
                ModItems.COPPER_HOE.get()
        );

        getOrCreateTagBuilder(
                commonTag("tools/axes")
        ).add(
                ModItems.COPPER_AXE.get(),
                ModItems.STEEL_AXE.get()
        );

        getOrCreateTagBuilder(
                commonTag("tools/pickaxes")
        ).add(
                ModItems.COPPER_PICKAXE.get(),
                ModItems.STEEL_PICKAXE.get()
        );

        getOrCreateTagBuilder(
                commonTag("tools/shovels")
        ).add(
                ModItems.STEEL_SHOVEL.get(),
                ModItems.COPPER_SHOVEL.get()
        );

        getOrCreateTagBuilder(
                commonTag("tools/swords")
        ).add(
                ModItems.STEEL_SWORD.get(),
                ModItems.COPPER_SWORD.get()
        );

        // ---------------------------------------------------------------------
        // Trimmable armor
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(
                        ModItems.STEEL_HELMET.get(),
                        ModItems.STEEL_CHESTPLATE.get(),
                        ModItems.STEEL_LEGGINGS.get(),
                        ModItems.STEEL_BOOTS.get(),

                        ModItems.COPPER_HELMET.get(),
                        ModItems.COPPER_CHESTPLATE.get(),
                        ModItems.COPPER_LEGGINGS.get(),
                        ModItems.COPPER_BOOTS.get()
                );

        // ---------------------------------------------------------------------
        // Arrows
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ItemTags.ARROWS)
                .add(
                        ModItems.LINGERING_ARROW.get(),
                        ModItems.IRON_UPGRADE_ARROW.get(),
                        ModItems.STEEL_UPGRADE_ARROW.get(),
                        ModItems.DIAMOND_UPGRADE_ARROW.get()
                );

        // ---------------------------------------------------------------------
        // Other custom tags
        // ---------------------------------------------------------------------

        getOrCreateTagBuilder(ModTags.Items.HOT_ITEMS)
                .add(Items.LAVA_BUCKET);

        getOrCreateTagBuilder(ModTags.Items.KNAPPABLE)
                .add(ModItems.ROCK.get());

        getOrCreateTagBuilder(ModTags.Items.TOOL_CAST)
                .add(
                        ModItems.CLAY_TOOL_CAST.get(),
                        ModItems.NETHER_TOOL_CAST.get()
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
        return TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.tryBuild("c", path)
        );
    }
}
