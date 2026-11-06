package net.stirdrem.overgeared;

import net.fabricmc.api.DedicatedServerModInitializer;

/** Dedicated server entrypoint; see {@link OvergearedFabric#registerTieredAnvils()}. */
public class OvergearedFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        OvergearedFabric.registerTieredAnvils();
    }
}
