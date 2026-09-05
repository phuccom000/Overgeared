package net.stirdrem.overgeared.compat.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.mojang.datafixers.util.Pair;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Wraps one anvil block entity so Polymorph can offer a recipe-choice UI whenever the current
 * grid contents match more than one forging recipe. Deliberately simpler than Polymorph's own
 * internal AbstractRecipeData: no per-call "did the input actually change" caching, since the
 * anvil block entity already gates how often getCurrentRecipe() re-resolves via its own
 * needsRecipeUpdate flag - this only runs when Overgeared itself decided a re-match is due.
 */
public class AnvilRecipeData implements IBlockEntityRecipeData {
    private final BlockEntity owner;
    private final Set<ServerPlayer> listeners = new HashSet<>();
    private final SortedSet<IRecipePair> recipesList = new TreeSet<>();
    private Recipe<?> selectedRecipe;
    private ResourceLocation loadedRecipeId;
    private boolean failing;

    public AnvilRecipeData(BlockEntity owner) {
        this.owner = owner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Recipe<C>, C extends Container> Optional<T> getRecipe(RecipeType<T> type, C inventory, Level level, List<T> recipesList) {
        if (isEmpty(inventory)) {
            setFailing(false);
            sendRecipesListToListeners(true);
            return Optional.empty();
        }

        if (recipesList.isEmpty()) {
            setFailing(true);
            sendRecipesListToListeners(true);
            return Optional.empty();
        }

        SortedSet<IRecipePair> newList = new TreeSet<>();
        for (T recipe : recipesList) {
            ItemStack output = recipe.getResultItem(level.registryAccess());
            if (!output.isEmpty()) {
                newList.add(new OvergearedRecipePair(recipe.getId(), output));
            }
        }
        setRecipesList(newList);

        // Restore a persisted selection once, right after load.
        if (loadedRecipeId != null) {
            for (T recipe : recipesList) {
                if (recipe.getId().equals(loadedRecipeId)) {
                    selectedRecipe = recipe;
                    break;
                }
            }
            loadedRecipeId = null;
        }

        T result = null;
        if (selectedRecipe != null) {
            for (T recipe : recipesList) {
                if (recipe.getId().equals(selectedRecipe.getId())) {
                    result = recipe;
                    break;
                }
            }
        }
        if (result == null) {
            // Nothing selected, or the previous selection no longer matches this grid - default
            // to the first entry (findAllMatches sorts largest-first, same as the pre-Polymorph
            // auto-pick behaviour).
            result = recipesList.get(0);
        }

        selectedRecipe = result;
        setFailing(false);
        sendRecipesListToListeners(false);
        return Optional.of(result);
    }

    @Override
    public void selectRecipe(Recipe<?> recipe) {
        setSelectedRecipe(recipe);
    }

    @Override
    public Optional<? extends Recipe<?>> getSelectedRecipe() {
        return Optional.ofNullable(selectedRecipe);
    }

    @Override
    public void setSelectedRecipe(Recipe<?> recipe) {
        this.selectedRecipe = recipe;
    }

    @Override
    public SortedSet<IRecipePair> getRecipesList() {
        return recipesList;
    }

    @Override
    public void setRecipesList(SortedSet<IRecipePair> recipesList) {
        this.recipesList.clear();
        this.recipesList.addAll(recipesList);
    }

    @Override
    public boolean isEmpty(Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<ServerPlayer> getListeners() {
        return listeners;
    }

    @Override
    public void sendRecipesListToListeners(boolean isEmpty) {
        Pair<SortedSet<IRecipePair>, ResourceLocation> data = isEmpty
                ? new Pair<>(new TreeSet<>(), null)
                : getPacketData();
        for (ServerPlayer listener : listeners) {
            PolymorphApi.common().getPacketDistributor()
                    .sendRecipesListS2C(listener, data.getFirst(), data.getSecond());
        }
    }

    @Override
    public Pair<SortedSet<IRecipePair>, ResourceLocation> getPacketData() {
        return new Pair<>(getRecipesList(), null);
    }

    @Override
    public BlockEntity getOwner() {
        return owner;
    }

    @Override
    public boolean isFailing() {
        return failing;
    }

    @Override
    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    @Override
    public void tick() {
        // No per-tick work needed - Overgeared's own block entity drives re-matching.
    }

    @Override
    public void addListener(ServerPlayer player) {
        listeners.add(player);
        // Force a fresh match on this anvil's next tick so a listener that connects after the
        // last ingredient change (e.g. just opening the screen on an already-filled grid) still
        // gets sent the current candidate list instead of nothing.
        if (owner instanceof net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity anvil) {
            anvil.forceRecipeUpdate();
        }
    }

    @Override
    public void removeListener(ServerPlayer player) {
        listeners.remove(player);
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (selectedRecipe != null) {
            nbt.putString("SelectedRecipe", selectedRecipe.getId().toString());
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt.contains("SelectedRecipe")) {
            loadedRecipeId = ResourceLocation.tryParse(nbt.getString("SelectedRecipe"));
        }
    }
}
