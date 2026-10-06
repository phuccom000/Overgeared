package net.stirdrem.overgeared.block.custom;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Forge's Fallable interface (letting a BaseEntityBlock also participate in gravity-block
 * landing/damage callbacks) has no Fabric equivalent, so the onLand/getFallDamageSource hooks
 * were dropped - FallingBlockEntity handles landing and fall damage generically on its own.
 * Likewise onDestroyedByPlayer/onBlockExploded were folded into the block entity's
 * preRemoveSideEffects (26.x), which fires for every removal path (break, explosion, or otherwise),
 * drops the contents and resets the minigame for whichever player was using this anvil.
 */
public abstract class AbstractSmithingAnvil extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final int HAMMER_SOUND_DURATION_TICKS = 6; // adjust to match your sound

    protected static String quality = null;
    protected static AnvilTier tier;

    public AbstractSmithingAnvil(AnvilTier anvilTier, Properties settings) {
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
    public abstract VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context);

    public TagKey<Item> hammerTag() {
        return ModTags.Items.SMITHING_HAMMERS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return handleUse(held, state, world, pos, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return handleUse(player.getMainHandItem(), state, world, pos, player, InteractionHand.MAIN_HAND);
    }

    protected InteractionResult handleUse(ItemStack held, BlockState state, Level world, BlockPos pos,
                                          Player player, InteractionHand hand) {
        boolean isHammer = held.is(hammerTag());
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide()) {
            if (player.isShiftKeyDown()) return InteractionResult.SUCCESS;
            if (anvil.hasRecipe() && isHammer) {
                AnvilMinigameEvents.resetPopUps();
                if (!pos.equals(AnvilMinigameEvents.getAnvilPos(player.getUUID()))) {
                    return InteractionResult.SUCCESS;
                }
                if (!AnvilMinigameEvents.isIsVisible())
                    return InteractionResult.SUCCESS;
                // Read the current counter at the moment of right-click:
                String currentQuality = AnvilMinigameEvents.handleHit();
                ClientPlayNetworking.send(new PacketSendCounterC2SPacket(pos, currentQuality));
                AnvilMinigameEvents.speedUp();

                return InteractionResult.SUCCESS;
            } else
                AnvilMinigameEvents.setIsVisible(pos, false);
            return InteractionResult.SUCCESS;

        }

        long now = world.getGameTime();

        if (anvil.hasRecipe()) {
            UUID currentOwner = anvil.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(player.getUUID()) && player instanceof ServerPlayer serverPlayer) {
                Player ownerPlayer = world.getPlayerByUUID(currentOwner);
                String ownerName;

                if (ownerPlayer != null) {
                    ownerName = ownerPlayer.getName().getString();
                } else {
                    ownerName = world.getServer() == null ? "Another player"
                            : world.getServer().services().nameToIdCache().get(currentOwner)
                            .map(nameAndId -> nameAndId.name()).orElse("Another player");
                }

                serverPlayer.sendOverlayMessage(
                        Component.translatable("message.overgeared.anvil_in_use_by_another", ownerName)
                                .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            if (isHammer && (anvil.isMinigameOn() || (!anvil.hasQuality() && !anvil.needsMinigame()) || !ServerConfig.ENABLE_MINIGAME.get())) {
                BlockPos pos1 = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
                if (pos1 != null && !pos.equals(ModItemInteractEvents.playerAnvilPositions.get(player.getUUID()))) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.sendOverlayMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }
                Boolean visible = ModItemInteractEvents.playerMinigameVisibility.get(player.getUUID());

                if (visible == null && anvil.isMinigameOn()) {
                    ModItemInteractEvents.hideMinigame((ServerPlayer) player);
                    player.openMenu(anvil);
                    return InteractionResult.SUCCESS;
                }
                if (!ServerConfig.ENABLE_MINIGAME.get())
                    anvil.setBusyUntil(now + HAMMER_SOUND_DURATION_TICKS);

                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                ServerPlayer serverPlayer = (ServerPlayer) player;
                held.hurtAndBreak(1, (ServerLevel) world, serverPlayer, broken -> {
                    serverPlayer.onEquippedItemBroken(broken, slot);
                    ModEvents.resetMinigameForPlayer(serverPlayer);
                });

                spawnAnvilParticles(world, pos);
                anvil.increaseForgingProgress(world, pos, state);
                if (anvil.getHitsRemaining() == 0) {
                    if (anvil.isFailedResult()) {
                        world.playSound(null, pos, ModSounds.FORGING_FAILED, SoundSource.BLOCKS, 1f, 1f);
                    } else
                        world.playSound(null, pos, ModSounds.FORGING_COMPLETE, SoundSource.BLOCKS, 1f, 1f);
                } else world.playSound(null, pos, ModSounds.ANVIL_HIT, SoundSource.BLOCKS, 1f, 1f);
                return InteractionResult.SUCCESS;
            }
            ModItemInteractEvents.hideMinigame((ServerPlayer) player);
            player.openMenu(anvil);
        } else {
            ModItemInteractEvents.releaseAnvil((ServerPlayer) player, pos);
            player.openMenu(anvil);
        }
        return InteractionResult.SUCCESS;
    }

    protected void spawnAnvilParticles(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverWorld) {

            RandomSource random = world.getRandom();
            for (int i = 0; i < 6; i++) {
                double offsetX = 0.5 + (random.nextFloat() - 0.5);
                double offsetY = 1.0 + random.nextFloat() * 0.5;
                double offsetZ = 0.5 + (random.nextFloat() - 0.5);
                double velocityX = (random.nextFloat() - 0.5) * 0.1;
                double velocityY = random.nextFloat() * 0.1;
                double velocityZ = (random.nextFloat() - 0.5) * 0.1;

                serverWorld.sendParticles(new DustParticleOptions(0xFF8000, 1.0f),
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
                serverWorld.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
            }
        }
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    public static String getTier() {
        return tier.getDisplayName();
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onPlace(state, world, pos, oldState, notify);
        world.scheduleTick(pos, this, 2); // Schedule an immediate fall check
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        BlockState stateBelow = world.getBlockState(below);
        if (FallingBlock.isFree(stateBelow)) {
            FallingBlockEntity falling = FallingBlockEntity.fall(world, pos, state);
            customizeFallingEntity(falling, world);
        }
    }

    protected void customizeFallingEntity(FallingBlockEntity entity, Level world) {
        entity.setHurtsEntities(2.0F, 40);
        entity.dropItem = true; // drop as item on breaking
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        ticks.scheduleTick(pos, this, 2);
        return super.updateShape(state, world, ticks, pos, direction, neighborPos, neighborState, random);
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
