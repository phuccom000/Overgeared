package net.stirdrem.overgeared.guide;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The Overgeared guide: a vanilla written book whose pages are translatable components
 * ({@code book.overgeared.guide.page.<n>} in the lang files), so it is localised on the client.
 * Every player gets one the first time they join a world; {@code /overgeared guide} gives another.
 */
public final class GuideBook {
    /** Number of {@code book.overgeared.guide.page.<n>} keys, numbered from 1. */
    public static final int PAGE_COUNT = 28;

    private static final String TITLE = "Overgeared Guide";
    private static final String AUTHOR = "Overgeared";

    /**
     * Entity tag set once a player got their first-join guide. Entity tags are saved with the player
     * and kept on respawn, on both loaders.
     */
    private static final String RECEIVED_TAG = "overgeared.received_guide";

    private GuideBook() {
    }

    public static ItemStack create() {
        List<Filterable<Component>> pages = new ArrayList<>(PAGE_COUNT);
        for (int i = 1; i <= PAGE_COUNT; i++) {
            pages.add(Filterable.passThrough(Component.translatable("book.overgeared.guide.page." + i)));
        }
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(Filterable.passThrough(TITLE), AUTHOR, 0, pages, true));
        return book;
    }

    public static void give(ServerPlayer player) {
        ItemStack book = create();
        if (!player.getInventory().add(book)) {
            player.drop(book, false, Prediction.SERVER_ONLY);
        }
    }

    /** Called when a player joins; hands out the guide once per player. */
    public static void onPlayerJoin(ServerPlayer player) {
        if (!ServerConfig.GIVE_GUIDE_BOOK_ON_FIRST_JOIN.get()) return;
        if (player.entityTags().contains(RECEIVED_TAG)) return;

        give(player);
        player.addTag(RECEIVED_TAG);
    }
}
