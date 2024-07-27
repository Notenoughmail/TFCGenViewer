package com.notenoughmail.tfcgenviewer.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec.IntValue defaultPreviewSize;

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
    }
}
