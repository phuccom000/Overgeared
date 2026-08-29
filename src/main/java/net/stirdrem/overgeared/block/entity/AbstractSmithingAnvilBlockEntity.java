package net.stirdrem.overgeared.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.item.custom.BlueprintItem;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ItemStackHandler;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.stirdrem.overgeared.Overgeared.getCooledItem;

/**
 * Fabric has no equivalent of Forge's IItemHandler capability system, so hopper/automation
 * interaction is implemented directly via Inventory/SidedInventory instead of a separate
 * capability object. Block entity sync also relies on the default full-NBT
 * toInitialChunkDataNbt()/toUpdatePacket() implementation rather than porting the original's
 * smaller custom update tag - functionally equivalent, just a little more data per sync packet.
 */
public abstract class AbstractSmithingAnvilBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory, SidedInventory {
    protected static final int INPUT_SLOT = 0;
    protected static final int OUTPUT_SLOT = 10;
    protected static final int BLUEPRINT_SLOT = 11;

    protected boolean needsRecipeUpdate = true;
    protected Optional<ForgingRecipe> cachedRecipe = Optional.empty();
    protected final ItemStackHandler itemHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            if (!world.isClient) {
                world.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
            }
            needsRecipeUpdate = true;
        }
    };

    protected final PropertyDelegate data;

    protected int progress;
    protected int maxProgress;
    protected int hitRemains = 0;
    protected long busyUntilGameTime = 0L;
    protected UUID ownerUUID = null;
    protected AnvilTier anvilTier;
    protected long sessionStartTime = 0L; // optional, for timeout logic
    protected ItemStack failedResult;
    protected PlayerEntity player;
    protected ForgingRecipe lastRecipe = null;
    protected ItemStack lastBlueprint = ItemStack.EMPTY;
    private boolean minigameOn = false;
    protected AbstractSmithingAnvil anvilBlock;

    public AbstractSmithingAnvilBlockEntity(AbstractSmithingAnvil anvilBlock, AnvilTier tier, BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        this.anvilTier = tier;
        this.anvilBlock = anvilBlock;
        this.data = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> AbstractSmithingAnvilBlockEntity.this.progress;
                    case 1 -> AbstractSmithingAnvilBlockEntity.this.maxProgress;
                    case 2 -> AbstractSmithingAnvilBlockEntity.this.hitRemains;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AbstractSmithingAnvilBlockEntity.this.progress = value;
                    case 1 -> AbstractSmithingAnvilBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int size() {
                return 3;
            }
        };
    }

    public ItemStack getRenderStack(int index) {
        return itemHandler.getStackInSlot(index);
    }

    public void drops() {
        SimpleInventory inventory = new SimpleInventory(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setStack(i, itemHandler.getStackInSlot(i));
        }
        ItemScatterer.spawn(this.world, this.pos, inventory);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("gui.overgeared.smithing_anvil");
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("hitRemains", hitRemains);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.put("inventory", itemHandler.serializeNBT());

        if (ownerUUID != null) {
            tag.putUuid("ownerUUID", ownerUUID);
            tag.putLong("sessionStartTime", sessionStartTime);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("inventory"));
        }

        hitRemains = tag.getInt("hitRemains");
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");

        if (tag.containsUuid("ownerUUID")) {
            ownerUUID = tag.getUuid("ownerUUID");
            sessionStartTime = tag.getLong("sessionStartTime");
        } else {
            ownerUUID = null;
            sessionStartTime = 0L;
        }

        // The cached recipe must be recalculated after loading.
        needsRecipeUpdate = true;
        cachedRecipe = Optional.empty();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
    
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public void increaseForgingProgress(World pLevel, BlockPos pPos, BlockState pState) {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (hasRecipe()) {
            ForgingRecipe currentRecipe = recipe.get();
            maxProgress = currentRecipe.getHammeringRequired();
            increaseCraftingProgress();
            markDirty(pLevel, pPos, pState);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    public void resetProgress() {
        progress = 0;
        maxProgress = 0;
        lastRecipe = null;
        if (!world.isClient) {
            ModEvents.resetMinigameForPlayer((ServerPlayerEntity) player);
            AbstractSmithingAnvil.setQuality(null);
        }
        player = null;
    }

    protected void craftItem() {
        Optional<ForgingRecipe> opt = getCurrentRecipe();
        if (opt.isEmpty()) return;

        ForgingRecipe recipe = opt.get();
        ItemStack result = recipe.getOutput(world.getRegistryManager());
        failedResult = recipe.getFailedResultItem(world.getRegistryManager());

        // Collect max ingredient quality
        ForgingQuality maxIngredientQuality = null;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.hasNbt()) continue;

            NbtCompound tag = stack.getNbt();
            if (tag == null || !tag.contains("ForgingQuality", NbtElement.STRING_TYPE)) {
                continue;
            }

            ForgingQuality q = ForgingQuality.fromString(tag.getString("ForgingQuality"));
            if (q == null) continue;

            if (maxIngredientQuality == null || q.ordinal() > maxIngredientQuality.ordinal()) {
                maxIngredientQuality = q;
            }
        }

        NbtCompound resultTag = result.getNbt();
        if (resultTag == null) resultTag = new NbtCompound();
        // Base result NBT
        if (recipe.hasQuality()
                && player != null
                && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            resultTag.putString("Creator", player.getName().getString());
        }

        if (recipe.needQuenching()
                && !result.isIn(ModTags.Items.HEATED_METALS)
                && !result.isIn(ModTags.Items.HOT_ITEMS)) {
            resultTag.putBoolean("Heated", true);
        }

        // Quality & minigame resolution
        if (ServerConfig.ENABLE_MINIGAME.get()
                && (recipe.hasQuality() || recipe.needsMinigame())) {

            ForgingQuality quality =
                    ForgingQuality.fromString(determineForgingQuality());

            if (quality != null && quality != ForgingQuality.NONE) {

                // Clamp minimum
                ForgingQuality minimum = recipe.getMinimumQuality();
                if (minimum != null && quality.ordinal() < minimum.ordinal()) {
                    quality = minimum;
                }

                // Clamp ingredient max
                if (maxIngredientQuality != null
                        && ServerConfig.INGREDIENTS_DEFINE_MAX_QUALITY.get()
                        && quality.ordinal() > maxIngredientQuality.ordinal()) {
                    quality = maxIngredientQuality;
                }

                // PERFECT -> MASTER roll
                if (quality == ForgingQuality.PERFECT
                        && ServerConfig.MASTER_QUALITY_CHANCE.get() > 0
                        && world.random.nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get()) {
                    quality = ForgingQuality.MASTER;
                }

                // Apply quality NBT
                if (recipe.hasQuality()) {
                    resultTag.putString("ForgingQuality", quality.getDisplayName());

                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        ModAdvancementTriggers.FORGING_QUALITY
                                .trigger(serverPlayer, quality.getDisplayName());
                    }
                    if (!(result.getItem() instanceof ArmorItem)
                            && !(result.getItem() instanceof ShieldItem)
                            && recipe.hasPolishing()) {
                        resultTag.putBoolean("Polished", false);
                    }
                }
                if (!failedResult.isEmpty() & rollFailure(quality)) {
                    result = failedResult.copy();
                }
            }
        }
        if (!resultTag.isEmpty())
            result.setNbt(resultTag);

        transferIngredientNBT(result, recipe);


        for (int i = 0; i < 9; i++) {
            itemHandler.extractItem(i, 1, false);
        }


        ItemStack existing = itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (existing.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result);
            return;
        }

        if (!ItemStack.canCombine(existing, result)) return;

        int total = existing.getCount() + result.getCount();
        int max = Math.min(existing.getMaxCount(),
                itemHandler.getSlotLimit(OUTPUT_SLOT));

        if (total <= max) {
            existing.increment(result.getCount());
        } else {
            int overflow = total - max;
            existing.setCount(max);

            ItemStack drop = result.copy();
            drop.setCount(overflow);
            ItemScatterer.spawn(world,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    drop);
        }

        itemHandler.setStackInSlot(OUTPUT_SLOT, existing);
    }

    private boolean rollFailure(ForgingQuality quality) {
        return switch (quality) {
            case POOR -> true;
            case WELL -> world.random.nextFloat()
                    < ServerConfig.FAIL_ON_WELL_QUALITY_CHANCE.get();
            case EXPERT -> world.random.nextFloat()
                    < ServerConfig.FAIL_ON_EXPERT_QUALITY_CHANCE.get();
            default -> false;
        };
    }

    protected void craftItemWithBlueprint() {

        // Get the crafted output item
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Skip blueprint progression if crafting failed
        if (result.isEmpty()) return;

        // Handle blueprint progression (slot 11)
        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
        if (!blueprint.isEmpty() && blueprint.hasNbt()) {
            NbtCompound tag = blueprint.getOrCreateNbt();

            if (tag.contains("Quality") && tag.contains("Uses")) {
                String currentQualityStr = tag.getString("Quality");
                int uses = tag.getInt("Uses");
                int usesToLevel = BlueprintItem.getUsesToNextLevel(blueprint);

                BlueprintQuality currentQuality = BlueprintQuality.fromString(currentQualityStr);

                // Attempt to read the ForgingQuality from result
                String forgingQualityStr = anvilBlock.getQuality();
                ForgingQuality resultQuality = ForgingQuality.fromString(forgingQualityStr);

                if (currentQuality != null && currentQuality != BlueprintQuality.PERFECT && currentQuality != BlueprintQuality.MASTER) {
                    if (!ServerConfig.EXPERT_ABOVE_INCREASE_BLUEPRINT.get() || resultQuality.ordinal() >= ForgingQuality.EXPERT.ordinal()) {
                        uses += switch (resultQuality) {
                            case PERFECT -> 2;
                            case MASTER -> 3;
                            default -> 1;
                        };
                    }


                    // Level up if threshold reached
                    if (uses >= usesToLevel) {
                        BlueprintQuality nextQuality = BlueprintQuality.getNext(currentQuality);
                        if (nextQuality != null) {
                            tag.putString("Quality", nextQuality.getDisplayName());
                            tag.putInt("Uses", 0);
                            if (player instanceof ServerPlayerEntity serverPlayer) {
                                if (nextQuality.equals(BlueprintQuality.PERFECT) || nextQuality.equals(BlueprintQuality.MASTER))
                                    ModAdvancementTriggers.MAX_LEVEL_BLUEPRINT.trigger(serverPlayer);
                                ModAdvancementTriggers.BLUEPRINT_QUALITY.trigger(serverPlayer, nextQuality.getDisplayName());
                            }
                        } else {
                            tag.putInt("Uses", usesToLevel); // Clamp
                        }


                    } else {
                        tag.putInt("Uses", uses); // Just increment
                    }

                    blueprint.setNbt(tag);
                    this.itemHandler.setStackInSlot(BLUEPRINT_SLOT, blueprint);
                }
            }
        }
    }

    private void transferIngredientNBT(ItemStack result, ForgingRecipe recipe) {
        NbtCompound resultTag = result.getNbt();
        if (resultTag == null)
            resultTag = new NbtCompound();

        List<ForgingRecipe.ForgingIngredient> ingredients =
                recipe.getForgingIngredients();

        int transferredDamage = Integer.MAX_VALUE;
        boolean foundDamage = false;

        for (int slot = 0; slot < Math.min(9, ingredients.size()); slot++) {
            ForgingRecipe.ForgingIngredient forgingIngredient =
                    ingredients.get(slot);

            if (!forgingIngredient.transferNbt()) continue;

            ItemStack ingredientStack = itemHandler.getStackInSlot(slot);
            if (ingredientStack.isEmpty()) continue;

            // Damage transfer (lowest)
            if (ingredientStack.isDamageable()
                    && result.isDamageable()) {

                transferredDamage = Math.min(
                        transferredDamage,
                        ingredientStack.getDamage()
                );
                foundDamage = true;
            }

            // NBT transfer
            if (!ingredientStack.hasNbt()) continue;

            NbtCompound ingredientTag = ingredientStack.getNbt();
            if (ingredientTag == null) continue;

            for (String key : ingredientTag.getKeys()) {
                if (key.equals("ForgingQuality")
                        || key.equals("Creator")
                        || key.equals("Heated")
                        || key.equals("Damage")) {
                    continue;
                }

                resultTag.put(key, ingredientTag.get(key).copy());
            }
        }

        if (foundDamage && result.isDamageable()) {
            result.setDamage(
                    Math.min(transferredDamage, result.getMaxDamage() - 1)
            );
        }

        if (!resultTag.isEmpty()) {
            result.setNbt(resultTag);
        }
    }


    public boolean isFailedResult() {
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        return ItemStack.areItemsEqual(result, failedResult);
    }

    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        AnvilTier requiredTier = AnvilTier.fromDisplayName(recipe.getAnvilTier());

        if (requiredTier == null || requiredTier.isEqualOrLowerThan(this.anvilTier)) {
            return false;
        }

        ItemStack resultStack = recipe.getOutput(world.getRegistryManager());

        return canInsertItemIntoOutputSlot(resultStack, recipe)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }

    public boolean hasRecipeWithBlueprint() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        // Tier check
        AnvilTier requiredTier = AnvilTier.fromDisplayName(recipe.getAnvilTier());
        if (requiredTier == null || requiredTier.isEqualOrLowerThan(this.anvilTier)) {
            return false;
        }

        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);

        if (recipe.requiresBlueprint()) {
            // Must have a valid matching blueprint
            if (blueprint.isEmpty() || !blueprint.hasNbt() || !blueprint.getNbt().contains("ToolType")) {
                return false;
            }

            String blueprintToolType = blueprint.getNbt().getString("ToolType").toLowerCase(Locale.ROOT);
            if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                return false;
            }
        } else {
            // Optional blueprint: if present, it must match
            if (!blueprint.isEmpty() && blueprint.hasNbt() && blueprint.getNbt().contains("ToolType")) {
                String blueprintToolType = blueprint.getNbt().getString("ToolType").toLowerCase(Locale.ROOT);
                if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                    return false;
                }
            }
        }

        ItemStack resultStack = recipe.getOutput(world.getRegistryManager());
        return canInsertItemIntoOutputSlot(resultStack, recipe)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }

    public Optional<ForgingRecipe> getCurrentRecipe() {
        if (world == null) return Optional.empty();

        if (needsRecipeUpdate) {
            SimpleInventory inventory = new SimpleInventory(this.itemHandler.getSlots());
            for (int i = 0; i < 9; i++) {
                inventory.setStack(i, itemHandler.getStackInSlot(i));
            }
            inventory.setStack(11, itemHandler.getStackInSlot(11));

            cachedRecipe = ForgingRecipe.findBestMatch(world, inventory)
                    .filter(this::matchesRecipeExactly);

            needsRecipeUpdate = false;
        }

        return cachedRecipe;
    }

    protected boolean canInsertItemIntoOutputSlot(ItemStack stackToInsert, ForgingRecipe currentRecipe) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (!existing.isEmpty() && currentRecipe != null && currentRecipe.hasFailedResult()) {
            return false;
        }

        return existing.isEmpty()
                || ItemStack.canCombine(existing, stackToInsert);
    }

    protected boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            return true;
        }
        return existing.getCount() + count <= existing.getMaxCount();
    }

    public boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    public void increaseCraftingProgress() {
        progress++;

        markDirty();

        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }

        if (data != null) {
            data.set(0, progress);
            data.set(1, maxProgress);
            data.set(2, hitRemains);
        }
    }


    public boolean isBusy(long currentGameTime) {
        return currentGameTime < busyUntilGameTime;
    }

    public void setBusyUntil(long time) {
        this.busyUntilGameTime = time;
        markDirty(world, pos, getCachedState());
    }


    public void tick(World lvl, BlockPos pos, BlockState st) {
        if (!pos.equals(this.pos)) return; // sanity check
        tickHeatedIngredients(lvl);
        try {
            // Check if blueprint changed mid-forging
            ItemStack currentBlueprint = this.itemHandler.getStackInSlot(11);
            if (!ItemStack.canCombine(currentBlueprint, lastBlueprint)) {
                if (progress > 0 || lastRecipe != null || isMinigameOn()) {
                    resetProgress();
                    setMinigameOn(false);
                    Overgeared.LOGGER.debug("Blueprint changed at {}, minigame reset", pos);
                }
            }
            lastBlueprint = currentBlueprint.copy();

            Optional<ForgingRecipe> currentRecipeOpt = getCurrentRecipe();
            if (currentRecipeOpt.isEmpty()) {
                if (progress > 0 || lastRecipe != null) {
                    resetProgress();
                }
                return;
            }

            ForgingRecipe currentRecipe = currentRecipeOpt.get();

            boolean recipeChanged = false;
            if (lastRecipe != null) {
                recipeChanged = !currentRecipe.getId().equals(lastRecipe.getId());
            } else if (maxProgress > 0) {
                recipeChanged = true;
            }

            if (recipeChanged) {
                resetProgress();
                lastRecipe = currentRecipe;
                return;
            }

            lastRecipe = currentRecipe;

            if (hasRecipe()) {
                maxProgress = currentRecipe.getHammeringRequired();
                hitRemains = maxProgress - progress;
                markDirty(lvl, pos, st);

                if (hasProgressFinished()) {
                    craftItem();
                    resetProgress();
                }
            } else {
                if (progress > 0 || maxProgress > 0) {
                    resetProgress();
                }
            }
        } catch (Exception e) {
            Overgeared.LOGGER.error("Error ticking smithing anvil at {}", pos, e);
            resetProgress();
        }

    }

    public int getHitsRemaining() {
        return maxProgress - progress;
    }

    public PropertyDelegate getContainerData() {
        return data;
    }

    protected boolean matchesRecipeExactly(ForgingRecipe recipe) {
        SimpleInventory inventory = new SimpleInventory(this.itemHandler.getSlots()); // 3x3 grid
        // Copy items from input slots (0-8) to our 3x3 grid
        for (int i = 0; i < 9; i++) {
            inventory.setStack(i, this.itemHandler.getStackInSlot(i));
        }
        inventory.setStack(11, this.itemHandler.getStackInSlot(11));
        return recipe.matches(inventory, world);
    }

    protected String determineForgingQuality() {
        String quality = anvilBlock.getQuality();
        if (quality == null) return "well";
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        ForgingRecipe recipe = recipeOptional.get();
        if (!recipe.getBlueprintTypes().isEmpty()) {

            ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);

            // Define tool quality tiers in order of strength
            List<String> qualityTiers = List.of("poor", "well", "expert", "perfect", "master");

            // If blueprint is missing or invalid, fallback logic
            if (blueprint.isEmpty() || !blueprint.hasNbt()) {
                return switch (quality.toLowerCase(Locale.ROOT)) {
                    case "poor" -> ForgingQuality.POOR.getDisplayName();
                    default -> "well"; // Cap quality at 'well' without blueprint
                };
            }

            NbtCompound nbt = blueprint.getNbt();
            if (nbt == null || !nbt.contains("Quality")) {
                return switch (quality.toLowerCase(Locale.ROOT)) {
                    case "poor" -> ForgingQuality.POOR.getDisplayName();
                    default -> "well"; // Cap quality at 'well' without ToolType
                };
            }

            String blueprintToolType = nbt.getString("Quality").toLowerCase(Locale.ROOT);

            // Determine capped quality
            int anvilTierIndex = qualityTiers.indexOf(quality.toLowerCase(Locale.ROOT));
            int blueprintTierIndex = qualityTiers.indexOf(blueprintToolType);

            // Default to lowest if any tier is missing
            if (anvilTierIndex == -1 || blueprintTierIndex == -1) {
                return ForgingQuality.NONE.getDisplayName();
            }

            int finalIndex = Math.min(anvilTierIndex, blueprintTierIndex);

            switch (qualityTiers.get(finalIndex)) {
                case "poor":
                    return ForgingQuality.POOR.getDisplayName();
                case "expert":
                    return ForgingQuality.EXPERT.getDisplayName();
                case "perfect": {
                    Random random = new Random();

                    // Check if any crafting slot contains a Master-quality ingredient
                    boolean hasMasterIngredient = false;
                    for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                        if (i == OUTPUT_SLOT || i == BLUEPRINT_SLOT) continue; // skip output + blueprint
                        ItemStack stack = this.itemHandler.getStackInSlot(i);
                        if (!stack.isEmpty() && stack.hasNbt() && stack.getNbt().contains("ForgingQuality")) {
                            String ingQuality = stack.getNbt().getString("ForgingQuality").toLowerCase(Locale.ROOT);
                            if ("master".equals(ingQuality)) {
                                hasMasterIngredient = true;
                                break;
                            }
                        }
                    }

                    // Normal Master roll from config
                    boolean masterRoll = ServerConfig.MASTER_QUALITY_CHANCE.get() != 0
                            && random.nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get();

                    // Ingredient-based boost
                    boolean ingredientMasterRoll = hasMasterIngredient
                            && random.nextFloat() < ServerConfig.MASTER_FROM_INGREDIENT_CHANCE.get();

                    if ("master".equals(blueprintToolType) || masterRoll || ingredientMasterRoll) {
                        return ForgingQuality.MASTER.getDisplayName();
                    } else {
                        return ForgingQuality.PERFECT.getDisplayName();
                    }
                }
                case "master":
                    return ForgingQuality.MASTER.getDisplayName();
                default:
                    return ForgingQuality.WELL.getDisplayName();
            }
        }
        return quality;
    }

    protected String determineForgingQualityNoBlueprint() {
        String quality = anvilBlock.getQuality();
        if (quality == null) {
            return ForgingQuality.POOR.getDisplayName(); // Default quality
        }
        if (quality.equals(ForgingQuality.PERFECT.getDisplayName())) {
            Random random = new Random();

            // Check if any crafting slot contains a Master-quality ingredient
            boolean hasMasterIngredient = false;
            for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                if (i == OUTPUT_SLOT || i == BLUEPRINT_SLOT) continue; // skip output + blueprint
                ItemStack stack = this.itemHandler.getStackInSlot(i);
                if (!stack.isEmpty() && stack.hasNbt() && stack.getNbt().contains("ForgingQuality")) {
                    String ingQuality = stack.getNbt().getString("ForgingQuality").toLowerCase(Locale.ROOT);
                    if ("master".equals(ingQuality)) {
                        hasMasterIngredient = true;
                        break;
                    }
                }
            }

            // Normal Master roll from config
            boolean masterRoll = ServerConfig.MASTER_QUALITY_CHANCE.get() != 0
                    && random.nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get();

            // Ingredient-based boost
            boolean ingredientMasterRoll = hasMasterIngredient
                    && random.nextFloat() < ServerConfig.MASTER_FROM_INGREDIENT_CHANCE.get();

            if (masterRoll || ingredientMasterRoll) {
                return ForgingQuality.MASTER.getDisplayName();
            } else {
                return ForgingQuality.PERFECT.getDisplayName();
            }
        } else
            return quality;
    }

    public String minigameQuality() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) {
            return "none"; // no recipe = base fallback
        }

        ForgingRecipe recipe = recipeOptional.get();
        if (!recipe.getBlueprintTypes().isEmpty()) {
            if (!recipe.getQualityDifficulty().equals(ForgingQuality.NONE))
                return recipe.getQualityDifficulty().getDisplayName();
            else return blueprintQuality();
        } else return recipe.getQualityDifficulty().getDisplayName();
    }

    public String blueprintQuality() {
        String quality = anvilBlock.getQuality();
        if (quality == null) {
            return ForgingQuality.NONE.getDisplayName(); // fallback when global quality is missing
        }

        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) {
            return "poor"; // no recipe = base fallback
        }

        ForgingRecipe recipe = recipeOptional.get();
        if (!recipe.getBlueprintTypes().isEmpty()) {
            if (!recipe.getQualityDifficulty().equals(ForgingQuality.NONE))
                return recipe.getQualityDifficulty().getDisplayName();
            ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);

            // Quality tiers in order
            List<String> qualityTiers = List.of("poor", "well", "expert", "perfect", "master");

            // Missing or invalid blueprint -> cap quality
            String poor = quality.equalsIgnoreCase("poor")
                    ? ForgingQuality.POOR.getDisplayName()
                    : ForgingQuality.NONE.getDisplayName();
            if (blueprint.isEmpty() || !blueprint.hasNbt()) {
                return poor;
            }

            NbtCompound nbt = blueprint.getNbt();
            if (nbt == null || !nbt.contains("Quality")) {
                return poor;
            }

            String bpQuality = nbt.getString("Quality").toLowerCase(Locale.ROOT);
            // ensure it's in our tier list, otherwise default
            return qualityTiers.contains(bpQuality) ? bpQuality : ForgingQuality.NONE.getDisplayName();
        }

        return ForgingQuality.NONE.getDisplayName(); // fallback if no blueprint types
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.markDirty();

        // Force sync to client
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }

        if (this.data != null) {
            this.data.set(0, progress);
        }
    }

    public int getRequiredProgress() {
        return getCurrentRecipe()
                .map(ForgingRecipe::getHammeringRequired)
                .orElse(0); // default to 0 if recipe is empty
    }

    public int getProgress() {
        if (world != null && world.isClient && data != null) {
            // On client, get from synced container data
            return data.get(0);
        }
        return this.progress;
    }

    public void setOwner(UUID uuid) {
        ownerUUID = uuid;
        sessionStartTime = world.getTime();
        markDirty();
    }

    public void clearOwner() {
        ownerUUID = null;
        sessionStartTime = 0L;
        markDirty();
    }

    public boolean isOwnedBy(PlayerEntity player) {
        return ownerUUID != null && ownerUUID.equals(player.getUuid());
    }

    public boolean isOwnedByOther(PlayerEntity player) {
        return ownerUUID != null && !ownerUUID.equals(player.getUuid());
    }

    public boolean hasQuality() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        // Only set quality if recipe supports it
        return recipe.hasQuality();
    }

    public boolean needsMinigame() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        // Only set quality if recipe supports it
        return !recipe.hasQuality() && recipe.needsMinigame();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean isMinigameOn() {
        return minigameOn;
    }

    public void setMinigameOn(boolean value) {
        this.minigameOn = value;
        markDirty(); // mark dirty for save
    }

    private static final String HEATED_TIME_TAG = "HeatedSince";

    public void tickHeatedIngredients(World world) {
        if (world.isClient) return;
        long tick = world.getTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!stack.isIn(ModTags.Items.HEATED_METALS)) continue;

            NbtCompound tag = stack.getNbt();
            if (tag == null) tag = new NbtCompound();
            long heatedSince = tag.getLong(HEATED_TIME_TAG);

            // Initialize timestamp if not present
            if (heatedSince == 0L) {
                tag.putLong(HEATED_TIME_TAG, tick);
                continue;
            }

            // Cooldown complete -> convert to cooled version
            if (tick - heatedSince >= cooldownTicks) {
                Item cooled = getCooledItem(stack.getItem(), world);
                if (cooled != null) {
                    ItemStack newStack = new ItemStack(cooled, stack.getCount());
                    // Preserve quality or other metadata if needed
                    if (stack.hasNbt()) {
                        NbtCompound oldTag = stack.getNbt().copy();
                        oldTag.remove(HEATED_TIME_TAG);
                        if (oldTag.isEmpty()) {
                            newStack.setNbt(null); // fully clear
                        } else {
                            newStack.setNbt(oldTag);
                        }
                    }
                    world.playSound(
                            null,                              // no player (broadcast to all nearby)
                            pos,                                // block position
                            SoundEvents.BLOCK_FIRE_EXTINGUISH, // extinguish sound
                            SoundCategory.BLOCKS,               // sound category
                            1.0F,                              // volume
                            1.0F                               // pitch
                    );
                    itemHandler.setStackInSlot(slot, newStack);
                    world.updateListeners(pos, getCachedState(), getCachedState(), 3);
                }
            }
        }
    }

    public AnvilTier getAnvilTier() {
        return anvilTier;
    }

    public boolean tryStartMinigame(ServerPlayerEntity player) {

        if (minigameOn) return false;

        if (ownerUUID != null && !ownerUUID.equals(player.getUuid())) {
            return false;
        }

        ownerUUID = player.getUuid();
        minigameOn = true;
        sessionStartTime = world.getTime();

        markDirty();
        world.updateListeners(pos, getCachedState(), getCachedState(), 3);

        return true;
    }

    // ---------------- Inventory / SidedInventory (replaces the Forge IItemHandler capability) ----------------

    @Override
    public int size() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return itemHandler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return itemHandler.extractItem(slot, itemHandler.getStackInSlot(slot).getCount(), false);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return world != null && world.getBlockEntity(pos) == this
                && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{OUTPUT_SLOT};
        }
        int[] slots = new int[itemHandler.getSlots()];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return dir != Direction.DOWN && itemHandler.isItemValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir != Direction.DOWN || slot == OUTPUT_SLOT;
    }
}
