package net.stirdrem.overgeared.platform;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.stirdrem.overgeared.Overgeared;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Exposes the mod's (common) container block entities - smithing anvils and furnaces - as a Forge item-handler
 * capability, so pipes and other capability-based mods keep working. The per-side rules come from each block
 * entity's {@code WorldlyContainer} implementation; a null side gets the full inventory.
 */
@Mod.EventBusSubscriber(modid = Overgeared.MOD_ID)
public final class ForgeInventoryCapabilities {
    private static final ResourceLocation ID = Overgeared.id("inventory");

    private ForgeInventoryCapabilities() {
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        // Covers the smithing anvils and the alloy/nether alloy/casting furnaces
        if (event.getObject() instanceof WorldlyContainer container && isOurs(event.getObject())) {
            Provider provider = new Provider(container);
            event.addCapability(ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    private static boolean isOurs(BlockEntity blockEntity) {
        ResourceLocation type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        return type != null && Overgeared.MOD_ID.equals(type.getNamespace());
    }

    private static final class Provider implements ICapabilityProvider {
        private final LazyOptional<IItemHandler> unsided;
        private final Map<Direction, LazyOptional<IItemHandler>> sided = new EnumMap<>(Direction.class);

        Provider(WorldlyContainer container) {
            this.unsided = LazyOptional.of(() -> new InvWrapper(container));
            for (Direction side : Direction.values()) {
                sided.put(side, LazyOptional.of(() -> new SidedInvWrapper(container, side)));
            }
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap != ForgeCapabilities.ITEM_HANDLER) return LazyOptional.empty();
            return (side == null ? unsided : sided.get(side)).cast();
        }

        void invalidate() {
            unsided.invalidate();
            sided.values().forEach(LazyOptional::invalidate);
        }
    }
}
