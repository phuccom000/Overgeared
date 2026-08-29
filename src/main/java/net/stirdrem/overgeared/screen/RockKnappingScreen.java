package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.ClientModMessages;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.KnappingChipC2SPacket;

import java.util.HashSet;
import java.util.Set;

public class RockKnappingScreen extends HandledScreen<RockKnappingScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(Overgeared.MOD_ID, "textures/gui/rock_knapping_gui.png");
    private static final Identifier CHIPPED_TEXTURE =
            new Identifier(Overgeared.MOD_ID, "textures/gui/blank.png");

    private static final int GRID_ORIGIN_X = 32;
    private static final int GRID_ORIGIN_Y = 19;
    private static final int SLOT_SIZE = 16;

    private final Set<Integer> chippedSpots = new HashSet<>();

    public RockKnappingScreen(RockKnappingScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        chippedSpots.clear();

        addKnappingButtons();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (!handler.isKnappingFinished()) {
            addKnappingButtons();
        } else this.clearChildren();
    }

    private void addKnappingButtons() {
        this.clearChildren();

        if (handler.isKnappingFinished()) return;

        boolean hasResult = !handler.getSlot(9).getStack().isEmpty();
        boolean resultCollected = handler.isResultCollected();

        boolean canContinueKnapping = hasResult && !resultCollected;

        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = this.x + GRID_ORIGIN_X + col * SLOT_SIZE;
            int y = this.y + GRID_ORIGIN_Y + row * SLOT_SIZE;

            final int index = i;
            Identifier texture = handler.isChipped(i) || resultCollected
                    ? CHIPPED_TEXTURE
                    : handler.getUnchippedTexture();

            boolean isChipped = handler.isChipped(i);

            TexturedButtonWidget button = new TexturedButtonWidget(
                    x, y,
                    SLOT_SIZE, SLOT_SIZE,
                    0, 0, 0,
                    texture,
                    SLOT_SIZE, SLOT_SIZE,
                    btn -> {
                        if ((!hasResult || canContinueKnapping) && !isChipped) {
                            handler.setChip(index);
                            chippedSpots.add(index);
                            if (!resultCollected) {
                                var buf = ModMessages.buf();
                                KnappingChipC2SPacket.encode(new KnappingChipC2SPacket(index), buf);
                                ClientModMessages.sendToServer(ModMessages.KNAPPING_CHIP, buf);

                                client.player.playSound(handler.getSound(), 1.0F, 1.0F);
                            }
                            addKnappingButtons();
                        }
                    }
            ) {
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (!handler.isKnappingFinished()) {
                        return super.mouseClicked(mouseX, mouseY, button);
                    }
                    return false;
                }

                @Override
                public void playDownSound(SoundManager handler) {
                }
            };

            button.active = !handler.isKnappingFinished();

            this.addDrawableChild(button);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, partialTick);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float partialTick, int mouseX, int mouseY) {
        int x = this.x;
        int y = this.y;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.playerInventoryTitleY, 0x404040, false);
    }

    private void handleKnappingDrag(double mouseX, double mouseY) {
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = this.x + GRID_ORIGIN_X + col * SLOT_SIZE;
            int y = this.y + GRID_ORIGIN_Y + row * SLOT_SIZE;

            if (mouseX >= x && mouseX < x + SLOT_SIZE &&
                    mouseY >= y && mouseY < y + SLOT_SIZE &&
                    !handler.isKnappingFinished() &&
                    !handler.isChipped(i)) {

                handler.setChip(i);
                chippedSpots.add(i);
                if (!handler.isResultCollected()) {
                    var buf = ModMessages.buf();
                    KnappingChipC2SPacket.encode(new KnappingChipC2SPacket(i), buf);
                    ClientModMessages.sendToServer(ModMessages.KNAPPING_CHIP, buf);
                    client.player.playSound(SoundEvents.BLOCK_STONE_BREAK, 1.0F, 1.0F);
                }

                addKnappingButtons();
                break;
            }
        }
    }
}
