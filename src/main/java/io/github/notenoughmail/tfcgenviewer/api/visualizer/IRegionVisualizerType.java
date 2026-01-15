package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface IRegionVisualizerType<C, O extends IVisualizerType.Options<O>> extends IVisualizerType<TFCChunkGenerator, C, TFCRegionVisualizer.Scale, O> {

    StreamCodec<RegistryFriendlyByteBuf, List<IRegionVisualizerType<?, ?>>> NETWORK_CODEC = ByteBufCodecs.registry(GenViewerAPI.TFC_REGION_VISUALIZER).apply(ByteBufCodecs.list());

    Codec<IRegionVisualizerType<?, ?>> CODEC = GenViewerAPI.TFC_REGION_VISUALIZER_REGISTRY.byNameCodec();
}
