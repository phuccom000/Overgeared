package net.stirdrem.overgeared.sound;

public final class AnvilSoundServices {
    private static AnvilSoundService INSTANCE;

    private AnvilSoundServices() {
    }

    public static void register(AnvilSoundService service) {
        INSTANCE = service;
    }

    public static AnvilSoundService get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("AnvilSoundService has not been registered");
        }
        return INSTANCE;
    }
}