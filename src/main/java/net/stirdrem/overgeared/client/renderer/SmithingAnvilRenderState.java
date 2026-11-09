package net.stirdrem.overgeared.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class SmithingAnvilRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public final List<Entry> entries = new ArrayList<>();

    /** One item placed on the anvil, with the transform parameters computed at extract time. */
    public record Entry(ItemStackRenderState item, float xOffset, float yOffset, float zOffset,
                        float rotationDegrees, float scale, float heightScale, boolean isBlockItem) {
    }
}
