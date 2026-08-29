package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import net.stirdrem.overgeared.Overgeared;

public class FletchingRecipe implements Recipe<Inventory> {
    private final Identifier id;
    private final Ingredient tip, shaft, feather, potion;
    private final ItemStack result;
    private final ItemStack resultTipped;
    private final ItemStack resultLingering;
    private final String tippedTag;
    private final String lingeringTag;

    public FletchingRecipe(Identifier id, Ingredient tip, Ingredient shaft, Ingredient feather, Ingredient potion,
                            ItemStack result, ItemStack resultTipped, ItemStack resultLingering,
                            String tippedTag, String lingeringTag) {
        this.id = id;
        this.tip = tip;
        this.shaft = shaft;
        this.feather = feather;
        this.potion = potion != null ? potion : Ingredient.EMPTY;
        this.result = result;
        this.resultTipped = resultTipped;
        this.resultLingering = resultLingering;
        this.tippedTag = tippedTag != null ? tippedTag : "Potion";
        this.lingeringTag = lingeringTag != null ? lingeringTag : "LingeringPotion";
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return (tip == Ingredient.EMPTY || tip.test(inventory.getStack(0))) &&
                (shaft == Ingredient.EMPTY || shaft.test(inventory.getStack(1))) &&
                (feather == Ingredient.EMPTY || feather.test(inventory.getStack(2))) &&
                (potion == Ingredient.EMPTY || potion.test(inventory.getStack(3)));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryAccess) {
        return getDefaultResult();
    }

    public Ingredient getTip() {
        return tip;
    }

    public Ingredient getShaft() {
        return shaft;
    }

    public Ingredient getFeather() {
        return feather;
    }

    public Ingredient getPotion() {
        return potion;
    }

    public boolean hasPotion() {
        return potion != null && !potion.isEmpty();
    }

    public ItemStack getDefaultResult() {
        return result.copy();
    }

    public ItemStack getTippedResult() {
        return resultTipped.copy();
    }

    public ItemStack getLingeringResult() {
        return resultLingering.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager access) {
        return getDefaultResult();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FLETCHING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FLETCHING;
    }

    public boolean hasTippedResult() {
        return !resultTipped.isEmpty();
    }

    public boolean hasLingeringResult() {
        return !resultLingering.isEmpty();
    }

    public String getTippedTag() {
        return tippedTag;
    }

    public String getLingeringTag() {
        return lingeringTag;
    }

    public static class Type implements RecipeType<FletchingRecipe> {
        public static final FletchingRecipe.Type INSTANCE = new FletchingRecipe.Type();
        public static final String ID = "fletching";
    }

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {
        public static final FletchingRecipe.Serializer INSTANCE = new FletchingRecipe.Serializer();
        public static final Identifier ID = new Identifier(Overgeared.MOD_ID, "fletching");

        @Override
        public FletchingRecipe read(Identifier id, JsonObject json) {
            JsonObject material = JsonHelper.getObject(json, "material");

            // Allow tip, shaft, feather to be optional
            Ingredient tip = material.has("tip") ? Ingredient.fromJson(material.get("tip")) : Ingredient.EMPTY;
            Ingredient shaft = material.has("shaft") ? Ingredient.fromJson(material.get("shaft")) : Ingredient.EMPTY;
            Ingredient feather = material.has("feather") ? Ingredient.fromJson(material.get("feather")) : Ingredient.EMPTY;

            // Optional potion
            Ingredient potion = json.has("potion") ? Ingredient.fromJson(json.get("potion")) : Ingredient.EMPTY;

            // Base result
            ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));

            // Optional tipped result
            ItemStack resultTipped = ItemStack.EMPTY;
            String tippedTag = null;
            if (json.has("result_tipped")) {
                JsonObject tippedJson = JsonHelper.getObject(json, "result_tipped");
                resultTipped = ShapedRecipe.outputFromJson(tippedJson);
                if (tippedJson.has("tag")) {
                    tippedTag = JsonHelper.getString(tippedJson, "tag");
                }
            }

            // Optional lingering result
            ItemStack resultLingering = ItemStack.EMPTY;
            String lingeringTag = null;
            if (json.has("result_lingering")) {
                JsonObject lingeringJson = JsonHelper.getObject(json, "result_lingering");
                resultLingering = ShapedRecipe.outputFromJson(lingeringJson);
                if (lingeringJson.has("tag")) {
                    lingeringTag = JsonHelper.getString(lingeringJson, "tag");
                }
            }

            return new FletchingRecipe(id, tip, shaft, feather, potion, result,
                    resultTipped, resultLingering,
                    tippedTag, lingeringTag);
        }

        @Override
        public FletchingRecipe read(Identifier id, PacketByteBuf buffer) {
            // Read ingredients (can be EMPTY)
            Ingredient tip = Ingredient.fromPacket(buffer);
            Ingredient shaft = Ingredient.fromPacket(buffer);
            Ingredient feather = Ingredient.fromPacket(buffer);
            Ingredient potion = Ingredient.fromPacket(buffer);

            // Read result stacks
            ItemStack result = buffer.readItemStack();
            ItemStack resultTipped = buffer.readItemStack();
            ItemStack resultLingering = buffer.readItemStack();

            // Read optional tags
            String tippedTag = buffer.readBoolean() ? buffer.readString() : null;
            String lingeringTag = buffer.readBoolean() ? buffer.readString() : null;

            return new FletchingRecipe(id, tip, shaft, feather, potion, result,
                    resultTipped, resultLingering,
                    tippedTag, lingeringTag);
        }

        @Override
        public void write(PacketByteBuf buffer, FletchingRecipe recipe) {
            // Write ingredients (Ingredient.EMPTY is supported by vanilla serializer)
            recipe.tip.write(buffer);
            recipe.shaft.write(buffer);
            recipe.feather.write(buffer);
            recipe.potion.write(buffer);

            // Write result stacks
            buffer.writeItemStack(recipe.result);
            buffer.writeItemStack(recipe.resultTipped);
            buffer.writeItemStack(recipe.resultLingering);

            // Write optional tags
            if (recipe.tippedTag != null) {
                buffer.writeBoolean(true);
                buffer.writeString(recipe.tippedTag);
            } else {
                buffer.writeBoolean(false);
            }

            if (recipe.lingeringTag != null) {
                buffer.writeBoolean(true);
                buffer.writeString(recipe.lingeringTag);
            } else {
                buffer.writeBoolean(false);
            }
        }

    }
}
