package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PropertyDelegate;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;

public class TierASmithingAnvilScreenHandler extends AbstractSmithingAnvilScreenHandler {
    public TierASmithingAnvilScreenHandler(int syncId, PlayerInventory inv, TierASmithingAnvilBlockEntity entity, PropertyDelegate data) {
        super(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU, syncId, inv, entity, data, true);
    }
}
