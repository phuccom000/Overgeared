package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.GrindingBlacklistReloadListener;
import net.stirdrem.overgeared.datapack.RockInteractionData;
import net.stirdrem.overgeared.datapack.RockInteractionReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.recipe.CoolingRecipe;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.GrindingRecipe;
import net.stirdrem.overgeared.recipe.ItemListInput;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RecipeLookup;
import net.stirdrem.overgeared.screen.FletchingStationScreenHandler;
import net.stirdrem.overgeared.screen.RockKnappingMenuProvider;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;
import net.stirdrem.overgeared.util.TippedPotionHelper;
import org.jspecify.annotations.Nullable;

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
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
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
                || heldStack.getOrDefault(ModComponents.HEATED, false);

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

        if (!world.isClientSide() && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.STONE_ANVIL_BASES)
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

        if (!world.isClientSide() && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.IRON_ANVIL_BASES)
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

        if (!world.isClientSide() && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.TIER_A_ANVIL_BASES)) {
            BlockState newState = ModBlocks.TIER_A_SMITHING_ANVIL.defaultBlockState().setValue(AbstractSmithingAnvil.FACING, player.getDirection().getClockWise());
            world.setBlock(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "tier_a");
            }
            return InteractionResult.SUCCESS;
        }

        if (!world.isClientSide() && player.isShiftKeyDown() && clickedState.is(ModTags.Blocks.TIER_B_ANVIL_BASES)) {
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
            if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                hideMinigame(serverPlayer);
            }
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        // =========================
        // SERVER LOGIC ONLY
        // =========================

        if (world.isClientSide()) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        UUID playerUUID = player.getUUID();

        if (!anvilBE.hasRecipe()) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED));
            return InteractionResult.PASS;
        }

        if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED));
            return InteractionResult.PASS;
        }

        UUID currentOwner = anvilBE.getOwnerUUID();

        if (currentOwner != null && !currentOwner.equals(playerUUID)) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED));
            return InteractionResult.PASS;
        }

        if (playerAnvilPositions.containsKey(playerUUID)
                && !pos.equals(playerAnvilPositions.get(playerUUID))) {

            serverPlayer.sendOverlayMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED));
            return InteractionResult.PASS;
        }

        Optional<RecipeHolder<ForgingRecipe>> recipeOpt = anvilBE.getCurrentRecipeHolder();
        if (recipeOpt.isEmpty()) return InteractionResult.PASS;
        RecipeHolder<ForgingRecipe> recipe = recipeOpt.get();

        if (serverPlayer.level().getGameRules().get(GameRules.LIMITED_CRAFTING)) {
            if (!serverPlayer.getRecipeBook().contains(recipe.id())) {
                serverPlayer.sendOverlayMessage(Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED));
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
            sync.store("anvilOwner", UUIDUtil.CODEC, playerUUID);
            sync.putLong("anvilPos", pos.asLong());
            ModMessages.sendMinigameSync(serverPlayer.level().getServer(), sync);

            ModMessages.sendStartMinigame(serverPlayer, pos, hitsRequired, quality);
        } else if (currentOwner.equals(playerUUID)) {
            boolean visible = playerMinigameVisibility.getOrDefault(playerUUID, false);
            playerMinigameVisibility.put(playerUUID, !visible);
            ModMessages.sendToggleMinigame(serverPlayer, pos, !visible);
        }

        return InteractionResult.SUCCESS;
    }

    public static void handleAnvilOwnershipSync(CompoundTag syncData) {
        UUID owner = syncData.read("anvilOwner", UUIDUtil.CODEC).orElse(null);
        if (owner != null && owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
            owner = null;
        }
        BlockPos pos = BlockPos.of(syncData.getLongOr("anvilPos", 0L));
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
            syncData.store("anvilOwner", UUIDUtil.CODEC, new UUID(0, 0));
            ModMessages.sendMinigameSync(player.level().getServer(), syncData);
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
        ModMessages.sendHideMinigame(player);
    }

    // =========================
    // Right-click item: cooling, grinding, polishing, durability repair, cleanup
    // =========================

    private static InteractionResult onRightClickItem(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand) {
        if (world.isClientSide()) return InteractionResult.PASS;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);

        if (handleCooling(player, stack, world)) return InteractionResult.SUCCESS;
        if (handleGrinding(player, stack, world)) return InteractionResult.SUCCESS;
        handleMinigameCleanup(player, world);
        return InteractionResult.PASS;
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
        Boolean polished = stack.get(ModComponents.POLISHED);
        if (polished == null || polished) return false;

        ItemStack resultItem;

        if (stack.getCount() > 1) {
            resultItem = stack.copy();
            resultItem.setCount(1);
            stack.shrink(1);
        } else {
            resultItem = stack;
        }

        resultItem.set(ModComponents.POLISHED, true);

        ForgingQuality quality = ForgingQuality.get(resultItem);
        if (resultItem.getOrDefault(ModComponents.HEATED, false) && quality != null) {
            resultItem.set(ModComponents.FORGING_QUALITY, quality.getLowerQuality());
        }

        if (resultItem != stack && !player.getInventory().add(resultItem)) {
            player.drop(resultItem, false, Prediction.SERVER_ONLY);
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
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);

        for (String entry : ServerConfig.GRINDING_BLACKLIST.get()) {
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.tryParse(entry.substring(1)));
                if (stack.is(tag)) return true;
            } else if (itemId != null && itemId.equals(Identifier.tryParse(entry))) {
                return true;
            }
        }

        return GrindingBlacklistReloadListener.isBlacklisted(stack);
    }

    private static void applyDurabilityRepair(ItemStack stack) {
        int reducedCount = stack.getOrDefault(ModComponents.REDUCED_GRIND_COUNT, 0);
        // 26.3: the unmodified max durability is the MAX_DAMAGE component (ItemStack#getMaxDamage is mixin-adjusted)
        int originalDurability = stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);

        float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
        float reduction = ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue();

        float qualityMultiplier = QualityHelper.getDurabilityMultiplier(stack);

        int adjustedMax = (int) (originalDurability * baseMultiplier * qualityMultiplier);

        float penalty = Math.max(0.1f, 1.0f - (reducedCount * reduction));
        int effectiveMax = Math.max(1, (int) (adjustedMax * penalty));

        int currentDamage = stack.getDamageValue();

        if (currentDamage <= (adjustedMax - effectiveMax)) {
            stack.set(ModComponents.REDUCED_GRIND_COUNT, reducedCount + 1);
            stack.setDamageValue(0);
            return;
        }

        float restorePercent = ServerConfig.DAMAGE_RESTORE_PER_GRIND.get().floatValue();
        int repairAmount = Math.max(1, (int) (adjustedMax * restorePercent));

        int newDamage = Math.max(adjustedMax - effectiveMax, currentDamage - repairAmount);

        stack.setDamageValue(newDamage);
        stack.set(ModComponents.REDUCED_GRIND_COUNT, reducedCount + 1);
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

    private static InteractionResult onUsingKnappable(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand) {
        ItemStack usedStack = player.getItemInHand(hand);

        if (!usedStack.is(ModTags.Items.KNAPPABLE)) return InteractionResult.PASS;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (!(mainHand.is(ModTags.Items.KNAPPABLE) && offHand.is(ModTags.Items.KNAPPABLE))) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {

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

        return InteractionResult.SUCCESS;
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

        return stripHeat(stack.transmuteCopy(cooled, stack.getCount()));
    }

    private static void coolItem(net.minecraft.world.entity.player.Player player, ItemStack stack) {
        Item cooled = getCooledItem(stack.getItem(), player.level());
        if (cooled == null) return;
        if (stack.getCount() <= 0) return;

        // === Tool Cast special handling ===
        coolCastOutput(stack, player.level());

        // === Original logic (unchanged) ===
        ItemStack cooledStack = stripHeat(stack.transmuteCopy(cooled, 1));

        stack.shrink(1);

        if (stack.isEmpty()) {
            if (player.getMainHandItem() == stack) {
                player.setItemInHand(InteractionHand.MAIN_HAND, cooledStack);
            } else if (player.getOffhandItem() == stack) {
                player.setItemInHand(InteractionHand.OFF_HAND, cooledStack);
            } else if (!player.getInventory().add(cooledStack)) {
                player.drop(cooledStack, false, Prediction.SERVER_ONLY);
            }
        } else {
            if (!player.getInventory().add(cooledStack)) {
                player.drop(cooledStack, false, Prediction.SERVER_ONLY);
            }
        }

        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
    }


    private static void coolItemEntity(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        Level world = entity.level();

        Item cooled = getCooledItem(stack.getItem(), world);
        if (cooled == null || stack.getCount() <= 0) return;

        coolCastOutput(stack, world);

        entity.setItem(stripHeat(stack.transmuteCopy(cooled, stack.getCount())));
    }


    private static void grindItem(net.minecraft.world.entity.player.Player player, ItemStack heldStack) {
        Item cooledItem = getGrindable(heldStack.getItem(), player.level());
        if (cooledItem != null) {
            ItemStack cooledIngot = heldStack.transmuteCopy(cooledItem, 1);
            cooledIngot.set(ModComponents.POLISHED, true);
            heldStack.shrink(1);

            if (heldStack.isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
            } else {
                if (!player.getInventory().add(cooledIngot)) {
                    player.drop(cooledIngot, false, Prediction.SERVER_ONLY);
                }
            }

            player.playSound(SoundEvents.GRINDSTONE_USE, 1.0F, 1.0F);
        }
    }

    private static Item getGrindable(@Nullable Item heatedItem, Level world) {
        if (heatedItem == null) return null;

        Optional<GrindingRecipe> recipeOpt = RecipeLookup.firstMatchValue(world, ModRecipeTypes.GRINDING_RECIPE,
                ItemListInput.of(new ItemStack(heatedItem)));

        if (recipeOpt.isEmpty()) {
            return heatedItem;
        }

        ItemStack result = recipeOpt.get().getResultItem();
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    public static boolean hasCoolingRecipe(@Nullable Item heatedItem, Level world) {
        if (heatedItem == null) return false;

        Optional<CoolingRecipe> recipeOpt = RecipeLookup.firstMatchValue(world, ModRecipeTypes.COOLING_RECIPE,
                ItemListInput.of(new ItemStack(heatedItem)));

        return recipeOpt.map(recipe -> !recipe.getResultItem().isEmpty())
                .orElse(false);
    }

    public static boolean hasGrindingRecipe(@Nullable Item heatedItem, Level world) {
        if (heatedItem == null) return false;

        Optional<GrindingRecipe> recipeOpt = RecipeLookup.firstMatchValue(world, ModRecipeTypes.GRINDING_RECIPE,
                ItemListInput.of(new ItemStack(heatedItem)));

        return recipeOpt.map(recipe -> !recipe.getResultItem().isEmpty())
                .orElse(false);
    }

    private static final Set<ItemEntity> knownTrackedEntities = Collections.newSetFromMap(new WeakHashMap<>());

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
            boolean isHeatedItem = stack.getOrDefault(ModComponents.HEATED, false);

            if (hasCoolingRecipe(stack.getItem(), world) || isHeatedItem) {
                trackedEntitiesPerWorld
                        .computeIfAbsent(world, w -> new ArrayList<>())
                        .add(itemEntity);

                Long heatedSince = stack.get(ModComponents.HEATED_TIME);
                if (heatedSince != null) {
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

                boolean isHeated = stack.getOrDefault(ModComponents.HEATED, false)
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

            Long heatedAt = stack.get(ModComponents.HEATED_TIME);
            if (heatedAt != null) {
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

        if (stack.getItem() instanceof ToolCastItem) {
            CastData cast = stack.get(ModComponents.CAST_DATA);
            if (cast != null && cast.hasOutput()) {
                ItemStack output = cast.outputStack();
                Item cooledOutputItem = getCooledItem(output.getItem(), player.level());
                if (cooledOutputItem != null) {
                    // (1.20 behaviour: heat markers on the output were kept here)
                    stack.set(ModComponents.CAST_DATA, cast.withOutput(output.transmuteCopy(cooledOutputItem, output.getCount())));
                }
            }
        }

        slot.setByPlayer(stripHeat(stack.transmuteCopy(cooled, stack.getCount())));
    }

    // =========================
    // Flint on stone (knapping-adjacent rock interactions)
    // =========================

    private static InteractionResult onFlintUsedOnStone(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack heldItem = player.getItemInHand(hand);

        for (RockInteractionData data : RockInteractionReloadListener.INSTANCE.getAll()) {

            if (!data.matches(state, heldItem)) continue;

            RockInteractionData.ToolEntry tool = data.getTool(heldItem);
            if (tool == null) continue;

            ServerLevel serverWorld = (ServerLevel) world;

            if (world.getRandom().nextFloat() < tool.dropChance()) {
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

            if (world.getRandom().nextFloat() < tool.breakChance()) {

                if (heldItem.isDamageableItem()) {
                    heldItem.hurtAndBreak(1, player, hand);
                } else {
                    heldItem.shrink(1);
                }

                world.playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS,
                        0.8F, 1.0F);
            } else {
                world.playSound(null, pos,
                        SoundEvents.STONE_HIT, SoundSource.BLOCKS,
                        1.0F, 1.0F);
            }

            player.swing(hand, SwingAnimation.DEFAULT, true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // =========================
    // Arrow tipping
    // =========================

    private static InteractionResult onArrowTipping(net.minecraft.world.entity.player.Player player, Level world, InteractionHand hand) {
        if (world.isClientSide()) return InteractionResult.PASS;
        if (!ServerConfig.TIPPING_TOGGLE.get()) return InteractionResult.PASS;

        ItemStack usedHand = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        boolean isVanillaArrow = usedHand.is(Items.ARROW) && otherStack.is(Items.POTION);
        boolean isCustomArrow = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get() && (usedHand.is(ModItems.IRON_UPGRADE_ARROW) ||
                usedHand.is(ModItems.STEEL_UPGRADE_ARROW) ||
                usedHand.is(ModItems.DIAMOND_UPGRADE_ARROW)) &&
                otherStack.is(Items.POTION);

        if (!isVanillaArrow && !isCustomArrow) {
            return InteractionResult.PASS;
        }

        int used = TippedPotionHelper.getTippedUses(otherStack);
        int maxUse = ServerConfig.MAX_POTION_TIPPING_USE.get();
        PotionContents contents = otherStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        ItemStack resultArrow;
        if (isVanillaArrow) {
            // vanilla tipped arrows only carry the base potion (1.20: PotionUtils.setPotion)
            resultArrow = new ItemStack(Items.TIPPED_ARROW);
            resultArrow.set(DataComponents.POTION_CONTENTS, new PotionContents(contents.potion(), Optional.empty(), List.of(), Optional.empty()));
        } else {
            resultArrow = usedHand.copyWithCount(1);
            // potion, custom effects and custom color (the old "Potion"/"CustomPotionEffects"/"CustomPotionColor" tags)
            resultArrow.set(DataComponents.POTION_CONTENTS, new PotionContents(contents.potion(), contents.customColor(), contents.customEffects(), Optional.empty()));
        }

        if (usedHand.getCount() == 1) {
            player.setItemInHand(hand, resultArrow);
        } else {
            usedHand.shrink(1);
            player.setItemInHand(hand, usedHand);
            if (!player.getInventory().add(resultArrow)) {
                player.drop(resultArrow, false, Prediction.SERVER_ONLY);
            }
        }

        if (otherStack.getCount() > 1) {
            ItemStack onePotion = otherStack.split(1);
            TippedPotionHelper.setTippedUses(onePotion, used + 1);
            player.setItemInHand(otherHand, otherStack);
        } else {
            used++;
            if (used >= maxUse) {
                player.setItemInHand(otherHand, new ItemStack(Items.GLASS_BOTTLE));
            } else {
                TippedPotionHelper.setTippedUses(otherStack, used);
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

        return InteractionResult.SUCCESS;
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

        if (world.isClientSide()) return InteractionResult.SUCCESS;

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

    // =========================
    // 26.3 component helpers
    // =========================

    /** Removes the heat markers (was removing the "Heated"/"HeatedSince" NBT keys). */
    private static ItemStack stripHeat(ItemStack stack) {
        stack.remove(ModComponents.HEATED);
        stack.remove(ModComponents.HEATED_TIME);
        return stack;
    }

    /** Cools the output stored in a tool cast (was the cast's "Output" NBT compound). */
    private static void coolCastOutput(ItemStack stack, Level world) {
        if (!(stack.getItem() instanceof ToolCastItem)) return;
        CastData cast = stack.get(ModComponents.CAST_DATA);
        if (cast == null || !cast.hasOutput()) return;
        ItemStack cooledOutput = coolSingleStack(cast.outputStack(), world);
        stack.set(ModComponents.CAST_DATA, cast.withOutput(cooledOutput));
    }
}
