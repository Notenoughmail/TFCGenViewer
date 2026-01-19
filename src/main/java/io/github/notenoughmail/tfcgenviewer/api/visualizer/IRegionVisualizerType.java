package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * A visualizer type specifically narrowed to TFC's {@link TFCChunkGenerator chunk generator} at {@link net.dries007.tfc.world.region.Units#GRID_WIDTH_IN_BLOCK grid scale}
 */
public interface IRegionVisualizerType<C, O extends IVisualizerType.Options<O>> extends IVisualizerType<TFCChunkGenerator, C, TFCRegionVisualizer.Scale, O> {

    StreamCodec<RegistryFriendlyByteBuf, List<IRegionVisualizerType<?, ?>>> NETWORK_CODEC = ByteBufCodecs.registry(GenViewerAPI.TFC_REGION_VISUALIZER).apply(ByteBufCodecs.list());

    Codec<IRegionVisualizerType<?, ?>> CODEC = GenViewerAPI.TFC_REGION_VISUALIZER_REGISTRY.byNameCodec();
}
