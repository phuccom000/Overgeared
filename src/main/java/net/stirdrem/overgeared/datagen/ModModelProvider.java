package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModModelProvider extends FabricModelProviderPlus {

    private static final LinkedHashMap<Identifier, Float> TRIM_MATERIALS =
            new LinkedHashMap<>();

    static {
        TRIM_MATERIALS.put(new Identifier("minecraft", "quartz"), 0.1F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "iron"), 0.2F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "netherite"), 0.3F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "redstone"), 0.4F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "copper"), 0.5F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "gold"), 0.6F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "emerald"), 0.7F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "diamond"), 0.8F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "lapis"), 0.9F);
        TRIM_MATERIALS.put(new Identifier("minecraft", "amethyst"), 1.0F);
    }

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    // -------------------------------------------------------------------------
    // BLOCK MODELS
    // -------------------------------------------------------------------------

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {

        // ---------------------------------------------------------------------
        // Simple blocks
        // ---------------------------------------------------------------------

        generator.registerSimpleCubeAll(ModBlocks.STEEL_BLOCK);

        // ---------------------------------------------------------------------
        // Horizontal blocks
        // ---------------------------------------------------------------------

        horizontalBlock(
                generator,
                ModBlocks.SMITHING_ANVIL,
                modLoc("block/smithing_anvil")
        );

        horizontalBlock(
                generator,
                ModBlocks.TIER_A_SMITHING_ANVIL,
                modLoc("block/tier_a_smithing_anvil")
        );

        horizontalBlock(
                generator,
                ModBlocks.TIER_B_SMITHING_ANVIL,
                modLoc("block/tier_b_smithing_anvil")
        );

        horizontalBlock(
                generator,
                ModBlocks.STONE_SMITHING_ANVIL,
                modLoc("block/stone_anvil")
        );

        // ---------------------------------------------------------------------
        // Lit blocks
        // ---------------------------------------------------------------------

        facingLitBlock(
                generator,
                ModBlocks.ALLOY_FURNACE,
                "alloy_furnace",
                "alloy_furnace_on"
        );

        facingLitBlock(
                generator,
                ModBlocks.NETHER_ALLOY_FURNACE,
                "nether_alloy_furnace",
                "nether_alloy_furnace_on"
        );

        facingLitBlock(
                generator,
                ModBlocks.CAST_FURNACE,
                "casting_furnace",
                "casting_furnace_on"
        );
    }

    // =========================================================================
    // HORIZONTAL BLOCK
    // =========================================================================

    private void horizontalBlock(
            BlockStateModelGenerator generator,
            Block block,
            Identifier model
    ) {
        generator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(block)
                        .coordinate(
                                BlockStateVariantMap.create(Properties.HORIZONTAL_FACING)
                                        .register(
                                                Direction.NORTH,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, model)
                                        )
                                        .register(
                                                Direction.EAST,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, model)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R90
                                                        )
                                        )
                                        .register(
                                                Direction.SOUTH,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, model)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R180
                                                        )
                                        )
                                        .register(
                                                Direction.WEST,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, model)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R270
                                                        )
                                        )
                        )
        );

        generator.registerParentedItemModel(block, model);
    }

    // =========================================================================
    // FACING + LIT BLOCK
    // =========================================================================

    private void facingLitBlock(
            BlockStateModelGenerator generator,
            Block block,
            String baseModelName,
            String litModelName
    ) {
        Identifier baseModel =
                modLoc("block/" + baseModelName);

        Identifier litModel =
                modLoc("block/" + litModelName);

        generator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(block)
                        .coordinate(
                                BlockStateVariantMap.create(
                                                Properties.HORIZONTAL_FACING,
                                                Properties.LIT
                                        )
                                        .register(
                                                Direction.NORTH,
                                                false,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, baseModel)
                                        )
                                        .register(
                                                Direction.EAST,
                                                false,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, baseModel)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R90
                                                        )
                                        )
                                        .register(
                                                Direction.SOUTH,
                                                false,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, baseModel)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R180
                                                        )
                                        )
                                        .register(
                                                Direction.WEST,
                                                false,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, baseModel)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R270
                                                        )
                                        )
                                        .register(
                                                Direction.NORTH,
                                                true,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, litModel)
                                        )
                                        .register(
                                                Direction.EAST,
                                                true,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, litModel)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R90
                                                        )
                                        )
                                        .register(
                                                Direction.SOUTH,
                                                true,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, litModel)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R180
                                                        )
                                        )
                                        .register(
                                                Direction.WEST,
                                                true,
                                                BlockStateVariant.create()
                                                        .put(VariantSettings.MODEL, litModel)
                                                        .put(
                                                                VariantSettings.Y,
                                                                VariantSettings.Rotation.R270
                                                        )
                                        )
                        )
        );

        generator.registerParentedItemModel(block, baseModel);
    }

    // -------------------------------------------------------------------------
    // ITEM MODELS
    // -------------------------------------------------------------------------

    @Override
    public void generateItemModels(ItemModelGenerator generator) {

        // ---------------------------------------------------------------------
        // Simple items
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.CRUDE_STEEL);
        simpleItem(generator, ModItems.HEATED_CRUDE_STEEL);
        simpleItem(generator, ModItems.ROCK);
        simpleItem(generator, ModItems.STEEL_INGOT);
        simpleItem(generator, ModItems.STEEL_NUGGET);
        simpleItem(generator, ModItems.NETHERITE_ALLOY);
        simpleItem(generator, ModItems.COPPER_NUGGET);
        simpleItem(generator, ModItems.DIAMOND_SHARD);
        simpleItem(generator, ModItems.IRON_ARROW_HEAD);
        simpleItem(generator, ModItems.STEEL_ARROW_HEAD);
        simpleItem(generator, ModItems.UNFIRED_TOOL_CAST);
        simpleItem(generator, ModItems.CLAY_TOOL_CAST);
        simpleItem(generator, ModItems.NETHER_TOOL_CAST);

        // ---------------------------------------------------------------------
        // Upgrade arrows
        // ---------------------------------------------------------------------

        upgradeArrowModel(generator, ModItems.IRON_UPGRADE_ARROW);
        upgradeArrowModel(generator, ModItems.STEEL_UPGRADE_ARROW);
        upgradeArrowModel(generator, ModItems.DIAMOND_UPGRADE_ARROW);

        // ---------------------------------------------------------------------
        // Heated metals
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.HEATED_COPPER_INGOT);
        simpleItem(generator, ModItems.HEATED_IRON_INGOT);
        simpleItem(generator, ModItems.HEATED_STEEL_INGOT);
        simpleItem(generator, ModItems.HEATED_SILVER_INGOT);
        simpleItem(generator, ModItems.HEATED_NETHERITE_ALLOY);

        // ---------------------------------------------------------------------
        // Plates / miscellaneous
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.COPPER_PLATE);
        simpleItem(generator, ModItems.IRON_PLATE);
        simpleItem(generator, ModItems.STEEL_PLATE);

        simpleItem(generator, ModItems.STEEL_TONG);
        simpleItem(generator, ModItems.IRON_TONG);

        simpleItem(generator, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE);
        simpleItem(generator, ModItems.EMPTY_BLUEPRINT);
        simpleItem(generator, ModItems.BLUEPRINT);

        // ---------------------------------------------------------------------
        // Armor
        // ---------------------------------------------------------------------

        generator.registerArmor((ArmorItem) ModItems.STEEL_HELMET);
        generator.registerArmor((ArmorItem) ModItems.STEEL_BOOTS);
        generator.registerArmor((ArmorItem) ModItems.STEEL_CHESTPLATE);
        generator.registerArmor((ArmorItem) ModItems.STEEL_LEGGINGS);

        generator.registerArmor((ArmorItem) ModItems.COPPER_HELMET);
        generator.registerArmor((ArmorItem) ModItems.COPPER_CHESTPLATE);

        registerArmorWithOverlay(generator, (ArmorItem) ModItems.COPPER_LEGGINGS);

        generator.registerArmor((ArmorItem) ModItems.COPPER_BOOTS);

        // ---------------------------------------------------------------------
        // Handheld items
        // ---------------------------------------------------------------------

        handheldItem(generator, ModItems.IRON_TONGS);
        handheldItem(generator, ModItems.STEEL_TONGS);
        handheldItem(generator, ModItems.WOODEN_TONGS);

        handheldItem(generator, ModItems.STONE_HAMMER_HEAD);
        handheldItem(generator, ModItems.COPPER_HAMMER_HEAD);
        handheldItem(generator, ModItems.STEEL_HAMMER_HEAD);

        handheldItem(generator, ModItems.SMITHING_HAMMER);
        handheldItem(generator, ModItems.COPPER_SMITHING_HAMMER);

        handheldItem(generator, ModItems.STEEL_SWORD);
        handheldItem(generator, ModItems.STEEL_PICKAXE);
        handheldItem(generator, ModItems.STEEL_AXE);
        handheldItem(generator, ModItems.STEEL_SHOVEL);
        handheldItem(generator, ModItems.STEEL_HOE);

        handheldItem(generator, ModItems.COPPER_SWORD);
        handheldItem(generator, ModItems.COPPER_PICKAXE);
        handheldItem(generator, ModItems.COPPER_AXE);
        handheldItem(generator, ModItems.COPPER_SHOVEL);
        handheldItem(generator, ModItems.COPPER_HOE);

        // ---------------------------------------------------------------------
        // Sword blades
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.STONE_SWORD_BLADE);
        simpleItem(generator, ModItems.IRON_SWORD_BLADE);
        simpleItem(generator, ModItems.GOLDEN_SWORD_BLADE);
        simpleItem(generator, ModItems.STEEL_SWORD_BLADE);
        simpleItem(generator, ModItems.COPPER_SWORD_BLADE);

        // ---------------------------------------------------------------------
        // Pickaxe heads
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.STONE_PICKAXE_HEAD);
        simpleItem(generator, ModItems.IRON_PICKAXE_HEAD);
        simpleItem(generator, ModItems.GOLDEN_PICKAXE_HEAD);
        simpleItem(generator, ModItems.STEEL_PICKAXE_HEAD);
        simpleItem(generator, ModItems.COPPER_PICKAXE_HEAD);

        // ---------------------------------------------------------------------
        // Axe heads
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.STONE_AXE_HEAD);
        simpleItem(generator, ModItems.IRON_AXE_HEAD);
        simpleItem(generator, ModItems.GOLDEN_AXE_HEAD);
        simpleItem(generator, ModItems.STEEL_AXE_HEAD);
        simpleItem(generator, ModItems.COPPER_AXE_HEAD);

        // ---------------------------------------------------------------------
        // Shovel heads
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.STONE_SHOVEL_HEAD);
        simpleItem(generator, ModItems.IRON_SHOVEL_HEAD);
        simpleItem(generator, ModItems.GOLDEN_SHOVEL_HEAD);
        simpleItem(generator, ModItems.STEEL_SHOVEL_HEAD);
        simpleItem(generator, ModItems.COPPER_SHOVEL_HEAD);

        // ---------------------------------------------------------------------
        // Hoe heads
        // ---------------------------------------------------------------------

        simpleItem(generator, ModItems.STONE_HOE_HEAD);
        simpleItem(generator, ModItems.IRON_HOE_HEAD);
        simpleItem(generator, ModItems.GOLDEN_HOE_HEAD);
        simpleItem(generator, ModItems.STEEL_HOE_HEAD);
        simpleItem(generator, ModItems.COPPER_HOE_HEAD);
    }

    // =========================================================================
    // BASIC ITEMS
    // =========================================================================

    private void simpleItem(ItemModelGenerator generator, Item item) {
        generator.register(item, Models.GENERATED);
    }

    private void handheldItem(ItemModelGenerator generator, Item item) {
        generator.register(item, Models.HANDHELD);
    }

    protected void registerArmorWithOverlay(
            ItemModelGenerator generators,
            ArmorItem armor
    ) {
        Identifier itemId = Registries.ITEM.getId(armor);
        Identifier baseModelId = ModelIds.getItemModelId(armor);

        Identifier baseTexture = TextureMap.getId(armor);

        Identifier skirtOverlay = Identifier.of(
                Overgeared.MOD_ID,
                "item/" + itemId.getPath() + "_overlay"
        );

        JsonArray overrides = new JsonArray();

        for (Map.Entry<Identifier, Float> entry : TRIM_MATERIALS.entrySet()) {
            Identifier trimMaterial = entry.getKey();
            float trimValue = entry.getValue();

            String trimName = trimMaterial.getPath();

            Identifier trimModelId =
                    baseModelId.withSuffixedPath(
                            "_" + trimName + "_trim"
                    );

            Identifier trimTexture = new Identifier(
                    "minecraft",
                    "trims/items/"
                            + getArmorType(armor)
                            + "_trim_"
                            + trimName
            );

            /*
             * Explicitly define:
             *
             * layer0 = base armor
             * layer1 = trim
             * layer2 = skirt overlay
             */
            TextureMap textures = new TextureMap()
                    .put(TextureKey.LAYER0, baseTexture)
                    .put(TextureKey.LAYER1, trimTexture)
                    .put(TextureKey.LAYER2, skirtOverlay);

            Models.GENERATED_THREE_LAYERS.upload(
                    trimModelId,
                    textures,
                    generators.writer
            );

            JsonObject predicate = new JsonObject();
            predicate.addProperty(
                    ItemModelGenerator.TRIM_TYPE.getPath(),
                    trimValue
            );

            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty(
                    "model",
                    trimModelId.toString()
            );

            overrides.add(override);
        }

        /*
         * Base model:
         *
         * layer0 = base armor
         * layer1 = skirt overlay
         */
        TextureMap baseTextures = new TextureMap()
                .put(TextureKey.LAYER0, baseTexture)
                .put(TextureKey.LAYER1, skirtOverlay);

        Models.GENERATED_TWO_LAYERS.upload(
                baseModelId,
                baseTextures,
                generators.writer,
                (id, textures) -> {
                    JsonObject json =
                            Models.GENERATED_TWO_LAYERS.createJson(
                                    id,
                                    textures
                            );

                    json.add("overrides", overrides);

                    return json;
                }
        );
    }

    private void upgradeArrowModel(
            ItemModelGenerator generator,
            Item item
    ) {
        Identifier itemId = Registries.ITEM.getId(item);
        String baseName = itemId.getPath();

        Identifier baseTexture = modLoc(
                "item/" + baseName
        );

        Identifier tippedHead = modLoc(
                "item/tipped_" + baseName + "_head"
        );

        Identifier tippedBase = modLoc(
                "item/tipped_" + baseName + "_base"
        );

        Identifier lingeringHead = modLoc(
                "item/lingering_" + baseName + "_head"
        );

        Identifier lingeringBase = modLoc(
                "item/lingering_" + baseName + "_base"
        );

        Identifier baseModel = ModelIds.getItemModelId(item);

        Identifier tippedModel = modLoc(
                "item/" + baseName + "_tipped"
        );

        Identifier lingeringModel = modLoc(
                "item/" + baseName + "_lingering"
        );

        // ---------------------------------------------------------------------
        // Base arrow
        // ---------------------------------------------------------------------

        JsonArray overrides = new JsonArray();

        // potion_type = 1 -> tipped
        JsonObject tippedPredicate = new JsonObject();
        tippedPredicate.addProperty(
                "overgeared:potion_type",
                1.0F
        );

        JsonObject tippedOverride = new JsonObject();
        tippedOverride.add("predicate", tippedPredicate);
        tippedOverride.addProperty(
                "model",
                tippedModel.toString()
        );

        overrides.add(tippedOverride);

        // potion_type = 2 -> lingering
        JsonObject lingeringPredicate = new JsonObject();
        lingeringPredicate.addProperty(
                "overgeared:potion_type",
                2.0F
        );

        JsonObject lingeringOverride = new JsonObject();
        lingeringOverride.add("predicate", lingeringPredicate);
        lingeringOverride.addProperty(
                "model",
                lingeringModel.toString()
        );

        overrides.add(lingeringOverride);

        Models.GENERATED.upload(
                baseModel,
                TextureMap.layer0(baseTexture),
                generator.writer,
                (id, textures) -> {
                    JsonObject json =
                            Models.GENERATED.createJson(
                                    id,
                                    textures
                            );

                    json.add("overrides", overrides);

                    return json;
                }
        );

        // ---------------------------------------------------------------------
        // Tipped arrow
        // ---------------------------------------------------------------------

        Models.GENERATED_TWO_LAYERS.upload(
                tippedModel,
                TextureMap.layered(
                        tippedHead,
                        tippedBase
                ),
                generator.writer
        );

        // ---------------------------------------------------------------------
        // Lingering arrow
        // ---------------------------------------------------------------------

        Models.GENERATED_TWO_LAYERS.upload(
                lingeringModel,
                TextureMap.layered(
                        lingeringHead,
                        lingeringBase
                ),
                generator.writer
        );
    }

    private String getArmorType(ArmorItem armor) {
        return switch (armor.getSlotType()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> "";
        };
    }

    private String getItemName(Item item) {
        return Registries.ITEM
                .getId(item)
                .getPath();
    }

    private Identifier modLoc(String path) {
        return new Identifier(
                Overgeared.MOD_ID,
                path
        );
    }
}