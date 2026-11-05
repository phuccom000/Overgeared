package net.stirdrem.overgeared.screen;

import net.minecraft.world.inventory.SimpleContainerData;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;

public class SteelSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    private SteelSmithingAnvilMenu(int syncId, Inventory inv, SteelSmithingAnvilBlockEntity entity) {
        this(syncId, inv, entity, new SimpleContainerData(entity.getContainerData().getCount()));
    }

    /** Client side: reads the block position written by {@code IPlatformHelper#openMenu}. */
    public SteelSmithingAnvilMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        this(syncId, inv, (SteelSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public SteelSmithingAnvilMenu(int syncId, Inventory inv, SteelSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), syncId, inv, entity, data, true);
    }
}
