package net.stirdrem.overgeared;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.client.ClientModEvents;
import net.stirdrem.overgeared.compat.ModCompat;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.item.ModCreativeModeTabs;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.loot.ModLootModifiers;
import net.stirdrem.overgeared.recipe.ModRecipeSerializers;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import net.stirdrem.overgeared.sound.ModSounds;
import org.slf4j.Logger;

@Mod(OvergearedMod.MOD_ID)
public final class OvergearedMod {
    public static final String MOD_ID = "overgeared";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final RecipeBookType FORGING = RecipeBookType.valueOf("OVERGEARED_FORGING");

    public OvergearedMod(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ClientModEvents::init);
        }
        ModAdvancementTriggers.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCapabilities.register(modEventBus);
        ModComponents.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.SERVER_CONFIG);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);

        ModCompat.init();
    }

    public static ResourceLocation loc(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
