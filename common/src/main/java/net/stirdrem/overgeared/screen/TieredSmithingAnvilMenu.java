package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.block.entity.TieredSmithingAnvilBlockEntity;

public class TieredSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    private TieredSmithingAnvilMenu(int syncId, Inventory inv, TieredSmithingAnvilBlockEntity entity) {
        this(syncId, inv, entity, new SimpleContainerData(entity.getContainerData().getCount()));
    }

    /** Client side: reads the block position written by {@code IPlatformHelper#openMenu}. */
    public TieredSmithingAnvilMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        this(syncId, inv, (TieredSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public TieredSmithingAnvilMenu(int syncId, Inventory inv, TieredSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIERED_SMITHING_ANVIL_MENU.get(), syncId, inv, entity, data);
    }
}
