package net.stirdrem.overgeared.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.Overgeared;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Overgeared.MOD_ID);

    public static final RegistryObject<SoundEvent> ANVIL_HIT = registerSoundEvents("anvil_hit");
    public static final RegistryObject<SoundEvent> FORGING_COMPLETE = registerSoundEvents("forging_complete");
    public static final RegistryObject<SoundEvent> FORGING_FAILED = registerSoundEvents("forging_failed");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.tryBuild(Overgeared.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}