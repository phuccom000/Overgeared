package net.stirdrem.overgeared;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.stirdrem.overgeared.datagen.*;

public class OvergearedDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        // Explicit lambdas: method refs to multi-constructor providers are ambiguous between
        // Pack.Factory and Pack.RegistryDependentFactory.
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<ModRecipeProvider>) ModRecipeProvider::new);
        pack.addProvider((output, registries) -> new ModPoiTagProvider(output, registries));
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<ModWorldGenerator>) ModWorldGenerator::new);
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<ModBlockTagProvider>) ModBlockTagProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<ModItemTagProvider>) ModItemTagProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<ModLootTableProvider>) ModLootTableProvider::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<ModModelProvider>) ModModelProvider::new);

    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
		/*registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::boostrap);
		registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::boostrap);
		registryBuilder.addRegistry(RegistryKeys.BIOME, ModBiomes::boostrap);
		registryBuilder.addRegistry(RegistryKeys.DIMENSION_TYPE, ModDimensions::bootstrapType);*/
    }
}