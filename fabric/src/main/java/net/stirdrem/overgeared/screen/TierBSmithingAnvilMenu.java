package net.stirdrem.overgeared.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;

public class TierBSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    public TierBSmithingAnvilMenu(int syncId, Inventory inv, TierBSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU, syncId, inv, entity, data, true);
    }
}
