package net.stirdrem.overgeared.sound;

import net.minecraft.sounds.SoundEvent;

public interface GetRecipeType {
    SoundEvent getForgingFailed();

    SoundEvent getForgingComplete();

    SoundEvent getAnvilHit();
}