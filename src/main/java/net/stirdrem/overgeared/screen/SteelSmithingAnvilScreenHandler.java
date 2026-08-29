package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PropertyDelegate;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;

public class SteelSmithingAnvilScreenHandler extends AbstractSmithingAnvilScreenHandler {
    public SteelSmithingAnvilScreenHandler(int syncId, PlayerInventory inv, SteelSmithingAnvilBlockEntity entity, PropertyDelegate data) {
        super(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU, syncId, inv, entity, data, true);
    }
}
