package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.OvergearedFabric;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.datapack.FabricRockInteractionReloadListener;
import net.stirdrem.overgeared.datapack.RockInteractionData;
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

import static net.stirdrem.overgeared.util.ItemInteractionHelper.*;

public class ModItemInteractEvents {

    private static final ConcurrentMap<ItemEntity, Long> trackedSinceMs = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, List<ItemEntity>> trackedEntitiesPerWorld = new HashMap<>();

    public static void register() {
        UseBlockCallback.EVENT.register(ModItemInteractEvents::onRightClickBlock);
        UseBlockCallback.EVENT.register(ModItemInteractEvents::onUseSmithingHammer);
        UseBlockCallback.EVENT.register(ModItemInteractEvents::onFlintUsedOnStone);
        UseBlockCallback.EVENT.register(ModItemInteractEvents::onRightClickFletching);

        UseItemCallback.EVENT.register(ModItemInteractEvents::onRightClickItem);
        UseItemCallback.EVENT.register(ModItemInteractEvents::onUsingKnappable);
        UseItemCallback.EVENT.register(ModItemInteractEvents::onArrowTipping);

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                hideMinigame(serverPlayer);
            }
            return InteractionResult.PASS;
        });

        // Fabric API has no "entity joined world" event; new heated item entities are picked
        // up by the periodic scan in onServerTick instead (see trackNewItemEntities).
        ServerTickEvents.END_SERVER_TICK.register(ModItemInteractEvents::onServerTick);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register(
                (entity, world) -> trackItemEntity(world, entity));
    }

    // =========================
    // Cauldron cooling
    // =========================

    private static InteractionResult onRightClickBlock(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);
        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        boolean isHeatedItem = heldStack.is(ModTags.Items.HEATED_METALS)
                || (heldStack.hasTag() && heldStack.getTag().getBoolean("Heated"));

        if (!isHeatedItem) return InteractionResult.PASS;

        if (state.is(Blocks.WATER_CAULDRON)) {
            ItemInteractionHelper.coolInCauldron(player, heldStack, state);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // =========================
    // Smithing hammer: anvil conversion + minigame open/toggle
    // =========================

    private static InteractionResult onUseSmithingHammer(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        ItemStack heldItem = player.getItemInHand(hand);

        if (!heldItem.is(ModTags.Items.SMITHING_HAMMERS)) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockEntity be = world.getBlockEntity(pos);
        BlockState clickedState = world.getBlockState(pos);

        // =========================
        // Convert blocks to anvils
        // =========================

        if (!world.isClientSide && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.STONE_ANVIL_BASES)
                && Overgeared.getConfig().ENABLE_STONE_TO_ANVIL()) {

            BlockState newState = ModBlocks.STONE_SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(AbstractSmithingAnvil.FACING, player.getDirection().getClockWise());

            world.setBlock(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);

            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "stone");
            }

            return InteractionResult.SUCCESS;
        }

        if (!world.isClientSide && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.IRON_ANVIL_BASES)
                && Overgeared.getConfig().ENABLE_ANVIL_TO_SMITHING()) {

            BlockState newState = ModBlocks.SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(AbstractSmithingAnvil.FACING, player.getDirection().getClockWise());

            world.setBlock(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);

            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "iron");
            }

            return InteractionResult.SUCCESS;
        }

        if (!world.isClientSide && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.TIER_A_ANVIL_BASES)) {
            BlockState newState = ModBlocks.TIER_A_SMITHING_ANVIL.get().defaultBlockState().setValue(AbstractSmithingAnvil.FACING, player.getDirection().getClockWise());
            world.setBlock(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "tier_a");
            }
            return InteractionResult.SUCCESS;
        }

        if (!world.isClientSide && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.TIER_B_ANVIL_BASES)) {
            BlockState newState = ModBlocks.TIER_B_SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(AbstractSmithingAnvil.FACING, player.getDirection().getClockWise());
            world.setBlock(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "tier_b");
            }
            return InteractionResult.SUCCESS;
        }

        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvilBE)) {
            if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                hideMinigame(serverPlayer);
            }
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        // =========================
        // SERVER LOGIC ONLY
        // =========================

        if (world.isClientSide) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        UUID playerUUID = player.getUUID();

        if (!anvilBE.hasRecipe()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.PASS;
        }

        if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.PASS;
        }

        UUID currentOwner = anvilBE.getOwnerUUID();

        if (currentOwner != null && !currentOwner.equals(playerUUID)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.PASS;
        }

        if (ClientAnvilMinigameData.playerAnvilPositions.containsKey(playerUUID)
                && !pos.equals(ClientAnvilMinigameData.playerAnvilPositions.get(playerUUID))) {

            serverPlayer.displayClientMessage(
                    Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.PASS;
        }

        Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
        ForgingRecipe recipe = recipeOpt.get();

        if (world.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING)) {
            if (!serverPlayer.getRecipeBook().contains(recipe)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResult.PASS;
            }
        }

        // =========================
        // START MINIGAME (SERVER)
        // =========================

        if (currentOwner == null) {

            anvilBE.setOwner(playerUUID);
            anvilBE.setPlayer(player);
            anvilBE.setMinigameOn(true);

            ClientAnvilMinigameData.playerAnvilPositions.put(playerUUID, pos);
            ClientAnvilMinigameData.playerMinigameVisibility.put(playerUUID, true);

            int hitsRequired = anvilBE.getRequiredProgress();
            String quality = anvilBE.minigameQuality();

            CompoundTag sync = new CompoundTag();
            sync.putUUID("anvilOwner", playerUUID);
            sync.putLong("anvilPos", pos.asLong());
            FriendlyByteBuf syncBuf = ModMessages.buf();
            MinigameSyncS2CPacket.encode(new MinigameSyncS2CPacket(sync), syncBuf);
            ModMessages.sendToAll(ModMessages.MINIGAME_SYNC, syncBuf, world.getServer());

            FriendlyByteBuf startBuf = ModMessages.buf();
            StartMinigameS2CPacket.encode(new StartMinigameS2CPacket(pos, hitsRequired, quality), startBuf);
            ModMessages.sendToPlayer(ModMessages.START_MINIGAME, startBuf, serverPlayer);
        } else if (currentOwner.equals(playerUUID)) {
            boolean visible = ClientAnvilMinigameData.playerMinigameVisibility.get(playerUUID);
            ClientAnvilMinigameData.playerMinigameVisibility.put(playerUUID, !visible);
            FriendlyByteBuf toggleBuf = ModMessages.buf();
            ToggleMinigameS2CPacket.encode(new ToggleMinigameS2CPacket(pos, !visible), toggleBuf);
            ModMessages.sendToPlayer(ModMessages.TOGGLE_MINIGAME, toggleBuf, serverPlayer);
        }

        return InteractionResult.SUCCESS;
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

        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.player != null
                && client.player.getUUID().equals(owner)
                && pos.equals(ClientAnvilMinigameData.getPendingMinigamePos())) {

            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE && anvilBE.hasRecipe()) {
                Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                recipeOpt.ifPresent(recipe -> ClientAnvilMinigameData.clearPendingMinigame());
            }
        }
    }

    public static void releaseAnvil(ServerPlayer player, BlockPos pos) {
        UUID playerId = player.getUUID();
        if (ClientAnvilMinigameData.playerMinigameVisibility.get(playerId) != null)
            ClientAnvilMinigameData.playerMinigameVisibility.remove(playerId);
        if (ClientAnvilMinigameData.playerAnvilPositions.get(playerId) != null
                && pos.equals(ClientAnvilMinigameData.playerAnvilPositions.get(playerId))
        ) {
            ClientAnvilMinigameData.playerAnvilPositions.remove(playerId);

            BlockEntity be = player.level().getBlockEntity(pos);
            String quality = "perfect";
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE) {
                anvilBE.clearOwner();
                quality = anvilBE.minigameQuality();
            }
            ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
            // AnvilMinigameEvents.reset(quality) intentionally not called here - it's a
            // client-only class and this method also runs on dedicated servers; the sync
            // packet below drives the same client-side reset safely.
            CompoundTag syncData = new CompoundTag();
            syncData.putLong("anvilPos", pos.asLong());
            syncData.putUUID("anvilOwner", new UUID(0, 0));
            FriendlyByteBuf buf = ModMessages.buf();
            MinigameSyncS2CPacket.encode(new MinigameSyncS2CPacket(syncData), buf);
            ModMessages.sendToAll(ModMessages.MINIGAME_SYNC, buf, player.getServer());
        }

    }

    public static ServerPlayer getUsingPlayer(BlockPos pos) {
        MinecraftServer server = OvergearedFabric.getServer();
        if (server == null) return null;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();

            if (ClientAnvilMinigameData.playerAnvilPositions.containsKey(playerId) &&
                    ClientAnvilMinigameData.playerAnvilPositions.get(playerId).equals(pos)) {
                return player;
            }
        }

        return null;
    }

    public static void hideMinigame(ServerPlayer player) {
        FriendlyByteBuf buf = ModMessages.buf();
        HideMinigameS2CPacket.encode(new HideMinigameS2CPacket(), buf);
        ModMessages.sendToPlayer(ModMessages.HIDE_MINIGAME, buf, player);
    }

    // =========================
    // Right-click item: cooling, grinding, polishing, durability repair, cleanup
    // =========================

    private static InteractionResultHolder<ItemStack> onRightClickItem(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand) {
        if (world.isClientSide) return InteractionResultHolder.pass(player.getItemInHand(hand));
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);

        if (ItemInteractionHelper.tryCoolInWater(player, stack, world)) return InteractionResultHolder.success(stack);
        if (ItemInteractionHelper.tryGrind(player, stack, world) != null) return InteractionResultHolder.success(stack);
        ItemInteractionHelper.handleMinigameCleanup(player, world);
        return InteractionResultHolder.pass(stack);
    }

    // =========================
    // Knapping (both hands)
    // =========================

    private static InteractionResultHolder<ItemStack> onUsingKnappable(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand) {
        ItemStack usedStack = player.getItemInHand(hand);

        if (!usedStack.is(ModTags.Items.KNAPPABLE)) return InteractionResultHolder.pass(usedStack);

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (!(mainHand.is(ModTags.Items.KNAPPABLE) && offHand.is(ModTags.Items.KNAPPABLE))) {
            return InteractionResultHolder.pass(usedStack);
        }

        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {

            world.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.STONE_PLACE,
                    SoundSource.PLAYERS,
                    0.6f,
                    1.0f
            );

            serverPlayer.openMenu(new RockKnappingMenuProvider());
        }

        return InteractionResultHolder.sidedSuccess(usedStack, world.isClientSide);
    }

    private static final Set<ItemEntity> knownTrackedEntities = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Starts tracking a heated dropped item. Called from ServerEntityEvents.ENTITY_LOAD (Fabric's counterpart of
     * Forge's EntityJoinLevelEvent) as soon as the item enters the world, and from the 10-tick sweep as a fallback;
     * knownTrackedEntities makes repeated calls harmless.
     */
    private static void trackItemEntity(ServerLevel world, Entity entity) {
        if (!(entity instanceof ItemEntity itemEntity)) return;
        if (!knownTrackedEntities.add(itemEntity)) return; // already seen

        ItemStack stack = itemEntity.getItem();
        boolean isHeatedItem = stack.hasTag() && stack.getTag().getBoolean("Heated");

        if (hasCoolingRecipe(stack.getItem(), world) || isHeatedItem) {
            trackedEntitiesPerWorld
                    .computeIfAbsent(world, w -> new ArrayList<>())
                    .add(itemEntity);
        }
    }

    private static void trackNewItemEntities(ServerLevel world) {
        for (Entity entity : world.getAllEntities()) {
            trackItemEntity(world, entity);
        }
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerLevel world : server.getAllLevels()) {
            long now = world.getGameTime();

            // Housekeeping every 10 ticks; dropped items are checked every tick so they
            // cool on the same tick as the rest of their stack in a player's inventory.
            if (now % 10 == 0) {
                trackNewItemEntities(world);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.containerMenu != null && player.containerMenu != player.inventoryMenu) {
                        checkContainerMenu(player, player.containerMenu);
                    }
                }
            }

            List<ItemEntity> tracked = trackedEntitiesPerWorld.get(world);
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

                boolean isHeated = (stack.hasTag() && stack.getTag().getBoolean("Heated"))
                        || hasCoolingRecipeCached(stack.getItem(), world);

                if (!isHeated) {
                    it.remove();
                    trackedSinceMs.remove(entity);
                    continue;
                }

                long started = getOrStampHeatedSince(stack, now);
                boolean cooled = now - started >= Overgeared.getConfig().HEATED_ITEM_COOLDOWN_TICKS();

                BlockPos pos = entity.blockPosition(); // floors correctly at negative coordinates

                BlockState state = world.getBlockState(pos);
                if (state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)) {
                    cooled = true;
                }

                if (cooled) {
                    coolItemEntity(entity);
                    world.sendParticles(ParticleTypes.SMOKE,
                            entity.getX(), entity.getY() + 0.25, entity.getZ(),
                            6, 0.15, 0.15, 0.15, 0.02);
                    world.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.0f);
                    trackedSinceMs.remove(entity);

                    if (entity.getItem().isEmpty()) {
                        it.remove();
                    }
                }
            }

            if (tracked.isEmpty()) {
                trackedEntitiesPerWorld.remove(world);
            }
        }
    }

    // =========================
    // Flint on stone (knapping-adjacent rock interactions)
    // =========================

    private static InteractionResult onFlintUsedOnStone(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack heldItem = player.getItemInHand(hand);

        for (RockInteractionData data : FabricRockInteractionReloadListener.INSTANCE.getAll()) {

            if (!data.matches(state, heldItem)) continue;

            RockInteractionData.ToolEntry tool = data.getTool(heldItem);
            if (tool == null) continue;

            ServerLevel serverWorld = (ServerLevel) world;

            if (world.random.nextFloat() < tool.dropChance()) {
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

                ItemEntity item = new ItemEntity(serverWorld, sx, sy, sz, dropStack);
                item.setDeltaMovement(dx * 0.25, dy * 0.25, dz * 0.25);
                item.setDefaultPickUpDelay();
                serverWorld.addFreshEntity(item);

                world.setBlockAndUpdate(pos, data.getResultBlock().defaultBlockState());
            }

            if (world.random.nextFloat() < tool.breakChance()) {

                if (heldItem.isDamageableItem()) {
                    heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand == InteractionHand.MAIN_HAND
                            ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
                } else {
                    heldItem.shrink(1);
                }

                world.playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                        0.8F, 1.0F);
            } else {
                world.playSound(null, pos,
                        SoundEvents.STONE_HIT, SoundSource.BLOCKS,
                        1.0F, 1.0F);
            }

            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // =========================
    // Arrow tipping
    // =========================

    private static InteractionResultHolder<ItemStack> onArrowTipping(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand) {
        if (world.isClientSide) return InteractionResultHolder.pass(player.getItemInHand(hand));
        if (!Overgeared.getConfig().TIPPING_TOGGLE()) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack usedHand = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        boolean isVanillaArrow = usedHand.is(Items.ARROW) && otherStack.is(Items.POTION);
        boolean isCustomArrow = Overgeared.getConfig().UPGRADE_ARROW_POTION_TOGGLE() && (usedHand.is(ModItems.IRON_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.STEEL_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.DIAMOND_UPGRADE_ARROW.get())) &&
                otherStack.is(Items.POTION);

        if (!isVanillaArrow && !isCustomArrow) {
            return InteractionResultHolder.pass(usedHand);
        }

        CompoundTag potionTag = otherStack.getOrCreateTag();
        int used = potionTag.getInt("TippedUsed");
        int maxUse = Overgeared.getConfig().MAX_POTION_TIPPING_USE();
        Potion basePotion = PotionUtils.getPotion(otherStack);

        ItemStack resultArrow;
        if (isVanillaArrow) {
            resultArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), basePotion);
        } else {
            resultArrow = usedHand.copy();
            resultArrow.setCount(1);

            CompoundTag arrowTag = new CompoundTag();
            arrowTag.putString("Potion", BuiltInRegistries.POTION.getKey(basePotion).toString());

            if (potionTag.contains("CustomPotionEffects", Tag.TAG_LIST)) {
                arrowTag.put("CustomPotionEffects", potionTag.getList("CustomPotionEffects", Tag.TAG_COMPOUND));
            }
            if (potionTag.contains("CustomPotionColor", Tag.TAG_INT)) {
                arrowTag.putInt("CustomPotionColor", potionTag.getInt("CustomPotionColor"));
            }

            resultArrow.setTag(arrowTag);
        }

        if (usedHand.getCount() == 1) {
            player.setItemInHand(hand, resultArrow);
        } else {
            usedHand.shrink(1);
            player.setItemInHand(hand, usedHand);
            if (!player.getInventory().add(resultArrow)) {
                player.drop(resultArrow, false);
            }
        }

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

        world.playSound(null,
                player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS,
                0.6F,
                1.2F
        );

        return InteractionResultHolder.success(usedHand);
    }

    // =========================
    // Fletching table
    // =========================

    private static InteractionResult onRightClickFletching(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (!Overgeared.getConfig().ENABLE_FLETCHING_RECIPES()) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!state.is(Blocks.FLETCHING_TABLE)) return InteractionResult.PASS;

        if (world.isClientSide) return InteractionResult.SUCCESS;

        SimpleMenuProvider provider = new SimpleMenuProvider(
                (syncId, playerInv, p) ->
                        new FletchingStationMenu(
                                syncId,
                                playerInv,
                                ContainerLevelAccess.create(world, pos)
                        ),
                Component.translatable("container.overgeared.fletching_table")
        );

        player.openMenu(provider);

        return InteractionResult.CONSUME;
    }
}
