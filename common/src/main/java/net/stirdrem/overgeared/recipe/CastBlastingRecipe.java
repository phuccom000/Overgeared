package net.stirdrem.overgeared.recipe;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;
import net.stirdrem.overgeared.item.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.stirdrem.overgeared.recipe.ingredients.Ingredients;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStackTemplate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.CodecUtils;
import net.stirdrem.overgeared.util.ModTags;

import java.util.HashMap;
import java.util.Map;

public class CastBlastingRecipe extends BlastingRecipe {

    // AbstractCookingRecipe keeps these private (and the result as an ItemStackTemplate) since 1.21.2
    protected final String group;
    protected final CookingBookCategory category;
    protected final Ingredient ingredient;
    protected final ItemStackTemplate result;
    protected final float experience;
    protected final int cookingTime;
    private final Map<String, Integer> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastBlastingRecipe(String group, CookingBookCategory category, Map<String, Integer> requiredMaterials,
                              String toolType, ItemStackTemplate result, boolean needPolishing, float experience, int cookingTime) {
        super(new Recipe.CommonInfo(true), new AbstractCookingRecipe.CookingBookInfo(category, group), Ingredient.of(ModItems.CLAY_TOOL_CAST.get(), ModItems.NETHER_TOOL_CAST.get()),
                result, experience, cookingTime);
        this.group = group;
        this.category = category;
        this.ingredient = input();
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;

        this.requiredMaterials = requiredMaterials;
        this.toolType = toolType;
        this.needPolishing = needPolishing;
    }

    /** The cast as shown in recipe viewers: a usable cast holding exactly the required materials. */
    public List<ItemStack> getDisplayInputs() {
        List<ItemStack> list = new ArrayList<>();

        Map<String, Integer> materials = new HashMap<>(requiredMaterials);
        int total = requiredMaterials.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

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

        ItemStack cast = Ingredients.stacks(this.ingredient).get(0).copy();
        cast.set(ModComponents.CAST_DATA.get(), displayData);

        list.add(cast);
        return list;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack stack = input.getItem(0);
        if (!(stack.getItem() instanceof ToolCastItem))
            return false;
        if (!(stack.is(ModTags.Items.USABLE_TOOL_CAST)))
            return false;

        CastData castData = stack.get(ModComponents.CAST_DATA.get());
        if (castData == null)
            return false;

        if (castData.hasOutput())
            return false;

        if (!toolType.equals(castData.toolType().toLowerCase(java.util.Locale.ROOT)))
            return false;
        if (castData.amount() <= 0)
            return false;

        Map<String, Integer> materials = castData.materials();

        // Check if cast has enough of each required material
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase(java.util.Locale.ROOT);
            double needed = entry.getValue();
            int available = materials.getOrDefault(material, 0);
            if (available < needed)
                return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack inputStack = input.getItem(0);

        // Copy the cast itself
        ItemStack cast = inputStack.copy();
        CastData castData = cast.get(ModComponents.CAST_DATA.get());
        if (castData == null)
            return ItemStack.EMPTY;

        // Build the real result item
        ItemStack result = this.result.create();

        // Transfer quality from cast to result
        if (!castData.quality().isEmpty() && !castData.quality().equals("none")) {
            ForgingQuality forgingQuality = ForgingQuality.fromString(castData.quality());
            result.set(ModComponents.FORGING_QUALITY.get(), forgingQuality);
        }

        // Polishing Component
        if (needPolishing) {
            result.set(ModComponents.POLISHED.get(), false);
        }

        // Creator (transfer custom name from cast to output tool)
        if (cast.has(DataComponents.CUSTOM_NAME) && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            Component customName = inputStack.get(DataComponents.CUSTOM_NAME);
            if (customName != null) {
                result.set(ModComponents.CREATOR.get(), customName.getString());
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
                true);
        cast.set(ModComponents.CAST_DATA.get(), updatedData);

        cast.set(ModComponents.HEATED_COMPONENT.get(), true);

        // Handle cast durability
        if (cast.isDamageableItem()) {
            int newDamage = cast.getDamageValue() + 1;
            if (newDamage >= cast.getMaxDamage()) {
                result.set(ModComponents.HEATED_COMPONENT.get(), true);
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
    @SuppressWarnings("unchecked")
    public RecipeSerializer<BlastingRecipe> getSerializer() {
        // The parent fixes the type parameter; the serializer is really a RecipeSerializer<CastBlastingRecipe>.
        return (RecipeSerializer<BlastingRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.CAST_BLASTING.get();
    }

    public static class Serializer {

        public static final MapCodec<CastBlastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                        CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC)
                                .forGetter(r -> r.category),
                        CodecUtils.MATERIAL_INT_MAP_CODEC.fieldOf("input").forGetter(r -> r.requiredMaterials),
                        Codec.STRING.fieldOf("tool_type").forGetter(r -> r.toolType),
                        ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                        Codec.BOOL.optionalFieldOf("need_polishing", false).forGetter(r -> r.needPolishing),
                        Codec.FLOAT.optionalFieldOf("experience", 0f).forGetter(r -> r.experience),
                        Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(r -> r.cookingTime))
                .apply(instance, CastBlastingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CastBlastingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        private static CastBlastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CookingBookCategory category = buffer.readEnum(CookingBookCategory.class);
            Map<String, Integer> reqMaterials = CodecUtils.MATERIAL_INT_MAP_STREAM.decode(buffer);
            String toolType = buffer.readUtf();
            ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
            boolean needPolishing = buffer.readBoolean();
            float experience = buffer.readFloat();
            int cookingTime = buffer.readVarInt();

            return new CastBlastingRecipe(group, category, reqMaterials, toolType, result, needPolishing, experience,
                    cookingTime);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, CastBlastingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            CodecUtils.MATERIAL_INT_MAP_STREAM.encode(buffer, recipe.requiredMaterials);
            buffer.writeUtf(recipe.toolType);
            ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.needPolishing);
            buffer.writeFloat(recipe.experience);
            buffer.writeVarInt(recipe.cookingTime);
        }

        public MapCodec<CastBlastingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, CastBlastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
