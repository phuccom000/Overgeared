package net.stirdrem.overgeared.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.guide.GuideBook;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.Locale;

public class ModCommands {

    private static final String[] QUALITIES = {"poor", "well", "expert", "perfect", "master"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /overgeared guide - anyone can get a fresh copy of the guide book
        dispatcher.register(
                Commands.literal("overgeared")
                        .then(Commands.literal("guide")
                                .executes(ctx -> {
                                    GuideBook.give(ctx.getSource().getPlayerOrException());
                                    return 1;
                                }))
        );


        // /setforgingquality <quality>
        dispatcher.register(
                Commands.literal("setforgingquality")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("quality", StringArgumentType.string())
                                .suggests((c, b) -> {
                                    for (String q : QUALITIES) b.suggest(q);
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String quality = StringArgumentType.getString(ctx, "quality").toLowerCase(Locale.ROOT);

                                    ItemStack inHand = player.getMainHandItem();
                                    if (inHand.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("You must hold an item"));
                                        return 0;
                                    }

                                    inHand.set(ModComponents.FORGING_QUALITY, ForgingQuality.fromString(quality));

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Set ForgingQuality to " + quality), false);

                                    return 1;
                                })
                        )
        );

        // /givecast <toolType> [quality] [material]
        dispatcher.register(
                Commands.literal("givecast")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("toolType", StringArgumentType.string())
                                .then(Commands.argument("quality", StringArgumentType.string())
                                        .suggests((c, b) -> {
                                            b.suggest("none");
                                            for (String q : QUALITIES) b.suggest(q);
                                            return b.buildFuture();
                                        })
                                        .then(Commands.argument("material", StringArgumentType.string())
                                                .suggests((c, b) -> {
                                                    b.suggest("clay");
                                                    b.suggest("nether");
                                                    return b.buildFuture();
                                                })
                                                .executes(ctx -> giveCast(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "toolType"),
                                                        StringArgumentType.getString(ctx, "quality"),
                                                        StringArgumentType.getString(ctx, "material")
                                                ))
                                        )
                                        .executes(ctx -> giveCast(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "toolType"),
                                                StringArgumentType.getString(ctx, "quality"),
                                                "clay"
                                        ))
                                )
                                .executes(ctx -> giveCast(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "toolType"),
                                        "none",
                                        "clay"
                                ))
                        )
        );
    }

    private static int giveCast(CommandSourceStack source, String toolType, String quality, String material) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        ItemStack stack = material.equalsIgnoreCase("nether") ?
                new ItemStack(ModItems.NETHER_TOOL_CAST) :
                new ItemStack(ModItems.CLAY_TOOL_CAST);

        CastData data = CastData.EMPTY
                .withToolType(toolType.toLowerCase(Locale.ROOT))
                .withAmount(0)
                .withMaxAmount(ConfigHelper.getMaxMaterialAmount(toolType));

        if (!quality.equalsIgnoreCase("none"))
            data = data.withQuality(quality.toLowerCase(Locale.ROOT));
        stack.set(ModComponents.CAST_DATA, data);

        player.addItem(stack);

        source.sendSuccess(
                () -> Component.literal("Gave cast: " + toolType +
                        (quality.equals("none") ? "" : " (" + quality + ")") +
                        " [" + material + "]"), false
        );

        return 1;
    }
}
