package net.stirdrem.overgeared.screen;

import net.minecraft.world.inventory.SimpleContainerData;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;

public class TierBSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    private TierBSmithingAnvilMenu(int syncId, Inventory inv, TierBSmithingAnvilBlockEntity entity) {
        this(syncId, inv, entity, new SimpleContainerData(entity.getContainerData().getCount()));
    }

    /** Client side: reads the block position written by {@code IPlatformHelper#openMenu}. */
    public TierBSmithingAnvilMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        this(syncId, inv, (TierBSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public TierBSmithingAnvilMenu(int syncId, Inventory inv, TierBSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(), syncId, inv, entity, data, true);
    }
}
