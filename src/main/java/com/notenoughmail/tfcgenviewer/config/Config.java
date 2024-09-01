package com.notenoughmail.tfcgenviewer.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec.IntValue defaultPreviewSize;

    public static ForgeConfigSpec.BooleanValue useThrobber, dingWhenGenerated, generationProgress, cancelPreviewOnError;

    public static void register() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        register(builder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, builder.build(), "tfcgenviewer.toml");
    }

    private static void register(ForgeConfigSpec.Builder builder) {
        defaultPreviewSize = builder.comment(
                "",
                " The preview size the preview screen will have when first opened",
                " Conversion to km:",
                "     (2 ^ (defaultPreviewSize + 5)) * 128 / 1000",
                ""
                ).defineInRange("defaultPreviewSize", 3, 0, 6);

        useThrobber = builder.comment(
                "",
                " If the preview should change to a loading icon when a preview is being generated",
                ""
        ).define("loadingIcon", true);

        dingWhenGenerated = builder.comment(
                "",
                " If a sound should be played when a preview finishes generating",
                ""
        ).define("dingWhenGenerated", true);

        generationProgress = builder.comment(
                "",
                " If the info pane should show a prgress bar while a preview is being generated",
                ""
        ).define("generationProgress", true);

        cancelPreviewOnError = builder.comment(
                "",
                " If errors encountered during preview generation should cancel further generation or simply only be logged",
                ""
        ).define("cancelPreviewOnError", true);
    }
}
