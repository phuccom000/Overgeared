package net.stirdrem.overgeared.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameEvents;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.datapack.RockInteractionData;
import net.stirdrem.overgeared.datapack.RockInteractionReloadListener;
import net.stirdrem.overgeared.heatedtem.HeatedItemProvider;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.HideMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.StartMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.ToggleMinigameS2CPacket;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.FletchingStationMenu;
import net.stirdrem.overgeared.screen.RockKnappingMenuProvider;
import net.stirdrem.overgeared.util.ItemInteractionHelper;
import net.stirdrem.overgeared.util.ModTags;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static net.stirdrem.overgeared.Overgeared.getCooledItem;
import static net.stirdrem.overgeared.util.ItemInteractionHelper.*;
import static net.stirdrem.overgeared.client.ClientAnvilMinigameData.playerAnvilPositions;
import static net.stirdrem.overgeared.client.ClientAnvilMinigameData.playerMinigameVisibility;

@Mod.EventBusSubscriber(modid = Overgeared.MOD_ID)
public class ModItemInteractEvents {

    private static final ConcurrentMap<ItemEntity, Long> trackedSinceMs = new ConcurrentHashMap<>();

    // timeout for pruning tracked entities that never went to water (milliseconds).
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        // Check if the item is heated either by tag or NBT
        boolean isHeatedItem = heldStack.is(ModTags.Items.HEATED_METALS)
                || (heldStack.hasTag() && heldStack.getTag().getBoolean("Heated"));

        if (!isHeatedItem) {
            return;
        }

        // Handle water cauldron interaction
        if (state.is(Blocks.WATER_CAULDRON)) {
            ItemInteractionHelper.coolInCauldron(player, heldStack, state);
            // Remove the "Heated" tag if it exists

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }

    }

    @SubscribeEvent
    public static void onUseSmithingHammer(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack heldItem = event.getItemStack();

        if (!heldItem.is(ModTags.Items.SMITHING_HAMMERS)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockEntity be = level.getBlockEntity(pos);
        BlockState clickedState = level.getBlockState(pos);

        // =========================
        // Convert blocks to anvils
        // =========================

        if (!level.isClientSide && player.isCrouching()
                && ItemInteractionHelper.tryBuildSmithingAnvil(player, level, pos, clickedState)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvilBE)) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                hideMinigame(serverPlayer);
            }
            return;
        }

        if (!player.isCrouching()) return;

        // =========================
        // SERVER LOGIC ONLY
        // =========================

        if (level.isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (clickedState.getBlock() instanceof AbstractSmithingAnvil anvil && !heldItem.is(anvil.hammerTag())) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.overgeared.wrong_hammer")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        UUID playerUUID = player.getUUID();

        // No recipe
        if (!anvilBE.hasRecipe()) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.overgeared.no_recipe")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Quality check
        if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.overgeared.item_has_no_quality")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        UUID currentOwner = anvilBE.getOwnerUUID();

        // Already used
        if (currentOwner != null && !currentOwner.equals(playerUUID)) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.overgeared.anvil_in_use_by_another")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Player already using another anvil
        if (playerAnvilPositions.containsKey(playerUUID)
                && !pos.equals(playerAnvilPositions.get(playerUUID))) {

            serverPlayer.sendSystemMessage(
                    Component.translatable("message.overgeared.another_anvil_in_use")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
        ForgingRecipe recipe = recipeOpt.get();

        if (level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING)) {
            if (!serverPlayer.getRecipeBook().contains(recipe)) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.overgeared.no_recipe")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }
        }
        // =========================
        // START MINIGAME (SERVER)
        // =========================

        if (currentOwner == null) {

            anvilBE.setOwner(playerUUID);
            anvilBE.setPlayer(player);
            anvilBE.setMinigameOn(true);

            playerAnvilPositions.put(playerUUID, pos);
            playerMinigameVisibility.put(playerUUID, true);

            int hitsRequired = anvilBE.getRequiredProgress();
            String quality = anvilBE.minigameQuality();

            // Sync ownership
            CompoundTag sync = new CompoundTag();
            sync.putUUID("anvilOwner", playerUUID);
            sync.putLong("anvilPos", pos.asLong());
            ModMessages.sendToAll(new MinigameSyncS2CPacket(sync));

            // Tell client to start minigame UI
            ModMessages.sendToPlayer(new StartMinigameS2CPacket(pos, hitsRequired, quality), serverPlayer);
        } else if (currentOwner.equals(playerUUID)) {
            boolean visible = playerMinigameVisibility.get(playerUUID);
            playerMinigameVisibility.put(playerUUID, !visible);
            ModMessages.sendToPlayer(new ToggleMinigameS2CPacket(pos, !visible), serverPlayer);

        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public static void handleAnvilOwnershipSync(CompoundTag syncData) {
        UUID owner = null;
        if (syncData.contains("anvilOwner")) {
            owner = syncData.getUUID("anvilOwner");
            if (owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
                owner = null;
            }
        }
        BlockPos pos = BlockPos.of(syncData.getLong("anvilPos"));
        ClientAnvilMinigameData.putOccupiedAnvil(pos, owner);

        // ✅ Only start minigame if this client is the new owner and it was waiting
        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.getUUID().equals(owner)
                && pos.equals(ClientAnvilMinigameData.getPendingMinigamePos())) {

            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE && anvilBE.hasRecipe()) {
                Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                recipeOpt.ifPresent(recipe -> {
                    ClientAnvilMinigameData.clearPendingMinigame(); // ✅ Done
                });
            }
        }
    }

    public static void releaseAnvil(ServerPlayer player, BlockPos pos) {
        UUID playerId = player.getUUID();
        if (playerMinigameVisibility.get(playerId) != null)
            playerMinigameVisibility.remove(playerId);
        if (playerAnvilPositions.get(playerId) != null
                && pos.equals(playerAnvilPositions.get(playerId))
        ) {
            playerAnvilPositions.remove(playerId);

            // 1. Clear ownership from the block entity (server-side)
            BlockEntity be = player.level().getBlockEntity(pos);
            String quality = "perfect";
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE) {
                anvilBE.clearOwner();
                quality = anvilBE.minigameQuality();
            }
            // 3. Clear client-side state
            ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
            AnvilMinigameEvents.reset(quality);
            // 4. Sync null ownership to all clients
            CompoundTag syncData = new CompoundTag();
            syncData.putLong("anvilPos", pos.asLong());
            syncData.putUUID("anvilOwner", new UUID(0, 0)); // special "no owner" UUID
            ModMessages.sendToAll(new MinigameSyncS2CPacket(syncData));
        }

    }

    public static ServerPlayer getUsingPlayer(BlockPos pos) {
        // Get the server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        // Iterate through all online players
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();

            // Check if this player is using the specified anvil
            if (playerAnvilPositions.containsKey(playerId) &&
                    playerAnvilPositions.get(playerId).equals(pos)) {
                return player;
            }
        }

        return null;
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide()) return;

        hideMinigame((ServerPlayer) player);
    }

    public static void hideMinigame(ServerPlayer player) {
        ModMessages.sendToPlayer(new HideMinigameS2CPacket(), player);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level world = event.getLevel();
        if (world.isClientSide()) return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (ItemInteractionHelper.tryCoolInWater(player, stack, world)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
        InteractionResult grindResult = ItemInteractionHelper.tryGrind(player, stack, world);
        if (grindResult != null) {
            event.setCancellationResult(grindResult);
            event.setCanceled(true);
            return;
        }
        ItemInteractionHelper.handleMinigameCleanup(player, world);
    }

    @SubscribeEvent
    public static void onUsingKnappable(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack usedStack = event.getItemStack();

        // Only trigger for knappable items
        if (!usedStack.is(ModTags.Items.KNAPPABLE)) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Require BOTH hands to be knappable
        if (!(mainHand.is(ModTags.Items.KNAPPABLE) && offHand.is(ModTags.Items.KNAPPABLE))) {
            return;
        }

        // Prevent vanilla handling
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.STONE_PLACE,
                    SoundSource.PLAYERS,
                    0.6f,
                    1.0f
            );

            NetworkHooks.openScreen(serverPlayer, new RockKnappingMenuProvider(), buf -> {
                buf.writeItem(mainHand);
                buf.writeItem(offHand);
            });
        }
    }

    private static final Map<ServerLevel, List<ItemEntity>> trackedEntitiesPerWorld = new HashMap<>();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        boolean isHeatedItem = stack.hasTag() && stack.getTag().getBoolean("Heated");
        Item cooled = getCooledItem(stack.getItem(), event.getLevel());

        if (hasCoolingRecipe(stack.getItem(), event.getLevel()) || isHeatedItem) {
            // Only track if cooled ingot exists or item is heated
            if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

            // Track per world
            trackedEntitiesPerWorld
                    .computeIfAbsent(serverLevel, w -> new ArrayList<>())
                    .add(itemEntity);

            // Only track items that already have HeatedSince
            if (stack.hasTag() && stack.getTag().contains("HeatedSince")) {
                long heatedSince = stack.getTag().getLong("HeatedSince");
                trackedSinceMs.put(itemEntity, heatedSince);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityPickupItem(EntityItemPickupEvent event) {
        if (!(event.getItem().level() instanceof ServerLevel serverLevel)) return;

        List<ItemEntity> tracked = trackedEntitiesPerWorld.get(serverLevel);
        if (tracked != null) {
            tracked.remove(event.getItem());
            if (tracked.isEmpty()) trackedEntitiesPerWorld.remove(serverLevel);
        }

        trackedSinceMs.remove(event.getItem());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        // Iterate over all loaded worlds
        for (ServerLevel level : server.getAllLevels()) {
            long now = level.getGameTime();

            // Housekeeping every 10 ticks; dropped items are checked every tick so they
            // cool on the same tick as the rest of their stack in a player's inventory.
            if (now % 10 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.containerMenu != null && player.containerMenu != player.inventoryMenu) {
                        checkContainerMenu(player, player.containerMenu);
                    }
                }
            }

            List<ItemEntity> tracked = trackedEntitiesPerWorld.get(level);
            if (tracked == null || tracked.isEmpty()) continue;

            Iterator<ItemEntity> it = tracked.iterator();

            while (it.hasNext()) {
                ItemEntity entity = it.next();
                if (!entity.isAlive()) {
                    it.remove();
                    trackedSinceMs.remove(entity);
                    continue;
                }

                ItemStack stack = entity.getItem();

                // --- Check if heated ---
                boolean isHeated = (stack.hasTag() && stack.getTag().getBoolean("Heated"))
                        || hasCoolingRecipeCached(stack.getItem(), level);

                if (!isHeated) {
                    it.remove();
                    trackedSinceMs.remove(entity);
                    continue;
                }

                long started = getOrStampHeatedSince(stack, now);
                boolean cooled = now - started >= Overgeared.getConfig().HEATED_ITEM_COOLDOWN_TICKS();

                // --- Water-based cooling ---
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                        entity.getX(), entity.getY(), entity.getZ());

                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)) {
                    cooled = true;
                }

                if (cooled) {
                    // Cool entire stack
                    coolItemEntity(entity);
                    level.sendParticles(ParticleTypes.SMOKE,
                            entity.getX(), entity.getY() + 0.25, entity.getZ(),
                            6, 0.15, 0.15, 0.15, 0.02);
                    level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.0f);
                    // Clear timestamp
                    trackedSinceMs.remove(entity);

                    // Remove entity if stack is empty
                    if (entity.getItem().isEmpty()) {
                        it.remove();
                    }
                }
            }

            // Clean up empty lists to avoid memory leaks
            if (tracked.isEmpty()) {
                trackedEntitiesPerWorld.remove(level);
            }
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ItemEntity) {
            event.addCapability(ResourceLocation.tryBuild(Overgeared.MOD_ID, "heated_item"),
                    new HeatedItemProvider());
        }
    }

    @SubscribeEvent
    public static void onFlintUsedOnStone(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack heldItem = event.getItemStack();
        Player player = event.getEntity();

        for (RockInteractionData data : RockInteractionReloadListener.INSTANCE.getAll()) {

            if (!data.matches(state, heldItem)) continue;

            RockInteractionData.ToolEntry tool = data.getTool(heldItem);
            if (tool == null) continue;

            ServerLevel serverLevel = (ServerLevel) level;

            // Drop roll
            if (level.random.nextFloat() < tool.dropChance()) {
                ItemStack dropStack = tool.dropItem().copy();

                double sx = pos.getX() + 0.5;
                double sy = pos.getY() + 0.9;
                double sz = pos.getZ() + 0.5;

                double dx = player.getX() - sx;
                double dy = (player.getY() + player.getEyeHeight()) - sy;
                double dz = player.getZ() - sz;

                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len != 0) {
                    dx /= len;
                    dy /= len;
                    dz /= len;
                }

                ItemEntity item = new ItemEntity(serverLevel, sx, sy, sz, dropStack);
                item.setDeltaMovement(dx * 0.25, dy * 0.25, dz * 0.25);
                item.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(item);

                level.setBlockAndUpdate(pos, data.getResultBlock().defaultBlockState());
            }

            // Break / damage roll
            if (level.random.nextFloat() < tool.breakChance()) {

                if (heldItem.isDamageableItem()) {
                    // Damage the tool by 1 durability
                    heldItem.hurtAndBreak(1, player, p ->
                            p.broadcastBreakEvent(event.getHand())
                    );
                } else {
                    // Non-damageable items (like flint) get consumed
                    heldItem.shrink(1);
                }

                level.playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                        0.8F, 1.0F);
            } else {
                level.playSound(null, pos,
                        SoundEvents.STONE_HIT, SoundSource.BLOCKS,
                        1.0F, 1.0F);
            }

            player.swing(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            hideMinigame((ServerPlayer) player);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            hideMinigame((ServerPlayer) player);
        }
    }

    @SubscribeEvent
    public static void onArrowTipping(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();

        if (level.isClientSide()) return;
        if (!Overgeared.getConfig().TIPPING_TOGGLE()) return;

        // Get items in both hands
        ItemStack usedHand = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        // Check if we're dealing with arrow + potion combination
        boolean isVanillaArrow = usedHand.is(Items.ARROW) && otherStack.is(Items.POTION);
        boolean isCustomArrow = Overgeared.getConfig().UPGRADE_ARROW_POTION_TOGGLE() && (usedHand.is(ModItems.IRON_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.STEEL_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.DIAMOND_UPGRADE_ARROW.get())) &&
                otherStack.is(Items.POTION);

        if (!isVanillaArrow && !isCustomArrow) {
            return;
        }

        CompoundTag potionTag = otherStack.getOrCreateTag();
        int used = potionTag.getInt("TippedUsed");
        int maxUse = Overgeared.getConfig().MAX_POTION_TIPPING_USE();
        Potion basePotion = PotionUtils.getPotion(otherStack);

        // Create the appropriate tipped arrow BEFORE consuming
        ItemStack resultArrow;
        if (isVanillaArrow) {
            resultArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), basePotion);
        } else {
            resultArrow = usedHand.copy();
            resultArrow.setCount(1);

            CompoundTag arrowTag = new CompoundTag();
            arrowTag.putString("Potion", BuiltInRegistries.POTION.getKey(basePotion).toString());

            if (potionTag.contains("CustomPotionEffects", 9)) {
                arrowTag.put("CustomPotionEffects", potionTag.getList("CustomPotionEffects", 10));
            }
            if (potionTag.contains("CustomPotionColor", 3)) {
                arrowTag.putInt("CustomPotionColor", potionTag.getInt("CustomPotionColor"));
            }

            resultArrow.setTag(arrowTag);
        }

        // Only consume arrow after we've successfully created the tipped version
        if (usedHand.getCount() == 1) {
            // For single arrow, replace it with the tipped version
            player.setItemInHand(hand, resultArrow);
        } else {
            // For stack, shrink and add new tipped arrow
            usedHand.shrink(1);
            player.setItemInHand(hand, usedHand);
            if (!player.getInventory().add(resultArrow)) {
                player.drop(resultArrow, false);
            }
        }

        // Handle potion stack
        if (otherStack.getCount() > 1) {
            ItemStack onePotion = otherStack.split(1);
            CompoundTag oneTag = onePotion.getOrCreateTag();
            oneTag.putInt("TippedUsed", used + 1);
            PotionUtils.setPotion(onePotion, basePotion);
            player.setItemInHand(otherHand, otherStack);
        } else {
            used++;
            if (used >= maxUse) {
                player.setItemInHand(otherHand, new ItemStack(Items.GLASS_BOTTLE));
            } else {
                potionTag.putInt("TippedUsed", used);
                PotionUtils.setPotion(otherStack, basePotion);
                player.setItemInHand(otherHand, otherStack);
            }
        }

        level.playSound(null,
                player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS,
                0.6F,
                1.2F
        );

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickFletching(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        if (!Overgeared.getConfig().ENABLE_FLETCHING_RECIPES()) return;

        // Main hand only
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.FLETCHING_TABLE)) return;

        // Prevent held item usage (bows, food, shields, etc.)
        event.setUseItem(Event.Result.DENY);
        event.setUseBlock(Event.Result.DENY);

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        SimpleMenuProvider provider = new SimpleMenuProvider(
                (windowId, playerInv, p) ->
                        new FletchingStationMenu(
                                windowId,
                                playerInv,
                                ContainerLevelAccess.create(level, pos)
                        ),
                Component.translatable("container.overgeared.fletching_table")
        );

        NetworkHooks.openScreen((ServerPlayer) player, provider, pos);

        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }
}