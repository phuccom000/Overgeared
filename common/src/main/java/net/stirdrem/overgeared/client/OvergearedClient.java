package net.stirdrem.overgeared.client;

import net.stirdrem.overgeared.compat.ModCompat;


/**
 * Client setup shared by both loaders.
 */
public final class OvergearedClient {
    private OvergearedClient() {
    }

    public static void init() {
        // Initialize client-side mod compatibility
        ModCompat.initClient();
    }

    // Arrow model variants (base / tipped / lingering) and potion tints are defined in the item
    // model files since 1.21.4 (assets/overgeared/items/*.json); ItemProperties no longer exists.
}
