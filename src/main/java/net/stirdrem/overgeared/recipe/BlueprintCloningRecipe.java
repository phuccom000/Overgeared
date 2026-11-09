package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

/**
 * {@code overgeared:crafting_cloning}: one empty blueprint + one written blueprint -> two copies of the written
 * blueprint with its BLUEPRINT_DATA quality downgraded one step. Any extra JSON fields are ignored.
 */
public class BlueprintCloningRecipe extends CustomRecipe {
    public static final BlueprintCloningRecipe INSTANCE = new BlueprintCloningRecipe();

    public BlueprintCloningRecipe() {
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        int blueprintCount = 0;
        ItemStack emptyBlueprint = ItemStack.EMPTY;

        for (int j = 0; j < inv.size(); ++j) {
            ItemStack stack = inv.getItem(j);
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.EMPTY_BLUEPRINT)) {
                    if (!emptyBlueprint.isEmpty()) {
                        return false; // Only 1 empty blueprint allowed
                    }
                    emptyBlueprint = stack;
                } else {
                    if (!stack.is(ModItems.BLUEPRINT)) {
                        return false;
                    }
                    ++blueprintCount;
                }
            }
        }

        return !emptyBlueprint.isEmpty() && blueprintCount > 0;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack source = ItemStack.EMPTY;

        for (int j = 0; j < inv.size(); ++j) {
            ItemStack stack = inv.getItem(j);
            if (!stack.isEmpty() && stack.is(ModItems.BLUEPRINT)) {
                if (!source.isEmpty()) return ItemStack.EMPTY; // only 1 blueprint source allowed
                source = stack;
            }
        }

        if (source.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = source.copyWithCount(2);

        BlueprintData data = source.get(ModComponents.BLUEPRINT_DATA);
        if (data != null) {
            BlueprintQuality downgraded = BlueprintQuality.getPrevious(BlueprintQuality.fromString(data.quality()));
            if (downgraded != null) {
                result.set(ModComponents.BLUEPRINT_DATA, data.withQuality(downgraded.getId()));
            }
        }

        return result;
    }

    @Override
    public RecipeSerializer<BlueprintCloningRecipe> getSerializer() {
        return ModRecipes.CRAFTING_BLUEPRINTCLONING;
    }

    public static final MapCodec<BlueprintCloningRecipe> MAP_CODEC = MapCodec.unit(BlueprintCloningRecipe::new); // new instance per recipe: 26.3 recipes are registry values and must be distinct
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintCloningRecipe> STREAM_CODEC = StreamCodec.of((buf, recipe) -> { }, buf -> new BlueprintCloningRecipe());
    public static final RecipeSerializer<BlueprintCloningRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
}
