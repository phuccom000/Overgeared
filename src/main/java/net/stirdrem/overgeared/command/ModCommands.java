package net.stirdrem.overgeared.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.Locale;

public class ModCommands {

    private static final String[] QUALITIES = {"poor", "well", "expert", "perfect", "master"};

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // /setforgingquality <quality>
        dispatcher.register(
                CommandManager.literal("setforgingquality")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("quality", StringArgumentType.string())
                                .suggests((c, b) -> {
                                    for (String q : QUALITIES) b.suggest(q);
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                                    String quality = StringArgumentType.getString(ctx, "quality").toLowerCase(Locale.ROOT);

                                    ItemStack inHand = player.getMainHandStack();
                                    if (inHand.isEmpty()) {
                                        ctx.getSource().sendError(Text.literal("You must hold an item"));
                                        return 0;
                                    }

                                    NbtCompound tag = inHand.getOrCreateNbt();
                                    tag.putString("ForgingQuality", quality);

                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Set ForgingQuality to " + quality), false);

                                    return 1;
                                })
                        )
        );

        // /givecast <toolType> [quality] [material]
        dispatcher.register(
                CommandManager.literal("givecast")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("toolType", StringArgumentType.string())
                                .then(CommandManager.argument("quality", StringArgumentType.string())
                                        .suggests((c, b) -> {
                                            b.suggest("none");
                                            for (String q : QUALITIES) b.suggest(q);
                                            return b.buildFuture();
                                        })
                                        .then(CommandManager.argument("material", StringArgumentType.string())
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

    private static int giveCast(ServerCommandSource source, String toolType, String quality, String material) {
        ServerPlayerEntity player = source.getPlayer();

        ItemStack stack = material.equalsIgnoreCase("nether") ?
                new ItemStack(ModItems.NETHER_TOOL_CAST) :
                new ItemStack(ModItems.CLAY_TOOL_CAST);

        NbtCompound tag = stack.getOrCreateNbt();
        tag.putString("ToolType", toolType.toLowerCase(Locale.ROOT));
        tag.putInt("Amount", 0);
        tag.putInt("MaxAmount", ConfigHelper.getMaxMaterialAmount(toolType));
        tag.put("Materials", new NbtCompound());

        if (!quality.equalsIgnoreCase("none"))
            tag.putString("Quality", quality.toLowerCase(Locale.ROOT));

        player.giveItemStack(stack);

        source.sendFeedback(
                () -> Text.literal("Gave cast: " + toolType +
                        (quality.equals("none") ? "" : " (" + quality + ")") +
                        " [" + material + "]"), false
        );

        return 1;
    }
}
