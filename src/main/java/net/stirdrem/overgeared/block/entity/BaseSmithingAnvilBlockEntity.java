package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.BaseSmithingAnvilBlock;

import java.util.*;

public abstract class BaseSmithingAnvilBlockEntity
        extends BlockEntity
        implements MenuProvider {


    protected static final int INPUT_SLOTS = 9;
    protected static final int OUTPUT_SLOT = 10;
    protected static final int BLUEPRINT_SLOT = 11;

    protected Player player;
    protected final ItemStackHandler itemHandler = new ItemStackHandler(12) {

        @Override
        protected void onContentsChanged(int slot) {

            setChanged();

            if (level == null || level.isClientSide())
                return;

            level.sendBlockUpdated(
                    getBlockPos(),
                    getBlockState(),
                    getBlockState(),
                    3
            );

            BaseSmithingAnvilBlockEntity.this.onSlotChanged(slot);
        }
    };


    protected UUID ownerUUID;
    protected long sessionStartTime;


    protected final Map<BlockPos, UUID> occupiedAnvils =
            Collections.synchronizedMap(new HashMap<>());


    protected final BaseSmithingAnvilBlock block;


    public BaseSmithingAnvilBlockEntity(
            BaseSmithingAnvilBlock block,
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        this.block = block;
    }



    /*
     * =========================
     * Abstract hooks
     * =========================
     */


    protected abstract void onSlotChanged(int slot);


    public abstract boolean isFailedResult();

    public abstract boolean hasRecipe();

    public abstract Optional<?> getCurrentRecipe();

    public abstract void tick(
            Level level,
            BlockPos pos,
            BlockState state
    );


    public abstract Component getDisplayName();


    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public abstract void increaseForgingProgress(Level pLevel, BlockPos pPos, BlockState pState);

    public abstract void craftItem();

    public abstract AnvilTier getAnvilTier();

    /*
     * =========================
     * Inventory
     * =========================
     */


    public abstract boolean hasQuality();

    public abstract boolean needsMinigame();

    public IItemHandlerModifiable getItemHandler() {
        return itemHandler;
    }


    public void drops() {

        SimpleContainer container =
                new SimpleContainer(itemHandler.getSlots());

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            container.setItem(
                    i,
                    itemHandler.getStackInSlot(i)
            );
        }


        Containers.dropContents(
                level,
                worldPosition,
                container
        );
    }




    /*
     * =========================
     * Save / Load
     * =========================
     */


    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider provider
    ) {

        super.saveAdditional(tag, provider);


        tag.put(
                "inventory",
                itemHandler.serializeNBT(provider)
        );


        if (ownerUUID != null) {
            tag.putUUID(
                    "ownerUUID",
                    ownerUUID
            );

            tag.putLong(
                    "sessionStartTime",
                    sessionStartTime
            );
        }
    }


    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider provider
    ) {

        super.loadAdditional(tag, provider);


        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(
                    provider,
                    tag.getCompound("inventory")
            );
        }


        if (tag.hasUUID("ownerUUID")) {

            ownerUUID =
                    tag.getUUID("ownerUUID");

            sessionStartTime =
                    tag.getLong("sessionStartTime");
        }
    }




    /*
     * =========================
     * Sync
     * =========================
     */


    public abstract int getHitsRemaining();

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {

        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider provider
    ) {

        CompoundTag tag =
                super.getUpdateTag(provider);

        tag.put(
                "inventory",
                itemHandler.serializeNBT(provider)
        );

        return tag;
    }





    /*
     * =========================
     * Ownership
     * =========================
     */


    protected abstract ForgingQuality determineForgingQuality();

    protected abstract ForgingQuality determineForgingQualityNoBlueprint();

    public abstract ForgingQuality minigameQuality();

    public abstract ForgingQuality qualityFromBlueprint();

    public void setOwner(UUID uuid) {

        ownerUUID = uuid;

        sessionStartTime =
                level.getGameTime();

        setChanged();
    }


    public boolean isOwned() {

        return ownerUUID != null;
    }


    public abstract void clearOwner();

    public boolean isOwnedBy(Player player) {

        return ownerUUID != null &&
                ownerUUID.equals(player.getUUID());
    }





    /*
     * =========================
     * Container validation
     * =========================
     */


    public abstract UUID getOwnerUUID();

    public abstract boolean isMinigameOn();

    public abstract void setMinigameOn(boolean value);

    public boolean stillValid(Player player) {

        if (level == null)
            return false;


        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
        ) <= 64;
    }


    public ItemStackHandler getHandler() {
        return itemHandler;
    }
}