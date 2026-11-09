package net.stirdrem.overgeared.event.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Loader-independent player interaction events. Each loader builds one of these from its own
 * event or callback, passes it to the common handlers, then applies the outcome.
 * <p>
 * The getters mirror NeoForge's {@code PlayerInteractEvent} so the handlers read the same.
 */
public abstract class PlayerInteractEvent {
    private final Player player;
    private final InteractionHand hand;
    private boolean canceled;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    protected PlayerInteractEvent(Player player, InteractionHand hand) {
        this.player = player;
        this.hand = hand;
    }

    public Player getEntity() {
        return player;
    }

    public Level getLevel() {
        return player.level();
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getItemStack() {
        return player.getItemInHand(hand);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public InteractionResult getCancellationResult() {
        return cancellationResult;
    }

    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }

    /** Right-clicking a block. */
    public static class RightClickBlock extends PlayerInteractEvent {
        private final BlockHitResult hitVec;
        private TriState useItem = TriState.DEFAULT;
        private TriState useBlock = TriState.DEFAULT;

        public RightClickBlock(Player player, InteractionHand hand, BlockHitResult hitVec) {
            super(player, hand);
            this.hitVec = hitVec;
        }

        public BlockPos getPos() {
            return hitVec.getBlockPos();
        }

        @Nullable
        public Direction getFace() {
            return hitVec.getDirection();
        }

        public BlockHitResult getHitVec() {
            return hitVec;
        }

        public TriState getUseItem() {
            return useItem;
        }

        public void setUseItem(TriState useItem) {
            this.useItem = useItem;
        }

        public TriState getUseBlock() {
            return useBlock;
        }

        public void setUseBlock(TriState useBlock) {
            this.useBlock = useBlock;
        }
    }

    /** Right-clicking with an item while not targeting a block. */
    public static class RightClickItem extends PlayerInteractEvent {
        public RightClickItem(Player player, InteractionHand hand) {
            super(player, hand);
        }
    }

    /** Left-clicking (starting to break) a block. */
    public static class LeftClickBlock extends PlayerInteractEvent {
        private final BlockPos pos;
        @Nullable
        private final Direction face;

        public LeftClickBlock(Player player, InteractionHand hand, BlockPos pos, @Nullable Direction face) {
            super(player, hand);
            this.pos = pos;
            this.face = face;
        }

        public BlockPos getPos() {
            return pos;
        }

        @Nullable
        public Direction getFace() {
            return face;
        }
    }
}
