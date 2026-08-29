package net.stirdrem.overgeared.config;

import java.nio.file.Path;

public class ClientConfig {

    public static final ConfigSpec CLIENT_CONFIG;


    public static final ConfigSpec.IntValue MINIGAME_OVERLAY_HEIGHT;
    public static final ConfigSpec.BooleanValue POP_UP_TOGGLE;
    public static final ConfigSpec.BooleanValue ENABLE_ANVIL_RECIPE_BOOK;


    static {
        final ConfigSpec.Builder builder = new ConfigSpec.Builder();
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

    public static void loadConfig(ConfigSpec spec, Path path) {
        spec.load(path);
    }

}
