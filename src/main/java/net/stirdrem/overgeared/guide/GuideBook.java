package net.stirdrem.overgeared.guide;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.stirdrem.overgeared.Overgeared;
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

    /** Set once a player has been given their first-join guide, so it is never handed out twice. */
    public static final AttachmentType<Boolean> RECEIVED_GUIDE = AttachmentRegistry.create(
            Overgeared.id("received_guide"),
            builder -> builder.persistent(Codec.BOOL).copyOnDeath());

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

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (!ServerConfig.GIVE_GUIDE_BOOK_ON_FIRST_JOIN.get()) return;
            if (player.getAttachedOrElse(RECEIVED_GUIDE, false)) return;

            give(player);
            player.setAttached(RECEIVED_GUIDE, true);
        });
    }
}
