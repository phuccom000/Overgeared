package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.compat.valkyrienskies.ValkyrienSkiesCompat;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import net.stirdrem.overgeared.networking.ModMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

/**
 * Non-networking server-side event handling: anvil-distance enforcement, the quality attribute
 * bonus (applied from a mixin, since Fabric has no ItemAttributeModifierEvent equivalent),
 * minigame reset hooks, and villager/wandering-trader trade registration. Tooltip rendering
 * (client-only) lives in client.OvergearedTooltipEvents instead.
 */
public class ModEvents {
    private static final int HEATED_ITEM_CHECK_INTERVAL = 20; // 1 second
    private static int serverTick = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::onServerTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> resetMinigameForPlayer(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> resetMinigameForPlayer(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> resetMinigameForPlayer(newPlayer));
        ServerLifecycleEvents.SERVER_STOPPING.register(ModEvents::onServerStopping);

        // 26.3 port: villager / wandering trader trades are data-driven now (data/overgeared/villager_trade
        // + data/minecraft/tags/villager_trade); quality rolling happens in VillagerTradeMixin.
    }

    private static void onServerTick(MinecraftServer server) {
        serverTick++;
        if (serverTick % HEATED_ITEM_CHECK_INTERVAL != 0) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            handleAnvilDistance(player, player.level());
        }
    }

    private static void handleAnvilDistance(ServerPlayer player, Level world) {
        if (FabricLoader.getInstance().isModLoaded("valkyrienskies")) {
            if (world instanceof ServerLevel serverWorld) {
                BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
                if (anvilPos != null) {
                    BlockEntity be = serverWorld.getBlockEntity(anvilPos);
                    if (be instanceof AbstractSmithingAnvilBlockEntity) {
                        Vec3 anvilWorldPos = ValkyrienSkiesCompat.getActualWorldPos(serverWorld, anvilPos);
                        Vec3 playerWorldPos = player.position();

                        double distSq = playerWorldPos.distanceToSqr(anvilWorldPos);
                        int maxDist = ServerConfig.MAX_ANVIL_DISTANCE.get();

                        if (distSq > (double) maxDist * maxDist) {
                            resetMinigameForPlayer(player);
                        }
                    }
                }
            }
            return;
        }

        // Fallback: vanilla behavior when Valkyrien Skies is not loaded
        BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
        if (anvilPos != null) {
            BlockEntity be = world.getBlockEntity(anvilPos);
            if (be instanceof AbstractSmithingAnvilBlockEntity) {
                double distSq = player.blockPosition().distSqr(anvilPos);
                int maxDist = ServerConfig.MAX_ANVIL_DISTANCE.get();
                if (distSq > (double) maxDist * maxDist) {
                    resetMinigameForPlayer(player);
                }
            }
        }
    }

    /**
     * Called from ItemStackAttributeMixin - Fabric has no ItemAttributeModifierEvent equivalent,
     * so the quality attribute bonus is applied to the ATTRIBUTE_MODIFIERS component value read in
     * ItemStack#forEachModifier. Returns the (possibly) modified modifiers.
     */
    public static ItemAttributeModifiers applyQualityAttributeModifiers(ItemStack stack, ItemAttributeModifiers modifiers) {
        if (isBroken(stack)) return modifiers;
        ForgingQuality forgingQuality = ForgingQuality.get(stack);
        if (forgingQuality == null || forgingQuality == ForgingQuality.NONE) return modifiers;
        String quality = forgingQuality.getDisplayName();

        for (QualityAttributeDefinition def : QualityAttributeReloadListener.INSTANCE.getAll()) {
            if (!matches(stack, def.targets())) continue;

            QualityValue value = def.qualities().get(quality);
            if (value == null || value.amount() == 0) continue;
            Optional<Holder.Reference<Attribute>> attribute = BuiltInRegistries.ATTRIBUTE.get(def.attribute());
            if (attribute.isEmpty()) continue;
            modifiers = modifyAttribute(modifiers, attribute.get(), value.amount(), value.operation(), quality);
        }
        return modifiers;
    }

    public static boolean matches(ItemStack stack, List<QualityTarget> targets) {
        Item item = stack.getItem();

        for (QualityTarget target : targets) {
            switch (target.type()) {
                case WEAPON -> {
                    if (QualityAttributeReloadListener.isWeaponItem(item)) {
                        return true;
                    }
                }

                case ARMOR -> {
                    if (QualityAttributeReloadListener.isArmorItem(item)) {
                        return true;
                    }
                }

                case ITEM -> {
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                    if (itemId != null && itemId.equals(target.id())) {
                        return true;
                    }
                }

                case ITEM_TAG -> {
                    if (target.id() == null) break;

                    TagKey<Item> itemTag = TagKey.create(Registries.ITEM, target.id());
                    if (stack.is(itemTag)) {
                        return true;
                    }
                }
                case ITEM_ALL -> {
                    return true;
                }
            }
        }
        return false;
    }

    private static ItemAttributeModifiers modifyAttribute(ItemAttributeModifiers modifiers, Holder<Attribute> attribute, double bonus,
                                                          AttributeModifier.Operation operation, String quality) {
        List<ItemAttributeModifiers.Entry> existing = modifiers.modifiers().stream()
                .filter(e -> e.attribute().value() == attribute.value())
                .toList();
        if (existing.isEmpty()) return modifiers;

        List<ItemAttributeModifiers.Entry> result = new ArrayList<>(modifiers.modifiers());
        for (ItemAttributeModifiers.Entry entry : existing) {
            if (operation == AttributeModifier.Operation.ADD_VALUE) result.remove(entry);
            result.add(new ItemAttributeModifiers.Entry(entry.attribute(),
                    createModifiedAttribute(entry.modifier(), bonus, operation, quality), entry.slot(), entry.display()));
        }
        return new ItemAttributeModifiers(result);
    }

    public static AttributeModifier createModifiedAttribute(AttributeModifier original,
                                                            double bonus,
                                                            AttributeModifier.Operation operation,
                                                            String quality) {
        Identifier id;
        double amount;

        if (operation == AttributeModifier.Operation.ADD_VALUE) {
            amount = original.amount() + bonus;
            id = original.id();
        } else {
            amount = bonus;
            id = original.id().withSuffix("_overgeared_" + quality + "_"
                    + operation.getSerializedName() + "_" + Integer.toHexString(Double.hashCode(bonus)));
        }

        return new AttributeModifier(id, amount, operation);
    }

    private static void onServerStopping(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            resetMinigameForPlayer(player);
        }
        Overgeared.LOGGER.info("Reset all minigames on server stop.");
    }

    public static void resetMinigameForPlayer(ServerPlayer player) {
        if (player == null) return;
        UUID playerId = player.getUUID();
        ModMessages.sendOnlyResetMinigame(player);

        if (ModItemInteractEvents.playerAnvilPositions.containsKey(playerId)) {
            BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
            BlockEntity be = player.level().getBlockEntity(anvilPos);

            if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                anvil.setProgress(0);
                anvil.setChanged();
                anvil.setMinigameOn(false);

                ModMessages.sendResetMinigame(player, anvilPos);

                ModItemInteractEvents.releaseAnvil(player, anvilPos);
                ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
            }
        }
        // Note: the original Forge code also called AnvilMinigameEvents.reset(...) directly here,
        // but that class is client-only in this port - the ONLY_RESET_MINIGAME/RESET_MINIGAME
        // packets sent above already trigger the equivalent client-side reset.
    }

    public static void resetMinigameForPlayer(ServerPlayer player, BlockPos anvilPos) {
        if (player == null) return;
        ModMessages.sendOnlyResetMinigame(player);

        BlockEntity be = player.level().getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
        }

        ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
    }

    public static void resetMinigameForAnvil(Level world, BlockPos anvilPos) {
        BlockEntity be = world.getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            anvil.clearOwner();
        }

        if (world instanceof ServerLevel serverWorld) {
            for (ServerPlayer player : serverWorld.getServer().getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                ModMessages.sendResetMinigame(player, anvilPos);

                if (anvilPos.equals(ModItemInteractEvents.playerAnvilPositions.get(playerId))) {
                    ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                    ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                    break; // Only one player should ever hold this anvil
                }
            }
        }
    }
}
