package net.stirdrem.overgeared.block.entity.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;

import java.util.HashSet;
import java.util.Set;

public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<AbstractSmithingAnvilBlockEntity> {
    private final ItemRenderer itemRenderer;

    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    private static final float BASE_Y = 1.01f;
    private static final float ITEM_HEIGHT = 0.02f;
    private static final float BLOCK_HEIGHT = 0.2f;
    private static final float BLOCK_BASE_Y_OFFSET = 0.09f;

    @Override
    public void render(AbstractSmithingAnvilBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light, int overlay) {
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

    private int renderPass(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
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
        Block block = Block.getBlockFromItem(item);
        return block != Blocks.AIR;
    }

    private void renderStack(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                              ItemStack itemStack, AbstractSmithingAnvilBlockEntity blockEntity,
                              float xOffset, float yOffset, float zOffset,
                              float rotationDegrees, float scale, float heightScale) {

        if (itemStack == null || itemStack.isEmpty()) return;

        matrices.push();

        BlockState state = blockEntity.getCachedState();
        Direction facing = state.contains(Properties.HORIZONTAL_FACING)
                ? state.get(Properties.HORIZONTAL_FACING)
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

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facingRotationDegrees));

        boolean isBlock = itemStack.getItem() instanceof BlockItem;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees));

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(isBlock ? 0 : 90));

        matrices.scale(scale, scale, scale * heightScale);

        itemRenderer.renderItem(itemStack, ModelTransformationMode.FIXED,
                getLightLevel(blockEntity.getWorld(), blockEntity.getPos()),
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, blockEntity.getWorld(), 1);

        matrices.pop();
    }

    private int getLightLevel(World world, BlockPos pos) {
        int bLight = world.getLightLevel(LightType.BLOCK, pos);
        int sLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(bLight, sLight);
    }
}
