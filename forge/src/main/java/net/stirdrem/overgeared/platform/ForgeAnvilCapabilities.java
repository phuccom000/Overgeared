package net.stirdrem.overgeared.platform;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Exposes the (common) smithing anvil block entities as a Forge item-handler capability, so pipes and other
 * capability-based mods keep working. The rules come from the block entity's {@code WorldlyContainer}
 * implementation: any slot from the sides/top, output-only extraction from below, full inventory for a null side.
 */
@Mod.EventBusSubscriber(modid = Overgeared.MOD_ID)
public final class ForgeAnvilCapabilities {
    private static final ResourceLocation ID = Overgeared.id("anvil_inventory");

    private ForgeAnvilCapabilities() {
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof AbstractSmithingAnvilBlockEntity anvil) {
            Provider provider = new Provider(anvil);
            event.addCapability(ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    private static final class Provider implements ICapabilityProvider {
        private final LazyOptional<IItemHandler> unsided;
        private final Map<Direction, LazyOptional<IItemHandler>> sided = new EnumMap<>(Direction.class);

        Provider(AbstractSmithingAnvilBlockEntity anvil) {
            this.unsided = LazyOptional.of(() -> new InvWrapper(anvil));
            for (Direction side : Direction.values()) {
                sided.put(side, LazyOptional.of(() -> new SidedInvWrapper(anvil, side)));
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
