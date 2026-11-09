package net.stirdrem.overgeared.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recipe lookups that work on both sides.
 * <p>
 * Since 1.21.2 the server no longer sends recipes to clients, but the mod's screens, tooltips and
 * recipe viewer plugins need them. The server therefore sends every Overgeared recipe to each
 * client ({@link net.stirdrem.overgeared.networking.packet.SyncRecipesS2CPacket}) on login and on
 * datapack reload; on the client the lookups read that copy instead of the recipe manager.
 */
public final class RecipeLookup {
    private static volatile List<RecipeHolder<?>> clientRecipes = List.of();
    private static final Map<RecipeType<?>, List<RecipeHolder<?>>> clientByType = new ConcurrentHashMap<>();

    private RecipeLookup() {
    }

    // ---- lookups -------------------------------------------------------------------------------

    public static <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(Level level, RecipeType<T> type, I input) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipeFor(type, input, level);
        }
        for (RecipeHolder<T> holder : getAllRecipesFor(level, type)) {
            if (holder.value().matches(input, level)) return Optional.of(holder);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> Collection<RecipeHolder<T>> getAllRecipesFor(Level level, RecipeType<T> type) {
        if (level instanceof ServerLevel serverLevel) {
            return byType(serverLevel.recipeAccess(), type);
        }
        return (Collection<RecipeHolder<T>>) (Collection<?>) clientByType.getOrDefault(type, List.of());
    }

    public static <T extends Recipe<?>> Collection<RecipeHolder<T>> getAllRecipesFor(MinecraftServer server, RecipeType<T> type) {
        return byType(server.getRecipeManager(), type);
    }

    public static Optional<RecipeHolder<?>> byKey(Level level, ResourceKey<Recipe<?>> key) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().byKey(key);
        }
        return clientRecipes.stream().filter(h -> h.id().equals(key)).findFirst();
    }

    /** The recipes the client currently knows about (empty on a dedicated server). */
    public static List<RecipeHolder<?>> clientRecipes() {
        return clientRecipes;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> Collection<RecipeHolder<T>> byType(RecipeManager manager, RecipeType<T> type) {
        List<RecipeHolder<T>> out = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value().getType() == type) out.add((RecipeHolder<T>) holder);
        }
        return out;
    }

    // ---- syncing -------------------------------------------------------------------------------

    /** The recipes sent to clients: those of the mod's own recipe types or serializers. */
    public static List<RecipeHolder<?>> recipesToSync(MinecraftServer server) {
        List<RecipeHolder<?>> out = new ArrayList<>();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            if (isOvergeared(BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType()))
                    || isOvergeared(BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer()))) {
                out.add(holder);
            }
        }
        return out;
    }

    private static boolean isOvergeared(Identifier id) {
        return id != null && id.getNamespace().equals(OvergearedMod.MOD_ID);
    }

    /** Called on the client when the server sends its recipes. */
    public static void acceptClientRecipes(List<RecipeHolder<?>> recipes) {
        clientRecipes = List.copyOf(recipes);
        clientByType.clear();
        for (RecipeHolder<?> holder : recipes) {
            clientByType.computeIfAbsent(holder.value().getType(), t -> new ArrayList<>()).add(holder);
        }
    }

    /** Called on the client when it leaves a world. */
    public static void clearClientRecipes() {
        acceptClientRecipes(List.of());
    }
}
