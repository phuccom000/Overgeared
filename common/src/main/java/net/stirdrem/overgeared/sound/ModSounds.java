package net.stirdrem.overgeared.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.stirdrem.overgeared.registry.DeferredHolder;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, OvergearedMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ANVIL_HIT = registerSoundEvents("anvil_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> FORGING_COMPLETE = registerSoundEvents("forging_complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> FORGING_FAILED = registerSoundEvents("forging_failed");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(OvergearedMod.loc(name)));
    }

    public static void register() {
        SOUND_EVENTS.register();
    }
}