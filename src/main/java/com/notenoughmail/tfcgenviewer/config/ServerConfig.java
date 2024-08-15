package com.notenoughmail.tfcgenviewer.config;

import com.notenoughmail.tfcgenviewer.util.Permissions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.EnumMap;

public class ServerConfig {

    public static ForgeConfigSpec.EnumValue<Permissions.Universal> viewPermission;
    public static EnumMap<Permissions.Category, ForgeConfigSpec.EnumValue<Permissions.Type>> permissionsByType;

    public static void register() {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        register(builder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build(), "tfcgenviewer-server.toml");
    }

    private static void register(ForgeConfigSpec.Builder builder) {
        viewPermission = builder
                .comment("")
                .comment("""
                         The permission type a player requires to open a visualizer of the current world
                           SEED_COMMAND | If the player has /seed permissions (level 2), they can access all information in the visualizer screen (default)
                           BY_CATEGORY  | Which information the player is able to access in the visualizer screen is dependent on the configuration options in the 'categories' section
                           NEVER        | Disallows all players from visualizing the world
                           ALWAYS       | Allows all players to access all information in the visualizer screen
                           ALLOW_LIST   | Allows players within the server's allow list to view the world, the information a player is dependent on the configuration options in the 'categories' section
                           DENY_LIST    | Allows players outside of the server's deny list to view the world, the information they can view is dependent on the configuration options in the 'categories' section
                        """)
                .defineEnum("viewPermission", Permissions.Universal.SEED_COMMAND);

        builder.comment("""
                 The categories of information that can be previewed, they all have the same options with the same meaning
                   SEED_COMMAND | If the player has /seed permissions (level 2), they can access this information
                   ALLOW_LIST   | If the player is part of this filter, they may view this information
                   DENY_LIST    | If the player is not part of this filter, they may view this information
                   NEVER        | No player may view this information
                   ALWAYS       | Players can always view this information
                 These are only relevant if viewPermission is set to BY_CATEGORY
                """).push("categories");

        permissionsByType = new EnumMap<>(Permissions.Category.class);

        for (Permissions.Category category : Permissions.Category.VALUES) {
            if (category != Permissions.Category.universal) {
                permissionsByType.put(category, builder
                        .comment("")
                        .comment(category.configInfo.apply(" The permission type the player requires to view %s info in the visualizer"))
                        .defineEnum("%sPermission".formatted(category.name()), Permissions.Type.SEED_COMMAND)
                );
            }
        }
    }
}
