package net.stirdrem.overgeared.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.*;

/**
 * The anvil and furnace-style screen handlers are registered "extended"
 * (Fabric's equivalent of
 * Forge's IContainerFactory) since the client needs the BlockPos to look up the
 * real block
 * entity - its inventory/progress data already syncs via the block entity's own
 * NBT sync
 * (Overgeared.java relies on the default toInitialChunkDataNbt/toUpdatePacket),
 * so the client
 * screen handler just reads straight from it. The furnace-style handlers still
 * get a fresh
 * ArrayPropertyDelegate client-side rather than the block entity's own
 * delegate, matching
 * vanilla's furnace convention, so their live progress bars sync via the screen
 * handler's own
 * per-tick property sync instead of only updating whenever the block entity's
 * NBT syncs.
 */
public class ModMenuTypes {

    public static final MenuType<SteelSmithingAnvilMenu> STEEL_SMITHING_ANVIL_MENU = ScreenHandlerRegistry
            .registerExtended(
                    Overgeared.id("smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        SteelSmithingAnvilBlockEntity be = (SteelSmithingAnvilBlockEntity) Minecraft
                                .getInstance().level.getBlockEntity(pos);
                        return new SteelSmithingAnvilMenu(syncId, inv, be,
                                be.getContainerData());
                    });

    public static final MenuType<TierASmithingAnvilMenu> TIER_A_SMITHING_ANVIL_MENU = ScreenHandlerRegistry
            .registerExtended(
                    Overgeared.id("tier_a_smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        TierASmithingAnvilBlockEntity be = (TierASmithingAnvilBlockEntity) Minecraft
                                .getInstance().level.getBlockEntity(pos);
                        return new TierASmithingAnvilMenu(syncId, inv, be,
                                be.getContainerData());
                    });

    public static final MenuType<TierBSmithingAnvilMenu> TIER_B_SMITHING_ANVIL_MENU = ScreenHandlerRegistry
            .registerExtended(
                    Overgeared.id("tier_b_smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        TierBSmithingAnvilBlockEntity be = (TierBSmithingAnvilBlockEntity) Minecraft
                                .getInstance().level.getBlockEntity(pos);
                        return new TierBSmithingAnvilMenu(syncId, inv, be,
                                be.getContainerData());
                    });

    public static final MenuType<StoneSmithingAnvilMenu> STONE_SMITHING_ANVIL_MENU = ScreenHandlerRegistry
            .registerExtended(
                    Overgeared.id("stone_smithing_anvil_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        StoneSmithingAnvilBlockEntity be = (StoneSmithingAnvilBlockEntity) Minecraft
                                .getInstance().level.getBlockEntity(pos);
                        return new StoneSmithingAnvilMenu(syncId, inv, be,
                                be.getContainerData());
                    });

    public static final MenuType<AlloySmelterMenu> ALLOY_SMELTER_MENU = ScreenHandlerRegistry
            .registerExtended(
                    Overgeared.id("alloy_smelter_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        AlloySmelterBlockEntity be = (AlloySmelterBlockEntity) Minecraft
                                .getInstance().level.getBlockEntity(pos);
                        return new AlloySmelterMenu(syncId, inv, be,
                                new SimpleContainerData(4));
                    });

    public static final MenuType<NetherAlloySmelterMenu> NETHER_ALLOY_SMELTER_MENU = ScreenHandlerRegistry
            .registerExtended(
                    Overgeared.id("nether_alloy_smelter_menu"), (syncId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        NetherAlloySmelterBlockEntity be = (NetherAlloySmelterBlockEntity) Minecraft
                                .getInstance().level.getBlockEntity(pos);
                        return new NetherAlloySmelterMenu(syncId, inv, be,
                                new SimpleContainerData(4));
                    });

    public static final MenuType<CastFurnaceMenu> CAST_FURNACE = ScreenHandlerRegistry.registerExtended(
            Overgeared.id("casting_furnace"), (syncId, inv, buf) -> {
                BlockPos pos = buf.readBlockPos();
                CastFurnaceBlockEntity be = (CastFurnaceBlockEntity) Minecraft.getInstance().level
                        .getBlockEntity(pos);
                return new CastFurnaceMenu(syncId, inv, be, new SimpleContainerData(4));
            });

    public static final MenuType<RockKnappingMenu> ROCK_KNAPPING_MENU = ScreenHandlerRegistry
            .registerSimple(
                    Overgeared.id("rock_knapping_menu"),
                    (syncId, inv) -> new RockKnappingMenu(syncId, inv,
                            Minecraft.getInstance().level.getRecipeManager()));

    public static final MenuType<BlueprintWorkbenchMenu> BLUEPRINT_WORKBENCH_MENU = ScreenHandlerRegistry
            .registerSimple(
                    Overgeared.id("blueprint_workbench"),
                    BlueprintWorkbenchMenu::new);

    public static final MenuType<FletchingStationMenu> FLETCHING_STATION_MENU = ScreenHandlerRegistry
            .registerSimple(
                    Overgeared.id("fletching_station"), FletchingStationMenu::new);

    public static void register() {
    }
}
