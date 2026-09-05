package net.stirdrem.overgeared.compat.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphCommon;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilScreenHandler;

import java.util.List;
import java.util.Optional;

/**
 * Registers the anvil with Polymorph, if present, so players can pick between forging recipes
 * that match the same grid contents instead of always getting whichever one happens to have the
 * most ingredients. Only called when FabricLoader.isModLoaded("polymorph") - nothing in this
 * class may be referenced otherwise, since Polymorph's classes wouldn't be on the classpath.
 */
public final class PolymorphIntegration {
    private PolymorphIntegration() {
    }

    public static void register() {
        IPolymorphCommon common = PolymorphApi.common();

        common.registerBlockEntity2RecipeData(blockEntity ->
                blockEntity instanceof AbstractSmithingAnvilBlockEntity anvil
                        ? new AnvilRecipeData(anvil)
                        : null);

        common.registerContainer2BlockEntity(container ->
                container instanceof AbstractSmithingAnvilScreenHandler handler
                        ? handler.getBlockEntity()
                        : null);
    }

    /**
     * Resolves which of the current grid's matching recipes is active, letting Polymorph's
     * per-block-entity selection state override the default "most ingredients wins" pick when
     * more than one recipe matches. Falls back to the first (largest) candidate if Polymorph has
     * no recipe data registered for this anvil yet.
     */
    public static Optional<ForgingRecipe> resolveRecipe(AbstractSmithingAnvilBlockEntity anvil, Level world, Container inventory) {
        List<ForgingRecipe> candidates = ForgingRecipe.findAllMatches(world, inventory);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        return PolymorphApi.common().getRecipeData(anvil)
                .flatMap(data -> data.getRecipe(ModRecipeTypes.FORGING, inventory, world, candidates))
                .or(() -> Optional.of(candidates.get(0)));
    }
}
