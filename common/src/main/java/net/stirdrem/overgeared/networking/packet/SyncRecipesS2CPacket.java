package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.PacketContext;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.recipe.RecipeLookup;

import java.util.List;

/** Sends the mod's recipes to a client, which no longer receives recipes from vanilla. See {@link RecipeLookup}. */
public record SyncRecipesS2CPacket(List<RecipeHolder<?>> recipes) implements CustomPacketPayload {
    public static final Type<SyncRecipesS2CPacket> TYPE = new Type<>(OvergearedMod.loc("sync_recipes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRecipesS2CPacket> STREAM_CODEC =
            RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()).map(SyncRecipesS2CPacket::new, SyncRecipesS2CPacket::recipes);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncRecipesS2CPacket payload, PacketContext context) {
        context.enqueueWork(() -> RecipeLookup.acceptClientRecipes(payload.recipes()));
    }

    public static void sendTo(ServerPlayer player) {
        Services.PLATFORM.sendToPlayer(player, new SyncRecipesS2CPacket(RecipeLookup.recipesToSync(player.level().getServer())));
    }

    public static void sendToAll(MinecraftServer server) {
        Services.PLATFORM.sendToAllPlayers(new SyncRecipesS2CPacket(RecipeLookup.recipesToSync(server)));
    }
}
