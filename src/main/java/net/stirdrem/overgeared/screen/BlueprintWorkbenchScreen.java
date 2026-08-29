package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.ClientModMessages;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.SelectToolTypeC2SPacket;

import java.util.List;

public class BlueprintWorkbenchScreen extends HandledScreen<BlueprintWorkbenchScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(Overgeared.MOD_ID, "textures/gui/blueprint_workbench.png");

    private final List<ToolType> toolTypes;
    private int selectedIndex = 0;
    private ButtonWidget prevButton;
    private ButtonWidget nextButton;
    private ButtonWidget selectButton;
    private Text currentToolName;

    public BlueprintWorkbenchScreen(BlueprintWorkbenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.toolTypes = ToolTypeRegistry.getRegisteredTypes();
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        createButtons(x, y);
        selectButton.active = false;

        if (toolTypes.isEmpty()) {
            handleNoToolsAvailable();
        } else {
            updateToolDisplay();
        }
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        boolean hasItem = !handler.getSlot(0).getStack().isEmpty();
        selectButton.active = hasItem && !toolTypes.isEmpty();
    }

    private void createButtons(int x, int y) {
        int centerX = x + backgroundWidth / 2;
        int buttonRowY = y + 15;
        int buttonWidth = 10;
        int buttonPosFromCenter = 40;

        prevButton = ButtonWidget.builder(Text.literal("<"), btn -> {
                    selectedIndex = (selectedIndex - 1 + toolTypes.size()) % toolTypes.size();
                    updateToolDisplay();
                })
                .position(centerX - buttonWidth / 2 - buttonPosFromCenter, buttonRowY)
                .size(buttonWidth, 12)
                .tooltip(Tooltip.of(Text.translatable("tooltip.overgeared.previous_tool")))
                .build();

        nextButton = ButtonWidget.builder(Text.literal(">"), btn -> {
                    selectedIndex = (selectedIndex + 1) % toolTypes.size();
                    updateToolDisplay();
                })
                .position(centerX - buttonWidth / 2 + buttonPosFromCenter, buttonRowY)
                .size(buttonWidth, 12)
                .tooltip(Tooltip.of(Text.translatable("tooltip.overgeared.next_tool")))
                .build();

        int selectButtonWidth = 60;
        int selectButtonY = y + 58;
        selectButton = ButtonWidget.builder(Text.translatable("button.overgeared.select"), btn -> {
                    if (!toolTypes.isEmpty()) {
                        var buf = ModMessages.buf();
                        new SelectToolTypeC2SPacket(toolTypes.get(selectedIndex).getId(), handler.syncId).toBytes(buf);
                        ClientModMessages.sendToServer(ModMessages.SELECT_TOOL_TYPE, buf);
                    }
                })
                .position(x + backgroundWidth / 2 - selectButtonWidth / 2, selectButtonY)
                .size(selectButtonWidth, 14)
                .build();

        this.addDrawableChild(prevButton);
        this.addDrawableChild(nextButton);
        this.addDrawableChild(selectButton);
    }

    private void handleNoToolsAvailable() {
        prevButton.active = false;
        nextButton.active = false;

        Tooltip noToolsTooltip = Tooltip.of(Text.translatable("tooltip.overgeared.no_tools_available"));
        prevButton.setTooltip(noToolsTooltip);
        nextButton.setTooltip(noToolsTooltip);
        selectButton.setTooltip(noToolsTooltip);

        currentToolName = Text.literal("Null");
    }

    private void updateToolDisplay() {
        if (!toolTypes.isEmpty()) {
            ToolType currentTool = toolTypes.get(selectedIndex);
            currentToolName = currentTool.getDisplayName();
            selectButton.setTooltip(Tooltip.of(Text.translatable("tooltip.overgeared.select_tool", currentToolName)));

            prevButton.active = true;
            nextButton.active = true;
        } else {
            handleNoToolsAvailable();
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if (currentToolName != null) {
            int textWidth = this.textRenderer.getWidth(currentToolName);
            int textColor = toolTypes.isEmpty() ? 0xFF0000 : 0x404040;
            context.drawText(this.textRenderer, currentToolName,
                    x + backgroundWidth / 2 - textWidth / 2, y + 18, textColor, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
