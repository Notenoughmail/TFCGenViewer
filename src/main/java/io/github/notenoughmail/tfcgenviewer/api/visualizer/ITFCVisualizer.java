package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface ITFCVisualizer<C> extends IVisualizer<TFCChunkGenerator, C> {

    StreamCodec<RegistryFriendlyByteBuf, List<ITFCVisualizer<?>>> CODEC = ByteBufCodecs.registry(GenViewerAPI.TFC_VISUALIZERS).apply(ByteBufCodecs.list());

    boolean isPermitted(ServerPlayer player);
}
