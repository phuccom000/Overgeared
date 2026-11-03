package net.stirdrem.overgeared.fabric.mixin.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.fabric.client.FabricRecipeCategories;
import net.stirdrem.overgeared.item.ModItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Adds the forging recipe book tabs, which NeoForge adds through enum extensions and
 * {@code RegisterRecipeBookCategoriesEvent}.
 */
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

    @Invoker("<init>")
    private static RecipeBookCategories overgeared$create(String name, int ordinal, ItemStack... icons) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overgeared$addForgingCategories(CallbackInfo ci) {
        int next = $VALUES.length;
        RecipeBookCategories search = overgeared$create("OVERGEARED_FORGING_SEARCH", next, new ItemStack(Items.COMPASS));
        RecipeBookCategories tools = overgeared$create("OVERGEARED_FORGING_TOOLS", next + 1, new ItemStack(ModItems.IRON_PICKAXE_HEAD.get()));
        RecipeBookCategories armors = overgeared$create("OVERGEARED_FORGING_ARMORS", next + 2, new ItemStack(Items.IRON_CHESTPLATE));
        RecipeBookCategories misc = overgeared$create("OVERGEARED_FORGING_MISC", next + 3, new ItemStack(Blocks.ANVIL));

        RecipeBookCategories[] values = Arrays.copyOf($VALUES, next + 4);
        values[next] = search;
        values[next + 1] = tools;
        values[next + 2] = armors;
        values[next + 3] = misc;
        $VALUES = values;

        AGGREGATE_CATEGORIES = ImmutableMap.<RecipeBookCategories, List<RecipeBookCategories>>builder()
                .putAll(AGGREGATE_CATEGORIES)
                .put(search, List.of(tools, armors, misc))
                .build();

        FabricRecipeCategories.init(search, tools, armors, misc);
    }

    @Inject(method = "getCategories", at = @At("HEAD"), cancellable = true)
    private static void overgeared$forgingCategories(RecipeBookType type, CallbackInfoReturnable<List<RecipeBookCategories>> cir) {
        if (type == OvergearedMod.FORGING) {
            cir.setReturnValue(FabricRecipeCategories.forgingCategories());
        }
    }
}
