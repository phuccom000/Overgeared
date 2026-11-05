package net.stirdrem.overgeared.screen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.block.entity.TieredSmithingAnvilBlockEntity;

public class TieredSmithingAnvilMenu extends AbstractSmithingAnvilMenu {

    public TieredSmithingAnvilMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, (TieredSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public TieredSmithingAnvilMenu(int containerId, Inventory inv, TieredSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIERED_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, true);
    }
}
