package com.notenoughmail.tfcgenviewer.network.packets;

import com.notenoughmail.tfcgenviewer.network.TFCGVChannel;
import com.notenoughmail.tfcgenviewer.util.Permissions;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;


public enum ViewerRequestPacket {
    INSTANCE;

    // TODO: 1.21 | Use neo's html-like formatting in the lang file instead
    public static final Component VIEWING_DISALLOWED = Component.translatable("tfcgenviewer.message.viewing_disallowed").withStyle(ChatFormatting.RED);
    public static final Component NON_TFC_WORLD = Component.translatable("tfcgenviewer.message.non_tfc_world").withStyle(ChatFormatting.YELLOW);

    public void handle(@Nullable ServerPlayer player) {
        if (player != null) {
            final byte permission = Permissions.get(player);
            if ((permission & 1) != 0 && !Permissions.isEmpty(permission)) {
                if (player.level() instanceof ServerLevel sl && sl.getChunkSource().getGenerator() instanceof TFCChunkGenerator gen) {
                    TFCGVChannel.send(PacketDistributor.PLAYER.with(() -> player), new ViewerResponsePacket(permission, sl.getSeed(), gen.settings()));
                } else {
                    player.sendSystemMessage(NON_TFC_WORLD);
                }
            } else {
                player.sendSystemMessage(VIEWING_DISALLOWED);
            }
        }
    }
}
