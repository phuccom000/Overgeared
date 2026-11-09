package net.stirdrem.overgeared.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, OvergearedMod.MOD_ID);

    public static final Supplier<RecipeSerializer<ForgingRecipe>> FORGING_SERIALIZER =
            RECIPE_SERIALIZERS.register("forging", () -> new RecipeSerializer<>(ForgingRecipe.Serializer.INSTANCE.codec(), ForgingRecipe.Serializer.INSTANCE.streamCodec()));
    public static final Supplier<RecipeSerializer<RockKnappingRecipe>> ROCK_KNAPPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("rock_knapping", serializer(RockKnappingRecipe.Serializer.CODEC, RockKnappingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<OvergearedShapelessRecipe>> CRAFTING_SHAPELESS =
            RECIPE_SERIALIZERS.register("crafting_shapeless", serializer(OvergearedShapelessRecipe.Serializer.CODEC, OvergearedShapelessRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<BlueprintCloningRecipe>> CRAFTING_BLUEPRINTCLONING =
            RECIPE_SERIALIZERS.register("crafting_cloning", serializer(BlueprintCloningRecipe.Serializer.CODEC, BlueprintCloningRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<DynamicToolCastRecipe>> CRAFTING_DYNAMIC_TOOL_CAST =
            RECIPE_SERIALIZERS.register("crafting_cast", serializer(DynamicToolCastRecipe.Serializer.CODEC, DynamicToolCastRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ClayToolCastRecipe>> CLAY_TOOL_CAST =
            RECIPE_SERIALIZERS.register("crafting_initial_cast", serializer(ClayToolCastRecipe.Serializer.CODEC, ClayToolCastRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fletching", serializer(FletchingRecipe.Serializer.CODEC, FletchingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CastSmeltingRecipe>> CAST_SMELTING =
            RECIPE_SERIALIZERS.register("cast_smelting", serializer(CastSmeltingRecipe.Serializer.CODEC, CastSmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CastBlastingRecipe>> CAST_BLASTING =
            RECIPE_SERIALIZERS.register("cast_blasting", serializer(CastBlastingRecipe.Serializer.CODEC, CastBlastingRecipe.Serializer.STREAM_CODEC));

    public static final Supplier<RecipeSerializer<NBTKeepingSmeltingRecipe>> NBT_SMELTING =
            RECIPE_SERIALIZERS.register("nbt_smelting", serializer(NBTKeepingSmeltingRecipe.Serializer.CODEC, NBTKeepingSmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<NBTKeepingBlastingRecipe>> NBT_BLASTING =
            RECIPE_SERIALIZERS.register("nbt_blasting", serializer(NBTKeepingBlastingRecipe.Serializer.CODEC, NBTKeepingBlastingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<AlloySmeltingRecipe>> ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("alloy_smelting", serializer(AlloySmeltingRecipe.Serializer.CODEC, AlloySmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("shaped_alloy_smelting", () -> new RecipeSerializer<>(new ShapedAlloySmeltingRecipe.Serializer().codec(), new ShapedAlloySmeltingRecipe.Serializer().streamCodec()));
    public static final Supplier<RecipeSerializer<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("nether_alloy_smelting", serializer(NetherAlloySmeltingRecipe.Serializer.CODEC, NetherAlloySmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ShapedNetherAlloySmeltingRecipe>> SHAPED_NETHER_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("shaped_nether_alloy_smelting", serializer(ShapedNetherAlloySmeltingRecipe.Serializer.CODEC, ShapedNetherAlloySmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ItemToToolTypeRecipe>> ITEM_TO_TOOLTYPE =
            RECIPE_SERIALIZERS.register("item_to_tooltype", serializer(ItemToToolTypeRecipe.Serializer.CODEC, ItemToToolTypeRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CoolingRecipe>> COOLING_SERIALIZER =
            RECIPE_SERIALIZERS.register("cooling", serializer(CoolingRecipe.Serializer.CODEC, CoolingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", serializer(GrindingRecipe.Serializer.CODEC, GrindingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CastingRecipe>> CASTING =
            RECIPE_SERIALIZERS.register("casting", serializer(CastingRecipe.Serializer.CODEC, CastingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<NBTCampfireRecipe>> CAMPFIRE_HEATING =
            RECIPE_SERIALIZERS.register("campfire_heating", serializer(NBTCampfireRecipe.Serializer.CODEC, NBTCampfireRecipe.Serializer.STREAM_CODEC));

    public static final Supplier<RecipeSerializer<DragonBreathBrewingRecipe>> DRAGON_BREATH_BREWING =
            RECIPE_SERIALIZERS.register("dragon_breath_brewing", serializer(DragonBreathBrewingRecipe.CODEC, DragonBreathBrewingRecipe.STREAM_CODEC));

    public static final Supplier<RecipeSerializer<DisabledRecipe>> DISABLED =
            RECIPE_SERIALIZERS.register("disabled", serializer(DisabledRecipe.CODEC, DisabledRecipe.STREAM_CODEC));

    private static <T extends Recipe<?>> Supplier<RecipeSerializer<T>> serializer(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return () -> new RecipeSerializer<>(codec, streamCodec);
    }

    public static void register() {
        RECIPE_SERIALIZERS.register();
    }
}

