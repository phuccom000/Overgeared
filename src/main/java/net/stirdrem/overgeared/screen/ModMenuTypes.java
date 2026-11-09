package net.stirdrem.overgeared.screen;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.block.entity.AlloySmelterBlockEntity;
import net.stirdrem.overgeared.block.entity.CastFurnaceBlockEntity;
import net.stirdrem.overgeared.block.entity.NetherAlloySmelterBlockEntity;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;

/**
 * The anvil and furnace-style menus are registered as Fabric ExtendedMenuTypes (StreamCodec-based
 * opening data) since the client needs the BlockPos to look up the real block entity - its
 * inventory/progress data already syncs via the block entity's own update packet, so the client
 * menu just reads straight from it. The furnace-style menus still get a fresh SimpleContainerData
 * client-side rather than the block entity's own data, matching vanilla's furnace convention, so
 * their live progress bars sync via the menu's own data-slot sync.
 */
public class ModMenuTypes {

    public static final MenuType<SteelSmithingAnvilScreenHandler> STEEL_SMITHING_ANVIL_MENU =
            registerExtended("smithing_anvil_menu", (syncId, inv, pos) -> {
                SteelSmithingAnvilBlockEntity be = (SteelSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(pos);
                return new SteelSmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
            });

    public static final MenuType<TierASmithingAnvilScreenHandler> TIER_A_SMITHING_ANVIL_MENU =
            registerExtended("tier_a_smithing_anvil_menu", (syncId, inv, pos) -> {
                TierASmithingAnvilBlockEntity be = (TierASmithingAnvilBlockEntity) inv.player.level().getBlockEntity(pos);
                return new TierASmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
            });

    public static final MenuType<TierBSmithingAnvilScreenHandler> TIER_B_SMITHING_ANVIL_MENU =
            registerExtended("tier_b_smithing_anvil_menu", (syncId, inv, pos) -> {
                TierBSmithingAnvilBlockEntity be = (TierBSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(pos);
                return new TierBSmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
            });

    public static final MenuType<StoneSmithingAnvilScreenHandler> STONE_SMITHING_ANVIL_MENU =
            registerExtended("stone_smithing_anvil_menu", (syncId, inv, pos) -> {
                StoneSmithingAnvilBlockEntity be = (StoneSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(pos);
                return new StoneSmithingAnvilScreenHandler(syncId, inv, be, be.getContainerData());
            });

    public static final MenuType<AlloySmelterScreenHandler> ALLOY_SMELTER_MENU =
            registerExtended("alloy_smelter_menu", (syncId, inv, pos) -> {
                AlloySmelterBlockEntity be = (AlloySmelterBlockEntity) inv.player.level().getBlockEntity(pos);
                return new AlloySmelterScreenHandler(syncId, inv, be, new SimpleContainerData(4));
            });

    public static final MenuType<NetherAlloySmelterScreenHandler> NETHER_ALLOY_SMELTER_MENU =
            registerExtended("nether_alloy_smelter_menu", (syncId, inv, pos) -> {
                NetherAlloySmelterBlockEntity be = (NetherAlloySmelterBlockEntity) inv.player.level().getBlockEntity(pos);
                return new NetherAlloySmelterScreenHandler(syncId, inv, be, new SimpleContainerData(4));
            });

    public static final MenuType<CastFurnaceScreenHandler> CAST_FURNACE =
            registerExtended("casting_furnace", (syncId, inv, pos) -> {
                CastFurnaceBlockEntity be = (CastFurnaceBlockEntity) inv.player.level().getBlockEntity(pos);
                return new CastFurnaceScreenHandler(syncId, inv, be, new SimpleContainerData(4));
            });

    public static final MenuType<RockKnappingScreenHandler> ROCK_KNAPPING_MENU =
            registerSimple("rock_knapping_menu", RockKnappingScreenHandler::new);

    public static final MenuType<BlueprintWorkbenchScreenHandler> BLUEPRINT_WORKBENCH_MENU =
            registerSimple("blueprint_workbench", BlueprintWorkbenchScreenHandler::new);

    public static final MenuType<FletchingStationScreenHandler> FLETCHING_STATION_MENU =
            registerSimple("fletching_station", FletchingStationScreenHandler::new);

    private static <T extends AbstractContainerMenu> MenuType<T> registerExtended(
            String name, ExtendedMenuType.ExtendedFactory<T, BlockPos> factory) {
        return Registry.register(BuiltInRegistries.MENU, Overgeared.id(name),
                new ExtendedMenuType<>(factory, BlockPos.STREAM_CODEC));
    }

    private static <T extends AbstractContainerMenu> MenuType<T> registerSimple(
            String name, MenuType.MenuSupplier<T> factory) {
        return Registry.register(BuiltInRegistries.MENU, Overgeared.id(name),
                new MenuType<>(factory, FeatureFlags.VANILLA_SET));
    }

    public static void register() {
    }
}
