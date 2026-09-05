package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.ClientModMessages;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.KnappingChipC2SPacket;

import java.util.HashSet;
import java.util.Set;

public class RockKnappingScreen extends AbstractContainerScreen<RockKnappingScreenHandler> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Overgeared.MOD_ID, "textures/gui/rock_knapping_gui.png");
    private static final ResourceLocation CHIPPED_TEXTURE =
            new ResourceLocation(Overgeared.MOD_ID, "textures/gui/blank.png");

    private static final int GRID_ORIGIN_X = 32;
    private static final int GRID_ORIGIN_Y = 19;
    private static final int SLOT_SIZE = 16;

    private final Set<Integer> chippedSpots = new HashSet<>();

    public RockKnappingScreen(RockKnappingScreenHandler handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
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
            ResourceLocation texture = menu.isChipped(i) || resultCollected
                    ? CHIPPED_TEXTURE
                    : menu.getUnchippedTexture();

            boolean isChipped = menu.isChipped(i);

            ImageButton button = new ImageButton(
                    x, y,
                    SLOT_SIZE, SLOT_SIZE,
                    0, 0, 0,
                    texture,
                    SLOT_SIZE, SLOT_SIZE,
                    btn -> {
                        if ((!hasResult || canContinueKnapping) && !isChipped) {
                            menu.setChip(index);
                            chippedSpots.add(index);
                            if (!resultCollected) {
                                var buf = ModMessages.buf();
                                KnappingChipC2SPacket.encode(new KnappingChipC2SPacket(index), buf);
                                ClientModMessages.sendToServer(ModMessages.KNAPPING_CHIP, buf);

                                minecraft.player.playSound(menu.getSound(), 1.0F, 1.0F);
                            }
                            addKnappingButtons();
                        }
                    }
            ) {
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (!menu.isKnappingFinished()) {
                        return super.mouseClicked(mouseX, mouseY, button);
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
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, partialTick);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        context.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        context.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        context.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
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
                    var buf = ModMessages.buf();
                    KnappingChipC2SPacket.encode(new KnappingChipC2SPacket(i), buf);
                    ClientModMessages.sendToServer(ModMessages.KNAPPING_CHIP, buf);
                    minecraft.player.playSound(SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                }

                addKnappingButtons();
                break;
            }
        }
    }
}
