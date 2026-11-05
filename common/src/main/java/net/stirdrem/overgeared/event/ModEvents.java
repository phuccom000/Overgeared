package net.stirdrem.overgeared.event;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.entity.Entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.networking.PacketDistributor;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.compat.sable.SableCompat;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.heateditem.HeatedItem;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.networking.packet.OnlyResetMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.ResetMinigameS2CPacket;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;
import java.util.UUID;

import static net.stirdrem.overgeared.util.BrokenHelper.isBroken;

public class ModEvents {
    private static final int HEATED_ITEM_CHECK_INTERVAL = 20; // 1 second

    private static int serverTick = 0;

    public static void onServerTick() {
        serverTick++;
    }

    public static void onPlayerTick(Player entity) {
        if (!(entity instanceof ServerPlayer player))
            return;
        if (serverTick % HEATED_ITEM_CHECK_INTERVAL != 0)
            return;
        Level level = player.level();
        handleAnvilDistance(player, level);
        handleAnvilDistance(player, level);
    }

    private static void handleAnvilDistance(ServerPlayer player, Level level) {
        if (ServerConfig.MAX_ANVIL_DISTANCE.get() == 0) return;

        if (AnvilMinigameEvents.hasAnvilPosition(player.getUUID())) {
            BlockPos anvilPos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
            BlockEntity be = level.getBlockEntity(anvilPos);
            if ((be instanceof AbstractSmithingAnvilBlockEntity)) {
                int maxDist = ServerConfig.MAX_ANVIL_DISTANCE.get();

                double distSq;

                if (SableCompat.LOADED) {
                    distSq = SableCompat.distanceSquaredWithSubLevels(level, player.position(), anvilPos.getCenter());
                } else {
                    distSq = player.blockPosition().distSqr(anvilPos);
                }

                if (distSq > maxDist * maxDist) {
                    resetMinigameForPlayer(player);
                }
            }
        }
    }

    public static void onPlayerJoinWorld(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            // Reset minigame state when joining any world
            resetMinigameForPlayer(player);

            // Start timeout counter if needed
            // startTimeoutCounter(player);
        }
    }

    public static void onPlayerDisconnect(Player entity) {
        if (entity instanceof ServerPlayer player) {
            resetMinigameForPlayer(player);
        }
    }

    public static void onPlayerChangedDimension(Player entity) {
        if (entity instanceof ServerPlayer player) {
            resetMinigameForPlayer(player);
            // startTimeoutCounter(player);
        }
    }

    public static void onServerStopping(MinecraftServer server) {

        // Iterate over all players and reset their minigame
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            resetMinigameForPlayer(player);
        }

        // Optional: Log for debugging
        OvergearedMod.LOGGER.info("Reset all minigames on server stop.");
    }

    public static void resetMinigameForPlayer(ServerPlayer player) {
        if (player == null)
            return;
        UUID playerId = player.getUUID();
        // Resets the client even if it isn't tracked at an anvil
        PacketDistributor.sendToPlayer(player, new OnlyResetMinigameS2CPacket());
        if (ModItemInteractEvents.playerAnvilPositions.containsKey(playerId)) {
            BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
            BlockEntity be = player.level().getBlockEntity(anvilPos);

            if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                anvil.setProgress(0);
                anvil.setChanged();
                anvil.setMinigameOn(false);
                anvil.clearOwner();

                // Releases ownership, syncs it to all clients and sends this player a ResetMinigameS2CPacket
                ModItemInteractEvents.releaseAnvil(player, anvilPos);
                ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
            }
        }
    }

    public static void resetMinigameForAnvil(Level level, BlockPos anvilPos) {
        // Only execute on server side
        String quality = "perfect";
        // Reset the anvil block entity
        BlockEntity be = level.getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            anvil.clearOwner(); // Clear ownership from the anvil itself
            quality = anvil.minigameQuality().getDisplayName();
        }

        // Find the player using this anvil and reset only them
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                if (ModItemInteractEvents.playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO).equals(anvilPos)) {
                    PacketDistributor.sendToPlayer(player, new ResetMinigameS2CPacket(anvilPos, quality));
                    // Clear server-side tracking for this player
                    ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                    ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                    break; // Only one player can use an anvil
                }
            }
        }
    }

    public static void onTooltip(ItemStack stack, List<Component> tooltip) {
        int insertOffset = 1;

        // Add Forging Quality
        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY.get());
        if (quality != null) {
            Component qualityComponent = switch (quality) {
                case MASTER ->
                        Component.translatable("tooltip.overgeared.master").withStyle(ChatFormatting.LIGHT_PURPLE);
                case PERFECT -> Component.translatable("tooltip.overgeared.perfect").withStyle(ChatFormatting.GOLD);
                case EXPERT -> Component.translatable("tooltip.overgeared.expert").withStyle(ChatFormatting.BLUE);
                case WELL -> Component.translatable("tooltip.overgeared.well").withStyle(ChatFormatting.YELLOW);
                case POOR -> Component.translatable("tooltip.overgeared.poor").withStyle(ChatFormatting.RED);
                default -> null;
            };
            if (qualityComponent != null) {
                tooltip.add(insertOffset++, qualityComponent);
            }
        }

        if (isBroken(stack)) {
            tooltip.add(insertOffset++,
                    Component.translatable("tooltip.overgeared.item_broken")
                            .withStyle(ChatFormatting.RED)
            );
        }

        if (Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT.get()))) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heated")
                    .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }

        // Cooling countdown for heated items (tag-based or component-based)
        if (HeatedItem.isHeated(stack)) {
            Long heatedSince = stack.get(ModComponents.HEATED_TIME.get());
            Level clientLevel = net.stirdrem.overgeared.client.ClientLevelAccess.getLevel();
            if (heatedSince != null && clientLevel != null) {
                long remainingTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get()
                        - (clientLevel.getGameTime() - heatedSince);
                long seconds = Math.max(0, (remainingTicks + 19) / 20);
                tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.cools_in", seconds)
                        .withStyle(ChatFormatting.GOLD));
            }
        }

        // Add Polish status
        Boolean isPolished = stack.get(ModComponents.POLISHED.get());
        if (isPolished != null) {
            Component polishComponent = isPolished
                    ? Component.translatable("tooltip.overgeared.polished").withStyle(ChatFormatting.BLUE,
                    ChatFormatting.ITALIC)
                    : Component.translatable("tooltip.overgeared.unpolished").withStyle(ChatFormatting.RED,
                    ChatFormatting.ITALIC);
            tooltip.add(insertOffset++, polishComponent);
        }
        if (Boolean.TRUE.equals(stack.get(ModComponents.FAILED_RESULT.get()))) {
            tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.failedResult")
                    .withStyle(ChatFormatting.RED));
        }

        if (stack.is(ModTags.Items.KNAPPABLES)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.knappable")
                    .withStyle(ChatFormatting.GRAY));
        }

        // Smithing Hammer special tooltip
        if (stack.is(ModTags.Items.SMITHING_HAMMERS)) {
            if (!Screen.hasShiftDown()) {
                tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.smithing_hammer.hold_shift")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
                tooltip.add(insertOffset++,
                        Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line1")
                                .withStyle(ChatFormatting.GRAY));
                tooltip.add(insertOffset++,
                        Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line2")
                                .withStyle(ChatFormatting.GRAY));
                if (ServerConfig.ENABLE_STONE_TO_ANVIL.get())
                    tooltip.add(insertOffset++,
                            Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line3")
                                    .withStyle(ChatFormatting.GRAY));
                if (ServerConfig.ENABLE_ANVIL_TO_SMITHING.get())
                    tooltip.add(insertOffset++,
                            Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line4")
                                    .withStyle(ChatFormatting.GRAY));
            }
        }

        // 🔽 Add Potion Uses Left Tooltip
        if (stack.is(Items.POTION) && ServerConfig.TIPPING_TOGGLE.get()) {
            int maxUses = ServerConfig.MAX_POTION_TIPPING_USE.get();
            Integer used = stack.getOrDefault(ModComponents.TIPPED_USES.get(), 0);

            int left = Math.max(0, maxUses - used);
            tooltip.add(Component.translatable("tooltip.overgeared.potion_uses", left, maxUses)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (!ServerConfig.ENABLE_MOD_TOOLTIPS.get())
            return;

        if (stack.is(Items.FLINT) && ServerConfig.GET_ROCK_USING_FLINT.get()) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.flint_flavor")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.DIAMOND_SHARD.get())) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.diamond_shard")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.STONE_HAMMER_HEAD.get())) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.stone_hammer_head")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModTags.Items.HEATED_METALS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heatedingots.tooltip")
                    .withStyle(ChatFormatting.RED));
        }

        if (stack.is(ModTags.Items.HOT_ITEMS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.hotitems.tooltip")
                    .withStyle(ChatFormatting.RED));
        }

        String creatorName = stack.get(ModComponents.CREATOR.get());
        if (creatorName != null) {
            Component creatorComponent = Component.translatable("tooltip.overgeared.made_by")
                    .append(" ")
                    .append(creatorName)
                    .withStyle(ChatFormatting.GRAY);
            tooltip.add(insertOffset++, creatorComponent);
        }
    }

    public static void onVillagerTrades(VillagerProfession type, Int2ObjectMap<List<VillagerTrades.ItemListing>> tradesByLevel) {

        // Wrap vanilla trades with quality
        if (type == VillagerProfession.WEAPONSMITH
                || type == VillagerProfession.TOOLSMITH
                || type == VillagerProfession.ARMORER) {

            for (int level = 1; level <= 5; level++) {
                List<VillagerTrades.ItemListing> trades = tradesByLevel.get(level);
                for (int i = 0; i < trades.size(); i++) {
                    VillagerTrades.ItemListing original = trades.get(i);

                    int finalLevel = level;
                    trades.set(i, (entity, random) -> {
                        MerchantOffer offer = original.getOffer(entity, random);
                        if (offer == null)
                            return null;

                        ItemStack result = offer.getResult();

                        if (isStoneTool(result)) {
                            return offer;
                        }

                        return new QualityWrappedTrade(original, finalLevel)
                                .getOffer(entity, random);
                    });
                }
            }
            tradesByLevel.get(1).add(new SteelEmeraldTrade(true)); // 1 steel → 2 emerald
            tradesByLevel.get(1).add(new SteelEmeraldTrade(false)); // 2 steel → 1 emerald
        }

        // Add profession-specific forged trades
        if (type == VillagerProfession.WEAPONSMITH) {
            addForgedTrades(tradesByLevel, WEAPONSMITH_ITEMS());
        }

        if (type == VillagerProfession.TOOLSMITH) {
            addForgedTrades(tradesByLevel, TOOLSMITH_ITEMS());
        }

        if (type == VillagerProfession.ARMORER) {
            addForgedTrades(tradesByLevel, ARMORER_ITEMS());
        }
    }

    public static void onWanderingTraderTrades(List<VillagerTrades.ItemListing> genericTrades, List<VillagerTrades.ItemListing> rareTrades) {

        // Regular wandering trader trades
        genericTrades.add(
                new BlueprintWanderingTrade(
                        new ItemStack(ModItems.BLUEPRINT.get()),
                        1, // max uses
                        5 // trader XP
                ));

        // Optional: Rare trade pool (appears less often)
        rareTrades.add(
                new BlueprintWanderingTrade(
                        new ItemStack(ModItems.BLUEPRINT.get()),
                        1,
                        10));
    }

    private static boolean isStoneTool(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        Item item = stack.getItem();

        return item == Items.STONE_SWORD
                || item == Items.STONE_PICKAXE
                || item == Items.STONE_AXE
                || item == Items.STONE_SHOVEL
                || item == Items.STONE_HOE;
    }

    private static List<Item> WEAPONSMITH_ITEMS() {
        return List.of(
                // ---- Sword Blades ----
                ModItems.IRON_SWORD_BLADE.get(),
                ModItems.GOLDEN_SWORD_BLADE.get(),
                ModItems.STEEL_SWORD_BLADE.get(),
                ModItems.COPPER_SWORD_BLADE.get(),

                // ---- Finished Weapons ----
                ModItems.COPPER_SWORD.get(),
                ModItems.STEEL_SWORD.get());
    }

    private static List<Item> TOOLSMITH_ITEMS() {
        return List.of(
                // ---- Pickaxe Heads ----
                ModItems.IRON_PICKAXE_HEAD.get(),
                ModItems.GOLDEN_PICKAXE_HEAD.get(),
                ModItems.STEEL_PICKAXE_HEAD.get(),
                ModItems.COPPER_PICKAXE_HEAD.get(),

                // ---- Axe Heads ----
                ModItems.IRON_AXE_HEAD.get(),
                ModItems.GOLDEN_AXE_HEAD.get(),
                ModItems.STEEL_AXE_HEAD.get(),
                ModItems.COPPER_AXE_HEAD.get(),

                // ---- Shovel Heads ----
                ModItems.IRON_SHOVEL_HEAD.get(),
                ModItems.GOLDEN_SHOVEL_HEAD.get(),
                ModItems.STEEL_SHOVEL_HEAD.get(),
                ModItems.COPPER_SHOVEL_HEAD.get(),

                // ---- Hoe Heads ----
                ModItems.IRON_HOE_HEAD.get(),
                ModItems.GOLDEN_HOE_HEAD.get(),
                ModItems.STEEL_HOE_HEAD.get(),
                ModItems.COPPER_HOE_HEAD.get(),

                // ---- Finished Tools ----
                ModItems.COPPER_PICKAXE.get(),
                ModItems.COPPER_AXE.get(),
                ModItems.COPPER_SHOVEL.get(),
                ModItems.COPPER_HOE.get(),

                ModItems.STEEL_PICKAXE.get(),
                ModItems.STEEL_AXE.get(),
                ModItems.STEEL_SHOVEL.get(),
                ModItems.STEEL_HOE.get());
    }

    private static List<Item> ARMORER_ITEMS() {
        return List.of(
                ModItems.COPPER_HELMET.get(),
                ModItems.COPPER_CHESTPLATE.get(),
                ModItems.COPPER_LEGGINGS.get(),
                ModItems.COPPER_BOOTS.get(),

                ModItems.STEEL_HELMET.get(),
                ModItems.STEEL_CHESTPLATE.get(),
                ModItems.STEEL_LEGGINGS.get(),
                ModItems.STEEL_BOOTS.get());
    }

    private static void addForgedTrades(
            Int2ObjectMap<List<VillagerTrades.ItemListing>> tradesByLevel,
            List<Item> items) {
        for (Item item : items) {
            String id = BuiltInRegistries.ITEM.getKey(item).getPath();

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

            // Base forged trade
            VillagerTrades.ItemListing baseTrade = new ForgedItemTrade(item, level, maxUses, xp);

            // Wrap it with quality logic
            VillagerTrades.ItemListing wrappedTrade = new QualityWrappedTrade(baseTrade, level);

            tradesByLevel.get(level).add(wrappedTrade);
        }
    }
}