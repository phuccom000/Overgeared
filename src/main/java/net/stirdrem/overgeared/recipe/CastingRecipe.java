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
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.CodecUtils;
import net.stirdrem.overgeared.util.ConfigHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CastingRecipe implements Recipe<RecipeInput> {
    private final String group;
    private final CookingBookCategory category;
    private final Map<String, Integer> requiredMaterials;
    private final String toolType;
    private final ItemStack result;
    private final boolean needPolishing;
    private final float experience;
    private final int cookingTime;

    public CastingRecipe(String group, CookingBookCategory category, Map<String, Integer> requiredMaterials, String toolType,
                         ItemStack result, boolean needPolishing, float experience, int cookingTime) {
        this.group = group;
        this.category = category;
        this.requiredMaterials = requiredMaterials;
        this.toolType = toolType.toLowerCase();
        this.result = result;
        this.needPolishing = needPolishing;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (level.isClientSide) return false;

        // Tool cast (slot 1)
        ItemStack cast = input.getItem(1);
        if (!(cast.getItem() instanceof ToolCastItem)) return false;

        // Get cast data component
        CastData castData = cast.get(ModComponents.CAST_DATA.get());
        if (castData == null) return false;

        // Tool type check (FROM CAST)
        if (castData.toolType().isEmpty()) return false;
        if (!toolType.equals(castData.toolType().toLowerCase())) return false;

        // Cast Data Check
        if (castData.hasOutput()) return false;
        if (!castData.materials().isEmpty()) return false;

        // Material input slot (slot 0)
        ItemStack materialStack = input.getItem(0);
        if (materialStack.isEmpty()) return false;

        // Must be a valid material
        if (!ConfigHelper.isValidMaterial(materialStack)) {
            return false;
        }

        // availableMaterials is derived ONLY from input slot
        Map<String, Integer> availableMaterials = ConfigHelper.getMaterialValuesForItem(materialStack);
        int count = materialStack.getCount();

        // Required material validation
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase();
            int needed = entry.getValue();
            int available = availableMaterials.getOrDefault(material, 0) * count;

            if (available < needed) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider provider) {
        ItemStack cast = input.getItem(1);
        if (cast.isEmpty()) return ItemStack.EMPTY;

        CastData castData = cast.get(ModComponents.CAST_DATA.get());
        if (castData == null) return ItemStack.EMPTY;

        // Build result item
        ItemStack out = result.copy();

        // Transfer forging quality from cast
        if (!castData.quality().isEmpty() && !castData.quality().equals("none")) {
            out.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.fromString(castData.quality()));
        }

        // Polishing flag
        if (needPolishing) {
            out.set(ModComponents.POLISHED.get(), false);
        }

        // Heated flag (used by your pipeline)
        out.set(ModComponents.HEATED_COMPONENT.get(), true);

        // Creator tooltip - check if cast has a custom name using Components API
        if (cast.has(DataComponents.CUSTOM_NAME) && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            // Get the custom name component
            Component customName = cast.get(DataComponents.CUSTOM_NAME);
            if (customName != null) {
                out.set(ModComponents.CREATOR.get(), customName.getString());
            }
        }

        return out;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(RecipeInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            // Handles cast durability
            if (i == 1 && stack.getItem() instanceof ToolCastItem) {
                if (stack.isDamageableItem()) {
                    ItemStack damaged = stack.copy();
                    int newDamage = damaged.getDamageValue() + 1;

                    if (newDamage < damaged.getMaxDamage()) {
                        damaged.setDamageValue(newDamage);
                        remaining.set(i, damaged);
                    }
                }
            }
        }

        return remaining;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        // Calculate total material amount
        int total = requiredMaterials.values().stream().mapToInt(Integer::intValue).sum();

        CastData castData = new CastData(
                "", // quality
                toolType,
                requiredMaterials,
                total,
                total,
                List.of(), // input
                ItemStack.EMPTY, // output
                false // heated
        );

        ItemStack dummyCast = new ItemStack(net.stirdrem.overgeared.item.ModItems.CLAY_TOOL_CAST.get());
        dummyCast.set(ModComponents.CAST_DATA.get(), castData);

        list.add(Ingredient.of(dummyCast));
        return list;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CASTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CASTING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public float getExperience() {
        return experience;
    }

    public boolean requiresPolishing() {
        return needPolishing;
    }

    public Map<String, Integer> getRequiredMaterials() {
        return requiredMaterials;
    }

    public String getToolType() {
        return toolType;
    }

    public CookingBookCategory getCategory() {
        return category;
    }

    public static class Serializer implements RecipeSerializer<CastingRecipe> {

        private static final MapCodec<CastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC).forGetter(r -> r.category),
                CodecUtils.MATERIAL_INT_MAP_CODEC.fieldOf("input").forGetter(r -> r.requiredMaterials),
                Codec.STRING.fieldOf("tool_type").forGetter(r -> r.toolType),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.BOOL.optionalFieldOf("need_polishing", false).forGetter(r -> r.needPolishing),
                Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cooking_time", 200).forGetter(r -> r.cookingTime)
        ).apply(instance, CastingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        private static CastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CookingBookCategory category = buffer.readEnum(CookingBookCategory.class);
            Map<String, Integer> reqMaterials = CodecUtils.decodeMaterialIntMap(buffer);
            String toolType = buffer.readUtf();
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            boolean need_polishing = buffer.readBoolean();
            float experience = buffer.readFloat();
            int cookingTime = buffer.readVarInt();

            return new CastingRecipe(group, category, reqMaterials, toolType, result, need_polishing, experience, cookingTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CastingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            CodecUtils.encodeMaterialIntMap(buffer, recipe.requiredMaterials);
            buffer.writeUtf(recipe.toolType);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.needPolishing);
            buffer.writeFloat(recipe.experience);
            buffer.writeVarInt(recipe.cookingTime);
        }

        @Override
        public MapCodec<CastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}