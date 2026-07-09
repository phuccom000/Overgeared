package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.entity.BaseSmithingAnvilBlockEntity;
import org.joml.Random;
import org.joml.Vector3f;


public abstract class BaseSmithingAnvilBlock
        extends BaseEntityBlock {


    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;


    protected BaseSmithingAnvilBlock(Properties properties) {
        super(properties);
    }

    public abstract void setQuality(ForgingQuality forgingQuality);


    /*
     * =========================
     * Required implementations
     * =========================
     */


    public abstract ForgingQuality getQuality();

    @Override
    public abstract VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext ctx
    );


    @Override
    public abstract BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    );



    /*
     * =========================
     * Render
     * =========================
     */


    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }



    /*
     * =========================
     * Breaking
     * =========================
     */


    @Override
    public void onRemove(
            BlockState oldState,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean moving
    ) {

        if (oldState.getBlock() != newState.getBlock()) {

            BlockEntity be =
                    level.getBlockEntity(pos);


            if (be instanceof BaseSmithingAnvilBlockEntity anvil) {

                anvil.drops();

            }
        }

        super.onRemove(
                oldState,
                level,
                pos,
                newState,
                moving
        );
    }





    /*
     * =========================
     * Falling
     * =========================
     */


    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean moving
    ) {

        super.onPlace(
                state,
                level,
                pos,
                oldState,
                moving
        );


        level.scheduleTick(
                pos,
                this,
                2
        );
    }


    @Override
    public void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {

        BlockState below =
                level.getBlockState(pos.below());


        if (FallingBlock.isFree(below)) {

            FallingBlockEntity entity =
                    FallingBlockEntity.fall(
                            level,
                            pos,
                            state
                    );


            customizeFallingEntity(
                    entity
            );
        }
    }


    protected void customizeFallingEntity(
            FallingBlockEntity entity
    ) {

        entity.setHurtsEntities(
                2.0F,
                40
        );

        entity.dropItem = true;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction dir,
            BlockState neighbor,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {

        level.scheduleTick(
                pos,
                this,
                2
        );

        return super.updateShape(
                state,
                dir,
                neighbor,
                level,
                pos,
                neighborPos
        );
    }


    @Override
    public void onBlockExploded(
            BlockState state,
            Level level,
            BlockPos pos,
            Explosion explosion
    ) {

        super.onBlockExploded(
                state,
                level,
                pos,
                explosion
        );
    }



    /*
     * =========================
     * Shared particles
     * =========================
     */


    protected void spawnHitParticles(
            Level level,
            BlockPos pos
    ) {

        if (level instanceof ServerLevel server) {

            Random random = new Random();


            for (int i = 0; i < 6; i++) {

                double x =
                        pos.getX() + 0.5 +
                                random.nextFloat() - 0.5;

                double y =
                        pos.getY() + 1 +
                                random.nextFloat() * 0.5;

                double z =
                        pos.getZ() + 0.5 +
                                random.nextFloat() - 0.5;


                server.sendParticles(
                        new DustParticleOptions(
                                new Vector3f(1, 0.5f, 0),
                                1
                        ),
                        x, y, z,
                        1,
                        0, 0.1, 0,
                        1
                );


                server.sendParticles(
                        ParticleTypes.CRIT,
                        x, y, z,
                        1,
                        0, 0.1, 0,
                        1
                );
            }
        }
    }
}