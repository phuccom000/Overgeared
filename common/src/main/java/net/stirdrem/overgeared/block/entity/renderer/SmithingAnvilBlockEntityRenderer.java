package net.stirdrem.overgeared.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Draws the anvil's ingredients, output and hammer on top of the block. */
public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<AbstractSmithingAnvilBlockEntity, SmithingAnvilBlockEntityRenderer.State> {
    private static final float BASE_Y = 1.01f;
    private static final float ITEM_HEIGHT = 0.02f;
    private static final float BLOCK_HEIGHT = 0.2f;
    private static final float BLOCK_BASE_Y_OFFSET = 0.09f;

    private final ItemModelResolver itemModelResolver;

    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    /** One item drawn on the anvil, with the transform the old immediate-mode renderer applied. */
    private record Placement(ItemStackRenderState item, float x, float y, float z, float facingDegrees,
                             float rotationDegrees, boolean upright, float scale, float heightScale) {
    }

    public static class State extends BlockEntityRenderState {
        final List<Placement> placements = new ArrayList<>();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AbstractSmithingAnvilBlockEntity blockEntity, State state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.placements.clear();

        // Output item from slot 10
        ItemStack output = blockEntity.getRenderStack(10);
        boolean inputsEmpty = areInputSlotsEmpty(blockEntity);
        float zOffset = inputsEmpty ? 0f : -0.43f;

        int progress = blockEntity.getContainerData().get(0);
        int max = blockEntity.getContainerData().get(1);
        float heightScale = max <= 0 ? 1.0f : 1.0f - ((float) progress / max);

        if (!output.isEmpty()) {
            float yOffset = isBlockItem(output) ? 1.05f : 1.02f;
            addStack(state, blockEntity, output, 0.0f, yOffset, zOffset, 110f, 0.4f, 1.0f, 10);
        }

        // Up to three input items: unique items first, then fill with any items
        Set<Item> renderedItems = new HashSet<>();
        Set<Integer> renderedSlots = new HashSet<>();
        int rendered = renderPass(state, blockEntity, renderedItems, renderedSlots, 0, true, heightScale);
        if (rendered < 3) {
            renderPass(state, blockEntity, renderedItems, renderedSlots, rendered, false, heightScale);
        }

        // Hammer from slot 9
        addStack(state, blockEntity, blockEntity.getRenderStack(9), 0f, 1.025f, 0.43f, 135f, 0.5f, 1.0f, 9);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Placement p : state.placements) {
            poseStack.pushPose();
            poseStack.translate(p.x(), p.y(), p.z());
            poseStack.rotateDegrees(Axis.YP, p.facingDegrees());
            poseStack.rotateDegrees(Axis.YP, p.rotationDegrees());
            poseStack.rotateDegrees(Axis.XP, p.upright() ? 0 : 90);
            poseStack.scale(p.scale(), p.scale(), p.scale() * p.heightScale());
            p.item().submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private boolean areInputSlotsEmpty(AbstractSmithingAnvilBlockEntity be) {
        for (int i = 0; i < 9; i++) {
            if (!be.getRenderStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int renderPass(State state, AbstractSmithingAnvilBlockEntity blockEntity,
                           Set<Item> renderedItems, Set<Integer> renderedSlots,
                           int renderedCount, boolean checkUniqueness, float heightScale) {
        // Stack height accumulated over the slots already drawn
        float currentHeight = BASE_Y;
        for (int i : renderedSlots) {
            currentHeight += isBlockItem(blockEntity.getRenderStack(i)) ? BLOCK_HEIGHT : ITEM_HEIGHT;
        }

        int rendered = renderedCount;
        for (int i = 0; i < 9 && rendered < 3; i++) {
            if (renderedSlots.contains(i)) continue;

            ItemStack stack = blockEntity.getRenderStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (checkUniqueness && renderedItems.contains(item)) continue;

            float scale = isBlockItem(stack) ? 0.4f : 0.35f;
            float rotation = 96f + (rendered * 14f);
            float yOffset = currentHeight;
            if (isBlockItem(stack)) {
                yOffset += BLOCK_BASE_Y_OFFSET;
            }
            addStack(state, blockEntity, stack, 0.0f, yOffset, 0f, rotation, scale, heightScale, i);

            currentHeight += isBlockItem(stack) ? BLOCK_HEIGHT : ITEM_HEIGHT;
            renderedItems.add(item);
            renderedSlots.add(i);
            rendered++;
        }
        return rendered;
    }

    private boolean isBlockItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return Block.byItem(stack.getItem()) != Blocks.AIR;
    }

    private void addStack(State state, AbstractSmithingAnvilBlockEntity blockEntity, ItemStack itemStack,
                          float xOffset, float yOffset, float zOffset,
                          float rotationDegrees, float scale, float heightScale, int seed) {
        if (itemStack == null || itemStack.isEmpty()) return;

        BlockState blockState = blockEntity.getBlockState();
        Direction facing = blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        float facingRotationDegrees = switch (facing) {
            case NORTH -> 180f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f;
        };

        // Rotate the offset by the facing
        double radians = Math.toRadians(facingRotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        float rotatedX = (float) (xOffset * cos - zOffset * sin);
        float rotatedZ = (float) (xOffset * sin + zOffset * cos);

        ItemStackRenderState itemState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(itemState, itemStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null,
                (int) blockEntity.getBlockPos().asLong() + seed);

        state.placements.add(new Placement(itemState,
                0.5f - rotatedX, (float) (yOffset - (0.01 * (1 - heightScale))), 0.5f + rotatedZ,
                facingRotationDegrees, rotationDegrees, itemStack.getItem() instanceof BlockItem, scale, heightScale));
    }
}
