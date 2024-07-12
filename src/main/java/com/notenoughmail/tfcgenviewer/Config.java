package com.notenoughmail.tfcgenviewer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static ForgeConfigSpec.IntValue previewSize;

    public static void register() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        register(builder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, builder.build(), "tfcgenviewer.toml");
    }

    private static void register(ForgeConfigSpec.Builder builder) {
        previewSize = builder.comment(
                "",
                " The vertical and horizontal size to visualize, in world grids (8 chunks), a restart is required to take effect",
                " Note that bigger sizes will take exponentially longer to preview",
                ""
                ).defineInRange("previewSize", 256, 16, 2048);
    }
}
