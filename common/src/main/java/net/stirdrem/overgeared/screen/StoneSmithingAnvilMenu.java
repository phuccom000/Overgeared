package net.stirdrem.overgeared.screen;

import net.minecraft.world.inventory.SimpleContainerData;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;

public class StoneSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    private StoneSmithingAnvilMenu(int syncId, Inventory inv, StoneSmithingAnvilBlockEntity entity) {
        this(syncId, inv, entity, new SimpleContainerData(entity.getContainerData().getCount()));
    }

    /** Client side: reads the block position written by {@code IPlatformHelper#openMenu}. */
    public StoneSmithingAnvilMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        this(syncId, inv, (StoneSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public StoneSmithingAnvilMenu(int syncId, Inventory inv, StoneSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), syncId, inv, entity, data, false);
    }
}
