package net.stirdrem.overgeared.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;

public class TierASmithingAnvilScreenHandler extends AbstractSmithingAnvilScreenHandler {
    public TierASmithingAnvilScreenHandler(int syncId, Inventory inv, TierASmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU, syncId, inv, entity, data, true);
    }
}
