package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import java.util.*;

public abstract class FabricModelProviderPlus extends FabricModelProvider {

    public FabricModelProviderPlus(FabricDataOutput output) {
        super(output);
    }

    // =========================================================================
    // ITEM WITH CONDITIONS
    // =========================================================================

    protected void registerItemWConditions(
            Item item,
            ModelTemplate model,
            ItemModelGenerators itemModelGenerator,
            OverrideCondition... conditions) {

        registerItemWConditions(
                item,
                model,
                itemModelGenerator,
                true,
                conditions
        );
    }

    protected void registerItemWConditions(
            Item item,
            ModelTemplate model,
            ItemModelGenerators itemModelGenerator,
            boolean joinConditions,
            OverrideCondition... conditions) {

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        String namespace = itemId.getNamespace();
        String path = itemId.getPath();

        Set<String> generatedModels = new HashSet<>();
        JsonArray overrides = new JsonArray();

        // ---------------------------------------------------------------------
        // Generate individual condition models
        // ---------------------------------------------------------------------

        for (OverrideCondition condition : conditions) {

            String modelName = condition.getModelName(path);

            generateOverrideModel(
                    item,
                    ModelTemplates.FLAT_ITEM,
                    modelName,
                    itemModelGenerator
            );

            generatedModels.add(modelName);

            addOverride(
                    overrides,
                    namespace,
                    condition.predicateKey(),
                    condition.predicateValue(),
                    modelName
            );
        }

        // ---------------------------------------------------------------------
        // Generate combined conditions
        // ---------------------------------------------------------------------

        if (joinConditions && conditions.length > 1) {

            List<List<OverrideCondition>> allCombinations =
                    generateAllCombinations(conditions);

            for (List<OverrideCondition> combination : allCombinations) {

                if (combination.size() <= 1) {
                    continue;
                }

                JsonObject combinedPredicate = new JsonObject();
                List<String> modelNames = new ArrayList<>();

                for (OverrideCondition condition : combination) {

                    combinedPredicate.addProperty(
                            condition.predicateKey().toString(),
                            condition.predicateValue()
                    );

                    modelNames.add(
                            condition.getModelName(path)
                    );
                }

                String combinedModelName =
                        combineMultipleModelNames(modelNames);

                if (!generatedModels.contains(combinedModelName)) {

                    generateOverrideModel(
                            item,
                            ModelTemplates.FLAT_ITEM,
                            combinedModelName,
                            itemModelGenerator
                    );

                    generatedModels.add(combinedModelName);
                }

                addOverride(
                        overrides,
                        namespace,
                        combinedPredicate,
                        combinedModelName
                );
            }
        }

        // ---------------------------------------------------------------------
        // Main model
        // ---------------------------------------------------------------------

        ResourceLocation modelId =
                new ResourceLocation(
                        namespace,
                        "item/" + path
                );

        /*
         * Normal item:
         *
         * layer0 = item/<path>
         *
         * Dyeable leather:
         *
         * layer0 = item/<path>
         * layer1 = item/<path>_overlay
         */

        TextureMapping textures;

        if (item instanceof DyeableLeatherItem) {

            textures = TextureMapping.layered(
                    new ResourceLocation(
                            namespace,
                            "item/" + path
                    ),
                    new ResourceLocation(
                            namespace,
                            "item/" + path + "_overlay"
                    )
            );

        } else {

            textures = TextureMapping.layer0(
                    new ResourceLocation(
                            namespace,
                            "item/" + path
                    )
            );
        }

        /*
         * Model.upload() is the Yarn 1.20.1 equivalent of the low-level
         * model creation used here.
         */
        model.create(
                modelId,
                textures,
                itemModelGenerator.output,
                (id, textureMap) -> {

                    JsonObject json =
                            model.createBaseTemplate(id, textureMap);

                    json.add(
                            "overrides",
                            overrides
                    );

                    return json;
                }
        );
    }

    // =========================================================================
    // ALL COMBINATIONS
    // =========================================================================

    private List<List<OverrideCondition>> generateAllCombinations(
            OverrideCondition[] conditions) {

        List<List<OverrideCondition>> allCombinations =
                new ArrayList<>();

        int n = conditions.length;

        for (int i = 1; i < (1 << n); i++) {

            List<OverrideCondition> combination =
                    new ArrayList<>();

            for (int j = 0; j < n; j++) {

                if ((i & (1 << j)) != 0) {
                    combination.add(conditions[j]);
                }
            }

            allCombinations.add(combination);
        }

        return allCombinations;
    }

    // =========================================================================
    // COMBINED MODEL NAME
    // =========================================================================

    private String combineMultipleModelNames(
            List<String> modelNames) {

        if (modelNames.isEmpty()) {
            return "";
        }

        if (modelNames.size() == 1) {
            return modelNames.get(0);
        }

        String[] firstParts =
                modelNames.get(0).split("_");

        String baseName =
                firstParts[0];

        for (int i = 1; i < firstParts.length; i++) {

            String potentialBase =
                    baseName + "_" + firstParts[i];

            boolean allStartWith = true;

            for (String modelName : modelNames) {

                if (!modelName.startsWith(
                        potentialBase + "_")) {

                    allStartWith = false;
                    break;
                }
            }

            if (allStartWith) {
                baseName = potentialBase;
            } else {
                break;
            }
        }

        Set<String> conditions =
                new HashSet<>();

        for (String modelName : modelNames) {

            String conditionPart =
                    modelName.substring(
                            baseName.length()
                    );

            if (conditionPart.startsWith("_")) {
                conditionPart =
                        conditionPart.substring(1);
            }

            if (!conditionPart.isEmpty()) {
                conditions.add(conditionPart);
            }
        }

        List<String> sortedConditions =
                new ArrayList<>(conditions);

        sortedConditions.sort(String::compareTo);

        return baseName + "_" +
                String.join("_", sortedConditions);
    }

    // =========================================================================
    // OVERRIDE MODEL
    // =========================================================================

    private void generateOverrideModel(
            Item item,
            ModelTemplate model,
            String modelName,
            ItemModelGenerators itemModelGenerator) {

        ResourceLocation itemId =
                BuiltInRegistries.ITEM.getKey(item);

        String namespace =
                itemId.getNamespace();

        ResourceLocation modelId =
                new ResourceLocation(
                        namespace,
                        "item/" + modelName
                );

        if (item instanceof DyeableLeatherItem) {

            TextureMapping textures =
                    TextureMapping.layered(
                            new ResourceLocation(
                                    namespace,
                                    "item/" + modelName
                            ),
                            new ResourceLocation(
                                    namespace,
                                    "item/" + modelName + "_overlay"
                            )
                    );

            /*
             * Fabric/Yarn 1.20.1 doesn't have the Forge
             * ModelTemplate/TextureSlot API you're using.
             *
             * Generate the layered model directly.
             */

            ModelTemplate layeredModel =
                    new ModelTemplate(
                            Optional.of(
                                    new ResourceLocation(
                                            "minecraft",
                                            "item/handheld"
                                    )
                            ),
                            Optional.empty()
                    );

            layeredModel.create(
                    modelId,
                    textures,
                    itemModelGenerator.output
            );

        } else {

            TextureMapping textures =
                    TextureMapping.layer0(
                            new ResourceLocation(
                                    namespace,
                                    "item/" + modelName
                            )
                    );

            model.create(
                    modelId,
                    textures,
                    itemModelGenerator.output
            );
        }
    }

    // =========================================================================
    // OVERRIDES
    // =========================================================================

    private void addOverride(
            JsonArray overrides,
            String namespace,
            ResourceLocation predicateKey,
            Number predicateValue,
            String modelName) {

        JsonObject predicate =
                new JsonObject();

        predicate.addProperty(
                predicateKey.toString(),
                predicateValue
        );

        addOverride(
                overrides,
                namespace,
                predicate,
                modelName
        );
    }

    private void addOverride(
            JsonArray overrides,
            String namespace,
            JsonObject predicate,
            String modelName) {

        JsonObject override =
                new JsonObject();

        override.add(
                "predicate",
                predicate
        );

        override.addProperty(
                "model",
                namespace + ":item/" + modelName
        );

        overrides.add(override);
    }

    // =========================================================================
    // BANNER PATTERNS
    // =========================================================================

    protected void generateBannerPatternModels(
            Item item,
            ModelTemplate model,
            ItemModelGenerators itemModelGenerator) {

        ResourceLocation itemId =
                BuiltInRegistries.ITEM.getKey(item);

        String[] bannerPatternNames = {
                "bl", "bo", "br", "bri", "bs", "bt",
                "bts", "cbo", "cr", "cre", "cs", "dls",
                "drs", "flo", "glb", "gra", "gru", "hh",
                "hhb", "ld", "ls", "lud", "mc", "moj",
                "mr", "ms", "pig", "rd", "rs", "rud",
                "sc", "sku", "ss", "tl", "tr", "ts",
                "tt", "tts", "vh", "vhr"
        };

        for (String pattern : bannerPatternNames) {

            ResourceLocation modelId =
                    new ResourceLocation(
                            itemId.getNamespace(),
                            "item/" +
                                    itemId.getPath() +
                                    "/" +
                                    pattern
                    );

            TextureMapping textures =
                    TextureMapping.layer0(
                            new ResourceLocation(
                                    itemId.getNamespace(),
                                    "item/" +
                                            itemId.getPath() +
                                            "/" +
                                            pattern
                            )
                    );

            model.create(
                    modelId,
                    textures,
                    itemModelGenerator.output
            );
        }
    }

    // =========================================================================
    // CUSTOM MODEL NAME
    // =========================================================================

    protected void registerWCustomName(
            Item item,
            ModelTemplate model,
            ItemModelGenerators itemModelGenerator,
            String modelName,
            ResourceLocation texturePath) {

        ResourceLocation itemId =
                BuiltInRegistries.ITEM.getKey(item);

        ResourceLocation modelId;

        if (modelName.isEmpty()) {

            modelId =
                    new ResourceLocation(
                            itemId.getNamespace(),
                            "item/" + itemId.getPath()
                    );

        } else {

            modelId =
                    new ResourceLocation(
                            itemId.getNamespace(),
                            "item/" + modelName
                    );
        }

        TextureMapping texture;

        if (texturePath != null) {

            texture =
                    TextureMapping.layer0(texturePath);

        } else {

            texture =
                    TextureMapping.layer0(
                            new ResourceLocation(
                                    itemId.getNamespace(),
                                    "item/" + itemId.getPath()
                            )
                    );
        }

        model.create(
                modelId,
                texture,
                itemModelGenerator.output
        );
    }

    // =========================================================================
    // OVERRIDE CONDITION
    // =========================================================================

    public record OverrideCondition(
            ResourceLocation predicateKey,
            Number predicateValue) {

        String getModelName(String basePath) {
            return basePath +
                    "_" +
                    predicateKey.getPath();
        }
    }
}