package net.stirdrem.overgeared.util;

import net.stirdrem.overgeared.ForgingQuality;

public class ForgingState {

    private static ForgingQuality quality;


    public static void setQuality(ForgingQuality value) {
        quality = value;
    }


    public static ForgingQuality getQuality() {
        return quality;
    }
}