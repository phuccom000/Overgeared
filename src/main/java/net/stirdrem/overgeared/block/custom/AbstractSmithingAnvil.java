package net.stirdrem.overgeared.block.custom;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.client.ClientModMessages;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Forge's Fallable interface (letting a BaseEntityBlock also participate in gravity-block
 * landing/damage callbacks) has no Fabric equivalent, so the onLand/getFallDamageSource hooks
 * were dropped - FallingBlockEntity handles landing and fall damage generically on its own.
 * Likewise onDestroyedByPlayer/onBlockExploded were folded into onStateReplaced, which already
 * fires for every removal path (break, explosion, or otherwise) and resets the minigame for
 * whichever player was using this anvil.
 */
public abstract class AbstractSmithingAnvil extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    protected static final int HAMMER_SOUND_DURATION_TICKS = 6; // adjust to match your sound

    protected static String quality = null;
    protected static AnvilTier tier;

    public AbstractSmithingAnvil(AnvilTier anvilTier, AbstractBlock.Settings settings) {
        super(settings);
        tier = anvilTier;
    }

    // Ensure getQuality() never returns null:
    public String getQuality() {
        return quality != null ? quality : "none";
    }

    public static void setQuality(String quality) {
        AbstractSmithingAnvil.quality = quality;
    }

    @Override
    public abstract VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context);

    /**
     * BlockWithEntity defaults to INVISIBLE (relying entirely on a block entity renderer) - the
     * anvil's own geometry comes from its blockstate/model, so this needs to opt back into normal
     * model rendering. Without this the anvil renders fine as an item/in creative (that path is
     * unrelated to getRenderType) but vanishes entirely once placed in the world.
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AbstractSmithingAnvilBlockEntity anvilBe) {
                anvilBe.drops();

                if (!world.isClient()) {
                    ModEvents.resetMinigameForAnvil(world, pos);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    public TagKey<Item> hammerTag() {
        return ModTags.Items.SMITHING_HAMMERS;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);
        boolean isHammer = held.isIn(hammerTag());
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvil)) {
            return ActionResult.PASS;
        }
        if (world.isClient()) {
            if (player.isSneaking()) return ActionResult.SUCCESS;
            if (anvil.hasRecipe() && isHammer) {
                AnvilMinigameEvents.resetPopUps();
                if (!pos.equals(AnvilMinigameEvents.getAnvilPos(player.getUuid()))) {
                    return ActionResult.SUCCESS;
                }
                if (!AnvilMinigameEvents.isIsVisible())
                    return ActionResult.SUCCESS;
                // Read the current counter at the moment of right-click:
                String currentQuality = AnvilMinigameEvents.handleHit();
                var buf = ModMessages.buf();
                PacketSendCounterC2SPacket.encode(new PacketSendCounterC2SPacket(pos, currentQuality), buf);
                ClientModMessages.sendToServer(ModMessages.SEND_COUNTER, buf);
                AnvilMinigameEvents.speedUp();

                return ActionResult.SUCCESS;
            } else
                AnvilMinigameEvents.setIsVisible(pos, false);
            return ActionResult.SUCCESS;

        }

        long now = world.getTime();

        if (anvil.hasRecipe()) {
            UUID currentOwner = anvil.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(player.getUuid()) && player instanceof ServerPlayerEntity serverPlayer) {
                PlayerEntity ownerPlayer = world.getPlayerByUuid(currentOwner);
                String ownerName;

                if (ownerPlayer != null) {
                    ownerName = ownerPlayer.getName().getString();
                } else {
                    GameProfile ownerProfile = world.getServer().getUserCache().getByUuid(currentOwner).orElse(null);
                    ownerName = ownerProfile != null ? ownerProfile.getName() : "Another player";
                }

                serverPlayer.sendMessage(
                        Text.translatable("message.overgeared.anvil_in_use_by_another", ownerName)
                                .formatted(Formatting.RED),
                        true
                );
                return ActionResult.FAIL;
            }

            if (isHammer && (anvil.isMinigameOn() || (!anvil.hasQuality() && !anvil.needsMinigame()) || !ServerConfig.ENABLE_MINIGAME.get())) {
                BlockPos pos1 = ModItemInteractEvents.playerAnvilPositions.get(player.getUuid());
                if (pos1 != null && !pos.equals(ModItemInteractEvents.playerAnvilPositions.get(player.getUuid()))) {
                    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                    serverPlayer.sendMessage(Text.translatable("message.overgeared.another_anvil_in_use").formatted(Formatting.RED), true);
                    return ActionResult.FAIL;
                }
                Boolean visible = ModItemInteractEvents.playerMinigameVisibility.get(player.getUuid());

                if (visible == null && anvil.isMinigameOn()) {
                    ModItemInteractEvents.hideMinigame((ServerPlayerEntity) player);
                    player.openHandledScreen(anvil);
                    return ActionResult.success(world.isClient());
                }
                if (!ServerConfig.ENABLE_MINIGAME.get())
                    anvil.setBusyUntil(now + HAMMER_SOUND_DURATION_TICKS);

                EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                held.damage(1, player, p -> {
                    p.sendEquipmentBreakStatus(slot);
                    ModEvents.resetMinigameForPlayer((ServerPlayerEntity) p);
                });

                spawnAnvilParticles(world, pos);
                anvil.increaseForgingProgress(world, pos, state);
                if (anvil.getHitsRemaining() == 0) {
                    if (anvil.isFailedResult()) {
                        world.playSound(null, pos, ModSounds.FORGING_FAILED, SoundCategory.BLOCKS, 1f, 1f);
                    } else
                        world.playSound(null, pos, ModSounds.FORGING_COMPLETE, SoundCategory.BLOCKS, 1f, 1f);
                } else world.playSound(null, pos, ModSounds.ANVIL_HIT, SoundCategory.BLOCKS, 1f, 1f);
                return ActionResult.success(world.isClient());
            }
            ModItemInteractEvents.hideMinigame((ServerPlayerEntity) player);
            player.openHandledScreen(anvil);
        } else {
            ModItemInteractEvents.releaseAnvil((ServerPlayerEntity) player, pos);
            player.openHandledScreen(anvil);
        }
        return ActionResult.success(world.isClient());
    }

    protected void spawnAnvilParticles(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {

            Random random = world.random;
            for (int i = 0; i < 6; i++) {
                double offsetX = 0.5 + (random.nextFloat() - 0.5);
                double offsetY = 1.0 + random.nextFloat() * 0.5;
                double offsetZ = 0.5 + (random.nextFloat() - 0.5);
                double velocityX = (random.nextFloat() - 0.5) * 0.1;
                double velocityY = random.nextFloat() * 0.1;
                double velocityZ = (random.nextFloat() - 0.5) * 0.1;

                serverWorld.spawnParticles(new DustParticleEffect(new Vector3f(1.0f, 0.5f, 0.0f), 1.0f),
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
                serverWorld.spawnParticles(ParticleTypes.CRIT,
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
            }
        }
    }

    @Nullable
    @Override
    public abstract BlockEntity createBlockEntity(BlockPos pos, BlockState state);

    public static String getTier() {
        return tier.getDisplayName();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        world.scheduleBlockTick(pos, this, 2); // Schedule an immediate fall check
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos below = pos.down();
        BlockState stateBelow = world.getBlockState(below);
        if (FallingBlock.canFallThrough(stateBelow)) {
            FallingBlockEntity falling = FallingBlockEntity.spawnFromBlock(world, pos, state);
            customizeFallingEntity(falling, world);
        }
    }

    protected void customizeFallingEntity(FallingBlockEntity entity, World world) {
        entity.setHurtEntities(2.0F, 40);
        entity.dropItem = true; // drop as item on breaking
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, 2);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    /**
     * Vanilla has no equivalent of NeoForge's BaseEntityBlock.createTickerHelper - this is the
     * same "does the requested type match, and if so hand back the ticker" pattern vanilla's own
     * furnace/etc. blocks implement privately per-class.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> validateTicker(
            BlockEntityType<T> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<T>) ticker : null;
    }
}
