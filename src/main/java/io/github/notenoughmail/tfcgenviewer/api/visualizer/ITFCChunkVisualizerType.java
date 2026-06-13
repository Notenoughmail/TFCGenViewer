package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.NotNull;

/**
 * A visualizer type specifically narrowed to TFC's {@link TFCChunkGenerator chunk generator} at a {@link ChunkScale chunk scale}.
 * <p>
 * All registered {@code ITFCChunkVisualizerType}s will be automatically included in TFCGenViewer's default chunk-scale
 * generator visualizer
 */
public interface ITFCChunkVisualizerType<C, O extends IVisualizerType.Options<O>>
    extends IVisualizerType<TFCChunkGenerator, C, ChunkScale, O>, Comparable<ITFCChunkVisualizerType<?, ?>> {

    int sort();

    @Override
    default int compareTo(@NotNull ITFCChunkVisualizerType<?, ?> o) {
        return Integer.compare(sort(), o.sort());
    }

    interface Simple<C> extends ITFCChunkVisualizerType<C, NoneOpt> {

        @Override
        default NoneOpt createOptions(RegistryAccess registryAccess) {
            return NoneOpt.INSTANCE;
        }
    }
}
