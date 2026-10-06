package net.stirdrem.overgeared.datagen;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.castcooking.CastBlastingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/** Datagen builder for {@code overgeared:cast_blasting} recipes. Ids get the suffix {@code _from_cast_blasting}. */
public class ToolCastBlastingRecipeBuilder {

    private final ItemLike result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new LinkedHashMap<>();
    private final RecipeBuilderSupport.Unlocks unlocks = new RecipeBuilderSupport.Unlocks();

    private String toolType;
    private boolean needPolishing = false;
    @Nullable
    private String group = "misc";

    public ToolCastBlastingRecipeBuilder(ItemLike result, float experience, int cookTime) {
        this.result = result;
        this.experience = experience;
        this.cookTime = cookTime;
    }

    public static ToolCastBlastingRecipeBuilder cast(ItemLike result, float xp, int time) {
        return new ToolCastBlastingRecipeBuilder(result, xp, time);
    }

    public ToolCastBlastingRecipeBuilder toolType(String type) {
        this.toolType = type;
        return this;
    }

    public ToolCastBlastingRecipeBuilder material(String material, int amount) {
        this.materialInput.put(material, amount);
        return this;
    }

    public ToolCastBlastingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    public ToolCastBlastingRecipeBuilder criterion(String name, Criterion<?> conditions) {
        unlocks.add(name, conditions);
        return this;
    }

    public ToolCastBlastingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    /** 26.3 port: cast cooking recipes always use the MISC cooking category; kept for source compatibility. */
    public ToolCastBlastingRecipeBuilder category(String category) {
        return this;
    }

    public Item getOutputItem() {
        return result.asItem();
    }

    public void offerTo(RecipeOutput output) {
        offerTo(output, RecipeBuilderSupport.defaultId(result));
    }

    public void offerTo(RecipeOutput output, Identifier id) {
        if (toolType == null) throw new IllegalStateException("Tool type missing for " + id);
        if (materialInput.isEmpty()) throw new IllegalStateException("No material input for " + id);

        Map<String, Double> materials = new LinkedHashMap<>();
        materialInput.forEach((k, v) -> materials.put(k, v.doubleValue()));

        CastBlastingRecipe recipe = new CastBlastingRecipe(group == null ? "" : group, new ItemStackTemplate(result.asItem()),
                experience, cookTime, materials, toolType, needPolishing);

        ResourceKey<Recipe<?>> key = RecipeBuilderSupport.key(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_from_cast_blasting"));
        output.accept(key, recipe, unlocks.build(output, key, "misc"));
    }
}
