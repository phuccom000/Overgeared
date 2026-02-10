package net.stirdrem.overgeared.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.custom.StoneSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.GrindingBlacklistReloadListener;
import net.stirdrem.overgeared.datapack.RockInteractionData;
import net.stirdrem.overgeared.datapack.RockInteractionReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.networking.packet.HideMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.MinigameSetStartedC2SPacket;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.SetMinigameVisibleC2SPacket;
import net.stirdrem.overgeared.recipe.CoolingRecipe;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.GrindingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.FletchingStationMenu;
import net.stirdrem.overgeared.screen.RockKnappingMenuProvider;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;
import net.stirdrem.overgeared.item.custom.HeatedItem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static net.stirdrem.overgeared.components.ModComponents.HEATED_COMPONENT;
import static net.stirdrem.overgeared.util.ItemUtils.copyComponentsExceptHeated;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModItemInteractEvents {
    public static final Map<UUID, BlockPos> playerAnvilPositions = new HashMap<>();
    public static final Map<UUID, Boolean> playerMinigameVisibility = new HashMap<>();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
                event.setUseBlock(TriState.FALSE);
                event.setUseItem(TriState.FALSE);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack heldItem = event.getItemStack();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // Smithing
        if (heldItem.is(ModTags.Items.SMITHING_HAMMERS) && event.getHand() == InteractionHand.MAIN_HAND) {
            onUseSmithingHammer(event, player, level, state);
        }
        // Fletching
        else if (state.is(Blocks.FLETCHING_TABLE)  && event.getHand() == InteractionHand.MAIN_HAND) {
            onUseFletching(event, player, level);
        }
        // Heated Ingot on Cauldron (maybe any water-y block later?)
        else if (state.is(Blocks.WATER_CAULDRON)) {
            onUseCauldron(event, player, heldItem, state);
        }
        // Stone
        else {
            for (RockInteractionData data : RockInteractionReloadListener.INSTANCE.getAll()) {
                if (!data.matches(state, heldItem)) continue;
                onFlintUsedOnStone(event, player, heldItem, level, data);
                return;
            }
        }
    }

    public static void onUseSmithingHammer(PlayerInteractEvent.RightClickBlock event, Player player, Level level, BlockState state) {
        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);

        // Shift-right-click to convert stone into smithing anvil
        if (!level.isClientSide && player.isCrouching() && state.is(ModTags.Blocks.ANVIL_BASES)
                && ServerConfig.ENABLE_STONE_TO_ANVIL.get()) {
            BlockState newState = ModBlocks.STONE_SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise());
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.get()
                        .trigger(serverPlayer, "stone");
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide && player.isCrouching() && state.is(Blocks.ANVIL)
                && ServerConfig.ENABLE_ANVIL_TO_SMITHING.get()) {
            BlockState newState = ModBlocks.SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise());
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancementTriggers.MAKE_SMITHING_ANVIL.get()
                        .trigger(serverPlayer, "iron");
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide()) {
            if (!(be instanceof AbstractSmithingAnvilBlockEntity)) {
                hideMinigame((ServerPlayer) player);
            }
        }

        if (!(be instanceof
                AbstractSmithingAnvilBlockEntity anvilBE)) return;
        UUID playerUUID = player.getUUID();

        if (!player.isCrouching()) return;

        if (!level.isClientSide) {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            // Server-side ownership logic
            if (anvilBE.hasRecipe() && !ServerConfig.ENABLE_MINIGAME.get()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.no_minigame").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!anvilBE.hasRecipe()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED), true);
                return;
            }

            UUID currentOwner = anvilBE.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(playerUUID)) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (currentOwner == null && !playerAnvilPositions.containsKey(player.getUUID())) {
                anvilBE.setOwner(playerUUID);

                // ADD SERVER-SIDE TRACKING
                playerAnvilPositions.put(playerUUID, pos);
                playerMinigameVisibility.put(playerUUID, true);

                CompoundTag sync = new CompoundTag();
                sync.putUUID("anvilOwner", playerUUID);
                sync.putLong("anvilPos", pos.asLong());
                PacketDistributor.sendToAllPlayers(new MinigameSyncS2CPacket(sync));
                return;
            }

            if (playerAnvilPositions.get(player.getUUID()) != null && !pos.equals(playerAnvilPositions.get(player.getUUID()))) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED), true);
                return;
            }
        } else {
            if (anvilBE.hasRecipe() && !ServerConfig.ENABLE_MINIGAME.get()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.no_minigame").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!anvilBE.hasRecipe()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED));
                return;
            }

            if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED));
                return;
            }

            // Client should trust the server's sync data in ClientAnvilMinigameData
            UUID currentOwner = ClientAnvilMinigameData.getOccupiedAnvil(pos);
            if (currentOwner != null && !currentOwner.equals(player.getUUID())) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED));
                return;
            }

            if (player.getUUID().equals(currentOwner)
                    || currentOwner == null
                    && ClientAnvilMinigameData.getPendingMinigamePos() == null) {
                BlockPos pos1 = pos;
                BlockPos anvilPos = playerAnvilPositions.get(player.getUUID());
                if (playerAnvilPositions.get(player.getUUID()) != null && !pos.equals(playerAnvilPositions.get(player.getUUID()))) {
                    //player.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.PASS);
                    return;
                }
                if (anvilBE.hasRecipe() || anvilBE.needsMinigame()) {
                    anvilBE.setOwner(playerUUID);
                    AtomicReference<String> quality = new AtomicReference<>("perfect");
                    Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                    recipeOpt.ifPresent(recipe -> {
                        if (AnvilMinigameEvents.minigameStarted) {
                            boolean isVisible = AnvilMinigameEvents.isVisible();
                            AnvilMinigameEvents.setIsVisible(pos, !isVisible);
                            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(!isVisible, pos));
                            playerMinigameVisibility.put(player.getUUID(), !isVisible);
                        } else {
                            quality.set(anvilBE.minigameQuality().getDisplayName());
                            AnvilMinigameEvents.reset(quality.get());
                            playerAnvilPositions.put(player.getUUID(), pos);
                            playerMinigameVisibility.put(player.getUUID(), true);
                            AnvilMinigameEvents.setMinigameStarted(pos, true);
                            PacketDistributor.sendToServer(new MinigameSetStartedC2SPacket(pos));
                            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(true, pos));
                            AnvilMinigameEvents.setHitsRemaining(anvilBE.getRequiredProgress());
                        }
                    });
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.PASS);
                return;
            }

            ClientAnvilMinigameData.setPendingMinigame(pos);
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static void onUseFletching(PlayerInteractEvent.RightClickBlock event, Player player, Level level) {
        if (!ServerConfig.ENABLE_FLETCHING_RECIPES.get()) return;

        BlockPos pos = event.getPos();
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (windowId, playerInv, p) ->
                        new FletchingStationMenu(windowId, playerInv, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.overgeared.fletching_table")
        );
        player.openMenu(provider, pos);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void onUseCauldron(PlayerInteractEvent.RightClickBlock event, Player player, ItemStack heldItem, BlockState state) {
        // Check if the item is heated either by tag or NBT
        if (!(heldItem.is(ModTags.Items.HEATED_METALS) || (Boolean.TRUE.equals(heldItem.get(HEATED_COMPONENT))))) return;

        IntegerProperty levelProperty = LayeredCauldronBlock.LEVEL;
        int waterLevel = state.getValue(levelProperty);

        if (waterLevel <= 0) return;
        if (!(heldItem.getItem() instanceof HeatedItem heatedItem)) return;
        heatedItem.setCooled(heldItem, player);
        
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    public static void onFlintUsedOnStone(PlayerInteractEvent.RightClickBlock event, Player player, ItemStack heldItem, Level level, RockInteractionData data) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        RockInteractionData.ToolEntry tool = data.getTool(heldItem);
        if (tool == null) return;

        BlockPos pos = event.getPos();
        // Drop roll
        if (level.random.nextFloat() < tool.dropChance()) {
            ItemStack dropStack = tool.dropItem().copy();

            double sx = pos.getX() + 0.5;
            double sy = pos.getY() + 0.9;
            double sz = pos.getZ() + 0.5;

            double dx = player.getX() - sx;
            double dy = (player.getEyeY()) - sy; // eyeY helper exists now
            double dz = player.getZ() - sz;

            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                dx /= len;
                dy /= len;
                dz /= len;
            }

            ItemEntity item = new ItemEntity(serverLevel, sx, sy, sz, dropStack);
            item.setDeltaMovement(dx * 0.25, dy * 0.25, dz * 0.25);
            item.setPickUpDelay(10); // setDefaultPickUpDelay removed
            serverLevel.addFreshEntity(item);

            level.setBlockAndUpdate(pos, data.getResultBlock().defaultBlockState());
        }

        // Tool damage / break roll
        if (level.random.nextFloat() < tool.breakChance()) {

            if (heldItem.isDamageableItem()) {
                heldItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
            } else {
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

        player.swing(event.getHand(), true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
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
                && pos.equals(playerAnvilPositions.get(playerId))) {
            playerAnvilPositions.remove(playerId);

            // 1. Clear ownership from the block entity (server-side)
            BlockEntity be = player.level().getBlockEntity(pos);
            String quality = "perfect";
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE) {
                anvilBE.clearOwner();
                quality = anvilBE.minigameQuality().getDisplayName();
            }
            // 3. Clear client-side state
            ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
            AnvilMinigameEvents.reset(quality);
            // 4. Sync null ownership to all clients
            CompoundTag syncData = new CompoundTag();
            syncData.putLong("anvilPos", pos.asLong());
            syncData.putUUID("anvilOwner", new UUID(0, 0)); // special "no owner" UUID
            PacketDistributor.sendToAllPlayers(new MinigameSyncS2CPacket(syncData));
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
        PacketDistributor.sendToPlayer(player, new HideMinigameS2CPacket());
    }

    // Probably would be better to move all of RightClickItem events into one method, too tired to do it right now :(
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide()) return;

        // Check if the player is holding a heated metal and targeting water
        if (stack.is(ModTags.Items.HEATED_METALS)) {
            HitResult hit = player.pick(5.0D, 0.0F, true);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                BlockState state = world.getBlockState(pos);
                if (state.getFluidState().isSource() && state.getBlock() == Blocks.WATER) {
                    
                    if (!(stack.getItem() instanceof HeatedItem heatedItem)) return;
                    heatedItem.setCooled(stack, player);

                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }

        HitResult hit = player.pick(5.0D, 0.0F, false);
        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (player.isCrouching() && state.is(ModTags.Blocks.GRINDSTONES)) {

            if (player.getMainHandItem() != stack) {
                return;
            }

            if (hasGrindingRecipe(stack.getItem(), event.getLevel())) {
                grindItem(player, stack);
                world.playSound(null, player.blockPosition(),
                        SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
                        1.0f, 1.2f); // Higher pitch for polishing sound
                spawnGrindParticles(world, pos);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
            if (Boolean.FALSE.equals(stack.get(ModComponents.POLISHED))) {
                // Only convert 1 item in the stack
                if (stack.getCount() > 1) {
                    // Split 1 item from the stack
                    ItemStack polishedItem = stack.copy();
                    polishedItem.setCount(1);
                    polishedItem.set(ModComponents.POLISHED, true);

                    // Reduce held stack by 1
                    stack.shrink(1);

                    // Try to add the polished item to player's inventory
                    if (!player.getInventory().add(polishedItem)) {
                        // If inventory is full, drop the item in the world
                        player.drop(polishedItem, false);
                    }
                } else {
                    // Only one item in stack, just polish it directly
                    stack.set(ModComponents.POLISHED, true);
                }

                world.playSound(null, player.blockPosition(),
                        SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
                        1.0f, 1.2f); // Higher pitch for polishing sound
                spawnGrindParticles(world, pos);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }

            if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                if (!ServerConfig.GRINDING_RESTORE_DURABILITY.get()) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    return;
                }
                Item item = stack.getItem();
                // Check blacklist
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                List<? extends String> blacklist = ServerConfig.GRINDING_BLACKLIST.get();

                for (String entry : blacklist) {
                    if (entry.startsWith("#")) {
                        // Handle tag-based blacklist entries
                        ResourceLocation tagId = ResourceLocation.parse(entry.substring(1));
                        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                        if (stack.is(tag)) {
                            event.setCancellationResult(InteractionResult.PASS);
                            event.setCanceled(true);
                            return;
                        }
                    } else {
                        // Handle direct item blacklist entries
                        if (itemId.equals(ResourceLocation.parse(entry))) {
                            event.setCancellationResult(InteractionResult.PASS);
                            event.setCanceled(true);
                            return;
                        }
                    }
                }

                boolean isBlacklisted = GrindingBlacklistReloadListener.isBlacklisted(stack);
                if (isBlacklisted) {
                    event.setCancellationResult(InteractionResult.PASS);
                    event.setCanceled(true);
                    return;
                }

                int reducedCount = stack.getOrDefault(ModComponents.REDUCED_GRIND_COUNT, 0);

                // Base vanilla durability
                int originalDurability = stack.getItem().getMaxDamage(stack);
                // Config multipliers
                float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
                float grindReduction = ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue();

                // Quality multiplier (if any)
                float qualityMultiplier = 1.0f;
                ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
                if (quality != null) {
                    qualityMultiplier = quality.getDamageMultiplier();
                }
                int newOriginalDurability = (int) (originalDurability * baseMultiplier * qualityMultiplier);

                // Final durability multiplier, clamped to 10% minimum
                float penaltyMultiplier = Math.max(0.1f, 1.0f - (reducedCount * grindReduction));

                int effectiveMaxDurability = (int) (newOriginalDurability * penaltyMultiplier);
                effectiveMaxDurability = Math.max(1, effectiveMaxDurability); // Clamp to avoid zero

                int currentDamage = stack.getDamageValue();

                // If already fully repaired relative to reduced max, skip
                if (currentDamage <= (newOriginalDurability - effectiveMaxDurability)) {
                    stack.set(ModComponents.REDUCED_GRIND_COUNT, reducedCount + 1);
                    stack.setDamageValue(0);
                    event.getLevel().playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    spawnGrindParticles(world, pos);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    return;
                }

                float restorePercent = ServerConfig.DAMAGE_RESTORE_PER_GRIND.get().floatValue(); // e.g., 0.05F for 5%
                int theoreticalMaxDurability = (int) (originalDurability * baseMultiplier * qualityMultiplier);
                int repairAmount = Math.max(1, (int) (theoreticalMaxDurability * restorePercent));

                // Respect effective max cap
                int newDamage = Math.max(theoreticalMaxDurability - effectiveMaxDurability, currentDamage - repairAmount);

                stack.setDamageValue(newDamage);
                stack.set(ModComponents.REDUCED_GRIND_COUNT, reducedCount + 1);
                event.getLevel().playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                spawnGrindParticles(world, pos);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }

        }
        ItemStack mainHand = player.getMainHandItem();
        // Only hide if MAIN HAND is not a hammer
        if (!mainHand.is(ModTags.Items.SMITHING_HAMMERS) || !state.is(ModTags.Blocks.SMITHING_ANVIL)) {
            hideMinigame((ServerPlayer) player);
        }
    }

    private static void spawnGrindParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    // Currently casts do nothing, BUT keeping the code in case there's plans for implementation :)
    // Do note that it would be better to move it to it's own item like HeatedItem is
    /*private static void coolItem(Player player, ItemStack stack) {
        ItemStack stack = entity.getItem();
        if (stack.getCount() <= 0) return;

        boolean isHeated = stack.getOrDefault(ModComponents.HEATED_COMPONENT, false);
        if (!stack.getOrDefault(ModComponents.HEATED_COMPONENT, false)) return;

        // === Tool Cast special handling ===
        if (stack.getItem() instanceof ToolCastItem) {
            CastData data = stack.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);
            if (data.hasOutput()) {
                ItemStack output = data.output();
                ItemStack cooledOutput = coolSingleStack(output, player.level()); // Not implemented here due to no need, example can be found in HeatedItem
                stack.set(ModComponents.CAST_DATA, data.withOutput(cooledOutput).withHeated(false));
                // Remove HEATED_COMPONENT from the cast itself so players stop taking damage
                stack.remove(ModComponents.HEATED_COMPONENT);
            }
            player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
            return; // Don't process further for casts
        }

        // Just remove the heated component and heated time e.g. for quenching
        stack.remove(ModComponents.HEATED_COMPONENT);
        stack.remove(ModComponents.HEATED_TIME);

        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
    }*/

    private static void grindItem(Player player, ItemStack heldStack) {
        Item cooledItem = getGrindable(heldStack.getItem(), player.level());
        if (cooledItem != null) {
            ItemStack cooledIngot = new ItemStack(cooledItem);
            // Copy all components for mod compatibility, then set polished
            copyComponentsExceptHeated(heldStack, cooledIngot);
            cooledIngot.set(ModComponents.POLISHED, true);
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

    private static Item getGrindable(Item heatedItem, Level level) {
        if (heatedItem == null || level == null) return null;

        // Wrap the single item in a container for recipe matching
        SingleRecipeInput container = new SingleRecipeInput(new ItemStack(heatedItem));

        // Find the first matching GrindingRecipe
        Optional<RecipeHolder<GrindingRecipe>> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())
                .stream()
                .filter(r -> r.value().matches(container, level))
                .findFirst();

        if (recipeOpt.isEmpty()) {
            return heatedItem; // no grinding recipe found
        }

        // Return the result item from the recipe
        GrindingRecipe recipe = recipeOpt.get().value();
        ItemStack result = recipe.getResultItem(level.registryAccess());
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    public static boolean hasCoolingRecipe(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null) return false;

        // Wrap the item in a container for recipe matching
        SingleRecipeInput container = new SingleRecipeInput(new ItemStack(heatedItem));

        // Find the first matching CoolingRecipe
        Optional<RecipeHolder<CoolingRecipe>> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE.get())
                .stream()
                .filter(r -> r.value().matches(container, level))
                .findFirst();

        // Return true if a recipe exists and produces a non-empty result
        return recipeOpt.map(recipe -> !recipe.value().getResultItem(level.registryAccess()).isEmpty())
                .orElse(false);
    }

    public static boolean hasGrindingRecipe(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null) return false;

        // Wrap the item in a container for recipe matching
        SingleRecipeInput container = new SingleRecipeInput(new ItemStack(heatedItem));

        // Find the first matching GrindingRecipe
        Optional<RecipeHolder<GrindingRecipe>> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())
                .stream()
                .filter(r -> r.value().matches(container, level))
                .findFirst();

        // Return true if a recipe exists and produces a non-empty result
        return recipeOpt.map(recipe -> !recipe.value().getResultItem(level.registryAccess()).isEmpty())
                .orElse(false);
    }

// TODO: this should be replaced with DataAttachments
//    @SubscribeEvent
//    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
//        if (event.getObject() instanceof ItemEntity) {
//            event.addCapability(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "heated_item"),
//                    new HeatedItemProvider());
//        }
//    }

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

        // Get items in both hands
        ItemStack usedHand = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        // Check if we're dealing with arrow + potion combination
        boolean isVanillaArrow = usedHand.is(Items.ARROW) && otherStack.is(Items.POTION);
        boolean isCustomArrow = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get() && (usedHand.is(ModItems.IRON_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.STEEL_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.DIAMOND_UPGRADE_ARROW.get())) &&
                otherStack.is(Items.POTION);

        if (!isVanillaArrow && !isCustomArrow) {
            return;
        }

        // Get potion contents from the potion item (1.21 API uses data components)
        PotionContents potionContents = otherStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return;

        // Track usage via data component
        int used = otherStack.getOrDefault(ModComponents.TIPPED_USES, 0);
        int maxUse = ServerConfig.MAX_POTION_TIPPING_USE.get();

        // Create the appropriate tipped arrow BEFORE consuming
        ItemStack resultArrow;
        if (isVanillaArrow) {
            resultArrow = new ItemStack(Items.TIPPED_ARROW);
            resultArrow.set(DataComponents.POTION_CONTENTS, potionContents);
        } else {
            resultArrow = usedHand.copy();
            resultArrow.setCount(1);
            // Apply potion contents to custom arrow
            resultArrow.set(DataComponents.POTION_CONTENTS, potionContents);
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

        // Handle potion usage tracking
        used++;
        if (used >= maxUse) {
            // Potion is exhausted, replace with empty bottle
            player.setItemInHand(otherHand, new ItemStack(Items.GLASS_BOTTLE));
        } else {
            // Update the usage count on the potion using data component
            otherStack.set(ModComponents.TIPPED_USES, used);
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

    @SubscribeEvent
    public static void onUsingKnappable(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack usedStack = event.getItemStack();

        // Only trigger for knappable items
        if (!usedStack.is(ModTags.Items.KNAPPABLES)) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Require BOTH hands to be knappable
        if (!(mainHand.is(ModTags.Items.KNAPPABLES) && offHand.is(ModTags.Items.KNAPPABLES))) {
            return;
        }
        // Both items must be the SAME item
        if (mainHand.getItem() != offHand.getItem()) {
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

            serverPlayer.openMenu(new RockKnappingMenuProvider(), buf -> {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, mainHand);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, offHand);
            });
        }
    }
}
