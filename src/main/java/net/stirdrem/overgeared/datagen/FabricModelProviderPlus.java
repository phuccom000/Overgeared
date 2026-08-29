package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
            Model model,
            ItemModelGenerator itemModelGenerator,
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
            Model model,
            ItemModelGenerator itemModelGenerator,
            boolean joinConditions,
            OverrideCondition... conditions) {

        Identifier itemId = Registries.ITEM.getId(item);

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
                    Models.GENERATED,
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
                            Models.GENERATED,
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

        Identifier modelId =
                new Identifier(
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

        TextureMap textures;

        if (item instanceof DyeableItem) {

            textures = TextureMap.layered(
                    new Identifier(
                            namespace,
                            "item/" + path
                    ),
                    new Identifier(
                            namespace,
                            "item/" + path + "_overlay"
                    )
            );

        } else {

            textures = TextureMap.layer0(
                    new Identifier(
                            namespace,
                            "item/" + path
                    )
            );
        }

        /*
         * Model.upload() is the Yarn 1.20.1 equivalent of the low-level
         * model creation used here.
         */
        model.upload(
                modelId,
                textures,
                itemModelGenerator.writer,
                (id, textureMap) -> {

                    JsonObject json =
                            model.createJson(id, textureMap);

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
            Model model,
            String modelName,
            ItemModelGenerator itemModelGenerator) {

        Identifier itemId =
                Registries.ITEM.getId(item);

        String namespace =
                itemId.getNamespace();

        Identifier modelId =
                new Identifier(
                        namespace,
                        "item/" + modelName
                );

        if (item instanceof DyeableItem) {

            TextureMap textures =
                    TextureMap.layered(
                            new Identifier(
                                    namespace,
                                    "item/" + modelName
                            ),
                            new Identifier(
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

            Model layeredModel =
                    new Model(
                            Optional.of(
                                    new Identifier(
                                            "minecraft",
                                            "item/handheld"
                                    )
                            ),
                            Optional.empty()
                    );

            layeredModel.upload(
                    modelId,
                    textures,
                    itemModelGenerator.writer
            );

        } else {

            TextureMap textures =
                    TextureMap.layer0(
                            new Identifier(
                                    namespace,
                                    "item/" + modelName
                            )
                    );

            model.upload(
                    modelId,
                    textures,
                    itemModelGenerator.writer
            );
        }
    }

    // =========================================================================
    // OVERRIDES
    // =========================================================================

    private void addOverride(
            JsonArray overrides,
            String namespace,
            Identifier predicateKey,
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
            Model model,
            ItemModelGenerator itemModelGenerator) {

        Identifier itemId =
                Registries.ITEM.getId(item);

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

            Identifier modelId =
                    new Identifier(
                            itemId.getNamespace(),
                            "item/" +
                                    itemId.getPath() +
                                    "/" +
                                    pattern
                    );

            TextureMap textures =
                    TextureMap.layer0(
                            new Identifier(
                                    itemId.getNamespace(),
                                    "item/" +
                                            itemId.getPath() +
                                            "/" +
                                            pattern
                            )
                    );

            model.upload(
                    modelId,
                    textures,
                    itemModelGenerator.writer
            );
        }
    }

    // =========================================================================
    // CUSTOM MODEL NAME
    // =========================================================================

    protected void registerWCustomName(
            Item item,
            Model model,
            ItemModelGenerator itemModelGenerator,
            String modelName,
            Identifier texturePath) {

        Identifier itemId =
                Registries.ITEM.getId(item);

        Identifier modelId;

        if (modelName.isEmpty()) {

            modelId =
                    new Identifier(
                            itemId.getNamespace(),
                            "item/" + itemId.getPath()
                    );

        } else {

            modelId =
                    new Identifier(
                            itemId.getNamespace(),
                            "item/" + modelName
                    );
        }

        TextureMap texture;

        if (texturePath != null) {

            texture =
                    TextureMap.layer0(texturePath);

        } else {

            texture =
                    TextureMap.layer0(
                            new Identifier(
                                    itemId.getNamespace(),
                                    "item/" + itemId.getPath()
                            )
                    );
        }

        model.upload(
                modelId,
                texture,
                itemModelGenerator.writer
        );
    }

    // =========================================================================
    // OVERRIDE CONDITION
    // =========================================================================

    public record OverrideCondition(
            Identifier predicateKey,
            Number predicateValue) {

        String getModelName(String basePath) {
            return basePath +
                    "_" +
                    predicateKey.getPath();
        }
    }
}