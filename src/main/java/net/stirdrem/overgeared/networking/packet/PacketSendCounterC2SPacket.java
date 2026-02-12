package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;

public record PacketSendCounterC2SPacket(String quality, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("packet_send_counter");
    public static final CustomPacketPayload.Type<PacketSendCounterC2SPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketSendCounterC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buffer, packet.quality);
                BlockPos.STREAM_CODEC.encode(buffer, packet.pos);
            },
            buffer -> new PacketSendCounterC2SPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buffer),
                    BlockPos.STREAM_CODEC.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSendCounterC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Level level = player.level();
            BlockPos pos = payload.pos;
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof AbstractSmithingAnvil)) return;

            AbstractSmithingAnvil.setQuality(ForgingQuality.fromString(payload.quality));

            // Process the hammer hit on the server (minigame hits come only through this packet)
            if (!(level.getBlockEntity(pos) instanceof AbstractSmithingAnvilBlockEntity anvilBE)) return;
            if (!anvilBE.isMinigameOn()) return;

            // Damage hammer
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.is(ModTags.Items.SMITHING_HAMMERS)) {
                mainHand.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }

            anvilBE.increaseForgingProgress(level, pos, state);
            AbstractSmithingAnvil.spawnAnvilParticles(level, pos);

            if (anvilBE.getHitsRemaining() == 1) {
                if (anvilBE.isFailedResult()) {
                    level.playSound(null, pos, ModSounds.FORGING_FAILED.get(), SoundSource.BLOCKS, 1f, 1f);
                } else {
                    level.playSound(null, pos, ModSounds.FORGING_COMPLETE.get(), SoundSource.BLOCKS, 1f, 1f);
                }
            } else {
                level.playSound(null, pos, ModSounds.ANVIL_HIT.get(), SoundSource.BLOCKS, 1f, 1f);
            }
        });
    }
}
