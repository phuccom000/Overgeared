package net.stirdrem.overgeared.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.stirdrem.overgeared.Overgeared;

public class ModSounds {

    public static final SoundEvent ANVIL_HIT = registerSoundEvents("anvil_hit");
    public static final SoundEvent FORGING_COMPLETE = registerSoundEvents("forging_complete");
    public static final SoundEvent FORGING_FAILED = registerSoundEvents("forging_failed");

    private static SoundEvent registerSoundEvents(String name) {
        ResourceLocation id = Overgeared.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
    }
}
