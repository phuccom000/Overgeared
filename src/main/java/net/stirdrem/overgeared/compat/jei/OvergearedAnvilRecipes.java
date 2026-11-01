package net.stirdrem.overgeared.compat.jei;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;

import java.util.ArrayList;
import java.util.List;

/**
 * Anvil repair entries for Overgeared's tools and armor (self-repair and material repair).
 */
public final class OvergearedAnvilRecipes {

    private OvergearedAnvilRecipes() {
    }

    private record RepairData(Ingredient material, List<Item> repairables) {
    }

    public static List<IJeiAnvilRecipe> getRepairRecipes(IVanillaRecipeFactory factory) {
        List<RepairData> repairData = List.of(
                new RepairData(Ingredient.of(Items.IRON_INGOT), List.of(
                        ModItems.IRON_TONGS.get()
                )),
                new RepairData(Ingredient.of(Items.COPPER_INGOT), List.of(
                        ModItems.COPPER_SWORD.get(),
                        ModItems.COPPER_PICKAXE.get(),
                        ModItems.COPPER_AXE.get(),
                        ModItems.COPPER_SHOVEL.get(),
                        ModItems.COPPER_HOE.get()
                )),
                new RepairData(Ingredient.of(ModItems.STEEL_INGOT.get()), List.of(
                        ModItems.STEEL_TONGS.get(),
                        ModItems.STEEL_SWORD.get(),
                        ModItems.STEEL_PICKAXE.get(),
                        ModItems.STEEL_AXE.get(),
                        ModItems.STEEL_SHOVEL.get(),
                        ModItems.STEEL_HOE.get(),
                        ModItems.STEEL_HELMET.get(),
                        ModItems.STEEL_CHESTPLATE.get(),
                        ModItems.STEEL_LEGGINGS.get(),
                        ModItems.STEEL_BOOTS.get()
                ))
        );

        List<IJeiAnvilRecipe> recipes = new ArrayList<>();
        for (RepairData data : repairData) {
            List<ItemStack> materials = List.of(data.material().getItems());
            for (Item item : data.repairables()) {
                ItemStack stack = new ItemStack(item);
                if (!stack.isDamageableItem()) continue;
                String path = BuiltInRegistries.ITEM.getKey(item).getPath();
                int max = stack.getMaxDamage();

                ItemStack damagedThreeQuarters = damaged(stack, max * 3 / 4);
                ItemStack damagedHalf = damaged(stack, max / 2);

                // Combine two damaged copies
                recipes.add(factory.createAnvilRecipe(
                        List.of(damagedThreeQuarters),
                        List.of(damagedThreeQuarters),
                        List.of(damagedHalf),
                        OvergearedMod.loc("self_repair." + path)
                ));

                // Repair with material
                if (!materials.isEmpty()) {
                    recipes.add(factory.createAnvilRecipe(
                            List.of(damaged(stack, max)),
                            materials,
                            List.of(damagedThreeQuarters),
                            OvergearedMod.loc("material_repair." + path)
                    ));
                }
            }
        }
        return recipes;
    }

    private static ItemStack damaged(ItemStack stack, int damage) {
        ItemStack copy = stack.copy();
        copy.setDamageValue(damage);
        return copy;
    }
}
