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
import net.stirdrem.overgeared.recipe.NetherAlloySmeltingRecipe;
import net.stirdrem.overgeared.recipe.ShapedNetherAlloySmeltingRecipe;
import net.stirdrem.overgeared.screen.NetherAlloySmelterScreenHandler;
import net.stirdrem.overgeared.util.ItemStackHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.stirdrem.overgeared.recipe.ItemListInput;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RecipeLookup;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NetherAlloySmelterBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos>, Container, WorldlyContainer {
    // Total slots: 9 inputs + 1 fuel + 1 output = 11 slots
    private static final int INPUT_SLOTS = 9;
    private static final int FUEL_SLOT = 9;
    private static final int OUTPUT_SLOT = 10;

    private final ItemStackHandler itemHandler = new ItemStackHandler(11) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ContainerData data;

    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int cookTimeTotal;
    private float storedExperience = 0.0F;

    public NetherAlloySmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETHER_ALLOY_FURNACE_BE, pos, state);

        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> burnTime;
                    case 1 -> maxBurnTime;
                    case 2 -> cookTime;
                    case 3 -> cookTimeTotal;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> burnTime = value;
                    case 1 -> maxBurnTime = value;
                    case 2 -> cookTime = value;
                    case 3 -> cookTimeTotal = value;
                }
            }

            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // --------------------------------------------------
    // Tick logic
    // --------------------------------------------------
    public static void tick(Level world, BlockPos pos, BlockState state, NetherAlloySmelterBlockEntity be) {
        boolean wasLit = be.burnTime > 0;
        boolean dirty = false;

        if (be.burnTime > 0) be.burnTime--;

        ItemStack fuel = be.itemHandler.getStackInSlot(FUEL_SLOT);

        if (be.burnTime == 0 && be.canSmelt() && world instanceof ServerLevel serverLevel) {
            be.maxBurnTime = be.burnTime = fuel.isEmpty() ? 0 : BlockEntityHelper.getBurnDuration(serverLevel, be, fuel);
            if (be.burnTime > 0 && !fuel.isEmpty()) {
                ItemStack fuelContainer = BlockEntityHelper.remainder(fuel);
                fuel.shrink(1);
                if (fuel.isEmpty() && !fuelContainer.isEmpty())
                    be.itemHandler.setStackInSlot(FUEL_SLOT, fuelContainer);
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
            state = state.setValue(BlockStateProperties.LIT, be.isLit());
            world.setBlock(pos, state, 3);
            dirty = true;
        }

        if (dirty) be.setChanged();
    }

    // --------------------------------------------------
    // Smelting logic
    // --------------------------------------------------
    private ItemListInput inputs() {
        return ItemListInput.of(this, 0, INPUT_SLOTS);
    }

    private boolean canSmelt() {
        ItemListInput inv = inputs();
        Optional<NetherAlloySmeltingRecipe> shapelessRecipe =
                RecipeLookup.firstMatchValue(level, ModRecipeTypes.NETHER_ALLOY_SMELTING, inv);
        Optional<ShapedNetherAlloySmeltingRecipe> shapedRecipe =
                RecipeLookup.firstMatchValue(level, ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING, inv);

        if (shapelessRecipe.isEmpty() && shapedRecipe.isEmpty()) return false;

        cookTimeTotal = shapelessRecipe.map(NetherAlloySmeltingRecipe::getCookingTime)
                .orElseGet(() -> shapedRecipe.get().getCookingTime());

        ItemStack result = shapelessRecipe.map(r -> r.assemble(inv))
                .orElseGet(() -> shapedRecipe.get().assemble(inv));

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        return !result.isEmpty() &&
                (output.isEmpty() || (output.is(result.getItem()) &&
                        output.getCount() + result.getCount() <= output.getMaxStackSize()));
    }

    private void smelt() {
        if (!canSmelt()) return;

        ItemListInput inv = inputs();
        Optional<NetherAlloySmeltingRecipe> shapelessRecipe =
                RecipeLookup.firstMatchValue(level, ModRecipeTypes.NETHER_ALLOY_SMELTING, inv);
        Optional<ShapedNetherAlloySmeltingRecipe> shapedRecipe =
                RecipeLookup.firstMatchValue(level, ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING, inv);

        ItemStack result;
        float xp;

        if (shapelessRecipe.isPresent()) {
            NetherAlloySmeltingRecipe recipe = shapelessRecipe.get();
            result = recipe.assemble(inv);
            xp = recipe.getExperience();
        } else if (shapedRecipe.isPresent()) {
            ShapedNetherAlloySmeltingRecipe recipe = shapedRecipe.get();
            result = recipe.assemble(inv);
            xp = recipe.getExperience();
        } else return;

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else if (output.is(result.getItem())) {
            output.grow(result.getCount());
        }

        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack input = itemHandler.getStackInSlot(i);

            if (input.isEmpty()) {
                continue;
            }

            ItemStack remainder = BlockEntityHelper.remainder(input);

            input.shrink(1);

            // If the input stack was completely consumed,
            // put the remainder back into that same slot.
            if (input.isEmpty()) {
                if (!remainder.isEmpty()) {
                    itemHandler.setStackInSlot(i, remainder);
                }
                continue;
            }

            // Input stack still exists, so try the other input slots.
            if (!remainder.isEmpty()) {
                for (int j = 0; j < INPUT_SLOTS; j++) {
                    ItemStack target = itemHandler.getStackInSlot(j);

                    if (target.isEmpty()) {
                        itemHandler.setStackInSlot(j, remainder);
                        remainder = ItemStack.EMPTY;
                        break;
                    }

                    if (ItemStack.isSameItemSameComponents(target, remainder)
                            && target.getCount() < target.getMaxStackSize()) {

                        int amount = Math.min(
                                remainder.getCount(),
                                target.getMaxStackSize() - target.getCount()
                        );

                        target.grow(amount);
                        remainder.shrink(amount);

                        if (remainder.isEmpty()) {
                            break;
                        }
                    }
                }

                // No room -> drop the remainder.
                if (!remainder.isEmpty() && level != null && !level.isClientSide()) {
                    Containers.dropItemStack(
                            level,
                            worldPosition.getX() + 0.5,
                            worldPosition.getY() + 1.0,
                            worldPosition.getZ() + 0.5,
                            remainder
                    );
                }
            }
        }

        if (!level.isClientSide() && xp > 0.0F) {
            storedExperience += xp;
        }
    }

    // --------------------------------------------------
    // Experience logic (vanilla accurate)
    // --------------------------------------------------
    private void spawnExperience(float xp) {
        if (this.level == null || this.level.isClientSide()) return;
        if (!(this.level instanceof ServerLevel serverWorld)) return;

        int i = Mth.floor(xp);
        float f = xp - i;
        if (f > 0.0F && Math.random() < f) i++;

        if (i > 0) {
            ExperienceOrb.award(serverWorld, new Vec3(
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5), i);
        }
    }

    private boolean isLit() {
        return burnTime > 0;
    }

    // --------------------------------------------------
    // Container & UI
    // --------------------------------------------------
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overgeared.nether_alloy_smelter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new NetherAlloySmelterScreenHandler(syncId, playerInventory, this, this.data);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return worldPosition;
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

    // --------------------------------------------------
    // NBT
    // --------------------------------------------------
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
        if (this.level != null) Containers.dropContents(this.level, this.worldPosition, this);
        spawnExperience(storedExperience);
    }

    // --------------------------------------------------
    // Hopper automation
    // --------------------------------------------------
    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            int[] inputSlots = new int[INPUT_SLOTS];
            for (int i = 0; i < INPUT_SLOTS; i++) inputSlots[i] = i;
            return inputSlots;
        } else if (side == Direction.DOWN) {
            return new int[]{OUTPUT_SLOT};
        } else {
            return new int[]{FUEL_SLOT};
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if (slot == OUTPUT_SLOT) return false;
        if (slot == FUEL_SLOT) {
            return BlockEntityHelper.isFuel(stack);
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT;
    }

    // --------------------------------------------------
    // Basic container methods
    // --------------------------------------------------
    @Override
    public int getContainerSize() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++)
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            ItemStack result = stack.split(amount);
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
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
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // Helper methods for slot access
    public int getInputSlotsCount() {
        return INPUT_SLOTS;
    }

    public int getFuelSlot() {
        return FUEL_SLOT;
    }

    public int getOutputSlot() {
        return OUTPUT_SLOT;
    }
}
