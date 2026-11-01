package net.stirdrem.overgeared.sound;

import net.minecraft.sounds.SoundEvent;

public interface AnvilSoundService {
    SoundEvent getForgingFailed();

    SoundEvent getForgingComplete();

    SoundEvent getAnvilHit();
}