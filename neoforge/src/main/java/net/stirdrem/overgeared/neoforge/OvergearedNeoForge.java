package net.stirdrem.overgeared.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.loot.ModLootModifiers;

@Mod(OvergearedMod.MOD_ID)
public final class OvergearedNeoForge {

    public OvergearedNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgePlatformHelper.modBus = modEventBus;

        OvergearedMod.init();
        ModLootModifiers.register(modEventBus);
        NeoForgeCapabilities.register(modEventBus);

        modEventBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(OvergearedMod::commonSetup));
    }
}
