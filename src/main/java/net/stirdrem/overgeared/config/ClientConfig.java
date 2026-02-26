package net.stirdrem.overgeared.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.IntValue MINIGAME_OVERLAY_HEIGHT;
    public static final ModConfigSpec.BooleanValue POP_UP_TOGGLE;
    public static final ModConfigSpec.BooleanValue ENABLE_ANVIL_RECIPE_BOOK;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("Minigame Config");

        MINIGAME_OVERLAY_HEIGHT = builder
                .comment("Vertical position of the minigame overlay")
                .defineInRange("overlayHeight", 55, -10000, 10000);
        POP_UP_TOGGLE = builder
                .comment("If minigame's pop up appear during minigame.")
                .define("PopupVisible", true);

        builder.pop();
        builder.push("Anvil Config");
        ENABLE_ANVIL_RECIPE_BOOK = builder.comment("Toggle Recipe Book for Smithing Anvils").define("enableRecipeBookAnvils", true);
        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
