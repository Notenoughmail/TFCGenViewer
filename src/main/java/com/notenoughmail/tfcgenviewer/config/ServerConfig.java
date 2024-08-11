package com.notenoughmail.tfcgenviewer.config;

import com.notenoughmail.tfcgenviewer.util.Permissions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig {

    public static ForgeConfigSpec.EnumValue<Permissions.Universal> viewPermission;
    public static ForgeConfigSpec.EnumValue<Permissions.Category>
            seedPermission,
            coordinatePermission,
            climatePermission,
            rockPermission,
            biomePermission,
            riverPermission;

    public static void register() {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        register(builder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build(), "tfcgenviewer-server.toml");
    }

    private static void register(ForgeConfigSpec.Builder builder) {
        viewPermission = builder
                .comment("""
                         The permission type a player requires to open a visualizer of the current world
                           SEED_COMMAND | If the player has /seed permissions (level 2) they can access all information in the visualizer screen (default)
                           BY_CATEGORY  | Which information the player is able to access in the visualizer screen is dependent on the configuration options in the 'categories' section
                           DISALLOWED   | Disallows all players from visualizing the world
                           ALLOWED      | Allows all players to access all information in the visualizer screen
                        """)
                .defineEnum("viewPermission", Permissions.Universal.SEED_COMMAND);

        builder.push("categories");
    }
}
