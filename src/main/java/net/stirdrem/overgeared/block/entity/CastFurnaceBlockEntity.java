package net.stirdrem.overgeared.block.entity;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.CastFurnaceScreenHandler;
import net.stirdrem.overgeared.util.ConfigHelper;
import net.stirdrem.overgeared.util.ItemStackHandler;
import net.stirdrem.overgeared.util.ModTags;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.recipe.ItemListInput;
import net.stirdrem.overgeared.recipe.RecipeLookup;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CastFurnaceBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos>, Container, WorldlyContainer {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_CAST = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int cookTimeTotal;
    private float storedExperience;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                case 2 -> cookTime;
                case 3 -> cookTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> maxBurnTime = value;
                case 2 -> cookTime = value;
                case 3 -> cookTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public CastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAST_FURNACE_BE, pos, state);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, CastFurnaceBlockEntity be) {
        boolean wasLit = be.isLit();
        boolean dirty = false;

        if (be.burnTime > 0) be.burnTime--;

        ItemStack fuel = be.itemHandler.getStackInSlot(SLOT_FUEL);

        if (be.burnTime == 0 && be.canSmelt() && world instanceof ServerLevel serverLevel) {
            be.maxBurnTime = be.burnTime = fuel.isEmpty() ? 0 : BlockEntityHelper.getBurnDuration(serverLevel, be, fuel);
            if (be.burnTime > 0 && !fuel.isEmpty()) {
                ItemStack remainder = BlockEntityHelper.remainder(fuel);
                fuel.shrink(1);
                if (fuel.isEmpty() && !remainder.isEmpty())
                    be.itemHandler.setStackInSlot(SLOT_FUEL, remainder);
                dirty = true;
            }
        }

        if (be.isLit() && be.canSmelt()) {
            be.cookTime++;
            if (be.cookTime >= be.cookTimeTotal) {
                be.cookTime = 0;
                be.smelt();
                dirty = true;
            }
        } else if (!be.canSmelt()) {
            be.cookTime = 0;
        }

        if (wasLit != be.isLit()) {
            world.setBlock(pos, state.setValue(BlockStateProperties.LIT, be.isLit()), 3);
            dirty = true;
        }

        if (dirty) be.setChanged();
    }

    private boolean isLit() {
        return burnTime > 0;
    }

    /** Casting recipes are matched against [material input, tool cast]. */
    private ItemListInput castingInput() {
        return ItemListInput.of(itemHandler.getStackInSlot(SLOT_INPUT), itemHandler.getStackInSlot(SLOT_CAST));
    }

    private Optional<CastingRecipe> findRecipe() {
        if (level == null) return Optional.empty();
        return RecipeLookup.firstMatchValue(level, ModRecipeTypes.CASTING, castingInput());
    }

    private boolean canSmelt() {
        if (level == null) return false;

        Optional<CastingRecipe> recipeOpt = findRecipe();
        if (recipeOpt.isEmpty()) return false;

        CastingRecipe recipe = recipeOpt.get();

        ItemStack previewOutput = buildResultStack(recipe);
        if (previewOutput.isEmpty()) return false;

        ItemStack outputSlot = itemHandler.getStackInSlot(SLOT_OUTPUT);

        cookTimeTotal = recipe.getCookingTime();

        if (outputSlot.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(outputSlot, previewOutput)) {
            return false;
        }

        return outputSlot.getCount() + previewOutput.getCount()
                <= outputSlot.getMaxStackSize();
    }

    /** Recipe result plus the cast's quality, the polishing flag and the heated flag. */
    private ItemStack buildResultStack(CastingRecipe recipe) {
        // 26.3 port: assumes the recipe agent keeps a no-arg getResultItem() accessor.
        ItemStack output = recipe.getResultItem().copy();

        CastData castData = itemHandler.getStackInSlot(SLOT_CAST).get(ModComponents.CAST_DATA);
        if (castData != null && !castData.quality().isEmpty() && !"none".equals(castData.quality())) {
            output.set(ModComponents.FORGING_QUALITY, ForgingQuality.fromString(castData.quality()));
        }

        if (recipe.requiresPolishing()) {
            output.set(ModComponents.POLISHED, false);
        }

        output.set(ModComponents.HEATED, true);
        return output;
    }

    private void smelt() {
        if (!canSmelt()) return;

        ItemStack cast = itemHandler.getStackInSlot(SLOT_CAST);
        CastingRecipe recipe = findRecipe().orElse(null);
        if (recipe == null) return;

        float xp = recipe.getExperience();
        ItemStack output = buildResultStack(recipe);

        if (itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, output);
        } else {
            itemHandler.getStackInSlot(SLOT_OUTPUT).grow(1);
        }
        Map<String, Integer> availableMaterials =
                ConfigHelper.getMaterialValuesForItem(itemHandler.getStackInSlot(SLOT_INPUT));
        Map<String, Double> requiredMaterials = recipe.getRequiredMaterials();
        int itemConsumeAmount = 1;
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase(Locale.ROOT);
            double needed = entry.getValue();
            double available = availableMaterials
                    .getOrDefault(material, (int) needed);

            itemConsumeAmount = (int) Math.max(1, Math.ceil(needed / available));
        }

        itemHandler.getStackInSlot(SLOT_INPUT).shrink(itemConsumeAmount);

        // Damage cast (respects Unbreaking like the old hurt(..) call)
        if (cast.isDamageableItem() && level instanceof ServerLevel serverLevel) {
            cast.hurtAndBreak(1, serverLevel, null, broken -> {
            });

            if (cast.isEmpty() || cast.getDamageValue() >= cast.getMaxDamage()) {
                itemHandler.setStackInSlot(SLOT_CAST, ItemStack.EMPTY);
            }
        }
        if (!level.isClientSide() && xp > 0)
            storedExperience += xp;
    }

    private void spawnExperience(float xp) {
        if (level == null || level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverWorld)) return;

        int i = Mth.floor(xp);
        float f = xp - i;
        if (f > 0 && Math.random() < f) i++;

        if (i > 0) {
            ExperienceOrb.award(serverWorld, new Vec3(
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5), i);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overgeared.casting_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new CastFurnaceScreenHandler(syncId, playerInventory, this, data);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return worldPosition;
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        BlockEntityHelper.saveInventory(tag, "inventory", itemHandler);
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("cookTime", cookTime);
        tag.putInt("cookTimeTotal", cookTimeTotal);
        tag.putFloat("storedXp", storedExperience);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        BlockEntityHelper.loadInventory(tag, "inventory", itemHandler);
        burnTime = tag.getIntOr("burnTime", 0);
        maxBurnTime = tag.getIntOr("maxBurnTime", 0);
        cookTime = tag.getIntOr("cookTime", 0);
        cookTimeTotal = tag.getIntOr("cookTimeTotal", 0);
        storedExperience = tag.getFloatOr("storedXp", 0.0F);
    }

    /** 26.x: contents are dropped by the vanilla Container handling in super; this also pops stored XP. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        spawnExperience(storedExperience);
        storedExperience = 0;
    }

    public void drops() {
        if (level != null) Containers.dropContents(level, worldPosition, this);
        spawnExperience(storedExperience);
    }

    public void awardStoredExperience(Player player) {
        if (this.level == null || this.level.isClientSide()) return;
        if (storedExperience > 0 && player != null) {
            int total = (int) storedExperience;
            float fractional = storedExperience - total;
            if (fractional > 0.0F && Math.random() < fractional) total++;

            player.giveExperiencePoints(total);

            this.level.playSound(
                    null,
                    worldPosition,
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5F,
                    this.level.getRandom().nextFloat() * 0.1F + 0.9F
            );

            storedExperience = 0;
            setChanged();
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) return new int[]{SLOT_INPUT, SLOT_CAST};
        if (side == Direction.DOWN) return new int[]{SLOT_OUTPUT};
        return new int[]{SLOT_FUEL};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == SLOT_OUTPUT) return false;
        if (slot == SLOT_FUEL) {
            return BlockEntityHelper.isFuel(stack);
        }
        if (slot == SLOT_CAST) return stack.is(ModTags.Items.TOOL_CAST);
        return ConfigHelper.isValidMaterial(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == SLOT_OUTPUT;
    }

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
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = stack.split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) return false;

        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
