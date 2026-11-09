package net.stirdrem.overgeared.block.entity;

import net.minecraft.world.item.SmithingTemplateItem;
import net.stirdrem.overgeared.item.ModItems;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.item.custom.BlueprintItem;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ItemListInput;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RecipeLookup;
import net.stirdrem.overgeared.util.ItemStackHandler;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.stirdrem.overgeared.Overgeared.getCooledItem;

/**
 * Fabric has no equivalent of Forge's IItemHandler capability system, so hopper/automation
 * interaction is implemented directly via Inventory/SidedInventory instead of a separate
 * capability object. Block entity sync sends the full saved data (getUpdateTag) rather than
 * porting the original's smaller custom update tag - functionally equivalent, just a little more
 * data per sync packet. Recipes are looked up through RecipeLookup (works on both sides).
 */
public abstract class AbstractSmithingAnvilBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos>, Container, WorldlyContainer {
    protected static final int INPUT_SLOT = 0;
    protected static final int HAMMER_SLOT = 9;
    protected static final int OUTPUT_SLOT = 10;
    protected static final int BLUEPRINT_SLOT = 11;

    protected boolean needsRecipeUpdate = true;
    protected Optional<RecipeHolder<ForgingRecipe>> cachedRecipe = Optional.empty();
    protected final ItemStackHandler itemHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            needsRecipeUpdate = true;
        }
    };

    protected final ContainerData data;

    protected int progress;
    protected int maxProgress;
    protected int hitRemains = 0;
    protected long busyUntilGameTime = 0L;
    protected UUID ownerUUID = null;
    protected AnvilTier anvilTier;
    protected long sessionStartTime = 0L; // optional, for timeout logic
    protected ItemStack failedResult;
    protected Player player;
    protected RecipeHolder<ForgingRecipe> lastRecipe = null;
    protected ItemStack lastBlueprint = ItemStack.EMPTY;
    private boolean minigameOn = false;
    protected AbstractSmithingAnvil anvilBlock;

    public AbstractSmithingAnvilBlockEntity(AbstractSmithingAnvil anvilBlock, AnvilTier tier, BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        this.anvilTier = tier;
        this.anvilBlock = anvilBlock;
        this.data = new ContainerData() {
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
            public int getCount() {
                return 3;
            }
        };
    }

    public ItemStack getRenderStack(int index) {
        return itemHandler.getStackInSlot(index);
    }

    public void drops() {
        if (this.level != null) Containers.dropContents(this.level, this.worldPosition, this);
    }

    /**
     * 26.x replacement for the block's onRemove: super drops the contents (vanilla handles any
     * BlockEntity that is a Container), then the minigame is reset for whoever was using it.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null && !this.level.isClientSide()) {
            ModEvents.resetMinigameForAnvil(this.level, pos);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return worldPosition;
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("hitRemains", hitRemains);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        BlockEntityHelper.saveInventory(tag, "inventory", itemHandler);

        if (ownerUUID != null) {
            tag.store("ownerUUID", UUIDUtil.CODEC, ownerUUID);
            tag.putLong("sessionStartTime", sessionStartTime);
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);

        BlockEntityHelper.loadInventory(tag, "inventory", itemHandler);

        hitRemains = tag.getIntOr("hitRemains", 0);
        progress = tag.getIntOr("progress", 0);
        maxProgress = tag.getIntOr("maxProgress", 0);

        ownerUUID = tag.read("ownerUUID", UUIDUtil.CODEC).orElse(null);
        sessionStartTime = ownerUUID != null ? tag.getLongOr("sessionStartTime", 0L) : 0L;

        // The cached recipe must be recalculated after loading.
        needsRecipeUpdate = true;
        cachedRecipe = Optional.empty();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void increaseForgingProgress(Level pLevel, BlockPos pPos, BlockState pState) {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (hasRecipe()) {
            ForgingRecipe currentRecipe = recipe.get();
            maxProgress = currentRecipe.getHammeringRequired();
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

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
        if (level != null && !level.isClientSide() && player instanceof ServerPlayer) {
            ModEvents.resetMinigameForPlayer((ServerPlayer) player);
            AbstractSmithingAnvil.setQuality(null);
        }
        player = null;
    }

    protected void craftItem() {
        Optional<ForgingRecipe> opt = getCurrentRecipe();
        if (opt.isEmpty()) return;

        ForgingRecipe recipe = opt.get();
        // 26.3 port: assumes the recipe agent keeps no-arg getResultItem()/getFailedResultItem().
        ItemStack result = recipe.getResultItem().copy();
        failedResult = recipe.getFailedResultItem();

        // Collect max ingredient quality
        ForgingQuality maxIngredientQuality = null;

        for (int i = 0; i < 9; i++) {
            ForgingQuality q = ForgingQuality.get(itemHandler.getStackInSlot(i));
            if (q == null) continue;

            if (maxIngredientQuality == null || q.ordinal() > maxIngredientQuality.ordinal()) {
                maxIngredientQuality = q;
            }
        }

        // Base result components
        if (recipe.hasQuality()
                && player != null
                && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            result.set(ModComponents.CREATOR, player.getName().getString());
        }

        if (recipe.needQuenching()
                && !result.is(ModTags.Items.HEATED_METALS)
                && !result.is(ModTags.Items.HOT_ITEMS)) {
            result.set(ModComponents.HEATED, true);
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
                        && level.getRandom().nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get()) {
                    quality = ForgingQuality.MASTER;
                }

                // Apply quality
                if (recipe.hasQuality()) {
                    result.set(ModComponents.FORGING_QUALITY, quality);

                    if (player instanceof ServerPlayer serverPlayer) {
                        ModAdvancementTriggers.FORGING_QUALITY
                                .trigger(serverPlayer, quality.getDisplayName());
                    }
                    if (!isArmor(result)
                            && !(result.getItem() instanceof ShieldItem)
                            && recipe.hasPolishing()) {
                        result.set(ModComponents.POLISHED, false);
                    }
                }
                if (!failedResult.isEmpty() & rollFailure(quality)) {
                    result = failedResult.copy();
                }
            }
        }

        transferIngredientComponents(result, recipe);


        for (int i = 0; i < 9; i++) {
            itemHandler.extractItem(i, 1, false);
        }


        ItemStack existing = itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (existing.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result);
            return;
        }

        if (!ItemStack.isSameItemSameComponents(existing, result)) return;

        int total = existing.getCount() + result.getCount();
        int max = Math.min(existing.getMaxStackSize(),
                itemHandler.getSlotLimit(OUTPUT_SLOT));

        if (total <= max) {
            existing.grow(result.getCount());
        } else {
            int overflow = total - max;
            existing.setCount(max);

            ItemStack drop = result.copy();
            drop.setCount(overflow);
            Containers.dropItemStack(level,
                    worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    drop);
        }

        itemHandler.setStackInSlot(OUTPUT_SLOT, existing);
    }

    /** ArmorItem no longer exists: armor is anything equippable in a humanoid armor slot. */
    private static boolean isArmor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    private boolean rollFailure(ForgingQuality quality) {
        return switch (quality) {
            case POOR -> true;
            case WELL -> level.getRandom().nextFloat()
                    < ServerConfig.FAIL_ON_WELL_QUALITY_CHANCE.get();
            case EXPERT -> level.getRandom().nextFloat()
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
        BlueprintData blueprintData = blueprint.get(ModComponents.BLUEPRINT_DATA);
        if (blueprint.isEmpty() || blueprintData == null) return;

        int uses = blueprintData.uses();
        int usesToLevel = BlueprintItem.getUsesToNextLevel(blueprint);

        BlueprintQuality currentQuality = blueprintData.getQualityEnum();

        // Attempt to read the ForgingQuality from the minigame result
        ForgingQuality resultQuality = ForgingQuality.fromString(anvilBlock.getQuality());

        if (currentQuality == BlueprintQuality.PERFECT || currentQuality == BlueprintQuality.MASTER) return;

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
                blueprint.set(ModComponents.BLUEPRINT_DATA,
                        blueprintData.withQuality(nextQuality.getDisplayName()).withUses(0));
                if (player instanceof ServerPlayer serverPlayer) {
                    if (nextQuality.equals(BlueprintQuality.PERFECT) || nextQuality.equals(BlueprintQuality.MASTER))
                        ModAdvancementTriggers.MAX_LEVEL_BLUEPRINT.trigger(serverPlayer);
                    ModAdvancementTriggers.BLUEPRINT_QUALITY.trigger(serverPlayer, nextQuality.getDisplayName());
                }
            } else {
                blueprint.set(ModComponents.BLUEPRINT_DATA, blueprintData.withUses(usesToLevel)); // Clamp
            }
        } else {
            blueprint.set(ModComponents.BLUEPRINT_DATA, blueprintData.withUses(uses)); // Just increment
        }

        this.itemHandler.setStackInSlot(BLUEPRINT_SLOT, blueprint);
    }

    /**
     * Copies the components of ingredients flagged transferNbt onto the result (was a raw NBT key
     * copy). Quality, creator, heated and damage are never transferred; damage is handled below.
     */
    private void transferIngredientComponents(ItemStack result, ForgingRecipe recipe) {
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
            if (ingredientStack.isDamageableItem()
                    && result.isDamageableItem()) {

                transferredDamage = Math.min(
                        transferredDamage,
                        ingredientStack.getDamageValue()
                );
                foundDamage = true;
            }

            DataComponentPatch patch = ingredientStack.getComponentsPatch().forget(type ->
                    type == ModComponents.FORGING_QUALITY
                            || type == ModComponents.CREATOR
                            || type == ModComponents.HEATED
                            || type == DataComponents.DAMAGE);
            if (!patch.isEmpty()) {
                result.applyComponents(patch);
            }
        }

        if (foundDamage && result.isDamageableItem()) {
            result.setDamageValue(
                    Math.min(transferredDamage, result.getMaxDamage() - 1)
            );
        }
    }


    public boolean isFailedResult() {
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        return ItemStack.isSameItem(result, failedResult);
    }

    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        AnvilTier requiredTier = AnvilTier.fromDisplayName(recipe.getAnvilTier());

        if (requiredTier == null || requiredTier.isEqualOrLowerThan(this.anvilTier)) {
            return false;
        }

        ItemStack resultStack = recipe.getResultItem();

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

        BlueprintData blueprintData = blueprint.isEmpty() ? null : blueprint.get(ModComponents.BLUEPRINT_DATA);

        if (recipe.requiresBlueprint()) {
            // Must have a valid matching blueprint
            if (blueprintData == null) {
                return false;
            }

            String blueprintToolType = blueprintData.toolType().toLowerCase(Locale.ROOT);
            if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                return false;
            }
        } else {
            // Optional blueprint: if present, it must match
            if (blueprintData != null) {
                String blueprintToolType = blueprintData.toolType().toLowerCase(Locale.ROOT);
                if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                    return false;
                }
            }
        }

        ItemStack resultStack = recipe.getResultItem();
        return canInsertItemIntoOutputSlot(resultStack, recipe)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }

    public Optional<ForgingRecipe> getCurrentRecipe() {
        return getCurrentRecipeHolder().map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<ForgingRecipe>> getCurrentRecipeHolder() {
        if (level == null) return Optional.empty();

        if (needsRecipeUpdate) {
            ItemListInput input = recipeInput();

            cachedRecipe = findBestMatch(input)
                    .filter(holder -> holder.value().matches(input, level));

            needsRecipeUpdate = false;
        }

        return cachedRecipe;
    }

    /** Recipe input indexed like the anvil inventory: grid 0-8, output 9-10 empty, blueprint 11. */
    protected ItemListInput recipeInput() {
        List<ItemStack> stacks = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            stacks.add(i < 9 || i == BLUEPRINT_SLOT ? itemHandler.getStackInSlot(i) : ItemStack.EMPTY);
        }
        return new ItemListInput(stacks);
    }

    /** Port of ForgingRecipe.findBestMatch: the largest matching recipe containing the first grid item. */
    private Optional<RecipeHolder<ForgingRecipe>> findBestMatch(ItemListInput input) {
        ItemStack keyStack = ItemStack.EMPTY;
        for (int i = 0; i < 9; i++) {
            if (!input.getItem(i).isEmpty()) {
                keyStack = input.getItem(i);
                break;
            }
        }
        if (keyStack.isEmpty()) return Optional.empty();

        final ItemStack key = keyStack;
        return RecipeLookup.<ItemListInput, ForgingRecipe>all(level, ModRecipeTypes.FORGING).stream()
                .filter(holder -> holder.value().containsIngredient(key))
                .filter(holder -> holder.value().matches(input, level))
                .max(Comparator.comparingInt((RecipeHolder<ForgingRecipe> holder) -> holder.value().getWidth() * holder.value().getHeight()));
    }

    protected boolean canInsertItemIntoOutputSlot(ItemStack stackToInsert, ForgingRecipe currentRecipe) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (!existing.isEmpty() && currentRecipe != null && currentRecipe.hasFailedResult()) {
            return false;
        }

        return existing.isEmpty()
                || ItemStack.isSameItemSameComponents(existing, stackToInsert);
    }

    protected boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            return true;
        }
        return existing.getCount() + count <= existing.getMaxStackSize();
    }

    public boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    public void increaseCraftingProgress() {
        progress++;

        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
        setChanged(level, worldPosition, getBlockState());
    }


    public void tick(Level lvl, BlockPos pos, BlockState st) {
        if (!pos.equals(this.worldPosition)) return; // sanity check
        tickHeatedIngredients(lvl);
        try {
            // Check if blueprint changed mid-forging
            ItemStack currentBlueprint = this.itemHandler.getStackInSlot(11);
            if (!ItemStack.isSameItemSameComponents(currentBlueprint, lastBlueprint)) {
                if (progress > 0 || lastRecipe != null || isMinigameOn()) {
                    resetProgress();
                    setMinigameOn(false);
                    Overgeared.LOGGER.debug("Blueprint changed at {}, minigame reset", pos);
                }
            }
            lastBlueprint = currentBlueprint.copy();

            Optional<RecipeHolder<ForgingRecipe>> currentRecipeOpt = getCurrentRecipeHolder();
            if (currentRecipeOpt.isEmpty()) {
                if (progress > 0 || lastRecipe != null) {
                    resetProgress();
                }
                return;
            }

            RecipeHolder<ForgingRecipe> currentHolder = currentRecipeOpt.get();
            ForgingRecipe currentRecipe = currentHolder.value();

            boolean recipeChanged = false;
            if (lastRecipe != null) {
                recipeChanged = !currentHolder.id().equals(lastRecipe.id());
            } else if (maxProgress > 0) {
                recipeChanged = true;
            }

            if (recipeChanged) {
                resetProgress();
                lastRecipe = currentHolder;
                return;
            }

            lastRecipe = currentHolder;

            if (hasRecipe()) {
                maxProgress = currentRecipe.getHammeringRequired();
                hitRemains = maxProgress - progress;
                setChanged(lvl, pos, st);

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

    public ContainerData getContainerData() {
        return data;
    }

    protected boolean matchesRecipeExactly(ForgingRecipe recipe) {
        return recipe.matches(recipeInput(), level);
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
            BlueprintData blueprintData = blueprint.isEmpty() ? null : blueprint.get(ModComponents.BLUEPRINT_DATA);
            if (blueprintData == null) {
                return switch (quality.toLowerCase(Locale.ROOT)) {
                    case "poor" -> ForgingQuality.POOR.getDisplayName();
                    default -> "well"; // Cap quality at 'well' without blueprint
                };
            }

            String blueprintToolType = blueprintData.quality().toLowerCase(Locale.ROOT);

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
                        if (ForgingQuality.get(stack) != null) {
                            if (ForgingQuality.get(stack) == ForgingQuality.MASTER) {
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
                if (ForgingQuality.get(stack) != null) {
                    if (ForgingQuality.get(stack) == ForgingQuality.MASTER) {
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
            BlueprintData blueprintData = blueprint.isEmpty() ? null : blueprint.get(ModComponents.BLUEPRINT_DATA);
            if (blueprintData == null) {
                return poor;
            }

            String bpQuality = blueprintData.quality().toLowerCase(Locale.ROOT);
            // ensure it's in our tier list, otherwise default
            return qualityTiers.contains(bpQuality) ? bpQuality : ForgingQuality.NONE.getDisplayName();
        }

        return ForgingQuality.NONE.getDisplayName(); // fallback if no blueprint types
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.setChanged();

        // Force sync to client
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
        if (level != null && level.isClientSide() && data != null) {
            // On client, get from synced container data
            return data.get(0);
        }
        return this.progress;
    }

    public void setOwner(UUID uuid) {
        ownerUUID = uuid;
        sessionStartTime = level.getGameTime();
        setChanged();
    }

    public void clearOwner() {
        ownerUUID = null;
        sessionStartTime = 0L;
        setChanged();
    }

    public boolean isOwnedBy(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    public boolean isOwnedByOther(Player player) {
        return ownerUUID != null && !ownerUUID.equals(player.getUUID());
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
        setChanged(); // mark dirty for save
    }

    public void tickHeatedIngredients(Level world) {
        if (world.isClientSide()) return;
        long tick = world.getGameTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!stack.is(ModTags.Items.HEATED_METALS)) continue;

            long heatedSince = stack.getOrDefault(ModComponents.HEATED_TIME, 0L);

            // Initialize timestamp if not present
            if (heatedSince == 0L) {
                stack.set(ModComponents.HEATED_TIME, tick);
                continue;
            }

            // Cooldown complete -> convert to cooled version
            if (tick - heatedSince >= cooldownTicks) {
                Item cooled = getCooledItem(stack.getItem(), world);
                if (cooled != null) {
                    ItemStack newStack = new ItemStack(cooled, stack.getCount());
                    // Preserve quality or other metadata
                    newStack.applyComponents(stack.getComponentsPatch()
                            .forget(type -> type == ModComponents.HEATED_TIME));
                    world.playSound(
                            null,                              // no player (broadcast to all nearby)
                            worldPosition,                     // block position
                            SoundEvents.FIRE_EXTINGUISH,       // extinguish sound
                            SoundSource.BLOCKS,                // sound category
                            1.0F,                              // volume
                            1.0F                               // pitch
                    );
                    itemHandler.setStackInSlot(slot, newStack);
                    world.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
        }
    }

    public AnvilTier getAnvilTier() {
        return anvilTier;
    }

    public boolean tryStartMinigame(ServerPlayer player) {

        if (minigameOn) return false;

        if (ownerUUID != null && !ownerUUID.equals(player.getUUID())) {
            return false;
        }

        ownerUUID = player.getUUID();
        minigameOn = true;
        sessionStartTime = level.getGameTime();

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        return true;
    }

    // ---------------- Inventory / SidedInventory (replaces the Forge IItemHandler capability) ----------------

    @Override
    public int getContainerSize() {
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
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return itemHandler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return itemHandler.extractItem(slot, itemHandler.getStackInSlot(slot).getCount(), false);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{OUTPUT_SLOT};
        }
        int[] slots = new int[itemHandler.getSlots()];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        if (dir == Direction.DOWN) return false;
        // Same rules as the menu slots: never into the output, only hammers / blueprints into their slots.
        if (slot == OUTPUT_SLOT) return false;
        if (slot == HAMMER_SLOT) return stack.is(ModTags.Items.SMITHING_HAMMERS);
        if (slot == BLUEPRINT_SLOT) return stack.is(ModItems.BLUEPRINT) || stack.getItem() instanceof SmithingTemplateItem;
        return itemHandler.isItemValid(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return dir != Direction.DOWN || slot == OUTPUT_SLOT;
    }
}
