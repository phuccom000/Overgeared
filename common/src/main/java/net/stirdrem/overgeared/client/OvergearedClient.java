package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.compat.ModCompat;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.armor.custom.ArmorModelHelper;
import net.stirdrem.overgeared.item.armor.model.CopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CopperLeggings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client setup shared by both loaders.
 */
public final class OvergearedClient {
    private OvergearedClient() {
    }

    public static void init() {
        // Initialize client-side mod compatibility
        ModCompat.initClient();
    }

    /** Must run on the main thread. */
    public static void registerItemProperties() {
        // Register item properties for arrow potion type variants
        // For LINGERING_ARROW, always show lingering variant when potion is present
        ItemProperties.register(ModItems.LINGERING_ARROW.get(), OvergearedMod.loc("potion_type"),
                (stack, level, entity, seed) -> {
                    PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    if (potionContents.potion().isPresent() || potionContents.hasEffects()) {
                        return 2.0f; // Lingering arrows always use lingering model
                    }
                    return 0.0f; // Base model
                });

        // For upgrade arrows, check LINGERING_STATUS to differentiate tipped vs lingering
        registerArrowProperties(ModItems.IRON_UPGRADE_ARROW.get());
        registerArrowProperties(ModItems.STEEL_UPGRADE_ARROW.get());
        registerArrowProperties(ModItems.DIAMOND_UPGRADE_ARROW.get());
    }

    private static void registerArrowProperties(Item item) {
        ItemProperties.register(item, OvergearedMod.loc("potion_type"),
                (stack, level, entity, seed) -> {
                    PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    boolean isLingering = stack.getOrDefault(ModComponents.LINGERING_STATUS.get(), false);

                    if (isLingering && (potionContents.potion().isPresent() || potionContents.hasEffects())) {
                        return 2.0f; // Lingering potion model
                    } else if (potionContents.potion().isPresent() || potionContents.hasEffects()) {
                        return 1.0f; // Tipped potion model
                    }
                    return 0.0f; // Base model
                });
    }

    /** Armor model drawn for the copper helmet. */
    public static HumanoidModel<?> copperHelmetModel() {
        return ArmorModelHelper.withPart("head", new CopperHelmet<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperHelmet.LAYER_LOCATION)).Head);
    }

    /** Armor model drawn for the copper leggings. */
    public static HumanoidModel<?> copperLeggingsModel() {
        CopperLeggings<?> leggings = new CopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperLeggings.LAYER_LOCATION));
        Map<String, ModelPart> parts = new HashMap<>();
        parts.put("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("body", leggings.Body);
        parts.put("right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("right_leg", leggings.RightLeg);
        parts.put("left_leg", leggings.LeftLeg);

        return new HumanoidModel<>(new ModelPart(Collections.emptyList(), parts));
    }
}
