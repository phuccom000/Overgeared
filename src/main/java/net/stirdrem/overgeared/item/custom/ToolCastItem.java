package net.stirdrem.overgeared.item.custom;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ConfigHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ToolCastItem extends Item {
    private final boolean allowMaterialInsert;
    private final boolean haveDurability;

    public ToolCastItem(boolean allowMaterialInsert, boolean haveDurability, Settings settings) {
        // Vanilla's Item#getMaxDamage() is final (derived from Settings at construction),
        // unlike Forge's per-stack getMaxDamage(ItemStack) override, so the configured
        // durability is baked in here instead of read dynamically on every call.
        super(haveDurability ? settings.maxDamage(ServerConfig.FIRED_CAST_DURABILITY.get()) : settings);
        this.allowMaterialInsert = allowMaterialInsert;
        this.haveDurability = haveDurability;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            return calculateAndReturnMaterials(stack, player, hand);
        }

        return super.use(world, player, hand);
    }

    private TypedActionResult<ItemStack> calculateAndReturnMaterials(ItemStack castStack, PlayerEntity player, Hand hand) {
        NbtCompound tag = castStack.getNbt();
        if (tag == null) {
            return TypedActionResult.fail(castStack);
        }

        if (tag.contains("Output", NbtElement.COMPOUND_TYPE)) {
            ItemStack output = ItemStack.fromNbt(tag.getCompound("Output"));

            if (!output.isEmpty()) {
                if (!player.getInventory().insertStack(output.copy())) {
                    player.dropItem(output.copy(), false);
                }

                tag.remove("Output");
                tag.remove("Materials");
                tag.remove("input");
                tag.remove("Heated");
                tag.putInt("Amount", 0);

                player.getWorld().playSound(
                        null,
                        player.getBlockPos(),
                        SoundEvents.ENTITY_ITEM_PICKUP,
                        SoundCategory.PLAYERS,
                        0.8F,
                        1.2F
                );
                return TypedActionResult.success(castStack, player.getWorld().isClient());
            }
        }

        List<ItemStack> inputItems = getInputItemsFromCast(castStack);
        if (inputItems.isEmpty()) {
            player.sendMessage(Text.translatable("message.overgeared.cast_empty"), true);
            return TypedActionResult.fail(castStack);
        }

        for (ItemStack inputItem : inputItems) {
            if (!player.getInventory().insertStack(inputItem.copy())) {
                player.dropItem(inputItem.copy(), false);
            }
        }

        tag.put("Materials", new NbtCompound());
        tag.putInt("Amount", 0);
        tag.remove("input");

        return TypedActionResult.success(castStack, player.getWorld().isClient());
    }

    @Override
    public boolean onStackClicked(ItemStack castStack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (!allowMaterialInsert) return false;
        if (clickType != ClickType.RIGHT) return false;
        if (!slot.canTakeItems(player)) return false;

        ItemStack slotStack = slot.getStack();
        if (slotStack.isEmpty()) return false;

        if (insertMaterial(castStack, slotStack, player)) {
            slotStack.decrement(1);
            slot.markDirty();
            return true;
        }

        return false;
    }

    @Override
    public boolean onClicked(ItemStack castStack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (!allowMaterialInsert) return false;
        if (clickType != ClickType.RIGHT) return false;

        if (insertMaterial(castStack, otherStack, player)) {
            otherStack.decrement(1);
            return true;
        }

        return false;
    }

    private boolean insertMaterial(ItemStack cast, ItemStack material, PlayerEntity player) {
        if (cast.hasNbt() && cast.getNbt().contains("Output")) {
            return false;
        }
        if (material.isEmpty()) return false;

        NbtCompound tag = cast.getOrCreateNbt();
        NbtList list = tag.getList("input", NbtElement.COMPOUND_TYPE);

        if (!ConfigHelper.isValidMaterial(material)) {
            player.sendMessage(Text.translatable("message.overgeared.invalid_material"), true);
            return false;
        }

        int value = ConfigHelper.getMaterialValue(material);
        if (value <= 0) return false;

        int amount = tag.getInt("Amount");
        int maxAmount = tag.contains("MaxAmount") ? tag.getInt("MaxAmount") : Integer.MAX_VALUE;

        if (amount + value > maxAmount) {
            return false;
        }

        String mat = ConfigHelper.getMaterialForItem(material);
        NbtCompound mats = tag.contains("Materials") ? tag.getCompound("Materials") : new NbtCompound();

        int prev = mats.getInt(mat);
        mats.putInt(mat, prev + value);
        tag.put("Materials", mats);

        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            ItemStack entryStack = ItemStack.fromNbt(entry);

            if (isSameItemSameNbt(entryStack, material)) {
                entryStack.increment(1);
                list.set(i, entryStack.writeNbt(entry));

                tag.put("input", list);
                tag.putInt("Amount", amount + value);
                playInsertSound(player);
                return true;
            }
        }

        ItemStack stored = material.copy();
        stored.setCount(1);
        list.add(stored.writeNbt(new NbtCompound()));

        tag.put("input", list);
        tag.putInt("Amount", amount + value);

        playInsertSound(player);
        return true;
    }

    private static boolean isSameItemSameNbt(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && Objects.equals(a.getNbt(), b.getNbt());
    }

    private void playInsertSound(PlayerEntity player) {
        player.getWorld().playSound(
                player,
                player.getBlockPos(),
                SoundEvents.ITEM_BUNDLE_INSERT,
                SoundCategory.PLAYERS,
                0.7F, 1.1F
        );
    }

    private List<ItemStack> getInputItemsFromCast(ItemStack cast) {
        List<ItemStack> items = new ArrayList<>();
        NbtCompound tag = cast.getNbt();

        if (tag != null && tag.contains("input", NbtElement.LIST_TYPE)) {
            NbtList inputList = tag.getList("input", NbtElement.COMPOUND_TYPE);

            for (NbtElement inputTag : inputList) {
                if (inputTag instanceof NbtCompound compound) {
                    ItemStack item = ItemStack.fromNbt(compound);
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }

    @Override
    public boolean isDamageable() {
        return haveDurability;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        NbtCompound tag = stack.getNbt();
        if (tag == null) return;

        if (tag.contains("Quality", NbtElement.STRING_TYPE)) {
            String quality = tag.getString("Quality");
            Formatting color = BlueprintQuality.getColor(quality);
            if (!quality.equals("NONE"))
                tooltip.add(
                        Text.translatable("tooltip.overgeared.tool_cast.quality")
                                .append(" ")
                                .append(
                                        Text.translatable("quality.overgeared." + quality.toLowerCase(Locale.ROOT))
                                                .formatted(color)
                                )
                                .formatted(Formatting.GRAY)
                );
        }

        if (tag.contains("ToolType", NbtElement.STRING_TYPE)) {
            String toolType = tag.getString("ToolType");
            tooltip.add(
                    Text.translatable("tooltip.overgeared.tool_cast.type")
                            .append(" ")
                            .append(Text.translatable("tooltype.overgeared." + toolType.toLowerCase(Locale.ROOT)).formatted(Formatting.BLUE))
                            .formatted(Formatting.GRAY)
            );
        }

        if (tag.contains("Materials", NbtElement.COMPOUND_TYPE)) {
            NbtCompound materials = tag.getCompound("Materials");
            if (!materials.isEmpty()) {
                tooltip.add(
                        Text.translatable("tooltip.overgeared.tool_cast.materials")
                                .formatted(Formatting.GRAY)
                );

                for (String key : materials.getKeys()) {
                    int amount = materials.getInt(key);

                    Text display = Text.translatable("material.overgeared." + key.toLowerCase(Locale.ROOT));
                    if (display.getString().equals("material.overgeared." + key.toLowerCase(Locale.ROOT))) {
                        display = Text.literal(key);
                    }

                    tooltip.add(
                            Text.literal("  • ").append(display)
                                    .append(Text.literal(": " + amount))
                                    .formatted(Formatting.WHITE)
                    );
                }
            }
        }

        if (tag.contains("Amount")) {
            int raw = tag.getInt("Amount");
            double amt = raw / 9.0;

            int maxRaw = tag.contains("MaxAmount") ? tag.getInt("MaxAmount") : raw;
            double maxAmt = maxRaw / 9.0;

            tooltip.add(
                    Text.translatable("tooltip.overgeared.tool_cast.amount")
                            .append(" ")
                            .append(
                                    Text.literal(String.format("%.2f", amt))
                                            .formatted(Formatting.YELLOW)
                            )
                            .append(" / ")
                            .append(
                                    Text.literal(String.format("%.2f", maxAmt))
                                            .formatted(Formatting.WHITE)
                            )
                            .formatted(Formatting.GRAY)
            );
            if (amt / maxAmt != 1)
                tooltip.add(
                        Text.translatable("tooltip.overgeared.add_materials")
                                .formatted(Formatting.DARK_GRAY, Formatting.ITALIC)
                );
        }
        if (tag.contains("Output", NbtElement.COMPOUND_TYPE)) {
            ItemStack output = ItemStack.fromNbt(tag.getCompound("Output"));

            tooltip.add(
                    Text.translatable("tooltip.overgeared.tool_cast.contains")
                            .formatted(Formatting.GRAY)
            );

            tooltip.add(
                    Text.literal("  • ")
                            .append(output.getName())
                            .formatted(Formatting.GOLD)
            );
        }
        if ((tag.contains("Materials", NbtElement.COMPOUND_TYPE) && !tag.getCompound("Materials").isEmpty()) ||
                (tag.contains("input", NbtElement.LIST_TYPE) && !tag.getList("input", NbtElement.COMPOUND_TYPE).isEmpty()) ||
                tag.contains("Output", NbtElement.COMPOUND_TYPE)) {
            tooltip.add(
                    Text.translatable("tooltip.overgeared.cast_right_click")
                            .formatted(Formatting.DARK_GRAY, Formatting.ITALIC)
            );
        }
    }
}
