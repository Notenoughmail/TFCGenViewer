package com.notenoughmail.tfcgenviewer.network.packets;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.network.TFCGVChannel;
import com.notenoughmail.tfcgenviewer.util.Permissions;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public enum ViewerRequestPacket {
    INSTANCE;

    public static final Component VIEWING_DISALLOWED = Component.translatable("tfcgenviewer.message.viewing_disallowed").withStyle(ChatFormatting.RED);
    public static final Component NON_TFC_WORLD = Component.translatable("tfcgenviewer.message.non_tfc_world").withStyle(ChatFormatting.YELLOW);

    public void handle(@Nullable ServerPlayer player) {
        if (player != null) {
            final byte permission = Permissions.get(player);
            if (!Permissions.isEmpty(permission)) {
                if (player.level() instanceof ServerLevel sl && sl.getChunkSource().getGenerator() instanceof TFCChunkGenerator gen) {
                    TFCGVChannel.send(PacketDistributor.PLAYER.with(() -> player), new ViewerResponsePacket(permission, sl.getSeed(), gen.settings(), getFeatures(sl.registryAccess())));
                } else {
                    player.sendSystemMessage(NON_TFC_WORLD);
                }
            } else {
                player.sendSystemMessage(VIEWING_DISALLOWED);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceKey<PlacedFeature>, PlacedFeature> getFeatures(RegistryAccess registryAccess) {
        return Map.ofEntries(
                registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
                        .get(TFCGenViewer.VISUALIZABLE_FEATURES)
                        .stream()
                        .flatMap(HolderSet.Named::stream)
                        .map(holder -> Map.entry(holder.unwrapKey().orElseThrow(), holder.value()))
                        .toArray(Map.Entry[]::new)
        );
    }
}
