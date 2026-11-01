package net.stirdrem.overgeared.sound;

import net.minecraft.sounds.SoundEvent;

public class FabricAnvilSoundService implements AnvilSoundService {
    @Override
    public SoundEvent getForgingFailed() {
        return ModSounds.FORGING_FAILED;
    }

    @Override
    public SoundEvent getForgingComplete() {
        return ModSounds.FORGING_COMPLETE;
    }

    @Override
    public SoundEvent getAnvilHit() {
        return ModSounds.ANVIL_HIT;
    }
}