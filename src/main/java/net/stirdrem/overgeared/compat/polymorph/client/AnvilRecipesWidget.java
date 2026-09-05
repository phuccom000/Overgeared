package net.stirdrem.overgeared.compat.polymorph.client;

import com.illusivesoulworks.polymorph.client.recipe.widget.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilScreenHandler;

/**
 * Adds Polymorph's recipe-choice button/list to an anvil screen, positioned off its result slot.
 * Extends Polymorph's own PersistentRecipesWidget (not just the public AbstractRecipesWidget)
 * because its generic screen hook (RecipesWidget.create) only sends the block-entity "listener"
 * registration packet - the thing that actually makes the server start pushing candidate recipes
 * to this client - for widgets of that specific type. It's an internal class, not the public api
 * package, but it's what Polymorph's own built-in furnace widget extends for the same reason.
 */
public class AnvilRecipesWidget extends PersistentRecipesWidget {
    private final Slot outputSlot;

    public AnvilRecipesWidget(AbstractContainerScreen<?> containerScreen) {
        super(containerScreen);
        this.outputSlot = ((AbstractSmithingAnvilScreenHandler) containerScreen.getMenu()).getResultSlot();
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }
}
