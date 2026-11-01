package net.stirdrem.overgeared.sound;

import net.minecraft.sounds.SoundEvent;

public class FabricAnvilSoundService implements AnvilSoundService {
    @Override
    public SoundEvent getForgingFailed() {
        return ModSounds.FORGING_FAILED.get();
    }

    @Override
    public SoundEvent getForgingComplete() {
        return ModSounds.FORGING_COMPLETE.get();
    }

    @Override
    public SoundEvent getAnvilHit() {
        return ModSounds.ANVIL_HIT.get();
    }
}