package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.Overgeared;
import net.stirdrem.overgeared.client.ClientModMessages;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.SelectToolTypeC2SPacket;

import java.util.List;

public class BlueprintWorkbenchScreen extends AbstractContainerScreen<BlueprintWorkbenchScreenHandler> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Overgeared.MOD_ID, "textures/gui/blueprint_workbench.png");

    private final List<ToolType> toolTypes;
    private int selectedIndex = 0;
    private Button prevButton;
    private Button nextButton;
    private Button selectButton;
    private Component currentToolName;

    public BlueprintWorkbenchScreen(BlueprintWorkbenchScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.toolTypes = ToolTypeRegistry.getRegisteredTypes();
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        createButtons(x, y);
        selectButton.active = false;

        if (toolTypes.isEmpty()) {
            handleNoToolsAvailable();
        } else {
            updateToolDisplay();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean hasItem = !menu.getSlot(0).getItem().isEmpty();
        selectButton.active = hasItem && !toolTypes.isEmpty();
    }

    private void createButtons(int x, int y) {
        int centerX = x + imageWidth / 2;
        int buttonRowY = y + 15;
        int buttonWidth = 10;
        int buttonPosFromCenter = 40;

        prevButton = Button.builder(Component.literal("<"), btn -> {
                    selectedIndex = (selectedIndex - 1 + toolTypes.size()) % toolTypes.size();
                    updateToolDisplay();
                })
                .pos(centerX - buttonWidth / 2 - buttonPosFromCenter, buttonRowY)
                .size(buttonWidth, 12)
                .tooltip(Tooltip.create(Component.translatable("tooltip.overgeared.previous_tool")))
                .build();

        nextButton = Button.builder(Component.literal(">"), btn -> {
                    selectedIndex = (selectedIndex + 1) % toolTypes.size();
                    updateToolDisplay();
                })
                .pos(centerX - buttonWidth / 2 + buttonPosFromCenter, buttonRowY)
                .size(buttonWidth, 12)
                .tooltip(Tooltip.create(Component.translatable("tooltip.overgeared.next_tool")))
                .build();

        int selectButtonWidth = 60;
        int selectButtonY = y + 58;
        selectButton = Button.builder(Component.translatable("button.overgeared.select"), btn -> {
                    if (!toolTypes.isEmpty()) {
                        var buf = ModMessages.buf();
                        new SelectToolTypeC2SPacket(toolTypes.get(selectedIndex).getId(), menu.containerId).toBytes(buf);
                        ClientModMessages.sendToServer(ModMessages.SELECT_TOOL_TYPE, buf);
                    }
                })
                .pos(x + imageWidth / 2 - selectButtonWidth / 2, selectButtonY)
                .size(selectButtonWidth, 14)
                .build();

        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);
        this.addRenderableWidget(selectButton);
    }

    private void handleNoToolsAvailable() {
        prevButton.active = false;
        nextButton.active = false;

        Tooltip noToolsTooltip = Tooltip.create(Component.translatable("tooltip.overgeared.no_tools_available"));
        prevButton.setTooltip(noToolsTooltip);
        nextButton.setTooltip(noToolsTooltip);
        selectButton.setTooltip(noToolsTooltip);

        currentToolName = Component.literal("Null");
    }

    private void updateToolDisplay() {
        if (!toolTypes.isEmpty()) {
            ToolType currentTool = toolTypes.get(selectedIndex);
            currentToolName = currentTool.getDisplayName();
            selectButton.setTooltip(Tooltip.create(Component.translatable("tooltip.overgeared.select_tool", currentToolName)));

            prevButton.active = true;
            nextButton.active = true;
        } else {
            handleNoToolsAvailable();
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        context.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (currentToolName != null) {
            int textWidth = this.font.width(currentToolName);
            int textColor = toolTypes.isEmpty() ? 0xFF0000 : 0x404040;
            context.drawString(this.font, currentToolName,
                    x + imageWidth / 2 - textWidth / 2, y + 18, textColor, false);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, partialTicks);
        this.renderTooltip(context, mouseX, mouseY);
    }
}
