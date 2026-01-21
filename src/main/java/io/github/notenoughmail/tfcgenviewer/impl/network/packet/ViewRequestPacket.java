package io.github.notenoughmail.tfcgenviewer.impl.network.packet;

import com.google.common.collect.Sets;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import io.github.notenoughmail.tfcgenviewer.impl.network.RegistrySync;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ViewRequestPacket(Set<ResourceLocation> clientGeneratorVisualizers) implements CustomPacketPayload {

    public static final Component FAIL = Component.translatable("tfcgenviewer.network.view_request.response.fail").withStyle(ChatFormatting.YELLOW);
    public static final Component EMPTY = Component.translatable("tfcgenviewer.network.view_request.response.empty").withStyle(ChatFormatting.DARK_AQUA);

    public static final Type<ViewRequestPacket> TYPE = new Type<>(TFCGenViewer.id("view_request"));

    public static final StreamCodec<ByteBuf, ViewRequestPacket> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.<ByteBuf, ResourceLocation, Set<ResourceLocation>>collection(HashSet::new))
                    .map(ViewRequestPacket::new, ViewRequestPacket::clientGeneratorVisualizers);

    @Override
    public Type<ViewRequestPacket> type() {
        return TYPE;
    }

    public void handleOnServerMainThread(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            if (player.serverLevel().getChunkSource().getGenerator() instanceof ChunkGeneratorExtension ext) {
                final Set<ResourceLocation> serverGenerators = ImplAPI.getVisualizersFor(ext)
                        .stream()
                        .filter(gv -> gv.allowedVisualizers(player).findAny().isPresent())
                        .map(IGeneratorVisualizer::id)
                        .collect(Collectors.toSet());
                final Set<ResourceLocation> ids = Sets.intersection(clientGeneratorVisualizers, serverGenerators);
                switch (ids.size()) {
                    case 0 -> player.sendSystemMessage(EMPTY);
                    case 1 -> {
                        final IGeneratorVisualizer<?, ?, ?, ?> generatorVisualizer = ImplAPI.GEN_IDS.get(ids.iterator().next());
                        final List<IVisualizerType<?, ?, ?, ?>> visualizerTypes = generatorVisualizer.allowedVisualizers(player)
                                .<IVisualizerType<?, ?, ?, ?>>map(IVisualizerType.class::cast)
                                .toList();
                        if (visualizerTypes.isEmpty()) {
                            player.sendSystemMessage(EMPTY);
                        } else {
                            final RegistrySync sync = new RegistrySync(player.serverLevel().registryAccess());
                            generatorVisualizer.additionalSynchronization(sync);
                            visualizerTypes.forEach(v -> v.additionalSynchronization(sync));
                            final SingleViewResponsePacket response = new SingleViewResponsePacket(
                                    generatorVisualizer,
                                    ext,
                                    visualizerTypes,
                                    sync.contents(),
                                    player.serverLevel().registryAccess(), // Needed for the packet contract, not actually used
                                    // TODO: 1.21.1 | These need permission handlers
                                    true,
                                    true,
                                    true,
                                    player.serverLevel().getSeed(),
                                    player.getBlockX(),
                                    player.getBlockZ()
                            );
                            PacketDistributor.sendToPlayer(player, response);
                        }
                    }
                    case 2 -> {
                    }
                }
            } else {
                player.sendSystemMessage(FAIL);
            }
        }
    }
}
