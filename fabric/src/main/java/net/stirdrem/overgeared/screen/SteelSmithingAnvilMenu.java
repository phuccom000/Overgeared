package net.stirdrem.overgeared.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;

public class SteelSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    public SteelSmithingAnvilMenu(int syncId, Inventory inv, SteelSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU, syncId, inv, entity, data, true);
    }
}
