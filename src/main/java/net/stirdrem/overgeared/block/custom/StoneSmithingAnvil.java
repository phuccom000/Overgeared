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
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The original Forge version breaks into cobblestone on landing when unsupported
 * (ServerConfig.ENABLE_STONE_ANVIL_BREAKING), via Fallable's onLand/onBrokenAfterFall hooks.
 * Vanilla's FallingBlockEntity has no equivalent per-block landing callback (it special-cases a
 * handful of vanilla blocks internally, not an extensible hook), so that flavor mechanic isn't
 * ported - a stone smithing anvil that falls just re-places itself like any other falling block.
 * Would need a mixin into FallingBlockEntity.tick() to restore.
 */
public class StoneSmithingAnvil extends AbstractSmithingAnvil {

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                buffer[1] = Shapes.or(buffer[1],
                        Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX));
            });
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    private static final VoxelShape Z1 = Block.box(2, 2, 0, 14, 16, 16);
    private static final VoxelShape Z2 = Block.box(1, 0, 0, 15, 2, 16);
    // X-axis oriented shape
    private static final VoxelShape X1 = rotateShape(Direction.NORTH, Direction.EAST, Z1);
    private static final VoxelShape X2 = rotateShape(Direction.NORTH, Direction.EAST, Z2);

    // Combined composite shapes
    private static final VoxelShape SHAPE_Z = Shapes.or(Z1, Z2);
    private static final VoxelShape SHAPE_X = Shapes.or(X1, X2);

    public StoneSmithingAnvil(BlockBehaviour.Properties settings) {
        super(AnvilTier.STONE, settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
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
        return new StoneSmithingAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClientSide) {
            return validateTicker(type, ModBlockEntities.STONE_SMITHING_ANVIL_BE,
                    (lvl, pos, st, be) -> be.tick(lvl, pos, st));
        }
        return null;
    }
}
