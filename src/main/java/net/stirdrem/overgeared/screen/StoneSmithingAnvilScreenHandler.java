package net.stirdrem.overgeared.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PropertyDelegate;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;

public class StoneSmithingAnvilScreenHandler extends AbstractSmithingAnvilScreenHandler {
    public StoneSmithingAnvilScreenHandler(int syncId, PlayerInventory inv, StoneSmithingAnvilBlockEntity entity, PropertyDelegate data) {
        super(ModMenuTypes.STONE_SMITHING_ANVIL_MENU, syncId, inv, entity, data, false);
    }
}
