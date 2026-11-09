package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.networking.PacketContext;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.BaseSmithingAnvilBlock;
import net.stirdrem.overgeared.block.entity.BaseSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.ParticleUtils;

public record PacketSendCounterC2SPacket(
        String quality,
        BlockPos pos,
        Identifier hitSound,
        Identifier completeSound,
        Identifier failedSound
) implements CustomPacketPayload {
    public static final Identifier ID = OvergearedMod.loc("packet_send_counter");
    public static final CustomPacketPayload.Type<PacketSendCounterC2SPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketSendCounterC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, packet.quality);
                BlockPos.STREAM_CODEC.encode(buffer, packet.pos);

                buffer.writeIdentifier(packet.hitSound);
                buffer.writeIdentifier(packet.completeSound);
                buffer.writeIdentifier(packet.failedSound);
            },
            buffer -> new PacketSendCounterC2SPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    BlockPos.STREAM_CODEC.decode(buffer),

                    buffer.readIdentifier(),
                    buffer.readIdentifier(),
                    buffer.readIdentifier()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSendCounterC2SPacket payload, PacketContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Level level = player.level();
            BlockPos pos = payload.pos;
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BaseSmithingAnvilBlock anvilBlock)) return;

            anvilBlock.setQuality(ForgingQuality.fromString(payload.quality));

            // Process the hammer hit on the server (minigame hits come only through this packet)
            if (!(level.getBlockEntity(pos) instanceof BaseSmithingAnvilBlockEntity anvilBE)) return;
            if (!anvilBE.isMinigameOn()) return;

            // Damage hammer
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.is(ModTags.Items.SMITHING_HAMMERS)) {
                mainHand.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }

            anvilBE.increaseForgingProgress(level, pos, state);
            ParticleUtils.spawnAnvilParticles(level, pos);

            if (anvilBE.getHitsRemaining() == 1) {
                if (anvilBE.isFailedResult()) {
                    level.playSound(
                            null,
                            pos,
                            BuiltInRegistries.SOUND_EVENT.getValue(payload.failedSound),
                            SoundSource.BLOCKS,
                            1f,
                            1f
                    );
                } else {
                    level.playSound(
                            null,
                            pos,
                            BuiltInRegistries.SOUND_EVENT.getValue(payload.completeSound),
                            SoundSource.BLOCKS,
                            1f,
                            1f
                    );
                }
            } else {
                level.playSound(
                        null,
                        pos,
                        BuiltInRegistries.SOUND_EVENT.getValue(payload.hitSound),
                        SoundSource.BLOCKS,
                        1f,
                        1f
                );
            }
        });
    }
}
