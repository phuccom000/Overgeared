package net.stirdrem.overgeared.event;

import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.compat.valkyrienskies.ValkyrienSkiesCompat;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.QualityAttributeReloadListener;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityAttributeDefinition;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityTarget;
import net.stirdrem.overgeared.datapack.quality_attribute.QualityValue;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.OnlyResetMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.ResetMinigameS2CPacket;

import java.nio.charset.StandardCharsets;
import java.util.List;
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

        registerVillagerTrades();
        registerWanderingTraderTrades();
    }

    private static void onServerTick(MinecraftServer server) {
        serverTick++;
        if (serverTick % HEATED_ITEM_CHECK_INTERVAL != 0) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handleAnvilDistance(player, player.getWorld());
        }
    }

    private static void handleAnvilDistance(ServerPlayerEntity player, World world) {
        if (FabricLoader.getInstance().isModLoaded("valkyrienskies")) {
            if (world instanceof ServerWorld serverWorld) {
                BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUuid());
                if (anvilPos != null) {
                    BlockEntity be = serverWorld.getBlockEntity(anvilPos);
                    if (be instanceof AbstractSmithingAnvilBlockEntity) {
                        Vec3d anvilWorldPos = ValkyrienSkiesCompat.getActualWorldPos(serverWorld, anvilPos);
                        Vec3d playerWorldPos = player.getPos();

                        double distSq = playerWorldPos.squaredDistanceTo(anvilWorldPos);
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
        BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUuid());
        if (anvilPos != null) {
            BlockEntity be = world.getBlockEntity(anvilPos);
            if (be instanceof AbstractSmithingAnvilBlockEntity) {
                double distSq = player.getBlockPos().getSquaredDistance(anvilPos);
                int maxDist = ServerConfig.MAX_ANVIL_DISTANCE.get();
                if (distSq > (double) maxDist * maxDist) {
                    resetMinigameForPlayer(player);
                }
            }
        }
    }

    /**
     * Called from ItemStackAttributeMixin - Fabric has no ItemAttributeModifierEvent equivalent,
     * so the quality attribute bonus is injected directly into ItemStack.getAttributeModifiers.
     */
    public static void applyQualityAttributeModifiers(ItemStack stack, Multimap<EntityAttribute, EntityAttributeModifier> modifiers) {
        if (isBroken(stack)) return;
        if (!stack.hasNbt()) return;
        String quality = stack.getNbt().getString("ForgingQuality");
        if (quality.isEmpty()) return;

        for (QualityAttributeDefinition def : QualityAttributeReloadListener.INSTANCE.getAll()) {
            if (!matches(stack, def.targets())) continue;

            QualityValue value = def.qualities().get(quality);
            if (value == null || value.amount() == 0) continue;
            EntityAttribute attribute = Registries.ATTRIBUTE.get(def.attribute());
            if (attribute == null) continue;
            modifyAttribute(modifiers, attribute, value.amount(), value.operation(), quality);
        }
    }

    public static boolean matches(ItemStack stack, List<QualityTarget> targets) {
        Item item = stack.getItem();

        for (QualityTarget target : targets) {
            switch (target.type()) {
                case WEAPON -> {
                    if (item instanceof ToolItem || item instanceof RangedWeaponItem) {
                        return true;
                    }
                }

                case ARMOR -> {
                    if (item instanceof ArmorItem) {
                        return true;
                    }
                }

                case ITEM -> {
                    Identifier itemId = Registries.ITEM.getId(item);
                    if (itemId != null && itemId.equals(target.id())) {
                        return true;
                    }
                }

                case ITEM_TAG -> {
                    if (target.id() == null) break;

                    TagKey<Item> itemTag = TagKey.of(RegistryKeys.ITEM, target.id());
                    if (stack.isIn(itemTag)) {
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

    private static void modifyAttribute(Multimap<EntityAttribute, EntityAttributeModifier> modifiers, EntityAttribute attribute, double bonus, EntityAttributeModifier.Operation operation, String quality) {
        if (!modifiers.containsKey(attribute)) return;

        List<EntityAttributeModifier> existing = List.copyOf(modifiers.get(attribute));

        for (EntityAttributeModifier modifier : existing) {
            if (operation == EntityAttributeModifier.Operation.ADDITION) modifiers.remove(attribute, modifier);
            modifiers.put(attribute, createModifiedAttribute(modifier, bonus, operation, quality));
        }
    }

    public static EntityAttributeModifier createModifiedAttribute(EntityAttributeModifier original,
                                                                    double bonus,
                                                                    EntityAttributeModifier.Operation operation,
                                                                    String quality) {

        UUID id;
        String key = original.getId() + "_overgeared_" + quality + "_" + operation + bonus;
        double amount;

        if (operation == EntityAttributeModifier.Operation.ADDITION) {
            amount = original.getValue() + bonus;
            id = original.getId();
        } else {
            amount = bonus;
            id = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        }

        return new EntityAttributeModifier(
                id,
                "Overgeared",
                amount,
                operation
        );
    }

    private static void onServerStopping(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            resetMinigameForPlayer(player);
        }
        Overgeared.LOGGER.info("Reset all minigames on server stop.");
    }

    public static void resetMinigameForPlayer(ServerPlayerEntity player) {
        if (player == null) return;
        UUID playerId = player.getUuid();
        ModMessages.sendToPlayer(ModMessages.ONLY_RESET_MINIGAME, ModMessages.buf(), player);

        if (ModItemInteractEvents.playerAnvilPositions.containsKey(playerId)) {
            BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
            BlockEntity be = player.getWorld().getBlockEntity(anvilPos);

            if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                anvil.setProgress(0);
                anvil.markDirty();
                anvil.setMinigameOn(false);

                var buf = ModMessages.buf();
                ResetMinigameS2CPacket.encode(new ResetMinigameS2CPacket(anvilPos), buf);
                ModMessages.sendToPlayer(ModMessages.RESET_MINIGAME, buf, player);

                ModItemInteractEvents.releaseAnvil(player, anvilPos);
                ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
            }
        }
        // Note: the original Forge code also called AnvilMinigameEvents.reset(...) directly here,
        // but that class is client-only in this port - the ONLY_RESET_MINIGAME/RESET_MINIGAME
        // packets sent above already trigger the equivalent client-side reset.
    }

    public static void resetMinigameForPlayer(ServerPlayerEntity player, BlockPos anvilPos) {
        if (player == null) return;
        ModMessages.sendToPlayer(ModMessages.ONLY_RESET_MINIGAME, ModMessages.buf(), player);

        BlockEntity be = player.getWorld().getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.markDirty();
            anvil.setMinigameOn(false);
        }

        ModItemInteractEvents.playerAnvilPositions.remove(player.getUuid());
        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUuid());
    }

    public static void resetMinigameForAnvil(World world, BlockPos anvilPos) {
        BlockEntity be = world.getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.markDirty();
            anvil.setMinigameOn(false);
            anvil.clearOwner();
        }

        if (world instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getServer().getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                var buf = ModMessages.buf();
                ResetMinigameS2CPacket.encode(new ResetMinigameS2CPacket(anvilPos), buf);
                ModMessages.sendToPlayer(ModMessages.RESET_MINIGAME, buf, player);

                if (anvilPos.equals(ModItemInteractEvents.playerAnvilPositions.get(playerId))) {
                    ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                    ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                    break; // Only one player should ever hold this anvil
                }
            }
        }
    }

    private static boolean isStoneTool(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();

        return item == Items.STONE_SWORD
                || item == Items.STONE_PICKAXE
                || item == Items.STONE_AXE
                || item == Items.STONE_SHOVEL
                || item == Items.STONE_HOE;
    }

    private static void registerVillagerTrades() {
        for (VillagerProfession profession : new VillagerProfession[]{
                VillagerProfession.WEAPONSMITH, VillagerProfession.TOOLSMITH, VillagerProfession.ARMORER}) {

            for (int level = 1; level <= 5; level++) {
                int finalLevel = level;
                TradeOfferHelper.registerVillagerOffers(profession, level, trades -> {
                    for (int i = 0; i < trades.size(); i++) {
                        TradeOffers.Factory original = trades.get(i);
                        trades.set(i, (entity, random) -> {
                            TradeOffer offer = original.create(entity, random);
                            if (offer == null) return null;

                            if (isStoneTool(offer.getSellItem())) {
                                return offer;
                            }

                            return new QualityWrappedTrade(original, finalLevel).create(entity, random);
                        });
                    }
                });
            }

            TradeOfferHelper.registerVillagerOffers(profession, 1, trades -> {
                trades.add(new SteelEmeraldTrade(true));   // 1 steel -> 2 emerald
                trades.add(new SteelEmeraldTrade(false));  // 2 steel -> 1 emerald
            });
        }

        addForgedTrades(VillagerProfession.WEAPONSMITH, WEAPONSMITH_ITEMS());
        addForgedTrades(VillagerProfession.TOOLSMITH, TOOLSMITH_ITEMS());
        addForgedTrades(VillagerProfession.ARMORER, ARMORER_ITEMS());
    }

    private static void registerWanderingTraderTrades() {
        // Level 1 = common pool, level 2 = rare pool (vanilla's WanderingTraderTradeOffers convention)
        TradeOfferHelper.registerWanderingTraderOffers(1, trades ->
                trades.add(new BlueprintWanderingTrade(new ItemStack(ModItems.BLUEPRINT), 1, 5)));
        TradeOfferHelper.registerWanderingTraderOffers(2, trades ->
                trades.add(new BlueprintWanderingTrade(new ItemStack(ModItems.BLUEPRINT), 1, 10)));
    }

    private static List<Item> WEAPONSMITH_ITEMS() {
        return List.of(
                ModItems.IRON_SWORD_BLADE, ModItems.GOLDEN_SWORD_BLADE,
                ModItems.STEEL_SWORD_BLADE, ModItems.COPPER_SWORD_BLADE,
                ModItems.COPPER_SWORD, ModItems.STEEL_SWORD
        );
    }

    private static List<Item> TOOLSMITH_ITEMS() {
        return List.of(
                ModItems.IRON_PICKAXE_HEAD, ModItems.GOLDEN_PICKAXE_HEAD,
                ModItems.STEEL_PICKAXE_HEAD, ModItems.COPPER_PICKAXE_HEAD,

                ModItems.IRON_AXE_HEAD, ModItems.GOLDEN_AXE_HEAD,
                ModItems.STEEL_AXE_HEAD, ModItems.COPPER_AXE_HEAD,

                ModItems.IRON_SHOVEL_HEAD, ModItems.GOLDEN_SHOVEL_HEAD,
                ModItems.STEEL_SHOVEL_HEAD, ModItems.COPPER_SHOVEL_HEAD,

                ModItems.IRON_HOE_HEAD, ModItems.GOLDEN_HOE_HEAD,
                ModItems.STEEL_HOE_HEAD, ModItems.COPPER_HOE_HEAD,

                ModItems.COPPER_PICKAXE, ModItems.COPPER_AXE,
                ModItems.COPPER_SHOVEL, ModItems.COPPER_HOE,

                ModItems.STEEL_PICKAXE, ModItems.STEEL_AXE,
                ModItems.STEEL_SHOVEL, ModItems.STEEL_HOE
        );
    }

    private static List<Item> ARMORER_ITEMS() {
        return List.of(
                ModItems.COPPER_HELMET, ModItems.COPPER_CHESTPLATE,
                ModItems.COPPER_LEGGINGS, ModItems.COPPER_BOOTS,

                ModItems.STEEL_HELMET, ModItems.STEEL_CHESTPLATE,
                ModItems.STEEL_LEGGINGS, ModItems.STEEL_BOOTS
        );
    }

    private static void addForgedTrades(VillagerProfession profession, List<Item> items) {
        for (Item item : items) {
            String id = Registries.ITEM.getId(item).getPath();

            int level;
            int maxUses;
            int xp;

            if (id.startsWith("copper_")) {
                level = 1;
                maxUses = 12;
                xp = 2;
            } else if (id.startsWith("iron_") || id.startsWith("golden_")) {
                level = 2;
                maxUses = 10;
                xp = 5;
            } else { // steel
                level = 3;
                maxUses = 6;
                xp = 10;
            }

            TradeOffers.Factory baseTrade = new ForgedItemTrade(item, level, maxUses, xp);
            TradeOffers.Factory wrappedTrade = new QualityWrappedTrade(baseTrade, level);

            TradeOfferHelper.registerVillagerOffers(profession, level, trades -> trades.add(wrappedTrade));
        }
    }
}
