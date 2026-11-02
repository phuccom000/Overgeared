package net.stirdrem.overgeared.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;

public class ModSounds {
    private static final RegistrationProvider<SoundEvent> SOUND_EVENTS = RegistrationProvider.get(Registries.SOUND_EVENT);

    public static final RegistryEntry<SoundEvent> ANVIL_HIT = registerSoundEvent("anvil_hit");
    public static final RegistryEntry<SoundEvent> FORGING_COMPLETE = registerSoundEvent("forging_complete");
    public static final RegistryEntry<SoundEvent> FORGING_FAILED = registerSoundEvent("forging_failed");

    private static RegistryEntry<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Overgeared.id(name)));
    }

    /** Class-loads this registry. Call from the loader entrypoint. */
    public static void init() {
    }
}
