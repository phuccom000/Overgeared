package net.stirdrem.overgeared.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.stirdrem.overgeared.item.ModItems;

import java.util.List;
import java.util.function.Supplier;

public class EnumParams {
    public static final EnumProxy<RecipeBookCategories> FORGING_SEARCH = new EnumProxy<>(
            RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.COMPASS))
    );
    public static final EnumProxy<RecipeBookCategories> FORGING_TOOLS = new EnumProxy<>(
            RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(ModItems.IRON_PICKAXE_HEAD.get()))
    );
    public static final EnumProxy<RecipeBookCategories> FORGING_ARMORS = new EnumProxy<>(
            RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.IRON_CHESTPLATE))
    );
    public static final EnumProxy<RecipeBookCategories> FORGING_MISC = new EnumProxy<>(
            RecipeBookCategories.class, (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Blocks.ANVIL))
    );
}
