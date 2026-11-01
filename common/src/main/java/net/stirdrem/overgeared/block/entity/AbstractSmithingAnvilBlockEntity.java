package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public interface AbstractSmithingAnvilBlockEntity {
    boolean hasRecipe();

    void drops();

    UUID getOwnerUUID();

    boolean isMinigameOn();

    boolean hasQuality();

    boolean needsMinigame();

    void setBusyUntil(long l);

    void increaseForgingProgress(Level level, BlockPos pos, BlockState state);

    int getHitsRemaining();

    boolean isFailedResult();
}
