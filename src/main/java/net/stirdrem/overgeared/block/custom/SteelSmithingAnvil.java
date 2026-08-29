package net.stirdrem.overgeared.block.custom;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

public class SteelSmithingAnvil extends AbstractSmithingAnvil {
    private static final VoxelShape Z1 = Block.createCuboidShape(3, 9, 0, 13, 16, 16);
    private static final VoxelShape Z2 = Block.createCuboidShape(3, 0, 1, 13, 3, 15);
    private static final VoxelShape Z3 = Block.createCuboidShape(4, 0, 4, 12, 3, 12);
    private static final VoxelShape Z4 = Block.createCuboidShape(5, 3, 3, 11, 4, 13);
    private static final VoxelShape Z5 = Block.createCuboidShape(6, 4, 4, 10, 9, 12);
    private static final VoxelShape X1 = Block.createCuboidShape(0, 9, 3, 16, 16, 13);
    private static final VoxelShape X2 = Block.createCuboidShape(1, 0, 3, 15, 3, 13);
    private static final VoxelShape X3 = Block.createCuboidShape(4, 0, 4, 12, 3, 12);
    private static final VoxelShape X4 = Block.createCuboidShape(3, 3, 5, 13, 4, 11);
    private static final VoxelShape X5 = Block.createCuboidShape(4, 4, 6, 12, 9, 10);

    // X-axis oriented shape
    private static final VoxelShape X_AXIS_AABB = VoxelShapes.union(X1, X2, X3, X4, X5);

    // Z-axis oriented shape
    private static final VoxelShape Z_AXIS_AABB = VoxelShapes.union(Z1, Z2, Z3, Z4, Z5);

    public SteelSmithingAnvil(AnvilTier tier, Settings settings) {
        super(tier, settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().rotateYClockwise());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SteelSmithingAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient) {
            return validateTicker(type, ModBlockEntities.STEEL_SMITHING_ANVIL_BE,
                    (lvl, pos, st, be) -> be.tick(lvl, pos, st));
        }
        return null;
    }

    @Override
    public TagKey<Item> hammerTag() {
        if (ServerConfig.ENABLE_TIERED_HAMMERS.get()) {
            return ModTags.Items.IRON_SMITHING_HAMMERS;
        }
        return super.hammerTag();
    }
}
