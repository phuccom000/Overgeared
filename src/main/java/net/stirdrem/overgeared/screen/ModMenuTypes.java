package net.stirdrem.overgeared.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AlloySmelterBlockEntity;
import net.stirdrem.overgeared.block.entity.CastFurnaceBlockEntity;
import net.stirdrem.overgeared.block.entity.NetherAlloySmelterBlockEntity;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;

/**
 * The anvil and furnace-style screen handlers are registered "extended" (Fabric's equivalent of
 * Forge's IContainerFactory) since the client needs the BlockPos to look up the real block
 * entity - its inventory/progress data already syncs via the block entity's own NBT sync
 * (Overgeared.java relies on the default toInitialChunkDataNbt/toUpdatePacket), so the client
 * screen handler just reads straight from it. The furnace-style handlers still get a fresh
 * ArrayPropertyDelegate client-side rather than the block entity's own delegate, matching
 * vanilla's furnace convention, so their live progress bars sync via the screen handler's own
 * per-tick property sync instead of only updating whenever the block entity's NBT syncs.
 */
public class ModMenuTypes {

    public static final ScreenHandlerType<SteelSmithingAnvilScreenHandler> STEEL_SMITHING_ANVIL_MENU =
            ScreenHandlerRegistry.registerExtended(
                    Overgeared.id("smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        SteelSmithingAnvilBlockEntity be = (SteelSmithingAnvilBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                        return new SteelSmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
                    });

    public static final ScreenHandlerType<TierASmithingAnvilScreenHandler> TIER_A_SMITHING_ANVIL_MENU =
            ScreenHandlerRegistry.registerExtended(
                    Overgeared.id("tier_a_smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        TierASmithingAnvilBlockEntity be = (TierASmithingAnvilBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                        return new TierASmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
                    });

    public static final ScreenHandlerType<TierBSmithingAnvilScreenHandler> TIER_B_SMITHING_ANVIL_MENU =
            ScreenHandlerRegistry.registerExtended(
                    Overgeared.id("tier_b_smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        TierBSmithingAnvilBlockEntity be = (TierBSmithingAnvilBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                        return new TierBSmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
                    });

    public static final ScreenHandlerType<StoneSmithingAnvilScreenHandler> STONE_SMITHING_ANVIL_MENU =
            ScreenHandlerRegistry.registerExtended(
                    Overgeared.id("stone_smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        StoneSmithingAnvilBlockEntity be = (StoneSmithingAnvilBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                        return new StoneSmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
                    });

    public static final ScreenHandlerType<AlloySmelterScreenHandler> ALLOY_SMELTER_MENU =
            ScreenHandlerRegistry.registerExtended(
                    Overgeared.id("alloy_smelter_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        AlloySmelterBlockEntity be = (AlloySmelterBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                        return new AlloySmelterScreenHandler(syncId, inv, be, new ArrayPropertyDelegate(4));
                    });

    public static final ScreenHandlerType<NetherAlloySmelterScreenHandler> NETHER_ALLOY_SMELTER_MENU =
            ScreenHandlerRegistry.registerExtended(
                    Overgeared.id("nether_alloy_smelter_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        NetherAlloySmelterBlockEntity be = (NetherAlloySmelterBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                        return new NetherAlloySmelterScreenHandler(syncId, inv, be, new ArrayPropertyDelegate(4));
                    });

    public static final ScreenHandlerType<CastFurnaceScreenHandler> CAST_FURNACE =
            ScreenHandlerRegistry.registerExtended(
                    Overgeared.id("casting_furnace"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        CastFurnaceBlockEntity be = (CastFurnaceBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                        return new CastFurnaceScreenHandler(syncId, inv, be, new ArrayPropertyDelegate(4));
                    });

    public static final ScreenHandlerType<RockKnappingScreenHandler> ROCK_KNAPPING_MENU =
            ScreenHandlerRegistry.registerSimple(
                    Overgeared.id("rock_knapping_menu"), (syncId, inv) ->
                            new RockKnappingScreenHandler(syncId, inv, MinecraftClient.getInstance().world.getRecipeManager()));

    public static final ScreenHandlerType<BlueprintWorkbenchScreenHandler> BLUEPRINT_WORKBENCH_MENU =
            ScreenHandlerRegistry.registerSimple(
                    Overgeared.id("blueprint_workbench"), BlueprintWorkbenchScreenHandler::new);

    public static final ScreenHandlerType<FletchingStationScreenHandler> FLETCHING_STATION_MENU =
            ScreenHandlerRegistry.registerSimple(
                    Overgeared.id("fletching_station"), FletchingStationScreenHandler::new);

    public static void register() {
    }
}
