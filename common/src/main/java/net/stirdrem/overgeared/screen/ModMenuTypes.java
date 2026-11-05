package net.stirdrem.overgeared.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.platform.services.IPlatformHelper;
import net.stirdrem.overgeared.registry.RegistrationProvider;
import net.stirdrem.overgeared.registry.RegistryEntry;

/**
 * Block-backed menus read the block position written by {@code IPlatformHelper#openMenu} and look the block entity
 * up on the client; the others are plain vanilla menu types.
 */
public class ModMenuTypes {
    public static final RegistrationProvider<MenuType<?>> MENUS = RegistrationProvider.get(Registries.MENU);

    public static final RegistryEntry<MenuType<SteelSmithingAnvilMenu>> STEEL_SMITHING_ANVIL_MENU =
            withBlockPos("smithing_anvil_menu", SteelSmithingAnvilMenu::new);
    public static final RegistryEntry<MenuType<TierASmithingAnvilMenu>> TIER_A_SMITHING_ANVIL_MENU =
            withBlockPos("tier_a_smithing_anvil_menu", TierASmithingAnvilMenu::new);
    public static final RegistryEntry<MenuType<TierBSmithingAnvilMenu>> TIER_B_SMITHING_ANVIL_MENU =
            withBlockPos("tier_b_smithing_anvil_menu", TierBSmithingAnvilMenu::new);
    public static final RegistryEntry<MenuType<StoneSmithingAnvilMenu>> STONE_SMITHING_ANVIL_MENU =
            withBlockPos("stone_smithing_anvil_menu", StoneSmithingAnvilMenu::new);
    public static final RegistryEntry<MenuType<AlloySmelterMenu>> ALLOY_SMELTER_MENU =
            withBlockPos("alloy_smelter_menu", AlloySmelterMenu::new);
    public static final RegistryEntry<MenuType<NetherAlloySmelterMenu>> NETHER_ALLOY_SMELTER_MENU =
            withBlockPos("nether_alloy_smelter_menu", NetherAlloySmelterMenu::new);
    public static final RegistryEntry<MenuType<CastFurnaceMenu>> CAST_FURNACE =
            withBlockPos("casting_furnace", CastFurnaceMenu::new);

    public static final RegistryEntry<MenuType<RockKnappingMenu>> ROCK_KNAPPING_MENU =
            simple("rock_knapping_menu", RockKnappingMenu::new);
    public static final RegistryEntry<MenuType<BlueprintWorkbenchMenu>> BLUEPRINT_WORKBENCH_MENU =
            simple("blueprint_workbench", BlueprintWorkbenchMenu::new);
    public static final RegistryEntry<MenuType<FletchingStationMenu>> FLETCHING_STATION_MENU =
            simple("fletching_station", FletchingStationMenu::new);

    private static <T extends AbstractContainerMenu> RegistryEntry<MenuType<T>> withBlockPos(
            String name, IPlatformHelper.MenuFactory<T> factory) {
        return MENUS.register(name, () -> Services.PLATFORM.createMenuType(factory));
    }

    private static <T extends AbstractContainerMenu> RegistryEntry<MenuType<T>> simple(
            String name, IPlatformHelper.SimpleMenuFactory<T> factory) {
        return MENUS.register(name, () -> Services.PLATFORM.createSimpleMenuType(factory));
    }

    /** Class-loads this registry; call from the loader entrypoint. */
    public static void init() {
    }
}
