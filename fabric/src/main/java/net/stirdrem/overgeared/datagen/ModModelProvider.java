package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModModelProvider extends FabricModelProviderPlus {

    private static final LinkedHashMap<ResourceLocation, Float> TRIM_MATERIALS =
            new LinkedHashMap<>();

    static {
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "quartz"), 0.1F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "iron"), 0.2F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "netherite"), 0.3F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "redstone"), 0.4F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "copper"), 0.5F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "gold"), 0.6F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "emerald"), 0.7F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "diamond"), 0.8F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "lapis"), 0.9F);
        TRIM_MATERIALS.put(new ResourceLocation("minecraft", "amethyst"), 1.0F);
    }

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    // -------------------------------------------------------------------------
    // BLOCK MODELS
    // -------------------------------------------------------------------------

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {

        // ---------------------------------------------------------------------
        // Simple blocks
        // ---------------------------------------------------------------------

        generator.createTrivialCube(ModBlocks.STEEL_BLOCK);

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
            BlockModelGenerators generator,
            Block block,
            ResourceLocation model
    ) {
        generator.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(
                                PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                                        .select(
                                                Direction.NORTH,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                        )
                                        .select(
                                                Direction.EAST,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R90
                                                        )
                                        )
                                        .select(
                                                Direction.SOUTH,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R180
                                                        )
                                        )
                                        .select(
                                                Direction.WEST,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, model)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R270
                                                        )
                                        )
                        )
        );

        generator.delegateItemModel(block, model);
    }

    // =========================================================================
    // FACING + LIT BLOCK
    // =========================================================================

    private void facingLitBlock(
            BlockModelGenerators generator,
            Block block,
            String baseModelName,
            String litModelName
    ) {
        ResourceLocation baseModel =
                modLoc("block/" + baseModelName);

        ResourceLocation litModel =
                modLoc("block/" + litModelName);

        generator.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block)
                        .with(
                                PropertyDispatch.properties(
                                                BlockStateProperties.HORIZONTAL_FACING,
                                                BlockStateProperties.LIT
                                        )
                                        .select(
                                                Direction.NORTH,
                                                false,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, baseModel)
                                        )
                                        .select(
                                                Direction.EAST,
                                                false,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, baseModel)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R90
                                                        )
                                        )
                                        .select(
                                                Direction.SOUTH,
                                                false,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, baseModel)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R180
                                                        )
                                        )
                                        .select(
                                                Direction.WEST,
                                                false,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, baseModel)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R270
                                                        )
                                        )
                                        .select(
                                                Direction.NORTH,
                                                true,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, litModel)
                                        )
                                        .select(
                                                Direction.EAST,
                                                true,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, litModel)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R90
                                                        )
                                        )
                                        .select(
                                                Direction.SOUTH,
                                                true,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, litModel)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R180
                                                        )
                                        )
                                        .select(
                                                Direction.WEST,
                                                true,
                                                Variant.variant()
                                                        .with(VariantProperties.MODEL, litModel)
                                                        .with(
                                                                VariantProperties.Y_ROT,
                                                                VariantProperties.Rotation.R270
                                                        )
                                        )
                        )
        );

        generator.delegateItemModel(block, baseModel);
    }

    // -------------------------------------------------------------------------
    // ITEM MODELS
    // -------------------------------------------------------------------------

    @Override
    public void generateItemModels(ItemModelGenerators generator) {

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

        generator.generateArmorTrims((ArmorItem) ModItems.STEEL_HELMET);
        generator.generateArmorTrims((ArmorItem) ModItems.STEEL_BOOTS);
        generator.generateArmorTrims((ArmorItem) ModItems.STEEL_CHESTPLATE);
        generator.generateArmorTrims((ArmorItem) ModItems.STEEL_LEGGINGS);

        generator.generateArmorTrims((ArmorItem) ModItems.COPPER_HELMET);
        generator.generateArmorTrims((ArmorItem) ModItems.COPPER_CHESTPLATE);

        registerArmorWithOverlay(generator, (ArmorItem) ModItems.COPPER_LEGGINGS);

        generator.generateArmorTrims((ArmorItem) ModItems.COPPER_BOOTS);

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

    private void simpleItem(ItemModelGenerators generator, Item item) {
        generator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private void handheldItem(ItemModelGenerators generator, Item item) {
        generator.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    protected void registerArmorWithOverlay(
            ItemModelGenerators generators,
            ArmorItem armor
    ) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(armor);
        ResourceLocation baseModelId = ModelLocationUtils.getModelLocation(armor);

        ResourceLocation baseTexture = TextureMapping.getItemTexture(armor);

        ResourceLocation skirtOverlay = ResourceLocation.tryBuild(
                Overgeared.MOD_ID,
                "item/" + itemId.getPath() + "_overlay"
        );

        JsonArray overrides = new JsonArray();

        for (Map.Entry<ResourceLocation, Float> entry : TRIM_MATERIALS.entrySet()) {
            ResourceLocation trimMaterial = entry.getKey();
            float trimValue = entry.getValue();

            String trimName = trimMaterial.getPath();

            ResourceLocation trimModelId =
                    baseModelId.withSuffix(
                            "_" + trimName + "_trim"
                    );

            ResourceLocation trimTexture = new ResourceLocation(
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
            TextureMapping textures = new TextureMapping()
                    .put(TextureSlot.LAYER0, baseTexture)
                    .put(TextureSlot.LAYER1, trimTexture)
                    .put(TextureSlot.LAYER2, skirtOverlay);

            ModelTemplates.THREE_LAYERED_ITEM.create(
                    trimModelId,
                    textures,
                    generators.output
            );

            JsonObject predicate = new JsonObject();
            predicate.addProperty(
                    ItemModelGenerators.TRIM_TYPE_PREDICATE_ID.getPath(),
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
        TextureMapping baseTextures = new TextureMapping()
                .put(TextureSlot.LAYER0, baseTexture)
                .put(TextureSlot.LAYER1, skirtOverlay);

        ModelTemplates.TWO_LAYERED_ITEM.create(
                baseModelId,
                baseTextures,
                generators.output,
                (id, textures) -> {
                    JsonObject json =
                            ModelTemplates.TWO_LAYERED_ITEM.createBaseTemplate(
                                    id,
                                    textures
                            );

                    json.add("overrides", overrides);

                    return json;
                }
        );
    }

    private void upgradeArrowModel(
            ItemModelGenerators generator,
            Item item
    ) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String baseName = itemId.getPath();

        ResourceLocation baseTexture = modLoc(
                "item/" + baseName
        );

        ResourceLocation tippedHead = modLoc(
                "item/tipped_" + baseName + "_head"
        );

        ResourceLocation tippedBase = modLoc(
                "item/tipped_" + baseName + "_base"
        );

        ResourceLocation lingeringHead = modLoc(
                "item/lingering_" + baseName + "_head"
        );

        ResourceLocation lingeringBase = modLoc(
                "item/lingering_" + baseName + "_base"
        );

        ResourceLocation baseModel = ModelLocationUtils.getModelLocation(item);

        ResourceLocation tippedModel = modLoc(
                "item/" + baseName + "_tipped"
        );

        ResourceLocation lingeringModel = modLoc(
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

        ModelTemplates.FLAT_ITEM.create(
                baseModel,
                TextureMapping.layer0(baseTexture),
                generator.output,
                (id, textures) -> {
                    JsonObject json =
                            ModelTemplates.FLAT_ITEM.createBaseTemplate(
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

        ModelTemplates.TWO_LAYERED_ITEM.create(
                tippedModel,
                TextureMapping.layered(
                        tippedHead,
                        tippedBase
                ),
                generator.output
        );

        // ---------------------------------------------------------------------
        // Lingering arrow
        // ---------------------------------------------------------------------

        ModelTemplates.TWO_LAYERED_ITEM.create(
                lingeringModel,
                TextureMapping.layered(
                        lingeringHead,
                        lingeringBase
                ),
                generator.output
        );
    }

    private String getArmorType(ArmorItem armor) {
        return switch (armor.getEquipmentSlot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> "";
        };
    }

    private String getItemName(Item item) {
        return BuiltInRegistries.ITEM
                .getKey(item)
                .getPath();
    }

    private ResourceLocation modLoc(String path) {
        return new ResourceLocation(
                Overgeared.MOD_ID,
                path
        );
    }
}