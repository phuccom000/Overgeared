package net.stirdrem.overgeared.event;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
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
    private static final Map<ServerWorld, List<ItemEntity>> trackedEntitiesPerWorld = new HashMap<>();
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
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                hideMinigame(serverPlayer);
            }
            return ActionResult.PASS;
        });

        // Fabric API has no "entity joined world" event; new heated item entities are picked
        // up by the periodic scan in onServerTick instead (see trackNewItemEntities).
        ServerTickEvents.END_SERVER_TICK.register(ModItemInteractEvents::onServerTick);
    }

    // =========================
    // Cauldron cooling
    // =========================

    private static ActionResult onRightClickBlock(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getStackInHand(hand);
        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);

        boolean isHeatedItem = heldStack.isIn(ModTags.Items.HEATED_METALS)
                || (heldStack.hasNbt() && heldStack.getNbt().getBoolean("Heated"));

        if (!isHeatedItem) return ActionResult.PASS;

        if (state.isOf(Blocks.WATER_CAULDRON)) {
            handleCauldronInteraction(world, pos, player, heldStack, state);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    // =========================
    // Smithing hammer: anvil conversion + minigame open/toggle
    // =========================

    private static ActionResult onUseSmithingHammer(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        ItemStack heldItem = player.getStackInHand(hand);

        if (!heldItem.isIn(ModTags.Items.SMITHING_HAMMERS)) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

        BlockEntity be = world.getBlockEntity(pos);
        BlockState clickedState = world.getBlockState(pos);

        // =========================
        // Convert blocks to anvils
        // =========================

        if (!world.isClient && player.isSneaking() && clickedState.isIn(ModTags.Blocks.STONE_ANVIL_BASES)
                && ServerConfig.ENABLE_STONE_TO_ANVIL.get()) {

            BlockState newState = ModBlocks.STONE_SMITHING_ANVIL
                    .getDefaultState()
                    .with(AbstractSmithingAnvil.FACING, player.getHorizontalFacing().rotateYClockwise());

            world.setBlockState(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "stone");
            }

            return ActionResult.SUCCESS;
        }

        if (!world.isClient && player.isSneaking() && clickedState.isIn(ModTags.Blocks.IRON_ANVIL_BASES)
                && ServerConfig.ENABLE_ANVIL_TO_SMITHING.get()) {

            BlockState newState = ModBlocks.SMITHING_ANVIL
                    .getDefaultState()
                    .with(AbstractSmithingAnvil.FACING, player.getHorizontalFacing().rotateYClockwise());

            world.setBlockState(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "iron");
            }

            return ActionResult.SUCCESS;
        }

        if (!world.isClient && player.isSneaking() && clickedState.isIn(ModTags.Blocks.TIER_A_ANVIL_BASES)) {
            BlockState newState = ModBlocks.TIER_A_SMITHING_ANVIL.getDefaultState().with(AbstractSmithingAnvil.FACING, player.getHorizontalFacing().rotateYClockwise());
            world.setBlockState(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "tier_a");
            }
            return ActionResult.SUCCESS;
        }

        if (!world.isClient && player.isSneaking() && clickedState.isIn(ModTags.Blocks.TIER_B_ANVIL_BASES)) {
            BlockState newState = ModBlocks.TIER_B_SMITHING_ANVIL
                    .getDefaultState()
                    .with(AbstractSmithingAnvil.FACING, player.getHorizontalFacing().rotateYClockwise());
            world.setBlockState(pos, newState, 3);
            world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.trigger(serverPlayer, "tier_b");
            }
            return ActionResult.SUCCESS;
        }

        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvilBE)) {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                hideMinigame(serverPlayer);
            }
            return ActionResult.PASS;
        }

        if (!player.isSneaking()) return ActionResult.PASS;

        // =========================
        // SERVER LOGIC ONLY
        // =========================

        if (world.isClient) return ActionResult.PASS;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

        UUID playerUUID = player.getUuid();

        if (!anvilBE.hasRecipe()) {
            serverPlayer.sendMessage(
                    Text.translatable("message.overgeared.no_recipe").formatted(Formatting.RED),
                    true
            );
            return ActionResult.PASS;
        }

        if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
            serverPlayer.sendMessage(
                    Text.translatable("message.overgeared.item_has_no_quality").formatted(Formatting.RED),
                    true
            );
            return ActionResult.PASS;
        }

        UUID currentOwner = anvilBE.getOwnerUUID();

        if (currentOwner != null && !currentOwner.equals(playerUUID)) {
            serverPlayer.sendMessage(
                    Text.translatable("message.overgeared.anvil_in_use_by_another").formatted(Formatting.RED),
                    true
            );
            return ActionResult.PASS;
        }

        if (playerAnvilPositions.containsKey(playerUUID)
                && !pos.equals(playerAnvilPositions.get(playerUUID))) {

            serverPlayer.sendMessage(
                    Text.translatable("message.overgeared.another_anvil_in_use").formatted(Formatting.RED),
                    true
            );
            return ActionResult.PASS;
        }

        Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
        ForgingRecipe recipe = recipeOpt.get();

        if (world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
            if (!serverPlayer.getRecipeBook().contains(recipe)) {
                serverPlayer.sendMessage(
                        Text.translatable("message.overgeared.no_recipe").formatted(Formatting.RED),
                        true
                );
                return ActionResult.PASS;
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

            NbtCompound sync = new NbtCompound();
            sync.putUuid("anvilOwner", playerUUID);
            sync.putLong("anvilPos", pos.asLong());
            PacketByteBuf syncBuf = ModMessages.buf();
            MinigameSyncS2CPacket.encode(new MinigameSyncS2CPacket(sync), syncBuf);
            ModMessages.sendToAll(ModMessages.MINIGAME_SYNC, syncBuf, world.getServer());

            PacketByteBuf startBuf = ModMessages.buf();
            StartMinigameS2CPacket.encode(new StartMinigameS2CPacket(pos, hitsRequired, quality), startBuf);
            ModMessages.sendToPlayer(ModMessages.START_MINIGAME, startBuf, serverPlayer);
        } else if (currentOwner.equals(playerUUID)) {
            boolean visible = playerMinigameVisibility.get(playerUUID);
            playerMinigameVisibility.put(playerUUID, !visible);
            PacketByteBuf toggleBuf = ModMessages.buf();
            ToggleMinigameS2CPacket.encode(new ToggleMinigameS2CPacket(pos, !visible), toggleBuf);
            ModMessages.sendToPlayer(ModMessages.TOGGLE_MINIGAME, toggleBuf, serverPlayer);
        }

        return ActionResult.SUCCESS;
    }

    public static void handleAnvilOwnershipSync(NbtCompound syncData) {
        UUID owner = null;
        if (syncData.contains("anvilOwner")) {
            owner = syncData.getUuid("anvilOwner");
            if (owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
                owner = null;
            }
        }
        BlockPos pos = BlockPos.fromLong(syncData.getLong("anvilPos"));
        ClientAnvilMinigameData.putOccupiedAnvil(pos, owner);

        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null
                && client.player.getUuid().equals(owner)
                && pos.equals(ClientAnvilMinigameData.getPendingMinigamePos())) {

            BlockEntity be = client.world.getBlockEntity(pos);
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE && anvilBE.hasRecipe()) {
                Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                recipeOpt.ifPresent(recipe -> ClientAnvilMinigameData.clearPendingMinigame());
            }
        }
    }

    public static void releaseAnvil(ServerPlayerEntity player, BlockPos pos) {
        UUID playerId = player.getUuid();
        if (playerMinigameVisibility.get(playerId) != null)
            playerMinigameVisibility.remove(playerId);
        if (playerAnvilPositions.get(playerId) != null
                && pos.equals(playerAnvilPositions.get(playerId))
        ) {
            playerAnvilPositions.remove(playerId);

            BlockEntity be = player.getWorld().getBlockEntity(pos);
            String quality = "perfect";
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE) {
                anvilBE.clearOwner();
                quality = anvilBE.minigameQuality();
            }
            ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
            // AnvilMinigameEvents.reset(quality) intentionally not called here - it's a
            // client-only class and this method also runs on dedicated servers; the sync
            // packet below drives the same client-side reset safely.
            NbtCompound syncData = new NbtCompound();
            syncData.putLong("anvilPos", pos.asLong());
            syncData.putUuid("anvilOwner", new UUID(0, 0));
            PacketByteBuf buf = ModMessages.buf();
            MinigameSyncS2CPacket.encode(new MinigameSyncS2CPacket(syncData), buf);
            ModMessages.sendToAll(ModMessages.MINIGAME_SYNC, buf, player.getServer());
        }

    }

    public static ServerPlayerEntity getUsingPlayer(BlockPos pos) {
        MinecraftServer server = Overgeared.getServer();
        if (server == null) return null;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID playerId = player.getUuid();

            if (playerAnvilPositions.containsKey(playerId) &&
                    playerAnvilPositions.get(playerId).equals(pos)) {
                return player;
            }
        }

        return null;
    }

    public static void hideMinigame(ServerPlayerEntity player) {
        PacketByteBuf buf = ModMessages.buf();
        HideMinigameS2CPacket.encode(new HideMinigameS2CPacket(), buf);
        ModMessages.sendToPlayer(ModMessages.HIDE_MINIGAME, buf, player);
    }

    // =========================
    // Right-click item: cooling, grinding, polishing, durability repair, cleanup
    // =========================

    private static TypedActionResult<ItemStack> onRightClickItem(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand) {
        if (world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(player.getStackInHand(hand));

        ItemStack stack = player.getStackInHand(hand);

        if (handleCooling(player, stack, world)) return TypedActionResult.success(stack);
        if (handleGrinding(player, stack, world)) return TypedActionResult.success(stack);
        handleMinigameCleanup(player, world);
        return TypedActionResult.pass(stack);
    }

    private static boolean handleCooling(net.minecraft.entity.player.PlayerEntity player, ItemStack stack, World world) {
        if (!stack.isIn(ModTags.Items.HEATED_METALS)) return false;

        HitResult hit = player.raycast(5.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getFluidState().isStill() && state.getBlock() == Blocks.WATER) {
            coolItem(player, stack);
            return true;
        }

        return false;
    }

    private static boolean handleGrinding(net.minecraft.entity.player.PlayerEntity player, ItemStack stack, World world) {
        if (!player.isSneaking()) return false;

        HitResult hit = player.raycast(5.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!state.isIn(ModTags.Blocks.GRINDSTONES)) return false;
        if (player.getMainHandStack() != stack) return false;

        if (handleGrindingRecipe(player, stack, world, pos)) return true;
        if (handlePolishing(player, stack, world, pos)) return true;
        return handleDurabilityGrinding(stack, world, pos);
    }

    private static boolean handleGrindingRecipe(net.minecraft.entity.player.PlayerEntity player, ItemStack stack, World world, BlockPos pos) {
        if (!hasGrindingRecipe(stack.getItem(), world)) return false;

        grindItem(player, stack);
        playGrindEffects(world, pos);
        return true;
    }

    private static boolean handlePolishing(net.minecraft.entity.player.PlayerEntity player, ItemStack stack, World world, BlockPos pos) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("Polished") || tag.getBoolean("Polished")) return false;

        ItemStack resultItem;

        if (stack.getCount() > 1) {
            resultItem = stack.copy();
            resultItem.setCount(1);
            stack.decrement(1);
        } else {
            resultItem = stack;
        }

        NbtCompound resultTag = resultItem.getOrCreateNbt();
        resultTag.putBoolean("Polished", true);

        if (tag.getBoolean("Heated") && tag.contains("ForgingQuality")) {
            ForgingQuality quality = ForgingQuality.fromString(tag.getString("ForgingQuality"));
            ForgingQuality downgraded = quality.getLowerQuality();
            resultTag.putString("ForgingQuality", downgraded.getDisplayName());
        }

        if (resultItem != stack && !player.getInventory().insertStack(resultItem)) {
            player.dropItem(resultItem, false);
        }

        playGrindEffects(world, pos);
        return true;
    }

    private static boolean handleDurabilityGrinding(ItemStack stack, World world, BlockPos pos) {
        if (!stack.isDamageable() || stack.getDamage() <= 0) return false;

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
        Identifier itemId = Registries.ITEM.getId(item);

        for (String entry : ServerConfig.GRINDING_BLACKLIST.get()) {
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, Identifier.tryParse(entry.substring(1)));
                if (stack.isIn(tag)) return true;
            } else if (itemId != null && itemId.equals(Identifier.tryParse(entry))) {
                return true;
            }
        }

        return GrindingBlacklistReloadListener.isBlacklisted(stack);
    }

    private static void applyDurabilityRepair(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();

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

        int currentDamage = stack.getDamage();

        if (currentDamage <= (adjustedMax - effectiveMax)) {
            tag.putInt("ReducedMaxDurability", reducedCount + 1);
            stack.setDamage(0);
            return;
        }

        float restorePercent = ServerConfig.DAMAGE_RESTORE_PER_GRIND.get().floatValue();
        int repairAmount = Math.max(1, (int) (adjustedMax * restorePercent));

        int newDamage = Math.max(adjustedMax - effectiveMax, currentDamage - repairAmount);

        stack.setDamage(newDamage);
        tag.putInt("ReducedMaxDurability", reducedCount + 1);
    }

    private static void playGrindEffects(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
        spawnGrindParticles(world, pos);
    }

    private static void handleMinigameCleanup(net.minecraft.entity.player.PlayerEntity player, World world) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        ItemStack mainHand = player.getMainHandStack();
        HitResult hit = player.raycast(5.0D, 0.0F, false);

        if (hit.getType() != HitResult.Type.BLOCK) {
            hideMinigame(serverPlayer);
            return;
        }

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!mainHand.isIn(ModTags.Items.SMITHING_HAMMERS) ||
                !state.isIn(ModTags.Blocks.SMITHING_ANVIL)) {
            hideMinigame(serverPlayer);
        }
    }

    // =========================
    // Knapping (both hands)
    // =========================

    private static TypedActionResult<ItemStack> onUsingKnappable(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand) {
        ItemStack usedStack = player.getStackInHand(hand);

        if (!usedStack.isIn(ModTags.Items.KNAPPABLE)) return TypedActionResult.pass(usedStack);

        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        if (!(mainHand.isIn(ModTags.Items.KNAPPABLE) && offHand.isIn(ModTags.Items.KNAPPABLE))) {
            return TypedActionResult.pass(usedStack);
        }

        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {

            world.playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.BLOCK_STONE_PLACE,
                    SoundCategory.PLAYERS,
                    0.6f,
                    1.0f
            );

            serverPlayer.openHandledScreen(new RockKnappingMenuProvider());
        }

        return TypedActionResult.success(usedStack, world.isClient);
    }

    private static void spawnGrindParticles(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.CRIT,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    private static void handleCauldronInteraction(World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player,
                                                    ItemStack heldStack, BlockState state) {
        int waterLevel = state.get(LeveledCauldronBlock.LEVEL);

        if (waterLevel > 0) {
            coolItem(player, heldStack);
        }
    }

    private static ItemStack coolSingleStack(ItemStack stack, World world) {
        Item cooled = getCooledItem(stack.getItem(), world);
        if (cooled == null) return stack;

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());

        if (stack.hasNbt()) {
            NbtCompound tag = stack.getNbt().copy();
            tag.remove("Heated");
            tag.remove("HeatedSince");
            if (tag.isEmpty()) {
                cooledStack.setNbt(null);
            } else {
                cooledStack.setNbt(tag);
            }
        }

        return cooledStack;
    }

    private static void coolItem(net.minecraft.entity.player.PlayerEntity player, ItemStack stack) {
        Item cooled = getCooledItem(stack.getItem(), player.getWorld());
        if (cooled == null) return;
        if (stack.getCount() <= 0) return;

        // === Tool Cast special handling ===
        if (stack.getItem() instanceof ToolCastItem && stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();

            if (tag != null && tag.contains("Output", NbtElement.COMPOUND_TYPE)) {
                ItemStack output = ItemStack.fromNbt(tag.getCompound("Output"));
                ItemStack cooledOutput = coolSingleStack(output, player.getWorld());
                tag.put("Output", cooledOutput.writeNbt(new NbtCompound()));
            }
        }

        // === Original logic (unchanged) ===
        ItemStack cooledStack = new ItemStack(cooled, 1);
        if (stack.hasNbt()) {
            cooledStack.setNbt(stack.getNbt().copy());
            cooledStack.removeSubNbt("HeatedSince");
            cooledStack.removeSubNbt("Heated");
        }

        stack.decrement(1);

        if (stack.isEmpty()) {
            if (player.getMainHandStack() == stack) {
                player.setStackInHand(Hand.MAIN_HAND, cooledStack);
            } else if (player.getOffHandStack() == stack) {
                player.setStackInHand(Hand.OFF_HAND, cooledStack);
            } else if (!player.getInventory().insertStack(cooledStack)) {
                player.dropItem(cooledStack, false);
            }
        } else {
            if (!player.getInventory().insertStack(cooledStack)) {
                player.dropItem(cooledStack, false);
            }
        }

        player.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
    }


    private static void coolItemEntity(ItemEntity entity) {
        ItemStack stack = entity.getStack();
        World world = entity.getWorld();

        Item cooled = getCooledItem(stack.getItem(), world);
        if (cooled == null || stack.getCount() <= 0) return;

        if (stack.getItem() instanceof ToolCastItem && stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();

            if (tag.contains("Output", NbtElement.COMPOUND_TYPE)) {
                ItemStack output = ItemStack.fromNbt(tag.getCompound("Output"));
                ItemStack cooledOutput = coolSingleStack(output, world);
                tag.put("Output", cooledOutput.writeNbt(new NbtCompound()));
            }
        }

        NbtCompound oldTag = stack.hasNbt() ? stack.getNbt().copy() : null;
        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());

        if (oldTag != null) {
            oldTag.remove("Heated");
            oldTag.remove("HeatedSince");
            if (oldTag.isEmpty()) {
                cooledStack.setNbt(null);
            } else {
                cooledStack.setNbt(oldTag);
            }
        }

        entity.setStack(cooledStack);
    }


    private static void grindItem(net.minecraft.entity.player.PlayerEntity player, ItemStack heldStack) {
        Item cooledItem = getGrindable(heldStack.getItem(), player.getWorld());
        if (cooledItem != null) {
            ItemStack cooledIngot = new ItemStack(cooledItem);
            if (heldStack.hasNbt()) {
                cooledIngot.setNbt(heldStack.getNbt().copy());
            }
            cooledIngot.getOrCreateNbt().putBoolean("Polished", true);
            heldStack.decrement(1);

            if (heldStack.isEmpty()) {
                player.setStackInHand(player.getActiveHand(), cooledIngot);
            } else {
                if (!player.getInventory().insertStack(cooledIngot)) {
                    player.dropItem(cooledIngot, false);
                }
            }

            player.playSound(SoundEvents.BLOCK_GRINDSTONE_USE, 1.0F, 1.0F);
        }
    }

    private static Item getGrindable(@Nullable Item heatedItem, @NotNull World world) {
        if (heatedItem == null) return null;

        net.minecraft.inventory.SimpleInventory container = new net.minecraft.inventory.SimpleInventory(new ItemStack(heatedItem));

        Optional<GrindingRecipe> recipeOpt = world.getRecipeManager()
                .listAllOfType(ModRecipeTypes.GRINDING_RECIPE)
                .stream()
                .filter(r -> r.matches(container, world))
                .findFirst();

        if (recipeOpt.isEmpty()) {
            return heatedItem;
        }

        GrindingRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.getOutput(world.getRegistryManager());
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    public static boolean hasCoolingRecipe(@Nullable Item heatedItem, @NotNull World world) {
        if (heatedItem == null) return false;

        net.minecraft.inventory.SimpleInventory container = new net.minecraft.inventory.SimpleInventory(new ItemStack(heatedItem));

        Optional<CoolingRecipe> recipeOpt = world.getRecipeManager()
                .listAllOfType(ModRecipeTypes.COOLING_RECIPE)
                .stream()
                .filter(r -> r.matches(container, world))
                .findFirst();

        return recipeOpt.map(recipe -> !recipe.getOutput(world.getRegistryManager()).isEmpty())
                .orElse(false);
    }

    public static boolean hasGrindingRecipe(@Nullable Item heatedItem, @NotNull World world) {
        if (heatedItem == null) return false;

        net.minecraft.inventory.SimpleInventory container = new net.minecraft.inventory.SimpleInventory(new ItemStack(heatedItem));

        Optional<GrindingRecipe> recipeOpt = world.getRecipeManager()
                .listAllOfType(ModRecipeTypes.GRINDING_RECIPE)
                .stream()
                .filter(r -> r.matches(container, world))
                .findFirst();

        return recipeOpt.map(recipe -> !recipe.getOutput(world.getRegistryManager()).isEmpty())
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
    private static void trackNewItemEntities(ServerWorld world) {
        for (Entity entity : world.iterateEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            if (!knownTrackedEntities.add(itemEntity)) continue; // already seen

            ItemStack stack = itemEntity.getStack();
            boolean isHeatedItem = stack.hasNbt() && stack.getNbt().getBoolean("Heated");

            if (hasCoolingRecipe(stack.getItem(), world) || isHeatedItem) {
                trackedEntitiesPerWorld
                        .computeIfAbsent(world, w -> new ArrayList<>())
                        .add(itemEntity);

                if (stack.hasNbt() && stack.getNbt().contains("HeatedSince")) {
                    long heatedSince = stack.getNbt().getLong("HeatedSince");
                    trackedSinceMs.put(itemEntity, heatedSince);
                }
            }
        }
    }

    private static boolean hasCoolingRecipeCached(Item item, World world) {
        return COOLING_CACHE.computeIfAbsent(item, i -> hasCoolingRecipe(i, world));
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            long now = world.getTime();

            if (now % 10 != 0) continue;

            trackNewItemEntities(world);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.currentScreenHandler != null && player.currentScreenHandler != player.playerScreenHandler) {
                    checkContainerMenu(player, player.currentScreenHandler);
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

                ItemStack stack = entity.getStack();

                boolean isHeated = (stack.hasNbt() && stack.getNbt().getBoolean("Heated"))
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

                BlockPos.Mutable pos = new BlockPos.Mutable(
                        (int) entity.getX(), (int) entity.getY(), (int) entity.getZ());

                BlockState state = world.getBlockState(pos);
                if (state.isOf(Blocks.WATER) || state.isOf(Blocks.WATER_CAULDRON)) {
                    cooled = true;
                }

                if (cooled) {
                    coolItemEntity(entity);
                    world.spawnParticles(ParticleTypes.SMOKE,
                            entity.getX(), entity.getY() + 0.25, entity.getZ(),
                            6, 0.15, 0.15, 0.15, 0.02);
                    world.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.0f);
                    trackedSinceMs.remove(entity);

                    if (entity.getStack().isEmpty()) {
                        it.remove();
                    }
                }
            }

            if (tracked.isEmpty()) {
                trackedEntitiesPerWorld.remove(world);
            }
        }
    }

    private static void checkContainerMenu(ServerPlayerEntity player, ScreenHandler menu) {
        long gameTime = player.getWorld().getTime();
        int cooldown = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();
        boolean playedSound = false;

        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            if (stack.hasNbt() && stack.getNbt().contains("HeatedSince")) {
                long heatedAt = stack.getNbt().getLong("HeatedSince");
                if (gameTime - heatedAt >= cooldown) {
                    coolItemInContainerSlot(player, slot);

                    if (!playedSound) {
                        player.getWorld().playSound(null, player.getBlockPos(),
                                SoundEvents.BLOCK_FIRE_EXTINGUISH,
                                SoundCategory.PLAYERS, 0.5F, 1.0F);
                        playedSound = true;
                    }
                }
            }
        }
    }

    private static void coolItemInContainerSlot(ServerPlayerEntity player, Slot slot) {
        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) return;

        Item cooled = getCooledItem(stack.getItem(), player.getWorld());
        if (cooled == null) return;

        if (stack.getItem() instanceof ToolCastItem && stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();
            if (tag != null && tag.contains("Output", NbtElement.COMPOUND_TYPE)) {
                ItemStack output = ItemStack.fromNbt(tag.getCompound("Output"));

                Item cooledOutputItem = getCooledItem(output.getItem(), player.getWorld());
                if (cooledOutputItem != null) {
                    ItemStack cooledOutput = new ItemStack(cooledOutputItem, output.getCount());
                    if (output.hasNbt()) {
                        cooledOutput.setNbt(output.getNbt().copy());
                    }
                    tag.put("Output", cooledOutput.writeNbt(new NbtCompound()));
                }
            }
        }

        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());

        if (stack.hasNbt()) {
            NbtCompound newTag = stack.getNbt().copy();
            newTag.remove("HeatedSince");
            newTag.remove("Heated");

            if (newTag.isEmpty()) {
                cooledStack.setNbt(null);
            } else {
                cooledStack.setNbt(newTag);
            }
        }

        slot.setStack(cooledStack);
    }

    // =========================
    // Flint on stone (knapping-adjacent rock interactions)
    // =========================

    private static ActionResult onFlintUsedOnStone(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack heldItem = player.getStackInHand(hand);

        for (RockInteractionData data : RockInteractionReloadListener.INSTANCE.getAll()) {

            if (!data.matches(state, heldItem)) continue;

            RockInteractionData.ToolEntry tool = data.getTool(heldItem);
            if (tool == null) continue;

            ServerWorld serverWorld = (ServerWorld) world;

            if (world.random.nextFloat() < tool.dropChance()) {
                ItemStack dropStack = tool.dropItem().copy();

                double sx = pos.getX() + 0.5;
                double sy = pos.getY() + 0.9;
                double sz = pos.getZ() + 0.5;

                double dx = player.getX() - sx;
                double dy = (player.getY() + player.getStandingEyeHeight()) - sy;
                double dz = player.getZ() - sz;

                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len != 0) {
                    dx /= len;
                    dy /= len;
                    dz /= len;
                }

                ItemEntity item = new ItemEntity(serverWorld, sx, sy, sz, dropStack);
                item.setVelocity(dx * 0.25, dy * 0.25, dz * 0.25);
                item.setToDefaultPickupDelay();
                serverWorld.spawnEntity(item);

                world.setBlockState(pos, data.getResultBlock().getDefaultState());
            }

            if (world.random.nextFloat() < tool.breakChance()) {

                if (heldItem.isDamageable()) {
                    heldItem.damage(1, player, p -> p.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND
                            ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
                } else {
                    heldItem.decrement(1);
                }

                world.playSound(null, player.getBlockPos(),
                        SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS,
                        0.8F, 1.0F);
            } else {
                world.playSound(null, pos,
                        SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                        1.0F, 1.0F);
            }

            player.swingHand(hand);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    // =========================
    // Arrow tipping
    // =========================

    private static TypedActionResult<ItemStack> onArrowTipping(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand) {
        if (world.isClient) return TypedActionResult.pass(player.getStackInHand(hand));
        if (!ServerConfig.TIPPING_TOGGLE.get()) return TypedActionResult.pass(player.getStackInHand(hand));

        ItemStack usedHand = player.getStackInHand(hand);
        Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack otherStack = player.getStackInHand(otherHand);

        boolean isVanillaArrow = usedHand.isOf(Items.ARROW) && otherStack.isOf(Items.POTION);
        boolean isCustomArrow = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get() && (usedHand.isOf(ModItems.IRON_UPGRADE_ARROW) ||
                usedHand.isOf(ModItems.STEEL_UPGRADE_ARROW) ||
                usedHand.isOf(ModItems.DIAMOND_UPGRADE_ARROW)) &&
                otherStack.isOf(Items.POTION);

        if (!isVanillaArrow && !isCustomArrow) {
            return TypedActionResult.pass(usedHand);
        }

        NbtCompound potionTag = otherStack.getOrCreateNbt();
        int used = potionTag.getInt("TippedUsed");
        int maxUse = ServerConfig.MAX_POTION_TIPPING_USE.get();
        Potion basePotion = PotionUtil.getPotion(otherStack);

        ItemStack resultArrow;
        if (isVanillaArrow) {
            resultArrow = PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW), basePotion);
        } else {
            resultArrow = usedHand.copy();
            resultArrow.setCount(1);

            NbtCompound arrowTag = new NbtCompound();
            arrowTag.putString("Potion", Registries.POTION.getId(basePotion).toString());

            if (potionTag.contains("CustomPotionEffects", NbtElement.LIST_TYPE)) {
                arrowTag.put("CustomPotionEffects", potionTag.getList("CustomPotionEffects", NbtElement.COMPOUND_TYPE));
            }
            if (potionTag.contains("CustomPotionColor", NbtElement.INT_TYPE)) {
                arrowTag.putInt("CustomPotionColor", potionTag.getInt("CustomPotionColor"));
            }

            resultArrow.setNbt(arrowTag);
        }

        if (usedHand.getCount() == 1) {
            player.setStackInHand(hand, resultArrow);
        } else {
            usedHand.decrement(1);
            player.setStackInHand(hand, usedHand);
            if (!player.getInventory().insertStack(resultArrow)) {
                player.dropItem(resultArrow, false);
            }
        }

        if (otherStack.getCount() > 1) {
            ItemStack onePotion = otherStack.split(1);
            NbtCompound oneTag = onePotion.getOrCreateNbt();
            oneTag.putInt("TippedUsed", used + 1);
            PotionUtil.setPotion(onePotion, basePotion);
            player.setStackInHand(otherHand, otherStack);
        } else {
            used++;
            if (used >= maxUse) {
                player.setStackInHand(otherHand, new ItemStack(Items.GLASS_BOTTLE));
            } else {
                potionTag.putInt("TippedUsed", used);
                PotionUtil.setPotion(otherStack, basePotion);
                player.setStackInHand(otherHand, otherStack);
            }
        }

        world.playSound(null,
                player.getBlockPos(),
                SoundEvents.BLOCK_BREWING_STAND_BREW,
                SoundCategory.PLAYERS,
                0.6F,
                1.2F
        );

        return TypedActionResult.success(usedHand);
    }

    // =========================
    // Fletching table
    // =========================

    private static ActionResult onRightClickFletching(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (!ServerConfig.ENABLE_FLETCHING_RECIPES.get()) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.FLETCHING_TABLE)) return ActionResult.PASS;

        if (world.isClient) return ActionResult.SUCCESS;

        SimpleNamedScreenHandlerFactory provider = new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) ->
                        new FletchingStationScreenHandler(
                                syncId,
                                playerInv,
                                ScreenHandlerContext.create(world, pos)
                        ),
                Text.translatable("container.overgeared.fletching_table")
        );

        ((ServerPlayerEntity) player).openHandledScreen(provider);

        return ActionResult.CONSUME;
    }
}
