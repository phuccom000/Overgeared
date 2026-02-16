package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.SteelSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SteelSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {
    private static final int BLUEPRINT_SLOT = 11;

    public SteelSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super((SteelSmithingAnvil) pBlockState.getBlock(), AnvilTier.IRON, ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new SteelSmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        } else return null;
    }

    @Override
    protected String determineForgingQuality() {
        // Get quality from anvil or use default if null
        if (!ServerConfig.ENABLE_BLUEPRINT_FORGING.get()) {
            String quality = anvilBlock.getQuality();
            if (quality == null) {
                return ForgingQuality.POOR.getDisplayName(); // Default quality
            }
            if (quality.equals(ForgingQuality.PERFECT.getDisplayName())) {
                Random random = new Random();

                // 🔹 Check if any crafting slot contains a Master-quality ingredient
                boolean hasMasterIngredient = false;
                for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                    if (i == OUTPUT_SLOT || i == BLUEPRINT_SLOT) continue; // skip output + blueprint
                    ItemStack stack = this.itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
                        String ingQuality = stack.getTag().getString("ForgingQuality").toLowerCase();
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
        } else return super.determineForgingQuality();
    }

    @Override
    public String blueprintQuality() {
        if (!ServerConfig.ENABLE_BLUEPRINT_FORGING.get())
            return BlueprintQuality.PERFECT.getDisplayName();
        else return super.blueprintQuality();
    }

    @Override
    protected void craftItem() {
        super.craftItem();
        super.craftItemWithBlueprint();
    }

    @Override
    public boolean hasRecipe() {
        return super.hasRecipeWithBlueprint();
    }


}
