package com.notenoughmail.tfcgenviewer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec.IntValue visualizeSize;

    public static void register() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        register(builder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, builder.build(), "tfcgenviewer.toml");
    }

    private static void register(ForgeConfigSpec.Builder builder) {
        visualizeSize = builder.comment(
                "",
                " The size vertical and horizontal distance to visualize, a restart is required to take effect",
                " Note that bigger sizes will take exponentially longer to preview",
                ""
                ).defineInRange("viewSize", 512, 16, 2048);
    }
}