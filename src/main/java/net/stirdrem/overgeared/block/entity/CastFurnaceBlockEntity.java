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
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.CastFurnaceScreenHandler;
import net.stirdrem.overgeared.util.ConfigHelper;
import net.stirdrem.overgeared.util.ItemStackHandler;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CastFurnaceBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, Inventory, SidedInventory {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_CAST = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int cookTimeTotal;
    private float storedExperience;

    private final PropertyDelegate data = new PropertyDelegate() {
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
        public int size() {
            return 4;
        }
    };

    public CastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAST_FURNACE_BE, pos, state);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CastFurnaceBlockEntity be) {
        boolean wasLit = be.isLit();
        boolean dirty = false;

        if (be.burnTime > 0) be.burnTime--;

        ItemStack fuel = be.itemHandler.getStackInSlot(SLOT_FUEL);

        if (be.burnTime == 0 && be.canSmelt()) {
            Integer fuelTime = FuelRegistry.INSTANCE.get(fuel.getItem());
            be.maxBurnTime = be.burnTime = fuelTime == null ? 0 : fuelTime;
            if (be.burnTime > 0 && !fuel.isEmpty()) {
                Item remainder = fuel.getItem().getRecipeRemainder();
                fuel.decrement(1);
                if (fuel.isEmpty() && remainder != null)
                    be.itemHandler.setStackInSlot(SLOT_FUEL, new ItemStack(remainder));
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
            world.setBlockState(pos, state.with(Properties.LIT, be.isLit()), 3);
            dirty = true;
        }

        if (dirty) be.markDirty();
    }

    private boolean isLit() {
        return burnTime > 0;
    }

    private boolean canSmelt() {
        if (world == null) return false;

        SimpleInventory inv = new SimpleInventory(2);
        inv.setStack(0, itemHandler.getStackInSlot(SLOT_INPUT));
        inv.setStack(1, itemHandler.getStackInSlot(SLOT_CAST));

        Optional<CastingRecipe> recipeOpt =
                world.getRecipeManager().getFirstMatch(ModRecipeTypes.CASTING, inv, world);

        if (recipeOpt.isEmpty()) return false;

        CastingRecipe recipe = recipeOpt.get();

        ItemStack previewOutput = buildResultStack(recipe);
        if (previewOutput.isEmpty()) return false;

        ItemStack outputSlot = itemHandler.getStackInSlot(SLOT_OUTPUT);

        cookTimeTotal = recipe.getCookingTime();

        if (outputSlot.isEmpty()) {
            return true;
        }

        if (!ItemStack.canCombine(outputSlot, previewOutput)) {
            return false;
        }

        return outputSlot.getCount() + previewOutput.getCount()
                <= outputSlot.getMaxCount();
    }

    private ItemStack buildResultStack(CastingRecipe recipe) {
        ItemStack output = recipe.getOutput(world.getRegistryManager()).copy();

        ItemStack cast = itemHandler.getStackInSlot(SLOT_CAST);
        NbtCompound castTag = cast.getNbt();
        NbtCompound outTag = output.getNbt();

        if (castTag != null && castTag.contains("Quality")) {
            String q = castTag.getString("Quality");
            if (!"none".equals(q)) {
                if (outTag == null) outTag = new NbtCompound();
                outTag.putString("ForgingQuality", q);
            }
        }

        if (recipe.requiresPolishing()) {
            if (outTag == null) outTag = new NbtCompound();
            outTag.putBoolean("Polished", false);
        }

        if (outTag == null) outTag = new NbtCompound();
        outTag.putBoolean("Heated", true);
        output.setNbt(outTag);

        return output;
    }

    private void smelt() {
        if (!canSmelt()) return;

        SimpleInventory inv = new SimpleInventory(2);
        inv.setStack(0, itemHandler.getStackInSlot(SLOT_INPUT));
        inv.setStack(1, itemHandler.getStackInSlot(SLOT_CAST));
        ItemStack cast = itemHandler.getStackInSlot(SLOT_CAST);
        NbtCompound castTag = cast.getOrCreateNbt();

        CastingRecipe recipe =
                world.getRecipeManager()
                        .getFirstMatch(ModRecipeTypes.CASTING, inv, world)
                        .orElse(null);

        if (recipe == null) return;

        ItemStack result = recipe.getOutput(world.getRegistryManager());
        float xp = recipe.getExperience();
        boolean needPolishing = recipe.requiresPolishing();

        ItemStack output = result.copy();
        NbtCompound outTag = output.getNbt();

        if (castTag.contains("Quality")) {
            String q = castTag.getString("Quality");
            if (!q.equals("none")) {
                if (outTag == null) outTag = new NbtCompound();
                outTag.putString("ForgingQuality", q);
            }
        }
        if (needPolishing) {
            if (outTag == null) outTag = new NbtCompound();
            outTag.putBoolean("Polished", false);
        }

        if (outTag == null) outTag = new NbtCompound();
        outTag.putBoolean("Heated", true);
        output.setNbt(outTag);

        if (itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, output);
        } else {
            itemHandler.getStackInSlot(SLOT_OUTPUT).increment(1);
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

        itemHandler.getStackInSlot(SLOT_INPUT).decrement(itemConsumeAmount);

        // Damage cast
        if (cast.isDamageable()) {
            cast.damage(1, world.random, null);

            if (cast.getDamage() >= cast.getMaxDamage()) {
                itemHandler.setStackInSlot(SLOT_CAST, ItemStack.EMPTY);
            }
        }
        if (!world.isClient && xp > 0)
            storedExperience += xp;
    }

    private void spawnExperience(float xp) {
        if (world == null || world.isClient) return;
        if (!(world instanceof ServerWorld serverWorld)) return;

        int i = MathHelper.floor(xp);
        float f = xp - i;
        if (f > 0 && Math.random() < f) i++;

        if (i > 0) {
            ExperienceOrbEntity.spawn(serverWorld, new Vec3d(
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5), i);
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.overgeared.casting_furnace");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CastFurnaceScreenHandler(syncId, playerInventory, this, data);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

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
        SimpleInventory inv = new SimpleInventory(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++)
            inv.setStack(i, itemHandler.getStackInSlot(i));
        ItemScatterer.spawn(world, pos, inv);
        spawnExperience(storedExperience);
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

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.UP) return new int[]{SLOT_INPUT, SLOT_CAST};
        if (side == Direction.DOWN) return new int[]{SLOT_OUTPUT};
        return new int[]{SLOT_FUEL};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == SLOT_OUTPUT) return false;
        if (slot == SLOT_FUEL) {
            Integer fuelTime = FuelRegistry.INSTANCE.get(stack.getItem());
            return fuelTime != null && fuelTime > 0;
        }
        if (slot == SLOT_CAST) return stack.isIn(ModTags.Items.TOOL_CAST);
        return ConfigHelper.isValidMaterial(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == SLOT_OUTPUT;
    }

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
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = stack.split(amount);
        if (!result.isEmpty()) markDirty();
        return result;
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
        if (world == null) return false;
        if (world.getBlockEntity(pos) != this) return false;

        return player.squaredDistanceTo(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clear() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
