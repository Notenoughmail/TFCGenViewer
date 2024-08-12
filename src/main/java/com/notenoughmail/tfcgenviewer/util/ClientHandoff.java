package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket;
import com.notenoughmail.tfcgenviewer.screen.ViewWorldScreen;
import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.world.region.Units;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientHandoff {

    public static void viewWorld(ViewerResponsePacket info) {
        final Player player = ClientHelpers.getPlayer();
        if (player != null) {
            final byte perms = info.fullPermissions();
            Minecraft.getInstance().setScreen(new ViewWorldScreen(
                    VisualizerType.getVisualizers(perms),
                    info.seed(),
                    info.levelSettings(),
                    Permissions.canExport(perms),
                    Permissions.canSeeSeed(perms),
                    Permissions.canSeeCoordinates(perms),
                    Units.blockToGrid(player.getBlockX()),
                    Units.blockToGrid(player.getBlockZ())
            ));
        }
    }
}
