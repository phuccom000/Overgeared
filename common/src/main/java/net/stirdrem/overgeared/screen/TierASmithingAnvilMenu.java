package net.stirdrem.overgeared.screen;

import net.minecraft.world.inventory.SimpleContainerData;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;

public class TierASmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    private TierASmithingAnvilMenu(int syncId, Inventory inv, TierASmithingAnvilBlockEntity entity) {
        this(syncId, inv, entity, new SimpleContainerData(entity.getContainerData().getCount()));
    }

    /** Client side: reads the block position written by {@code IPlatformHelper#openMenu}. */
    public TierASmithingAnvilMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        this(syncId, inv, (TierASmithingAnvilBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public TierASmithingAnvilMenu(int syncId, Inventory inv, TierASmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(), syncId, inv, entity, data, true);
    }
}
