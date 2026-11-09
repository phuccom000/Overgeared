package net.stirdrem.overgeared.datagen;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceKey;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, completableFuture, OvergearedMod.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // Repair materials of the steel tool tier and steel armor (ToolMaterial / ArmorMaterial need tags since 1.21.2)
        this.tag(ModTags.Items.STEEL_TOOL_MATERIALS).add(key(ModItems.STEEL_INGOT.get()));
        this.tag(ModTags.Items.REPAIRS_STEEL_ARMOR).add(key(ModItems.STEEL_INGOT.get()));

        this.tag(ModTags.Items.TONGS)
                .add(key(ModItems.IRON_TONGS.get()),
                        key(ModItems.STEEL_TONGS.get()),
                        key(ModItems.WOODEN_TONGS.get()));
        this.tag(Tags.Items.INGOTS)
                .add(key(ModItems.STEEL_INGOT.get()));
        this.tag(Tags.Items.NUGGETS)
                .add(key(ModItems.STEEL_NUGGET.get()));
        this.tag(ModTags.Items.TOOL_PARTS)
                .add(
                        key(// Stone
                        ModItems.STONE_HAMMER_HEAD.get()),
                        key(ModItems.STONE_SWORD_BLADE.get()),
                        key(ModItems.STONE_PICKAXE_HEAD.get()),
                        key(ModItems.STONE_AXE_HEAD.get()),
                        key(ModItems.STONE_SHOVEL_HEAD.get()),
                        key(ModItems.STONE_SPEAR_HEAD.get()),
                        key(ModItems.STONE_HOE_HEAD.get()),

                        key(// Copper
                        ModItems.COPPER_HAMMER_HEAD.get()),
                        key(ModItems.COPPER_SWORD_BLADE.get()),
                        key(ModItems.COPPER_PICKAXE_HEAD.get()),
                        key(ModItems.COPPER_AXE_HEAD.get()),
                        key(ModItems.COPPER_HOE_HEAD.get()),
                        key(ModItems.COPPER_SHOVEL_HEAD.get()),
                        key(ModItems.COPPER_SPEAR_HEAD.get()),

                        key(// Iron
                        ModItems.IRON_SWORD_BLADE.get()),
                        key(ModItems.IRON_PICKAXE_HEAD.get()),
                        key(ModItems.IRON_AXE_HEAD.get()),
                        key(ModItems.IRON_SHOVEL_HEAD.get()),
                        key(ModItems.IRON_SPEAR_HEAD.get()),
                        key(ModItems.IRON_HOE_HEAD.get()),

                        key(// Golden
                        ModItems.GOLDEN_SWORD_BLADE.get()),
                        key(ModItems.GOLDEN_PICKAXE_HEAD.get()),
                        key(ModItems.GOLDEN_AXE_HEAD.get()),
                        key(ModItems.GOLDEN_SHOVEL_HEAD.get()),
                        key(ModItems.GOLDEN_SPEAR_HEAD.get()),
                        key(ModItems.GOLDEN_HOE_HEAD.get()),

                        key(// Steel
                        ModItems.STEEL_HAMMER_HEAD.get()),
                        key(ModItems.STEEL_SWORD_BLADE.get()),
                        key(ModItems.STEEL_PICKAXE_HEAD.get()),
                        key(ModItems.STEEL_AXE_HEAD.get()),
                        key(ModItems.STEEL_SHOVEL_HEAD.get()),
                        key(ModItems.STEEL_SPEAR_HEAD.get()),
                        key(ModItems.STEEL_HOE_HEAD.get()),

                        key(ModItems.IRON_ARROW_HEAD.get()),
                        key(ModItems.STEEL_ARROW_HEAD.get()),
                        key(ModItems.DIAMOND_SHARD.get()));

        this.tag(ModTags.Items.HEATED_METALS)
                .add(
                        key(ModItems.HEATED_STEEL_INGOT.get()),
                        key(ModItems.HEATED_IRON_INGOT.get()),
                        key(ModItems.HEATED_CRUDE_STEEL.get()),
                        key(ModItems.HEATED_COPPER_INGOT.get()),
                        key(ModItems.HEATED_SILVER_INGOT.get()),
                        key(ModItems.HEATED_NETHERITE_ALLOY.get()));
        this.tag(ModTags.Items.SMITHING_HAMMERS)
                .add(
                        key(ModItems.SMITHING_HAMMER.get()),
                        key(ModItems.COPPER_SMITHING_HAMMER.get()))
                .addTag(ModTags.Items.STONE_SMITHING_HAMMERS)
                .addTag(ModTags.Items.IRON_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS)
        ;
        this.tag(ModTags.Items.STONE_SMITHING_HAMMERS)
                .add(key(ModItems.COPPER_SMITHING_HAMMER.get()))
                .addTag(ModTags.Items.IRON_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);
        this.tag(ModTags.Items.IRON_SMITHING_HAMMERS)
                .add(key(ModItems.SMITHING_HAMMER.get()))
                .addTag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        this.tag(ModTags.Items.TIER_A_SMITHING_HAMMERS)
                .addTag(ModTags.Items.TIER_B_SMITHING_HAMMERS);

        this.tag(ModTags.Items.TIER_B_SMITHING_HAMMERS);
        this.tag(ItemTags.create(Identifier.parse("c:ingots/steel")))
                .add(key(ModItems.STEEL_INGOT.get()));
        this.tag(ItemTags.create(Identifier.parse("c:nuggets/steel")))
                .add(key(ModItems.STEEL_NUGGET.get()));
        this.tag(ItemTags.create(Identifier.parse("c:plates/copper")))
                .add(key(ModItems.COPPER_PLATE.get()));
        this.tag(ItemTags.create(Identifier.parse("c:plates/iron")))
                .add(key(ModItems.IRON_PLATE.get()));
        this.tag(ItemTags.create(Identifier.parse("c:plates/steel")))
                .add(key(ModItems.STEEL_PLATE.get()));
        this.tag(ItemTags.create(Identifier.parse("c:storage_blocks/steel")))
                .add(key(ModBlocks.STEEL_BLOCK.get().asItem()));

        this.tag(ItemTags.HEAD_ARMOR)
                .add(key(ModItems.STEEL_HELMET.get()))
                .add(key(Items.COPPER_HELMET))
        ;
        this.tag(ItemTags.CHEST_ARMOR)
                .add(key(ModItems.STEEL_CHESTPLATE.get()))
                .add(key(Items.COPPER_CHESTPLATE));
        this.tag(ItemTags.LEG_ARMOR)
                .add(key(ModItems.STEEL_LEGGINGS.get()))
                .add(key(Items.COPPER_LEGGINGS))
        ;
        this.tag(ItemTags.FOOT_ARMOR)
                .add(key(ModItems.STEEL_BOOTS.get()))
                .add(key(Items.COPPER_BOOTS))
        ;

        // Enchantability is decided by tag since 1.21: hammers and tongs were DiggerItems in
        // 1.20.1 and keep the same enchantments (Unbreaking, Mending, Efficiency, Fortune, ...)
        this.tag(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(ModTags.Items.SMITHING_HAMMERS)
                .addTag(ModTags.Items.TONGS);
        this.tag(ItemTags.MINING_ENCHANTABLE)
                .addTag(ModTags.Items.SMITHING_HAMMERS)
                .addTag(ModTags.Items.TONGS);
        this.tag(ItemTags.MINING_LOOT_ENCHANTABLE)
                .addTag(ModTags.Items.SMITHING_HAMMERS)
                .addTag(ModTags.Items.TONGS);

        this.tag(Tags.Items.TOOLS)
                .add(
                        key(ModItems.STEEL_AXE.get()),
                        key(ModItems.STEEL_PICKAXE.get()),
                        key(ModItems.STEEL_HOE.get()),
                        key(ModItems.STEEL_SHOVEL.get()),
                        key(ModItems.STEEL_SWORD.get()),
                        key(ModItems.STEEL_SPEAR.get()),
                        key(Items.COPPER_AXE),
                        key(Items.COPPER_PICKAXE),
                        key(Items.COPPER_HOE),
                        key(Items.COPPER_SHOVEL),
                        key(Items.COPPER_SWORD),
                        key(ModItems.WOODEN_TONGS.get()),
                        key(ModItems.IRON_TONGS.get()),
                        key(ModItems.STEEL_TONGS.get()),
                        key(ModItems.SMITHING_HAMMER.get()),
                        key(ModItems.COPPER_SMITHING_HAMMER.get()));
        // Add to vanilla Minecraft tags
        this.tag(ItemTags.HOES)
                .add(key(Items.COPPER_HOE))
                .add(key(ModItems.STEEL_HOE.get()));
        this.tag(ItemTags.AXES)
                .add(key(Items.COPPER_AXE))
                .add(key(ModItems.STEEL_AXE.get()))
        ;
        this.tag(ItemTags.PICKAXES)
                .add(key(Items.COPPER_PICKAXE))
                .add(key(ModItems.STEEL_PICKAXE.get()))
        ;
        this.tag(ItemTags.SHOVELS)
                .add(key(Items.COPPER_SHOVEL))
                .add(key(ModItems.STEEL_SHOVEL.get()));
        this.tag(ItemTags.SWORDS)
                .add(key(Items.COPPER_SWORD))
                .add(key(ModItems.STEEL_SWORD.get()));
        // 26.x spears; vanilla's spear enchantment tags (lunge, melee, durability) include #minecraft:spears
        this.tag(ItemTags.SPEARS)
                .add(key(ModItems.STEEL_SPEAR.get()));
        // Hoes
        this.tag(ItemTags.create(Identifier.parse("c:tools/hoes")))
                .add(key(ModItems.STEEL_HOE.get()))
                .add(key(Items.COPPER_HOE));

        // Axes
        this.tag(ItemTags.create(Identifier.parse("c:tools/axes")))
                .add(key(Items.COPPER_AXE))
                .add(key(ModItems.STEEL_AXE.get()))
        ;

        // Pickaxes
        this.tag(ItemTags.create(Identifier.parse("c:tools/pickaxes")))
                .add(key(Items.COPPER_PICKAXE))
                .add(key(ModItems.STEEL_PICKAXE.get()))
        ;

        // Shovels
        this.tag(ItemTags.create(Identifier.parse("c:tools/shovels")))
                .add(key(ModItems.STEEL_SHOVEL.get()))
                .add(key(Items.COPPER_SHOVEL))
        ;

        // Swords
        this.tag(ItemTags.create(Identifier.parse("c:tools/swords")))
                .add(key(ModItems.STEEL_SWORD.get()))
                .add(key(Items.COPPER_SWORD))
        ;

        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(
                        key(ModItems.STEEL_HELMET.get()),
                        key(ModItems.STEEL_CHESTPLATE.get()),
                        key(ModItems.STEEL_LEGGINGS.get()),
                        key(ModItems.STEEL_BOOTS.get()),
                        key(Items.COPPER_HELMET),
                        key(Items.COPPER_CHESTPLATE),
                        key(Items.COPPER_LEGGINGS),
                        key(Items.COPPER_BOOTS));
        this.tag(ItemTags.ARROWS)
                .add(
                        key(ModItems.LINGERING_ARROW.get()),
                        key(ModItems.IRON_UPGRADE_ARROW.get()),
                        key(ModItems.STEEL_UPGRADE_ARROW.get()),
                        key(ModItems.DIAMOND_UPGRADE_ARROW.get()));
        this.tag(ModTags.Items.HOT_ITEMS)
                .add(
                        key(Items.LAVA_BUCKET));
        this.tag(ModTags.Items.KNAPPABLES)
                .add(
                        key(ModItems.ROCK.get()));
        this.tag(ModTags.Items.TOOL_CAST)
                .add(
                        key(ModItems.UNFIRED_TOOL_CAST.get()),
                        key(ModItems.CLAY_TOOL_CAST.get()),
                        key(ModItems.NETHER_TOOL_CAST.get()));

        this.tag(ModTags.Items.USABLE_TOOL_CAST)
                .add(
                        key(ModItems.CLAY_TOOL_CAST.get()),
                        key(ModItems.NETHER_TOOL_CAST.get()));
        this.tag(ModTags.Items.QUALITY_BLACKLIST)
                .add(
                        key(Items.WOODEN_SWORD),
                        key(Items.WOODEN_PICKAXE),
                        key(Items.WOODEN_AXE),
                        key(Items.WOODEN_SHOVEL),
                        key(Items.WOODEN_HOE),
                        key(Items.WOODEN_SPEAR),
                        key(Items.LEATHER_HELMET),
                        key(Items.LEATHER_CHESTPLATE),
                        key(Items.LEATHER_LEGGINGS),
                        key(Items.LEATHER_BOOTS),
                        key(Items.FLINT_AND_STEEL),
                        key(Items.ELYTRA));
    }

    private static ResourceKey<Item> key(net.minecraft.world.level.ItemLike value) {
        return value.asItem().builtInRegistryHolder().key();
    }
}

