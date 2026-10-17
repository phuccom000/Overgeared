package net.stirdrem.overgeared.block.entity.renderer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashSet;
import java.util.Set;

public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<AbstractSmithingAnvilBlockEntity> {
    private final ItemRenderer itemRenderer;

    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    private static final float BASE_Y = 1.01f;
    private static final float ITEM_HEIGHT = 0.02f;
    private static final float BLOCK_HEIGHT = 0.2f;
    private static final float BLOCK_BASE_Y_OFFSET = 0.09f;

    @Override
    public void render(AbstractSmithingAnvilBlockEntity blockEntity, float tickDelta, PoseStack matrices,
                        MultiBufferSource vertexConsumers, int light, int overlay) {
        // Render the output item from slot 10
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
        if (!output.isEmpty()) {
            float yOffset = isBlockItem(output) ? 1.05f : 1.02f;
            renderStack(matrices, vertexConsumers, output, blockEntity, 0.0f, yOffset, zOffset, 110f, 0.4f, 1.0f);
        }

        // First pass: render up to three unique input items
        Set<Item> renderedItems = new HashSet<>();
        Set<Integer> renderedSlots = new HashSet<>();
        int rendered;

        rendered = renderPass(matrices, vertexConsumers, blockEntity,
                renderedItems, renderedSlots, 0f, 0, true, heightScale);

        // Second pass: fill remaining slots with any items
        if (rendered < 3) {
            renderPass(matrices, vertexConsumers, blockEntity,
                    renderedItems, renderedSlots, 0f, rendered, false, heightScale);
        }

        // Render the hammer from slot 9
        ItemStack hammer = blockEntity.getRenderStack(9);
        renderStack(matrices, vertexConsumers, hammer, blockEntity, 0f, 1.025f, 0.43f, 135f, 0.5f, 1.0f);
    }

    private boolean areInputSlotsEmpty(AbstractSmithingAnvilBlockEntity be) {
        for (int i = 0; i < 9; i++) { // assuming slots 0-8 are inputs
            if (!be.getRenderStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int renderPass(PoseStack matrices, MultiBufferSource vertexConsumers,
                            AbstractSmithingAnvilBlockEntity blockEntity,
                            Set<Item> renderedItems, Set<Integer> renderedSlots,
                            float zOffset, int renderedCount, boolean checkUniqueness, float heightScale) {

        float currentHeight = BASE_Y;

        for (int i : renderedSlots) {
            ItemStack prev = blockEntity.getRenderStack(i);
            currentHeight += isBlockItem(prev) ? BLOCK_HEIGHT : ITEM_HEIGHT;
        }

        int rendered = renderedCount;

        for (int i = 0; i < 9 && rendered < 3; i++) {

            if (renderedSlots.contains(i)) {
                continue;
            }

            ItemStack stack = blockEntity.getRenderStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (checkUniqueness && renderedItems.contains(item)) {
                continue;
            }

            float scale = isBlockItem(stack) ? 0.4f : 0.35f;
            float rotation = 96f + (rendered * 14f);

            float yOffset = currentHeight;

            if (isBlockItem(stack)) {
                yOffset += BLOCK_BASE_Y_OFFSET;
            }
            renderStack(
                    matrices, vertexConsumers, stack, blockEntity,
                    0.0f, yOffset, zOffset, rotation, scale, heightScale
            );

            currentHeight += isBlockItem(stack) ? BLOCK_HEIGHT : ITEM_HEIGHT;

            renderedItems.add(item);
            renderedSlots.add(i);
            rendered++;
        }

        return rendered;
    }

    // Helper method to determine if an ItemStack is a block item
    private boolean isBlockItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        Block block = Block.byItem(item);
        return block != Blocks.AIR;
    }

    private void renderStack(PoseStack matrices, MultiBufferSource vertexConsumers,
                              ItemStack itemStack, AbstractSmithingAnvilBlockEntity blockEntity,
                              float xOffset, float yOffset, float zOffset,
                              float rotationDegrees, float scale, float heightScale) {

        if (itemStack == null || itemStack.isEmpty()) return;

        matrices.pushPose();

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH; // default fallback

        float facingRotationDegrees = switch (facing) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f;
        };

        double radians = Math.toRadians(facingRotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        float rotatedX = (float) (xOffset * cos - zOffset * sin);
        float rotatedZ = (float) (xOffset * sin + zOffset * cos);

        matrices.translate(0.5f - rotatedX, yOffset - (0.01 * (1 - heightScale)), 0.5f + rotatedZ);

        matrices.mulPose(Axis.YP.rotationDegrees(facingRotationDegrees));

        boolean isBlock = itemStack.getItem() instanceof BlockItem;

        matrices.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        matrices.mulPose(Axis.XP.rotationDegrees(isBlock ? 0 : 90));

        matrices.scale(scale, scale, scale * heightScale);

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED,
                getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, matrices, vertexConsumers, blockEntity.getLevel(), 1);

        matrices.popPose();
    }

    private int getLightLevel(Level world, BlockPos pos) {
        int bLight = world.getBrightness(LightLayer.BLOCK, pos);
        int sLight = world.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
