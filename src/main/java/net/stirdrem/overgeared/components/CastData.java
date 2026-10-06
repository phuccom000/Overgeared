package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tool cast item data (was the cast's "Quality", "ToolType", "Materials", "Amount", "MaxAmount",
 * "input", "Output" and "Heated" NBT keys). Immutable: use the with* methods and set the result
 * back on the stack.
 * <p>
 * Stacks are stored as {@link ItemStackTemplate}s because component values must be immutable
 * and implement equals.
 *
 * @param maxAmount 0 means "no limit" (the 1.20.1 code treated a missing MaxAmount as unlimited)
 */
public record CastData(
        String quality,
        String toolType,
        Map<String, Integer> materials,
        int amount,
        int maxAmount,
        List<ItemStackTemplate> input,
        Optional<ItemStackTemplate> output,
        boolean heated
) {
    public static final CastData EMPTY = new CastData("", "", Map.of(), 0, 0, List.of(), Optional.empty(), false);

    public static final Codec<CastData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("quality", "").forGetter(CastData::quality),
            Codec.STRING.optionalFieldOf("tool_type", "").forGetter(CastData::toolType),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("materials", Map.of()).forGetter(CastData::materials),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(CastData::amount),
            Codec.INT.optionalFieldOf("max_amount", 0).forGetter(CastData::maxAmount),
            ItemStackTemplate.CODEC.listOf().optionalFieldOf("input", List.of()).forGetter(CastData::input),
            ItemStackTemplate.CODEC.optionalFieldOf("output").forGetter(CastData::output),
            Codec.BOOL.optionalFieldOf("heated", false).forGetter(CastData::heated)
    ).apply(instance, CastData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Integer>> MATERIALS_STREAM_CODEC =
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);

    public static final StreamCodec<RegistryFriendlyByteBuf, CastData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CastData::quality,
            ByteBufCodecs.STRING_UTF8, CastData::toolType,
            MATERIALS_STREAM_CODEC, CastData::materials,
            ByteBufCodecs.VAR_INT, CastData::amount,
            ByteBufCodecs.VAR_INT, CastData::maxAmount,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), CastData::input,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), CastData::output,
            ByteBufCodecs.BOOL, CastData::heated,
            CastData::new);

    public CastData {
        materials = Map.copyOf(materials);
        input = List.copyOf(input);
    }

    public boolean hasOutput() {
        return output.isPresent();
    }

    public ItemStack outputStack() {
        return output.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    public List<ItemStack> inputStacks() {
        List<ItemStack> stacks = new ArrayList<>(input.size());
        for (ItemStackTemplate template : input) stacks.add(template.create());
        return stacks;
    }

    /** True when adding {@code value} would go over {@link #maxAmount} (0 = unlimited). */
    public boolean wouldOverflow(int value) {
        return maxAmount > 0 && amount + value > maxAmount;
    }

    public CastData withQuality(String quality) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
    }

    public CastData withToolType(String toolType) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
    }

    public CastData withMaterials(Map<String, Integer> materials) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
    }

    public CastData withAmount(int amount) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
    }

    public CastData withMaxAmount(int maxAmount) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
    }

    public CastData withInput(List<ItemStackTemplate> input) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
    }

    public CastData withOutput(ItemStack output) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input,
                output.isEmpty() ? Optional.empty() : Optional.of(ItemStackTemplate.fromNonEmptyStack(output)), heated);
    }

    public CastData withHeated(boolean heated) {
        return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
    }

    /** Adds {@code value} units of {@code materialKey} plus one of {@code stack} to the input list. */
    public CastData withAddedMaterial(String materialKey, int value, ItemStack stack) {
        Map<String, Integer> newMaterials = new LinkedHashMap<>(materials);
        newMaterials.merge(materialKey, value, Integer::sum);

        List<ItemStackTemplate> newInput = new ArrayList<>(input);
        ItemStack single = stack.copyWithCount(1);
        boolean merged = false;
        for (int i = 0; i < newInput.size(); i++) {
            ItemStack existing = newInput.get(i).create();
            if (ItemStack.isSameItemSameComponents(existing, single)) {
                newInput.set(i, newInput.get(i).withCount(existing.getCount() + 1));
                merged = true;
                break;
            }
        }
        if (!merged) newInput.add(ItemStackTemplate.fromNonEmptyStack(single));

        return new CastData(quality, toolType, newMaterials, amount + value, maxAmount, newInput, output, heated);
    }

    /** Clears materials and input, keeping quality, tool type and capacity. */
    public CastData cleared() {
        return new CastData(quality, toolType, Map.of(), 0, maxAmount, List.of(), Optional.empty(), false);
    }
}
