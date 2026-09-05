package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.GrindingBlacklistReloadListener;
import net.stirdrem.overgeared.datapack.RockInteractionData;
import net.stirdrem.overgeared.datapack.RockInteractionReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.HideMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.StartMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.ToggleMinigameS2CPacket;
import net.stirdrem.overgeared.recipe.CoolingRecipe;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.GrindingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.FletchingStationScreenHandler;
import net.stirdrem.overgeared.screen.RockKnappingMenuProvider;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static net.stirdrem.overgeared.Overgeared.getCooledItem;

public class ModItemInteractEvents {
    public static final Map<UUID, BlockPos> playerAnvilPositions = new HashMap<>();
    public static final Map<UUID, Boolean> playerMinigameVisibility = new HashMap<>();

    private static final ConcurrentMap<ItemEntity, Long> trackedSinceMs = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, List<ItemEntity>> trackedEntitiesPerWorld = new HashMap<>();
    private static final Map<Item, Boolean> COOLING_CACHE = new HashMap<>();

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
            handleCauldronInteraction(world, pos, player, heldStack, state);
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
                && ServerConfig.ENABLE_STONE_TO_ANVIL.get()) {

            BlockState newState = ModBlocks.STONE_SMITHING_ANVIL
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
                && ServerConfig.ENABLE_ANVIL_TO_SMITHING.get()) {

            BlockState newState = ModBlocks.SMITHING_ANVIL
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
            BlockState newState = ModBlocks.TIER_A_SMITHING_ANVIL.defaultBlockState().setValue(AbstractSmithingAnvil.FACING, player.getDirection().getClockWise());
            world.setBlock(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "tier_a");
            }
            return InteractionResult.SUCCESS;
        }

        if (!world.isClientSide && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.TIER_B_ANVIL_BASES)) {
            BlockState newState = ModBlocks.TIER_B_SMITHING_ANVIL
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

        if (playerAnvilPositions.containsKey(playerUUID)
                && !pos.equals(playerAnvilPositions.get(playerUUID))) {

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

            playerAnvilPositions.put(playerUUID, pos);
            playerMinigameVisibility.put(playerUUID, true);

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
            boolean visible = playerMinigameVisibility.get(playerUUID);
            playerMinigameVisibility.put(playerUUID, !visible);
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
        if (playerMinigameVisibility.get(playerId) != null)
            playerMinigameVisibility.remove(playerId);
        if (playerAnvilPositions.get(playerId) != null
                && pos.equals(playerAnvilPositions.get(playerId))
        ) {
            playerAnvilPositions.remove(playerId);

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
        MinecraftServer server = Overgeared.getServer();
        if (server == null) return null;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();

            if (playerAnvilPositions.containsKey(playerId) &&
                    playerAnvilPositions.get(playerId).equals(pos)) {
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

        if (handleCooling(player, stack, world)) return InteractionResultHolder.success(stack);
        if (handleGrinding(player, stack, world)) return InteractionResultHolder.success(stack);
        handleMinigameCleanup(player, world);
        return InteractionResultHolder.pass(stack);
    }

    private static boolean handleCooling(net.minecraft.world.entity.player.Player player, ItemStack stack, Level world) {
        if (!stack.is(ModTags.Items.HEATED_METALS)) return false;

        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getFluidState().isSource() && state.getBlock() == Blocks.WATER) {
            coolItem(player, stack);
            return true;
        }

        return false;
    }

    private static boolean handleGrinding(net.minecraft.world.entity.player.Player player, ItemStack stack, Level world) {
        if (!player.isShiftKeyDown()) return false;

        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!state.is(ModTags.Blocks.GRINDSTONES)) return false;
        if (player.getMainHandItem() != stack) return false;

        if (handleGrindingRecipe(player, stack, world, pos)) return true;
        if (handlePolishing(player, stack, world, pos)) return true;
        return handleDurabilityGrinding(stack, world, pos);
    }

    private static boolean handleGrindingRecipe(net.minecraft.world.entity.player.Player player, ItemStack stack, Level world, BlockPos pos) {
        if (!hasGrindingRecipe(stack.getItem(), world)) return false;

        grindItem(player, stack);
        playGrindEffects(world, pos);
        return true;
    }

    private static boolean handlePolishing(net.minecraft.world.entity.player.Player player, ItemStack stack, Level world, BlockPos pos) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Polished") || tag.getBoolean("Polished")) return false;

        ItemStack resultItem;

        if (stack.getCount() > 1) {
            resultItem = stack.copy();
            resultItem.setCount(1);
            stack.shrink(1);
        } else {
            resultItem = stack;
        }

        CompoundTag resultTag = resultItem.getOrCreateTag();
        resultTag.putBoolean("Polished", true);

        if (tag.getBoolean("Heated") && tag.contains("ForgingQuality")) {
            ForgingQuality quality = ForgingQuality.fromString(tag.getString("ForgingQuality"));
            ForgingQuality downgraded = quality.getLowerQuality();
            resultTag.putString("ForgingQuality", downgraded.getDisplayName());
        }

        if (resultItem != stack && !player.getInventory().add(resultItem)) {
            player.drop(resultItem, false);
        }

        playGrindEffects(world, pos);
        return true;
    }

    private static boolean handleDurabilityGrinding(ItemStack stack, Level world, BlockPos pos) {
        if (!stack.isDamageableItem() || stack.getDamageValue() <= 0) return false;

        if (!ServerConfig.GRINDING_RESTORE_DURABILITY.get()) {
            return true;
        }

        if (isBlacklisted(stack)) {
            return true;
        }

        applyDurabilityRepair(stack);
        playGrindEffects(world, pos);
        return true;
    }

    private static boolean isBlacklisted(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        for (String entry : ServerConfig.GRINDING_BLACKLIST.get()) {
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.tryParse(entry.substring(1)));
                if (stack.is(tag)) return true;
            } else if (itemId != null && itemId.equals(ResourceLocation.tryParse(entry))) {
                return true;
            }
        }

        return GrindingBlacklistReloadListener.isBlacklisted(stack);
    }

    private static void applyDurabilityRepair(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        int reducedCount = tag.getInt("ReducedMaxDurability");
        int originalDurability = stack.getItem().getMaxDamage();

        float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
        float reduction = ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue();

        float qualityMultiplier = tag.contains("ForgingQuality")
                ? QualityHelper.getDurabilityMultiplier(stack)
                : 1.0f;

        int adjustedMax = (int) (originalDurability * baseMultiplier * qualityMultiplier);

        float penalty = Math.max(0.1f, 1.0f - (reducedCount * reduction));
        int effectiveMax = Math.max(1, (int) (adjustedMax * penalty));

        int currentDamage = stack.getDamageValue();

        if (currentDamage <= (adjustedMax - effectiveMax)) {
            tag.putInt("ReducedMaxDurability", reducedCount + 1);
            stack.setDamageValue(0);
            return;
        }

        float restorePercent = ServerConfig.DAMAGE_RESTORE_PER_GRIND.get().floatValue();
        int repairAmount = Math.max(1, (int) (adjustedMax * restorePercent));

        int newDamage = Math.max(adjustedMax - effectiveMax, currentDamage - repairAmount);

        stack.setDamageValue(newDamage);
        tag.putInt("ReducedMaxDurability", reducedCount + 1);
    }

    private static void playGrindEffects(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
        spawnGrindParticles(world, pos);
    }

    private static void handleMinigameCleanup(net.minecraft.world.entity.player.Player player, Level world) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack mainHand = player.getMainHandItem();
        HitResult hit = player.pick(5.0D, 0.0F, false);

        if (hit.getType() != HitResult.Type.BLOCK) {
            hideMinigame(serverPlayer);
            return;
        }

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!mainHand.is(ModTags.Items.SMITHING_HAMMERS) ||
                !state.is(ModTags.Blocks.SMITHING_ANVIL)) {
            hideMinigame(serverPlayer);
        }
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

    private static void spawnGrindParticles(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(ParticleTypes.CRIT,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    private static void handleCauldronInteraction(Level world, BlockPos pos, net.minecraft.world.entity.player.Player player,
                                                    ItemStack heldStack, BlockState state) {
        int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);

        if (waterLevel > 0) {
            coolItem(player, heldStack);
        }
    }

    private static ItemStack coolSingleStack(ItemStack stack, Level world) {
        Item cooled = getCooledItem(stack.getItem(), world);
        if (cooled == null) return stack;

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());

        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag().copy();
            tag.remove("Heated");
            tag.remove("HeatedSince");
            if (tag.isEmpty()) {
                cooledStack.setTag(null);
            } else {
                cooledStack.setTag(tag);
            }
        }

        return cooledStack;
    }

    private static void coolItem(net.minecraft.world.entity.player.Player player, ItemStack stack) {
        Item cooled = getCooledItem(stack.getItem(), player.level());
        if (cooled == null) return;
        if (stack.getCount() <= 0) return;

        // === Tool Cast special handling ===
        if (stack.getItem() instanceof ToolCastItem && stack.hasTag()) {
            CompoundTag tag = stack.getTag();

            if (tag != null && tag.contains("Output", Tag.TAG_COMPOUND)) {
                ItemStack output = ItemStack.of(tag.getCompound("Output"));
                ItemStack cooledOutput = coolSingleStack(output, player.level());
                tag.put("Output", cooledOutput.save(new CompoundTag()));
            }
        }

        // === Original logic (unchanged) ===
        ItemStack cooledStack = new ItemStack(cooled, 1);
        if (stack.hasTag()) {
            cooledStack.setTag(stack.getTag().copy());
            cooledStack.removeTagKey("HeatedSince");
            cooledStack.removeTagKey("Heated");
        }

        stack.shrink(1);

        if (stack.isEmpty()) {
            if (player.getMainHandItem() == stack) {
                player.setItemInHand(InteractionHand.MAIN_HAND, cooledStack);
            } else if (player.getOffhandItem() == stack) {
                player.setItemInHand(InteractionHand.OFF_HAND, cooledStack);
            } else if (!player.getInventory().add(cooledStack)) {
                player.drop(cooledStack, false);
            }
        } else {
            if (!player.getInventory().add(cooledStack)) {
                player.drop(cooledStack, false);
            }
        }

        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
    }


    private static void coolItemEntity(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        Level world = entity.level();

        Item cooled = getCooledItem(stack.getItem(), world);
        if (cooled == null || stack.getCount() <= 0) return;

        if (stack.getItem() instanceof ToolCastItem && stack.hasTag()) {
            CompoundTag tag = stack.getTag();

            if (tag.contains("Output", Tag.TAG_COMPOUND)) {
                ItemStack output = ItemStack.of(tag.getCompound("Output"));
                ItemStack cooledOutput = coolSingleStack(output, world);
                tag.put("Output", cooledOutput.save(new CompoundTag()));
            }
        }

        CompoundTag oldTag = stack.hasTag() ? stack.getTag().copy() : null;
        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());

        if (oldTag != null) {
            oldTag.remove("Heated");
            oldTag.remove("HeatedSince");
            if (oldTag.isEmpty()) {
                cooledStack.setTag(null);
            } else {
                cooledStack.setTag(oldTag);
            }
        }

        entity.setItem(cooledStack);
    }


    private static void grindItem(net.minecraft.world.entity.player.Player player, ItemStack heldStack) {
        Item cooledItem = getGrindable(heldStack.getItem(), player.level());
        if (cooledItem != null) {
            ItemStack cooledIngot = new ItemStack(cooledItem);
            if (heldStack.hasTag()) {
                cooledIngot.setTag(heldStack.getTag().copy());
            }
            cooledIngot.getOrCreateTag().putBoolean("Polished", true);
            heldStack.shrink(1);

            if (heldStack.isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
            } else {
                if (!player.getInventory().add(cooledIngot)) {
                    player.drop(cooledIngot, false);
                }
            }

            player.playSound(SoundEvents.GRINDSTONE_USE, 1.0F, 1.0F);
        }
    }

    private static Item getGrindable(@Nullable Item heatedItem, @NotNull Level world) {
        if (heatedItem == null) return null;

        net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(new ItemStack(heatedItem));

        Optional<GrindingRecipe> recipeOpt = world.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE)
                .stream()
                .filter(r -> r.matches(container, world))
                .findFirst();

        if (recipeOpt.isEmpty()) {
            return heatedItem;
        }

        GrindingRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.getResultItem(world.registryAccess());
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    public static boolean hasCoolingRecipe(@Nullable Item heatedItem, @NotNull Level world) {
        if (heatedItem == null) return false;

        net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(new ItemStack(heatedItem));

        Optional<CoolingRecipe> recipeOpt = world.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE)
                .stream()
                .filter(r -> r.matches(container, world))
                .findFirst();

        return recipeOpt.map(recipe -> !recipe.getResultItem(world.registryAccess()).isEmpty())
                .orElse(false);
    }

    public static boolean hasGrindingRecipe(@Nullable Item heatedItem, @NotNull Level world) {
        if (heatedItem == null) return false;

        net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(new ItemStack(heatedItem));

        Optional<GrindingRecipe> recipeOpt = world.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE)
                .stream()
                .filter(r -> r.matches(container, world))
                .findFirst();

        return recipeOpt.map(recipe -> !recipe.getResultItem(world.registryAccess()).isEmpty())
                .orElse(false);
    }

    private static final Set<ItemEntity> knownTrackedEntities = Collections.newSetFromMap(new java.util.WeakHashMap<>());

    /**
     * Forge's EntityJoinLevelEvent has no Fabric API equivalent (fabric-entity-events-v1 only
     * covers combat/sleep/elytra/world-change/respawn hooks, nothing for "entity spawned"), so
     * new heated ItemEntities are picked up here instead, once per 10-tick sweep alongside the
     * existing cooldown check - equivalent behavior, just detected up to ~10 ticks later than
     * the instant join-event would have.
     */
    private static void trackNewItemEntities(ServerLevel world) {
        for (Entity entity : world.getAllEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            if (!knownTrackedEntities.add(itemEntity)) continue; // already seen

            ItemStack stack = itemEntity.getItem();
            boolean isHeatedItem = stack.hasTag() && stack.getTag().getBoolean("Heated");

            if (hasCoolingRecipe(stack.getItem(), world) || isHeatedItem) {
                trackedEntitiesPerWorld
                        .computeIfAbsent(world, w -> new ArrayList<>())
                        .add(itemEntity);

                if (stack.hasTag() && stack.getTag().contains("HeatedSince")) {
                    long heatedSince = stack.getTag().getLong("HeatedSince");
                    trackedSinceMs.put(itemEntity, heatedSince);
                }
            }
        }
    }

    private static boolean hasCoolingRecipeCached(Item item, Level world) {
        return COOLING_CACHE.computeIfAbsent(item, i -> hasCoolingRecipe(i, world));
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerLevel world : server.getAllLevels()) {
            long now = world.getGameTime();

            if (now % 10 != 0) continue;

            trackNewItemEntities(world);

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.containerMenu != null && player.containerMenu != player.inventoryMenu) {
                    checkContainerMenu(player, player.containerMenu);
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

                Long started = trackedSinceMs.get(entity);
                boolean cooled = false;

                if (started != null && now - started > ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get()) {
                    cooled = true;
                }

                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                        (int) entity.getX(), (int) entity.getY(), (int) entity.getZ());

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

    private static void checkContainerMenu(ServerPlayer player, AbstractContainerMenu menu) {
        long gameTime = player.level().getGameTime();
        int cooldown = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();
        boolean playedSound = false;

        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            if (stack.hasTag() && stack.getTag().contains("HeatedSince")) {
                long heatedAt = stack.getTag().getLong("HeatedSince");
                if (gameTime - heatedAt >= cooldown) {
                    coolItemInContainerSlot(player, slot);

                    if (!playedSound) {
                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.FIRE_EXTINGUISH,
                                SoundSource.PLAYERS, 0.5F, 1.0F);
                        playedSound = true;
                    }
                }
            }
        }
    }

    private static void coolItemInContainerSlot(ServerPlayer player, Slot slot) {
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;

        Item cooled = getCooledItem(stack.getItem(), player.level());
        if (cooled == null) return;

        if (stack.getItem() instanceof ToolCastItem && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Output", Tag.TAG_COMPOUND)) {
                ItemStack output = ItemStack.of(tag.getCompound("Output"));

                Item cooledOutputItem = getCooledItem(output.getItem(), player.level());
                if (cooledOutputItem != null) {
                    ItemStack cooledOutput = new ItemStack(cooledOutputItem, output.getCount());
                    if (output.hasTag()) {
                        cooledOutput.setTag(output.getTag().copy());
                    }
                    tag.put("Output", cooledOutput.save(new CompoundTag()));
                }
            }
        }

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());

        if (stack.hasTag()) {
            CompoundTag newTag = stack.getTag().copy();
            newTag.remove("HeatedSince");
            newTag.remove("Heated");

            if (newTag.isEmpty()) {
                cooledStack.setTag(null);
            } else {
                cooledStack.setTag(newTag);
            }
        }

        slot.setByPlayer(cooledStack);
    }

    // =========================
    // Flint on stone (knapping-adjacent rock interactions)
    // =========================

    private static InteractionResult onFlintUsedOnStone(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack heldItem = player.getItemInHand(hand);

        for (RockInteractionData data : RockInteractionReloadListener.INSTANCE.getAll()) {

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
        if (!ServerConfig.TIPPING_TOGGLE.get()) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack usedHand = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        boolean isVanillaArrow = usedHand.is(Items.ARROW) && otherStack.is(Items.POTION);
        boolean isCustomArrow = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get() && (usedHand.is(ModItems.IRON_UPGRADE_ARROW) ||
                usedHand.is(ModItems.STEEL_UPGRADE_ARROW) ||
                usedHand.is(ModItems.DIAMOND_UPGRADE_ARROW)) &&
                otherStack.is(Items.POTION);

        if (!isVanillaArrow && !isCustomArrow) {
            return InteractionResultHolder.pass(usedHand);
        }

        CompoundTag potionTag = otherStack.getOrCreateTag();
        int used = potionTag.getInt("TippedUsed");
        int maxUse = ServerConfig.MAX_POTION_TIPPING_USE.get();
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
        if (!ServerConfig.ENABLE_FLETCHING_RECIPES.get()) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!state.is(Blocks.FLETCHING_TABLE)) return InteractionResult.PASS;

        if (world.isClientSide) return InteractionResult.SUCCESS;

        SimpleMenuProvider provider = new SimpleMenuProvider(
                (syncId, playerInv, p) ->
                        new FletchingStationScreenHandler(
                                syncId,
                                playerInv,
                                ContainerLevelAccess.create(world, pos)
                        ),
                Component.translatable("container.overgeared.fletching_table")
        );

        ((ServerPlayer) player).openMenu(provider);

        return InteractionResult.CONSUME;
    }
}
