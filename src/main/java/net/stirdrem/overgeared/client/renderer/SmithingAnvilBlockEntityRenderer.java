package net.stirdrem.overgeared.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Renders the anvil contents (up to three inputs stacked, the output and the hammer) on top of
 * the smithing anvils. 26.3 port of block/entity/renderer/SmithingAnvilBlockEntityRenderer:
 * all placement math now happens in {@link #extractRenderState}, {@link #submit} only applies it.
 */
public class SmithingAnvilBlockEntityRenderer
        implements BlockEntityRenderer<AbstractSmithingAnvilBlockEntity, SmithingAnvilRenderState> {

    private static final float BASE_Y = 1.01f;
    private static final float ITEM_HEIGHT = 0.02f;
    private static final float BLOCK_HEIGHT = 0.2f;
    private static final float BLOCK_BASE_Y_OFFSET = 0.09f;

    private final ItemModelResolver itemModelResolver;

    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public SmithingAnvilRenderState createRenderState() {
        return new SmithingAnvilRenderState();
    }

    @Override
    public void extractRenderState(AbstractSmithingAnvilBlockEntity blockEntity, SmithingAnvilRenderState state,
                                   float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.entries.clear();

        BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        // Output item from slot 10
        ItemStack output = blockEntity.getRenderStack(10);
        boolean inputsEmpty = areInputSlotsEmpty(blockEntity);
        float zOffset = inputsEmpty ? 0f : -0.43f;

        float heightScale;
        int progress = blockEntity.getContainerData().get(0);
        int max = blockEntity.getContainerData().get(1);
        if (max <= 0) {
            heightScale = 1.0f; // default when no recipe / not started
        } else {
            heightScale = 1.0f - ((float) progress / max);
        }

        int seed = (int) blockEntity.getBlockPos().asLong();
        if (!output.isEmpty()) {
            float yOffset = isBlockItem(output) ? 1.05f : 1.02f;
            addEntry(state, blockEntity, output, seed + 10, 0.0f, yOffset, zOffset, 110f, 0.4f, 1.0f);
        }

        // First pass: up to three unique input items; second pass: fill with any items
        Set<Item> renderedItems = new HashSet<>();
        Set<Integer> renderedSlots = new HashSet<>();
        int rendered = collectPass(state, blockEntity, seed, renderedItems, renderedSlots, 0f, 0, true, heightScale);
        if (rendered < 3) {
            collectPass(state, blockEntity, seed, renderedItems, renderedSlots, 0f, rendered, false, heightScale);
        }

        // Hammer from slot 9
        ItemStack hammer = blockEntity.getRenderStack(9);
        addEntry(state, blockEntity, hammer, seed + 9, 0f, 1.025f, 0.43f, 135f, 0.5f, 1.0f);
    }

    private boolean areInputSlotsEmpty(AbstractSmithingAnvilBlockEntity be) {
        for (int i = 0; i < 9; i++) { // slots 0-8 are inputs
            if (!be.getRenderStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int collectPass(SmithingAnvilRenderState state, AbstractSmithingAnvilBlockEntity blockEntity, int seed,
                            Set<Item> renderedItems, Set<Integer> renderedSlots,
                            float zOffset, int renderedCount, boolean checkUniqueness, float heightScale) {
        float currentHeight = BASE_Y;
        for (int i : renderedSlots) {
            ItemStack prev = blockEntity.getRenderStack(i);
            currentHeight += isBlockItem(prev) ? BLOCK_HEIGHT : ITEM_HEIGHT;
        }

        int rendered = renderedCount;
        for (int i = 0; i < 9 && rendered < 3; i++) {
            if (renderedSlots.contains(i)) continue;

            ItemStack stack = blockEntity.getRenderStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (checkUniqueness && renderedItems.contains(item)) continue;

            boolean block = isBlockItem(stack);
            float scale = block ? 0.4f : 0.35f;
            float rotation = 96f + (rendered * 14f);
            float yOffset = currentHeight + (block ? BLOCK_BASE_Y_OFFSET : 0f);

            addEntry(state, blockEntity, stack, seed + i, 0.0f, yOffset, zOffset, rotation, scale, heightScale);

            currentHeight += block ? BLOCK_HEIGHT : ITEM_HEIGHT;
            renderedItems.add(item);
            renderedSlots.add(i);
            rendered++;
        }
        return rendered;
    }

    private void addEntry(SmithingAnvilRenderState state, AbstractSmithingAnvilBlockEntity blockEntity, ItemStack stack,
                          int seed, float xOffset, float yOffset, float zOffset,
                          float rotationDegrees, float scale, float heightScale) {
        if (stack == null || stack.isEmpty()) return;
        ItemStackRenderState itemState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, seed);
        state.entries.add(new SmithingAnvilRenderState.Entry(itemState, xOffset, yOffset, zOffset,
                rotationDegrees, scale, heightScale, stack.getItem() instanceof BlockItem));
    }

    private static boolean isBlockItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return Block.byItem(stack.getItem()) != Blocks.AIR;
    }

    @Override
    public void submit(SmithingAnvilRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        float facingRotationDegrees = switch (state.facing) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f;
        };
        double radians = Math.toRadians(facingRotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        for (SmithingAnvilRenderState.Entry e : state.entries) {
            if (e.item().isEmpty()) continue;
            poseStack.pushPose();

            float rotatedX = (float) (e.xOffset() * cos - e.zOffset() * sin);
            float rotatedZ = (float) (e.xOffset() * sin + e.zOffset() * cos);

            poseStack.translate(0.5f - rotatedX, e.yOffset() - (0.01f * (1 - e.heightScale())), 0.5f + rotatedZ);
            poseStack.rotateDegrees(Axis.YP, facingRotationDegrees);
            poseStack.rotateDegrees(Axis.YP, e.rotationDegrees());
            poseStack.rotateDegrees(Axis.XP, e.isBlockItem() ? 0 : 90);
            poseStack.scale(e.scale(), e.scale(), e.scale() * e.heightScale());

            e.item().submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
