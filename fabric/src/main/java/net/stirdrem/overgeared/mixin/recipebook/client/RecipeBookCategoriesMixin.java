package net.stirdrem.overgeared.mixin.recipebook.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipebook.FabricRecipeBookCategories;
import net.stirdrem.overgeared.recipebook.FabricRecipeBookTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Adds the smithing-anvil recipe-book tabs (Forge registers the same through RegisterRecipeBookCategoriesEvent). */
@Mixin(RecipeBookCategories.class)
public abstract class RecipeBookCategoriesMixin {
    @Shadow
    @Final
    @Mutable
    private static RecipeBookCategories[] $VALUES;

    @Shadow
    @Final
    @Mutable
    public static Map<RecipeBookCategories, List<RecipeBookCategories>> AGGREGATE_CATEGORIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$addForgingCategories(CallbackInfo ci) {
        RecipeBookCategories[] values = $VALUES;
        int n = values.length;
        RecipeBookCategories search = RecipeBookCategoriesAccessor.overgeared$create("OVERGEARED_FORGING_SEARCH", n, new ItemStack(Items.COMPASS));
        RecipeBookCategories tools = RecipeBookCategoriesAccessor.overgeared$create("OVERGEARED_FORGING_TOOLS", n + 1, new ItemStack(ModItems.IRON_PICKAXE_HEAD.get()));
        RecipeBookCategories armors = RecipeBookCategoriesAccessor.overgeared$create("OVERGEARED_FORGING_ARMORS", n + 2, new ItemStack(Items.IRON_CHESTPLATE));
        RecipeBookCategories misc = RecipeBookCategoriesAccessor.overgeared$create("OVERGEARED_FORGING_MISC", n + 3, new ItemStack(Items.ANVIL));

        RecipeBookCategories[] extended = Arrays.copyOf(values, n + 4);
        extended[n] = search;
        extended[n + 1] = tools;
        extended[n + 2] = armors;
        extended[n + 3] = misc;
        $VALUES = extended;

        AGGREGATE_CATEGORIES = ImmutableMap.<RecipeBookCategories, List<RecipeBookCategories>>builder()
                .putAll(AGGREGATE_CATEGORIES)
                .put(search, List.of(tools, armors, misc))
                .build();

        FabricRecipeBookCategories.init(search, tools, armors, misc);
    }

    @Inject(method = "getCategories", at = @At("HEAD"), cancellable = true)
    private static void overgeared$forgingCategories(RecipeBookType type, CallbackInfoReturnable<List<RecipeBookCategories>> cir) {
        if (type == FabricRecipeBookTypes.forging()) {
            cir.setReturnValue(FabricRecipeBookCategories.ALL);
        }
    }
}
