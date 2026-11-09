package net.stirdrem.overgeared.client;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlacklistEditorScreen extends Screen {
    private final String blacklistName;
  private final Consumer<List<? extends String>> setter;
    private final double savedScrollPosition;
    private final Screen parentScreen;

    private BlacklistEditorList itemList;
    private EditBox addItemField;
    private final List<String> items;

    public BlacklistEditorScreen(String blacklistName, List<? extends String> currentItems,
                                 Consumer<List<? extends String>> setter,
                                 double savedScrollPosition, Screen parentScreen) {
        super(Component.literal("Edit " + blacklistName));
        this.blacklistName = blacklistName;
        List<String> originalItems = new ArrayList<>(currentItems);
        this.setter = setter;
        this.savedScrollPosition = savedScrollPosition;
        this.parentScreen = parentScreen;
        this.items = new ArrayList<>(originalItems);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int listWidth = this.width; // Reasonable max width
        int listHeight = this.height - 100;

        // Create the item list
        this.itemList = new BlacklistEditorList(
                Minecraft.getInstance(),
                listWidth, listHeight,
                40,
                20
        );
        this.addWidget(itemList);

        // Populate the list with current items
        refreshItemList();

        // Add item input field
        this.addItemField = new EditBox(font, centerX - 120, this.height - 50, 200, 20,
                Component.literal("Enter item ID or tag..."));
        this.addItemField.setMaxLength(256);
        this.addRenderableWidget(addItemField);

        // Add button
      Button addButton = Button.builder(Component.literal("Add"), btn -> {
        String newItem = addItemField.getValue().trim();
        if (!newItem.isEmpty() && !items.contains(newItem)) {
          items.add(newItem);
          addItemField.setValue("");
          refreshItemList();
        }
      }).bounds(centerX + 90, this.height - 50, 50, 20).build();
        this.addRenderableWidget(addButton);

        // Done button
      // Save the changes back to the config
      // Add this line
      Button doneButton = Button.builder(Component.literal("Done"), btn -> {
        // Save the changes back to the config
        setter.accept(items);
        if (parentScreen instanceof OvergearedConfigScreen configScreen) {
          configScreen.setScrollPosition(savedScrollPosition);
          configScreen.restoreScrollPosition(); // Add this line
        }
        this.minecraft.gui.setScreen(parentScreen);
      }).bounds(centerX - 105, this.height - 25, 100, 20).build();
        this.addRenderableWidget(doneButton);

        // Cancel button
        Button cancelButton = Button.builder(Component.literal("Cancel"), btn -> {
            if (parentScreen instanceof OvergearedConfigScreen configScreen) {
              configScreen.setScrollPosition(savedScrollPosition);
                configScreen.restoreScrollPosition(); // Add this line
            }
            this.minecraft.gui.setScreen(parentScreen);
        }).bounds(centerX + 5, this.height - 25, 100, 20).build();
        this.addRenderableWidget(cancelButton);
    }

    private void refreshItemList() {
        itemList.children().clear();
        for (String item : items) {
            itemList.addNewEntry(new BlacklistItemEntry(item));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
        this.itemList.extractRenderState(gui, mouseX, mouseY, partialTicks);
        super.extractRenderState(gui, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;

        // Draw title
        gui.centeredText(font, "Edit " + blacklistName, centerX, 15, 0xFFFFFFFF);
        gui.centeredText(font, "Add item IDs or tags (prefix with # for tags)", centerX, 25, 0xFFAAAAAA);
        gui.centeredText(font, "Current items: " + items.size(), centerX, this.height - 70, 0xFFAAAAAA);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.keycode();
        int modifiers = event.modifiers();
        if (keyCode == 256) { // ESC key
            if (parentScreen instanceof OvergearedConfigScreen configScreen) {
                configScreen.setScrollPosition(savedScrollPosition);
                configScreen.restoreScrollPosition(); // Add this line
            }
            this.minecraft.gui.setScreen(parentScreen);
            return true;
        } else if (keyCode == 257 && addItemField.isFocused()) { // Enter key in add field
            String newItem = addItemField.getValue().trim();
            if (!newItem.isEmpty() && !items.contains(newItem)) {
                items.add(newItem);
                addItemField.setValue("");
                refreshItemList();
            }
            return true;
        }
        return super.keyPressed(event);
    }

    // Inner class for individual blacklist item entries
    private class BlacklistItemEntry extends ObjectSelectionList.Entry<BlacklistItemEntry> {
        private final String item;
        private final Button removeButton;

        public BlacklistItemEntry(String item) {
            this.item = item;
            this.removeButton = Button.builder(Component.literal("Remove"), btn -> {
                items.remove(item);
                refreshItemList();
            }).bounds(0, 0, 60, 16).build();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor gui, int mouseX, int mouseY, boolean hovered, float pt) {
            int y = this.getContentY();
            int x = this.getContentX();
            int listWidth = this.getContentWidth();
            int entryHeight = this.getContentHeight();
            int color = item.startsWith("#") ? 0x55FF55 : 0xFFFFFF; // Green for tags, white for items
            gui.text(font, item, x + 5, y + 2, color);

            removeButton.setX(x + listWidth - 90);
            removeButton.setY(y);
            removeButton.extractRenderState(gui, mouseX, mouseY, pt);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
            if (removeButton.isMouseOver(mouseX, mouseY)) {
                return removeButton.mouseClicked(event, doubleClick);
            }
            return false;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
            return removeButton.mouseReleased(event);
        }

        @Override
        public Component getNarration() {
            return Component.literal(item);
        }
    }

    // Custom list for blacklist items
    private class BlacklistEditorList extends ObjectSelectionList<BlacklistItemEntry> {

      public BlacklistEditorList(Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
        }

        @Override
        public int getRowWidth() {
          // Padding on left and right of each entry
          int entryPadding = 30;
          return this.width - (entryPadding * 2); // Reduce available width by padding
        }

        public void addNewEntry(BlacklistItemEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
            if (super.mouseClicked(event, doubleClick)) {
                return true;
            }
            this.setSelected(null);
            return false;
        }
    }
}