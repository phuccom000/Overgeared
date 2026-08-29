package net.stirdrem.overgeared.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;

public class ModSounds {

    public static final SoundEvent ANVIL_HIT = registerSoundEvents("anvil_hit");
    public static final SoundEvent FORGING_COMPLETE = registerSoundEvents("forging_complete");
    public static final SoundEvent FORGING_FAILED = registerSoundEvents("forging_failed");

    private static SoundEvent registerSoundEvents(String name) {
        Identifier id = Overgeared.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {
    }
}
