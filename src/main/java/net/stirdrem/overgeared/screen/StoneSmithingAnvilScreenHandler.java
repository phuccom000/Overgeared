package net.stirdrem.overgeared.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;

public class StoneSmithingAnvilScreenHandler extends AbstractSmithingAnvilScreenHandler {
    public StoneSmithingAnvilScreenHandler(int syncId, Inventory inv, StoneSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STONE_SMITHING_ANVIL_MENU, syncId, inv, entity, data, false);
    }
}
