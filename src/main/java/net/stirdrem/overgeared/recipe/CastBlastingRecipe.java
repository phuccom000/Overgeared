package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.CodecUtils;
import net.stirdrem.overgeared.util.ModTags;

import java.util.HashMap;
import java.util.Map;

public class CastBlastingRecipe extends BlastingRecipe {
    private final Map<String, Integer> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastBlastingRecipe(String group, CookingBookCategory category, Map<String, Integer> requiredMaterials,
                                    String toolType, ItemStack result, boolean needPolishing, float experience, int cookingTime) {
        super(group, category, Ingredient.of(ModTags.Items.USABLE_TOOL_CAST), result, experience, cookingTime);

        this.requiredMaterials = requiredMaterials;
        this.toolType = toolType;
        this.needPolishing = needPolishing;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        Map<String, Integer> materials = requiredMaterials;
        int total = requiredMaterials.values().stream().mapToInt(Integer::intValue).sum();

        CastData displayData = new CastData(
                "",
                toolType,
                materials,
                total,
                total,
                java.util.List.of(),
                ItemStack.EMPTY,
                false
        );

        ItemStack cast = this.ingredient.getItems()[0];
        cast.set(ModComponents.CAST_DATA, displayData);

        list.add(Ingredient.of(cast));
        return list;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack stack = input.getItem(0);
        if (!(stack.getItem() instanceof ToolCastItem)) return false;
        if (!(stack.is(ModTags.Items.USABLE_TOOL_CAST))) return false;

        CastData castData = stack.get(ModComponents.CAST_DATA);
        if (castData == null) return false;

        if (castData.hasOutput()) return false;

        if (!toolType.equals(castData.toolType().toLowerCase())) return false;
        if (castData.amount() <= 0) return false;

        Map<String, Integer> materials = castData.materials();

        // Check if cast has enough of each required material
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase();
            double needed = entry.getValue();
            int available = materials.getOrDefault(material, 0);
            if (available < needed) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack inputStack = input.getItem(0);

        // Copy the cast itself
        ItemStack cast = inputStack.copy();
        CastData castData = cast.get(ModComponents.CAST_DATA);
        if (castData == null) return ItemStack.EMPTY;

        // Build the real result item
        ItemStack result = this.result.copy();

        // Transfer quality from cast to result (convert BlueprintQuality string to ForgingQuality)
        if (!castData.quality().isEmpty() && !castData.quality().equals("none")) {
            ForgingQuality forgingQuality = ForgingQuality.fromString(castData.quality());
            result.set(ModComponents.FORGING_QUALITY, forgingQuality);
        }

        // Polishing Component
        if (needPolishing) {
            result.set(ModComponents.POLISHED, false);
        }

        // Creator (transfer custom name from cast to output tool)
        if (cast.has(DataComponents.CUSTOM_NAME) && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            Component customName = inputStack.get(DataComponents.CUSTOM_NAME);
            if (customName != null) {
                result.set(ModComponents.CREATOR, customName.getString());
            }
        }

        // Update cast data: store output, clear materials, mark as heated
        CastData updatedData = new CastData(
                castData.quality(),
                castData.toolType(),
                Map.of(),
                0,
                castData.maxAmount(),
                java.util.List.of(),
                result,
                true
        );
        cast.set(ModComponents.CAST_DATA, updatedData);

        cast.set(ModComponents.HEATED_COMPONENT, true);

        // Handle cast durability
        if (cast.isDamageableItem()) {
            int newDamage = cast.getDamageValue() + 1;
            if (newDamage >= cast.getMaxDamage()) {
                result.set(ModComponents.HEATED_COMPONENT, true);
                return result;
            } else {
                cast.setDamageValue(newDamage);
            }
        }

        return cast;
    }

    public Map<String, Integer> getRequiredMaterials() {
        return requiredMaterials;
    }

    public String getToolType() {
        return toolType;
    }

    public boolean requiresPolishing() {
        return needPolishing;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CAST_BLASTING.get();
    }

    public static class Serializer implements RecipeSerializer<CastBlastingRecipe> {

        private static final MapCodec<CastBlastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC).forGetter(r -> r.category),
                CodecUtils.MATERIAL_INT_MAP_CODEC.fieldOf("input").forGetter(r -> r.requiredMaterials),
                Codec.STRING.fieldOf("tool_type").forGetter(r -> r.toolType),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.BOOL.optionalFieldOf("need_polishing", false).forGetter(r -> r.needPolishing),
                Codec.FLOAT.optionalFieldOf("experience", 0f).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cooking_time", 100).forGetter(r -> r.cookingTime)
        ).apply(instance, CastBlastingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CastBlastingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static CastBlastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CookingBookCategory category = buffer.readEnum(CookingBookCategory.class);
            Map<String, Integer> reqMaterials = CodecUtils.MATERIAL_INT_MAP_STREAM.decode(buffer);
            String toolType = buffer.readUtf();
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            boolean needPolishing = buffer.readBoolean();
            float experience = buffer.readFloat();
            int cookingTime = buffer.readVarInt();

            return new CastBlastingRecipe(group, category, reqMaterials, toolType, result, needPolishing, experience, cookingTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CastBlastingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            CodecUtils.MATERIAL_INT_MAP_STREAM.encode(buffer, recipe.requiredMaterials);
            buffer.writeUtf(recipe.toolType);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.needPolishing);
            buffer.writeFloat(recipe.experience);
            buffer.writeVarInt(recipe.cookingTime);
        }

        @Override
        public MapCodec<CastBlastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CastBlastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
