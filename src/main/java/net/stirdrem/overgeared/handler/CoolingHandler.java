package net.stirdrem.overgeared.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ChunkHelper;
import net.stirdrem.overgeared.util.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static net.stirdrem.overgeared.util.ItemUtils.copyComponentsExceptHeated;
import static net.stirdrem.overgeared.util.ItemUtils.getCooledItem;

/**
 * Handles automatic cooling of heated items in containers and player inventories.
 * Items are detected by the HEATED_METALS tag or HEATED_COMPONENT data component.
 * Cooling timestamps are stored in the HEATED_TIME component.
 */
@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class CoolingHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        
        int checkRate = ServerConfig.COOLING_CHECK_RATE.get();
        if (level.getGameTime() % checkRate != 0) return;

        long now = level.getGameTime();
        int cooldown = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();
        List<String> blacklist = (List<String>) ServerConfig.COOLING_BLOCK_ENTITY_BLACKLIST.get();

        List<BlockPos> blockEntityPositions = ChunkHelper.getBlockEntityPositions(level)
                .stream()
                .filter(pos -> level.isAreaLoaded(pos, 1))
                .toList();
        
        if (blockEntityPositions.isEmpty()) return;

        for (BlockPos pos : blockEntityPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null || be.isRemoved() || !be.hasLevel()) continue;

            // Check if block entity type is blacklisted
            ResourceLocation beType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType());
            if (beType != null && blacklist.contains(beType.toString())) {
                continue;
            }

            // Prefer Container interface for direct slot access (bypasses slot restrictions)
            if (be instanceof Container container) {
                processContainer(container, now, cooldown, level, pos);
            } else {
                // Fallback to IItemHandler for non-Container block entities
                IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
                if (itemHandler != null && itemHandler.getSlots() > 0) {
                    processItemHandler(itemHandler, now, cooldown, level, pos);
                }
            }
        }
    }

    /**
     * Process a Container directly using setItem() - bypasses slot restrictions.
     */
    private static void processContainer(Container container, long now, int cooldown, ServerLevel level, BlockPos pos) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            boolean isHeated = stack.is(ModTags.Items.HEATED_METALS) 
                || Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT));
            if (!isHeated) continue;

            Long heatedAt = stack.get(ModComponents.HEATED_TIME);
            
            // Initialize timestamp if missing
            if (heatedAt == null) {
                stack.set(ModComponents.HEATED_TIME, now);
                container.setChanged();
                continue;
            }

            if (now - heatedAt >= cooldown) {
                Item cooledItem = getCooledItem(stack.getItem(), level);
                if (cooledItem != null) {
                    ItemStack cooled = new ItemStack(cooledItem, stack.getCount());
                    copyComponentsExceptHeated(stack, cooled);
                    container.setItem(i, cooled);
                    container.setChanged();
                }
            }
        }
    }

    /**
     * Fallback processing using IItemHandler for containers that don't implement Container.
     */
    private static void processItemHandler(IItemHandler itemHandler, long now, int cooldown, ServerLevel level, BlockPos pos) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            boolean isHeated = stack.is(ModTags.Items.HEATED_METALS) 
                || Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT));
            if (!isHeated) continue;

            Long heatedAt = stack.get(ModComponents.HEATED_TIME);
            
            // Initialize timestamp if missing
            if (heatedAt == null) {
                stack.set(ModComponents.HEATED_TIME, now);
                continue;
            }

            if (now - heatedAt >= cooldown) {
                Item cooledItem = getCooledItem(stack.getItem(), level);
                if (cooledItem != null) {
                    ItemStack cooled = new ItemStack(cooledItem, stack.getCount());
                    copyComponentsExceptHeated(stack, cooled);
                    
                    if (!itemHandler.isItemValid(i, cooled)) {
                        OvergearedMod.LOGGER.debug("[COOLING] Cannot cool item at {} - slot doesn't accept cooled item type", pos);
                        continue;
                    }
                    
                    itemHandler.extractItem(i, stack.getCount(), false);
                    ItemStack remainder = itemHandler.insertItem(i, cooled, false);
                    if (!remainder.isEmpty()) {
                        OvergearedMod.LOGGER.warn("[COOLING] Insert failed at {} - restoring original item", pos);
                        itemHandler.insertItem(i, stack, false);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        int checkRate = ServerConfig.COOLING_CHECK_RATE.get();
        if (player.level().getGameTime() % checkRate != 0) return;

        long now = player.level().getGameTime();
        int cooldown = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();
        int invCount = player.getInventory().getContainerSize();

        for (int i = 0; i < invCount; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            boolean isHeated = stack.is(ModTags.Items.HEATED_METALS) 
                || Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT));
            if (!isHeated) continue;

            Long heatedAt = stack.get(ModComponents.HEATED_TIME);
            
            // Initialize timestamp if missing
            if (heatedAt == null) {
                stack.set(ModComponents.HEATED_TIME, now);
                continue;
            }

            if (now - heatedAt >= cooldown) {
                Item cooledItem = getCooledItem(stack.getItem(), player.level());
                if (cooledItem != null) {
                    ItemStack cooled = new ItemStack(cooledItem, stack.getCount());
                    copyComponentsExceptHeated(stack, cooled);
                    player.getInventory().setItem(i, cooled);
                }
            }
        }
    }
}
