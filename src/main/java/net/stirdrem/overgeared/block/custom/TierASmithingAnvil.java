package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;
import org.jetbrains.annotations.Nullable;

public class TierASmithingAnvil extends AbstractSmithingAnvil {
    private static final VoxelShape Z1 = Block.box(3, 9, 0, 13, 16, 16);
    private static final VoxelShape Z2 = Block.box(3, 0, 1, 13, 3, 15);
    private static final VoxelShape Z3 = Block.box(4, 0, 4, 12, 3, 12);
    private static final VoxelShape Z4 = Block.box(5, 3, 3, 11, 4, 13);
    private static final VoxelShape Z5 = Block.box(6, 4, 4, 10, 9, 12);
    private static final VoxelShape X1 = Block.box(0, 9, 3, 16, 16, 13);
    private static final VoxelShape X2 = Block.box(1, 0, 3, 15, 3, 13);
    private static final VoxelShape X3 = Block.box(4, 0, 4, 12, 3, 12);
    private static final VoxelShape X4 = Block.box(3, 3, 5, 13, 4, 11);
    private static final VoxelShape X5 = Block.box(4, 4, 6, 12, 9, 10);

    private static final VoxelShape X_AXIS_AABB = Shapes.or(X1, X2, X3, X4, X5);
    private static final VoxelShape Z_AXIS_AABB = Shapes.or(Z1, Z2, Z3, Z4, Z5);

    public TierASmithingAnvil(AnvilTier tier, BlockBehaviour.Properties settings) {
        super(tier, settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getClockWise());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TierASmithingAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClientSide) {
            return validateTicker(type, ModBlockEntities.TIER_A_SMITHING_ANVIL_BE,
                    (lvl, pos, st, be) -> be.tick(lvl, pos, st));
        }
        return null;
    }
}
