package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PropertyDelegate;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;

public class TierBSmithingAnvilScreenHandler extends AbstractSmithingAnvilScreenHandler {
    public TierBSmithingAnvilScreenHandler(int syncId, PlayerInventory inv, TierBSmithingAnvilBlockEntity entity, PropertyDelegate data) {
        super(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU, syncId, inv, entity, data, true);
    }
}
