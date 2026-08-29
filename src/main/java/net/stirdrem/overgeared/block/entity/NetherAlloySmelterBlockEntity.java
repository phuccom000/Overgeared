package net.stirdrem.overgeared.block.entity;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.stirdrem.overgeared.recipe.NetherAlloySmeltingRecipe;
import net.stirdrem.overgeared.recipe.ShapedNetherAlloySmeltingRecipe;
import net.stirdrem.overgeared.screen.NetherAlloySmelterScreenHandler;
import net.stirdrem.overgeared.util.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NetherAlloySmelterBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory, SidedInventory {
    // Total slots: 9 inputs + 1 fuel + 1 output = 11 slots
    private static final int INPUT_SLOTS = 9;
    private static final int FUEL_SLOT = 9;
    private static final int OUTPUT_SLOT = 10;

    private final ItemStackHandler itemHandler = new ItemStackHandler(11) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    private final PropertyDelegate data;

    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int cookTimeTotal;
    private float storedExperience = 0.0F;

    public NetherAlloySmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETHER_ALLOY_FURNACE_BE, pos, state);

        this.data = new PropertyDelegate() {
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

            public int size() {
                return 4;
            }
        };
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    // --------------------------------------------------
    // Tick logic
    // --------------------------------------------------
    public static void tick(World world, BlockPos pos, BlockState state, NetherAlloySmelterBlockEntity be) {
        boolean wasLit = be.burnTime > 0;
        boolean dirty = false;

        if (be.burnTime > 0) be.burnTime--;

        ItemStack fuel = be.itemHandler.getStackInSlot(FUEL_SLOT);

        if (be.burnTime == 0 && be.canSmelt()) {
            Integer fuelTime = FuelRegistry.INSTANCE.get(fuel.getItem());
            be.maxBurnTime = be.burnTime = fuelTime == null ? 0 : fuelTime;
            if (be.burnTime > 0 && !fuel.isEmpty()) {
                Item fuelContainer = fuel.getItem().getRecipeRemainder();
                fuel.decrement(1);
                if (fuel.isEmpty() && fuelContainer != null)
                    be.itemHandler.setStackInSlot(FUEL_SLOT, new ItemStack(fuelContainer));
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
            state = state.with(Properties.LIT, be.isLit());
            world.setBlockState(pos, state, 3);
            dirty = true;
        }

        if (dirty) be.markDirty();
    }

    // --------------------------------------------------
    // Smelting logic
    // --------------------------------------------------
    private boolean canSmelt() {
        SimpleInventory inv = new SimpleInventory(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) inv.setStack(i, itemHandler.getStackInSlot(i));

        Optional<NetherAlloySmeltingRecipe> shapelessRecipe =
                world.getRecipeManager().getFirstMatch(NetherAlloySmeltingRecipe.Type.INSTANCE, inv, world);

        Optional<ShapedNetherAlloySmeltingRecipe> shapedRecipe =
                world.getRecipeManager().getFirstMatch(ShapedNetherAlloySmeltingRecipe.Type.INSTANCE, inv, world);

        if (shapelessRecipe.isEmpty() && shapedRecipe.isEmpty()) return false;

        cookTimeTotal = shapelessRecipe.map(NetherAlloySmeltingRecipe::getCookingTime)
                .orElseGet(() -> shapedRecipe.get().getCookingTime());

        ItemStack result = shapelessRecipe.map(r -> r.getOutput(world.getRegistryManager()))
                .orElseGet(() -> shapedRecipe.get().getOutput(world.getRegistryManager()));

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        return !result.isEmpty() &&
                (output.isEmpty() || (output.isOf(result.getItem()) &&
                        output.getCount() + result.getCount() <= output.getMaxCount()));
    }

    private void smelt() {
        if (!canSmelt()) return;

        SimpleInventory inv = new SimpleInventory(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) inv.setStack(i, itemHandler.getStackInSlot(i));

        Optional<NetherAlloySmeltingRecipe> shapelessRecipe =
                world.getRecipeManager().getFirstMatch(NetherAlloySmeltingRecipe.Type.INSTANCE, inv, world);
        Optional<ShapedNetherAlloySmeltingRecipe> shapedRecipe =
                world.getRecipeManager().getFirstMatch(ShapedNetherAlloySmeltingRecipe.Type.INSTANCE, inv, world);

        ItemStack result;
        float xp;

        if (shapelessRecipe.isPresent()) {
            NetherAlloySmeltingRecipe recipe = shapelessRecipe.get();
            result = recipe.getOutput(world.getRegistryManager());
            xp = recipe.getExperience();
        } else if (shapedRecipe.isPresent()) {
            ShapedNetherAlloySmeltingRecipe recipe = shapedRecipe.get();
            result = recipe.getOutput(world.getRegistryManager());
            xp = recipe.getExperience();
        } else return;

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else if (output.isOf(result.getItem())) {
            output.increment(result.getCount());
        }

        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack input = itemHandler.getStackInSlot(i);

            if (input.isEmpty()) {
                continue;
            }

            ItemStack remainder = input.getRecipeRemainder();

            input.decrement(1);

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

                    if (ItemStack.canCombine(target, remainder)
                            && target.getCount() < target.getMaxCount()) {

                        int amount = Math.min(
                                remainder.getCount(),
                                target.getMaxCount() - target.getCount()
                        );

                        target.increment(amount);
                        remainder.decrement(amount);

                        if (remainder.isEmpty()) {
                            break;
                        }
                    }
                }

                // No room -> drop the remainder.
                if (!remainder.isEmpty() && world != null && !world.isClient) {
                    ItemScatterer.spawn(
                            world,
                            pos.getX() + 0.5,
                            pos.getY() + 1.0,
                            pos.getZ() + 0.5,
                            remainder
                    );
                }
            }
        }

        if (!world.isClient && xp > 0.0F) {
            storedExperience += xp;
        }
    }

    // --------------------------------------------------
    // Experience logic (vanilla accurate)
    // --------------------------------------------------
    private void spawnExperience(float xp) {
        if (this.world == null || this.world.isClient) return;
        if (!(this.world instanceof ServerWorld serverWorld)) return;

        int i = MathHelper.floor(xp);
        float f = xp - i;
        if (f > 0.0F && Math.random() < f) i++;

        if (i > 0) {
            ExperienceOrbEntity.spawn(serverWorld, new Vec3d(
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5), i);
        }
    }

    private boolean isLit() {
        return burnTime > 0;
    }

    // --------------------------------------------------
    // Container & UI
    // --------------------------------------------------
    @Override
    public Text getDisplayName() {
        return Text.translatable("container.overgeared.nether_alloy_smelter");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new NetherAlloySmelterScreenHandler(syncId, playerInventory, this, this.data);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public void awardStoredExperience(PlayerEntity player) {
        if (this.world == null || this.world.isClient) return;
        if (storedExperience > 0 && player != null) {
            int total = (int) storedExperience;
            float fractional = storedExperience - total;
            if (fractional > 0.0F && Math.random() < fractional) total++;

            player.addExperience(total);

            this.world.playSound(
                    null,
                    pos,
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS,
                    0.5F,
                    this.world.random.nextFloat() * 0.1F + 0.9F
            );

            storedExperience = 0;
            markDirty();
        }
    }

    // --------------------------------------------------
    // NBT
    // --------------------------------------------------
    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("cookTime", cookTime);
        tag.putInt("cookTimeTotal", cookTimeTotal);
        tag.putFloat("storedXp", storedExperience);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
        cookTime = tag.getInt("cookTime");
        cookTimeTotal = tag.getInt("cookTimeTotal");
        storedExperience = tag.getFloat("storedXp");
    }

    public void drops() {
        SimpleInventory inventory = new SimpleInventory(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setStack(i, itemHandler.getStackInSlot(i));
        }
        ItemScatterer.spawn(this.world, this.pos, inventory);
        spawnExperience(storedExperience);
    }

    // --------------------------------------------------
    // Hopper automation
    // --------------------------------------------------
    @Override
    public int[] getAvailableSlots(Direction side) {
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
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction direction) {
        if (slot == OUTPUT_SLOT) return false;
        if (slot == FUEL_SLOT) {
            Integer fuelTime = FuelRegistry.INSTANCE.get(stack.getItem());
            return fuelTime != null && fuelTime > 0;
        }
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT;
    }

    // --------------------------------------------------
    // Basic container methods
    // --------------------------------------------------
    @Override
    public int size() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++)
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            ItemStack result = stack.split(amount);
            markDirty();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) return false;
        return player.squaredDistanceTo(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clear() {
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
