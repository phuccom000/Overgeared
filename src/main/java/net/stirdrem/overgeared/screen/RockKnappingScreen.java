package net.stirdrem.overgeared.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.networking.packet.KnappingChipC2SPacket;

import java.util.HashSet;
import java.util.Set;

public class RockKnappingScreen extends AbstractContainerScreen<RockKnappingScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/rock_knapping_gui.png");
    private static final Identifier CHIPPED_TEXTURE =
            Identifier.fromNamespaceAndPath(Overgeared.MOD_ID, "textures/gui/blank.png");

    private static final int GRID_ORIGIN_X = 32;
    private static final int GRID_ORIGIN_Y = 19;
    private static final int SLOT_SIZE = 16;

    private final Set<Integer> chippedSpots = new HashSet<>();

    public RockKnappingScreen(RockKnappingScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title, 176, 166);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        chippedSpots.clear();

        addKnappingButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!menu.isKnappingFinished()) {
            addKnappingButtons();
        } else this.clearWidgets();
    }

    private void addKnappingButtons() {
        this.clearWidgets();

        if (menu.isKnappingFinished()) return;

        boolean hasResult = !menu.getSlot(9).getItem().isEmpty();
        boolean resultCollected = menu.isResultCollected();

        boolean canContinueKnapping = hasResult && !resultCollected;

        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = this.leftPos + GRID_ORIGIN_X + col * SLOT_SIZE;
            int y = this.topPos + GRID_ORIGIN_Y + row * SLOT_SIZE;

            final int index = i;
            Identifier texture = menu.isChipped(i) || resultCollected
                    ? CHIPPED_TEXTURE
                    : menu.getUnchippedTexture();

            boolean isChipped = menu.isChipped(i);

            // ImageButton now only takes GUI sprites; the unchipped texture is a datapack-defined
            // full texture, so the button blits it directly.
            Button button = new Button(
                    x, y,
                    SLOT_SIZE, SLOT_SIZE,
                    Component.empty(),
                    btn -> {
                        if ((!hasResult || canContinueKnapping) && !isChipped) {
                            menu.setChip(index);
                            chippedSpots.add(index);
                            if (!resultCollected) {
                                ClientPlayNetworking.send(new KnappingChipC2SPacket(index));

                                minecraft.player.playSound(menu.getSound(), 1.0F, 1.0F);
                            }
                            addKnappingButtons();
                        }
                    },
                    narration -> narration.get()
            ) {
                @Override
                protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), 0.0F, 0.0F,
                            SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                }

                @Override
                public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                    if (!menu.isKnappingFinished()) {
                        return super.mouseClicked(event, doubleClick);
                    }
                    return false;
                }

                @Override
                public void playDownSound(SoundManager handler) {
                }
            };

            button.active = !menu.isKnappingFinished();

            this.addRenderableWidget(button);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(context, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        context.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        context.text(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFF404040, false);
    }

    private void handleKnappingDrag(double mouseX, double mouseY) {
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = this.leftPos + GRID_ORIGIN_X + col * SLOT_SIZE;
            int y = this.topPos + GRID_ORIGIN_Y + row * SLOT_SIZE;

            if (mouseX >= x && mouseX < x + SLOT_SIZE &&
                    mouseY >= y && mouseY < y + SLOT_SIZE &&
                    !menu.isKnappingFinished() &&
                    !menu.isChipped(i)) {

                menu.setChip(i);
                chippedSpots.add(i);
                if (!menu.isResultCollected()) {
                    ClientPlayNetworking.send(new KnappingChipC2SPacket(i));
                    minecraft.player.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                }

                addKnappingButtons();
                break;
            }
        }
    }
}
