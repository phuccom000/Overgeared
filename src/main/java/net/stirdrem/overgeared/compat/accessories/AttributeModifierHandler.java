package net.stirdrem.overgeared.compat.accessories;

import com.google.common.collect.Multimap;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.attributes.AttributeModificationData;
import io.wispforest.accessories.api.events.AdjustAttributeModifierCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.utils.AttributeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.Collection;
import java.util.List;

// Register to modify attributes dynamically
public class AttributeModifierHandler implements AdjustAttributeModifierCallback {

    public static void register() {
        AdjustAttributeModifierCallback.EVENT.register(new AttributeModifierHandler());
    }

    @Override
    public void adjustAttributes(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        Item item = stack.getItem();
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");

            //if (isWeapon(item)) {
            applyWeaponAttributes(builder, quality);

            //if (isArmor(item))
            applyArmorAttributes(builder, quality);

        }
    }

    // Apply weapon-type attribute modifications
    private void applyWeaponAttributes(AccessoryAttributeBuilder builder, String quality) {
        double damageBonus = getDamageBonusForQuality(quality);
        double speedBonus = getSpeedBonusForQuality(quality);

        modifyAttribute(builder, Attributes.ATTACK_DAMAGE, damageBonus);
        modifyAttribute(builder, Attributes.ATTACK_SPEED, speedBonus);
    }

    // Apply armor-type attribute modifications
    private void applyArmorAttributes(AccessoryAttributeBuilder builder, String quality) {
        double armorBonus = getArmorBonusForQuality(quality);

        modifyAttribute(builder, Attributes.ARMOR, armorBonus);
        modifyAttribute(builder, Attributes.ARMOR_TOUGHNESS, armorBonus);
        modifyAttribute(builder, Attributes.ARMOR_TOUGHNESS, armorBonus);
    }


    // Main modifier method - exactly like your pattern
    private void modifyAttribute(AccessoryAttributeBuilder builder, Attribute attribute, double bonus) {
        if (bonus == 0) return;

        // Get all existing modifiers for this attribute
        Multimap<Attribute, AttributeModifier> originalModifiers = builder.getAttributeModifiers(false);

        if (!originalModifiers.containsKey(attribute)) return;

        List<AttributeModifier> modifiers = List.copyOf(originalModifiers.get(attribute));

        for (AttributeModifier modifier : modifiers) {
            if (modifier.getAmount() == 0) continue;

            // Get the ResourceLocation for this modifier
            ResourceLocation location = AttributeUtils.getLocation(modifier.getName());

            // Check if it's an exclusive or stackable modifier
            AttributeModificationData exclusiveData = builder.getExclusive(attribute, location);

            if (exclusiveData != null) {
                // It's an exclusive modifier - remove and replace
                builder.removeExclusive(attribute, location);

                // Create modified attribute
                AttributeModifier modified = createModifiedAttribute(modifier, bonus);

                // Add it back as exclusive
                builder.addExclusive(attribute, modified);
            } else {
                // It's a stackable modifier - get all stackable modifiers with this location
                Collection<AttributeModificationData> stackableData = builder.getStacks(attribute, location);

                if (!stackableData.isEmpty()) {
                    // Remove all stackable modifiers with this location
                    builder.removeStacks(attribute, location);

                    // Add modified versions
                    for (AttributeModificationData data : stackableData) {
                        AttributeModifier originalStackMod = data.modifier();
                        AttributeModifier modifiedStack = createModifiedAttribute(originalStackMod, bonus);

                        // Add as stackable with same location
                        builder.addStackable(attribute, location, modifiedStack.getAmount(), modifiedStack.getOperation());
                    }
                }
            }
        }
    }

    // Create modified attribute - exactly like your method
    private static AttributeModifier createModifiedAttribute(AttributeModifier original, double bonus) {
        return new AttributeModifier(
                original.getId(),
                "Overgeared",  // Your custom name
                original.getAmount() + bonus,  // Add bonus instead of multiply
                original.getOperation()
        );
    }

    // Create a unique ResourceLocation for the modifier
    private ResourceLocation createModifiedResourceLocation(AttributeModifier modifier) {
        // Create a deterministic ID based on the original modifier
        String baseName = modifier.getName().toLowerCase()
                .replaceAll("[^a-z0-9/._-]", "")
                .replace(" ", "_");

        return ResourceLocation.fromNamespaceAndPath(
                OvergearedMod.MOD_ID,
                "overgeared_" + baseName + "_" + Math.abs(modifier.getId().hashCode())
        );
    }

    // Check if item is a weapon - exactly like your method
    private static boolean isWeapon(Item item) {
        return item instanceof TieredItem ||
                item instanceof ProjectileWeaponItem;
    }

    // Check if item is armor - exactly like your method
    private static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
    }

    private static double getDamageBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master" -> ServerConfig.MASTER_WEAPON_DAMAGE.get();
            case "perfect" -> ServerConfig.PERFECT_WEAPON_DAMAGE.get();
            case "expert" -> ServerConfig.EXPERT_WEAPON_DAMAGE.get();
            case "well" -> ServerConfig.WELL_WEAPON_DAMAGE.get();
            case "poor" -> ServerConfig.POOR_WEAPON_DAMAGE.get();
            default -> 0.0;
        };
    }

    private static double getSpeedBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master" -> ServerConfig.MASTER_WEAPON_SPEED.get();
            case "perfect" -> ServerConfig.PERFECT_WEAPON_SPEED.get();
            case "expert" -> ServerConfig.EXPERT_WEAPON_SPEED.get();
            case "well" -> ServerConfig.WELL_WEAPON_SPEED.get();
            case "poor" -> ServerConfig.POOR_WEAPON_SPEED.get();
            default -> 0.0;
        };
    }

    private static double getArmorBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master" -> ServerConfig.MASTER_ARMOR_BONUS.get();
            case "perfect" -> ServerConfig.PERFECT_ARMOR_BONUS.get();
            case "expert" -> ServerConfig.EXPERT_ARMOR_BONUS.get();
            case "well" -> ServerConfig.WELL_ARMOR_BONUS.get();
            case "poor" -> ServerConfig.POOR_ARMOR_BONUS.get();
            default -> 0.0;
        };
    }
}