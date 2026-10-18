package net.stirdrem.overgeared.datapack;

import net.minecraft.world.item.Item;
import net.stirdrem.overgeared.item.ModItems;

public class ForgeRockInteractionReloadListener extends RockInteractionReloadListener {

    @Override
    protected Item getRock() {
        return ModItems.ROCK.get();
    }
}
