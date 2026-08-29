package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForgingRecipe implements Recipe<Inventory> {
    private static final ForgingIngredient EMPTY_ING =
            new ForgingIngredient(Ingredient.EMPTY, false, false);

    private static int BLUEPRINT_SLOT = 11;
    public final int width;
    public final int height;
    private final Identifier id;
    private final String group;
    private final Set<String> blueprintTypes;
    private final String tier;
    private final DefaultedList<ForgingIngredient> ingredients;
    private final ItemStack result;
    private final ItemStack failedResult;
    private final int hammering;
    private final boolean hasQuality;
    private final boolean needsMinigame;
    private final boolean requiresBlueprint;
    private final boolean hasPolishing;
    private final boolean needQuenching;
    private final boolean showNotification;
    private final ForgingQuality minimumQuality;
    private final ForgingQuality qualityDifficulty;
    private final ForgingBookCategory tab;


    public ForgingRecipe(Identifier id, String group, boolean requireBlueprint, Set<String> blueprintTypes, String tier, DefaultedList<ForgingIngredient> ingredients,
                          ItemStack result, ItemStack failedResult, int hammering, boolean hasQuality, boolean needsMinigame, boolean hasPolishing, boolean needQuenching, boolean showNotification, ForgingQuality minimumQuality, ForgingQuality qualityDifficulty, int width, int height, ForgingBookCategory tab) {
        this.id = id;
        this.group = group;
        this.blueprintTypes = blueprintTypes;
        this.tier = tier;
        this.ingredients = ingredients;
        this.result = result;
        this.failedResult = failedResult;
        this.hammering = hammering;
        this.hasQuality = hasQuality;
        this.requiresBlueprint = requireBlueprint;
        this.needsMinigame = needsMinigame;
        this.hasPolishing = hasPolishing;
        this.needQuenching = needQuenching;
        this.showNotification = showNotification;
        this.minimumQuality = minimumQuality;
        this.width = width;
        this.height = height;
        this.qualityDifficulty = qualityDifficulty;
        this.tab = tab;
    }

    public static Optional<ForgingRecipe> findBestMatch(World world, Inventory inv) {
        var manager = world.getRecipeManager();
        //  Find a key item (first non-empty slot)
        ItemStack keyStack = IntStream.range(0, 9).mapToObj(inv::getStack).filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
        // If empty grid → no recipe
        if (keyStack.isEmpty()) {
            return Optional.empty();
        }

        return manager.listAllOfType(ModRecipeTypes.FORGING)
                .stream()
                .filter(recipe -> recipe.containsIngredient(keyStack))
                .filter(recipe -> recipe.matches(inv, world))
                .max(Comparator.comparingInt(ForgingRecipe::getRecipeSize));
    }

    public boolean containsIngredient(ItemStack stack) {
        for (ForgingIngredient ing : this.ingredients) {
            if (ing.test(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkBlueprint(Inventory inv) {
        ItemStack blueprintStack = inv.getStack(BLUEPRINT_SLOT);

        // If blueprint not required and no types defined -> slot must be empty
        if (!requiresBlueprint && blueprintTypes.isEmpty()) {
            return blueprintStack.isEmpty();
        }

        // If slot empty
        if (blueprintStack.isEmpty()) {
            return !requiresBlueprint;
        }

        NbtCompound nbt = blueprintStack.getNbt();
        if (nbt == null || !nbt.contains("ToolType")) return false;

        String toolType = nbt.getString("ToolType");
        return blueprintTypes.contains(toolType);
    }

    @Override
    public boolean matches(Inventory inv, World world) {
        if (!checkBlueprint(inv)) return false;

        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesPattern(inv, x, y) && checkSurroundingBlanks(inv, x, y)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkSurroundingBlanks(Inventory inv, int xOffset, int yOffset) {
        // Check if slots outside the recipe pattern are empty
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                // Skip slots that are part of the recipe
                if (x >= xOffset && x < xOffset + width &&
                        y >= yOffset && y < yOffset + height) {
                    continue;
                }

                // Check if non-recipe slots are empty
                int invSlot = y * 3 + x;
                if (!inv.getStack(invSlot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesPattern(Inventory inv, int xOffset, int yOffset) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int invSlot = (y + yOffset) * 3 + (x + xOffset);
                Ingredient ingredient = ingredients.get(y * width + x).ingredient;

                // If recipe expects empty, inventory slot must be empty
                if (ingredient.isEmpty()) {
                    if (!inv.getStack(invSlot).isEmpty()) {
                        return false;
                    }
                }
                // If recipe expects item, must match and have at least 1 count
                else if (!ingredients.get(y * width + x).test(inv.getStack(invSlot))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(Inventory inv, DynamicRegistryManager registryAccess) {
        ItemStack out = result.copy();
        NbtCompound merged = new NbtCompound();

        for (int i = 0; i < ingredients.size(); i++) {
            ForgingIngredient ing = ingredients.get(i);
            if (!ing.transferNbt()) continue;

            ItemStack stack = inv.getStack(i);
            if (!stack.hasNbt()) continue;

            mergeCompound(merged, stack.getNbt());
        }

        if (!merged.isEmpty()) {
            out.setNbt(merged);
        }

        return out;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryAccess) {
        return result.copy();
    }

    public boolean hasFailedResult() {
        return failedResult != null
                && !failedResult.isEmpty()
                && !failedResult.isOf(result.getItem());
    }

    public ItemStack getFailedResultItem(DynamicRegistryManager registryAccess) {
        return hasFailedResult() ? failedResult.copy() : ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> list =
                DefaultedList.ofSize(this.width * this.height, Ingredient.EMPTY);

        for (int i = 0; i < this.width * this.height; i++) {
            list.set(i, ingredients.get(i).ingredient);
        }

        return list;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public DefaultedList<ForgingIngredient> getForgingIngredients() {
        return ingredients;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORGING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FORGING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForgingRecipe that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public int getHammeringRequired() {
        return hammering;
    }

    public String getAnvilTier() {
        return tier;
    }

    public boolean hasQuality() {
        return hasQuality;
    }

    public boolean needsMinigame() {
        return needsMinigame;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    public ForgingQuality getMinimumQuality() {
        return minimumQuality;
    }

    public ForgingQuality getQualityDifficulty() {
        return qualityDifficulty;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }

    public int getRemainingHits() {
        return hammering;
    }

    public boolean hasPolishing() {
        return hasPolishing;
    }

    public Set<String> getBlueprintTypes() {
        return blueprintTypes.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean requiresBlueprint() {
        return requiresBlueprint;
    }

    public boolean needQuenching() {
        return needQuenching;
    }

    private int getRecipeSize() {
        return width * height;
    }

    public ForgingBookCategory getRecipeBookTab() {
        return tab;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static class Type implements RecipeType<ForgingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "forging";
    }

    public static class Serializer implements RecipeSerializer<ForgingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final Identifier ID = new Identifier(Overgeared.MOD_ID, "forging");

        private static Map<Character, ForgingIngredient> parseKey(JsonObject keyJson) {
            Map<Character, ForgingIngredient> keyMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : keyJson.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException("Invalid key: " + entry.getKey());
                }

                JsonObject obj = JsonHelper.asObject(entry.getValue(), entry.getKey());

                Ingredient ingredient = Ingredient.fromJson(obj);
                boolean requiresHeated = JsonHelper.getBoolean(obj, "requires_heated", false);
                boolean transferNbt = JsonHelper.getBoolean(obj, "transfer_nbt", false);

                keyMap.put(entry.getKey().charAt(0),
                        new ForgingIngredient(ingredient, requiresHeated, transferNbt));
            }

            return keyMap;
        }


        private static DefaultedList<ForgingIngredient> dissolvePattern(
                JsonArray pattern,
                Map<Character, ForgingIngredient> keys,
                int width,
                int height
        ) {
            DefaultedList<ForgingIngredient> ingredients =
                    DefaultedList.ofSize(width * height,
                            new ForgingIngredient(Ingredient.EMPTY, false, false));

            for (int y = 0; y < height; y++) {
                String row = JsonHelper.asString(pattern.get(y), "pattern[" + y + "]");
                if (row.length() != width) {
                    throw new JsonSyntaxException("Pattern row width mismatch");
                }

                for (int x = 0; x < width; x++) {
                    char c = row.charAt(x);

                    ForgingIngredient ingredient = keys.get(c);

                    if (ingredient == null) {
                        if (c == ' ') {
                            ingredient = new ForgingIngredient(Ingredient.EMPTY, false, false);
                        } else {
                            throw new JsonSyntaxException(
                                    "Pattern references undefined symbol: '" + c + "'"
                            );
                        }
                    }

                    ingredients.set(y * width + x, ingredient);
                }
            }

            return ingredients;
        }


        @Override
        public ForgingRecipe read(Identifier recipeId, JsonObject json) {
            String group = JsonHelper.getString(json, "group", "");

            Set<String> blueprintTypes = new LinkedHashSet<>();
            if (json.has("blueprint")) {
                JsonElement blueprintElement = json.get("blueprint");

                if (blueprintElement.isJsonArray()) {
                    for (JsonElement element : blueprintElement.getAsJsonArray()) {
                        String bp = element.getAsString().toLowerCase(Locale.ROOT);
                        if (!bp.isBlank()) {
                            blueprintTypes.add(bp);
                        }
                    }
                } else if (blueprintElement.isJsonPrimitive()) {
                    String bp = blueprintElement.getAsString().toLowerCase(Locale.ROOT);
                    if (!bp.isBlank()) {
                        blueprintTypes.add(bp);
                    }
                } else {
                    throw new JsonSyntaxException("'blueprint' must be either a string or array of strings");
                }
            }

            boolean requiresBlueprint = JsonHelper.getBoolean(json, "requires_blueprint", false);

            String tier = JsonHelper.getString(json, "tier", AnvilTier.IRON.getDisplayName());
            int hammering = JsonHelper.getInt(json, "hammering", 1);
            boolean hasQuality = JsonHelper.getBoolean(json, "has_quality", true);
            boolean needsMinigame = JsonHelper.getBoolean(json, "needs_minigame", false);
            boolean hasPolishing = JsonHelper.getBoolean(json, "has_polishing", true);

            boolean showNotification = JsonHelper.getBoolean(json, "show_notification", true);
            ForgingQuality minimumQuality = ForgingQuality.fromString(
                    JsonHelper.getString(json, "minimumQuality", ForgingQuality.POOR.getDisplayName())
            );
            ForgingQuality qualityDifficulty = ForgingQuality.fromString(
                    JsonHelper.getString(json, "quality_difficulty", ForgingQuality.NONE.getDisplayName())
            );

            Map<Character, ForgingIngredient> keyMap =
                    parseKey(JsonHelper.getObject(json, "key"));

            JsonArray pattern = JsonHelper.getArray(json, "pattern");
            final String tabKeyIn = JsonHelper.getString(json, "category", ForgingBookCategory.MISC.toString());
            ForgingBookCategory tabIn = ForgingBookCategory.findByName(tabKeyIn);
            if (tabIn == null) {
                tabIn = ForgingBookCategory.MISC; // safe fallback
            }
            int width = pattern.get(0).getAsString().length();
            int height = pattern.size();

            DefaultedList<ForgingIngredient> ingredients =
                    dissolvePattern(pattern, keyMap, width, height);

            ItemStack result =
                    ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));

            boolean defaultQuench = !(result.getItem() instanceof ArmorItem);
            boolean needQuenching =
                    JsonHelper.getBoolean(json, "need_quenching", defaultQuench);

            ItemStack failedResult =
                    ShapedRecipe.outputFromJson(
                            JsonHelper.getObject(
                                    json,
                                    "result_failed",
                                    JsonHelper.getObject(json, "result")
                            )
                    );

            return new ForgingRecipe(
                    recipeId,
                    group,
                    requiresBlueprint,
                    blueprintTypes,
                    tier,
                    ingredients,
                    result,
                    failedResult,
                    hammering,
                    hasQuality,
                    needsMinigame,
                    hasPolishing,
                    needQuenching,
                    showNotification,
                    minimumQuality,
                    qualityDifficulty,
                    width,
                    height,
                    tabIn
            );
        }


        @Override
        public ForgingRecipe read(Identifier recipeId, PacketByteBuf buffer) {
            String group = buffer.readString();
            boolean requiresBlueprint = buffer.readBoolean();
            int blueprintCount = buffer.readVarInt();
            Set<String> blueprintTypes = new LinkedHashSet<>();
            for (int i = 0; i < blueprintCount; i++) {
                blueprintTypes.add(buffer.readString());
            }
            String tier = buffer.readString();
            int hammering = buffer.readVarInt();
            boolean hasQuality = buffer.readBoolean();
            boolean needsMinigame = buffer.readBoolean();
            boolean hasPolishing = buffer.readBoolean();
            boolean needQuenching = buffer.readBoolean();
            boolean showNotification = buffer.readBoolean();
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            ForgingQuality minimumQuality = ForgingQuality.fromString(buffer.readString());
            DefaultedList<ForgingIngredient> ingredients =
                    DefaultedList.ofSize(
                            width * height,
                            new ForgingIngredient(Ingredient.EMPTY, false, false)
                    );

            ingredients.replaceAll(ignored ->
                    new ForgingIngredient(
                            Ingredient.fromPacket(buffer),
                            buffer.readBoolean(),
                            buffer.readBoolean()
                    )
            );

            ForgingQuality qualityDifficulty = ForgingQuality.fromString(buffer.readString());


            ItemStack result = buffer.readItemStack();
            ItemStack failedResult = buffer.readItemStack();

            String tabKey = buffer.readString();
            ForgingBookCategory tabIn = ForgingBookCategory.findByName(tabKey);
            if (tabIn == null) {
                tabIn = ForgingBookCategory.MISC;
            }

            return new ForgingRecipe(recipeId, group, requiresBlueprint, blueprintTypes, tier, ingredients, result, failedResult, hammering, hasQuality, needsMinigame, hasPolishing, needQuenching, showNotification, minimumQuality, qualityDifficulty, width, height, tabIn);
        }

        @Override
        public void write(PacketByteBuf buffer, ForgingRecipe recipe) {
            buffer.writeString(recipe.group);
            buffer.writeBoolean(recipe.requiresBlueprint);
            buffer.writeVarInt(recipe.blueprintTypes.size());
            for (String type : recipe.blueprintTypes) {
                buffer.writeString(type);
            }
            buffer.writeString(recipe.tier);
            buffer.writeVarInt(recipe.hammering);
            buffer.writeBoolean(recipe.hasQuality);
            buffer.writeBoolean(recipe.needsMinigame);
            buffer.writeBoolean(recipe.hasPolishing);
            buffer.writeBoolean(recipe.needQuenching);
            buffer.writeBoolean(recipe.showNotification);
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);
            buffer.writeString(recipe.minimumQuality.toString());

            for (ForgingIngredient ingredient : recipe.ingredients) {
                ingredient.ingredient.write(buffer);
                buffer.writeBoolean(ingredient.requiresHeated);
                buffer.writeBoolean(ingredient.transferNbt);
            }

            buffer.writeString(recipe.qualityDifficulty.toString());

            buffer.writeItemStack(recipe.result);
            buffer.writeItemStack(recipe.failedResult);
            buffer.writeString(recipe.tab.getFolderName());
        }
    }

    public record ForgingIngredient(
            Ingredient ingredient,
            boolean requiresHeated,
            boolean transferNbt
    ) {
        public boolean test(ItemStack stack) {
            if (!ingredient.test(stack)) return false;

            if (requiresHeated) {
                if (!stack.hasNbt()) return false;
                if (!stack.getNbt().getBoolean("Heated")) return false;
            }

            return true;
        }
    }

    private static void mergeCompound(NbtCompound target, NbtCompound source) {
        for (String key : source.getKeys()) {
            if (target.contains(key)
                    && target.get(key) instanceof NbtCompound t
                    && source.get(key) instanceof NbtCompound s) {
                // Deep merge
                mergeCompound(t, s);
            } else {
                // Overwrite or add
                target.put(key, source.get(key).copy());
            }
        }
    }

}
