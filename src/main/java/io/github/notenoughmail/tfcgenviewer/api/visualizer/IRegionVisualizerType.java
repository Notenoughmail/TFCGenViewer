package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import net.dries007.tfc.world.TFCChunkGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * A visualizer type specifically narrowed to TFC's {@link TFCChunkGenerator chunk generator} at a {@link net.dries007.tfc.world.region.Units#GRID_WIDTH_IN_BLOCK grid scale}.
 * <p>
 * All registered {@code IRegionVisualizerType}s will be automatically included in TFCGenViewer's default grid-scale generator visualizer
 */
public interface IRegionVisualizerType<C, O extends IVisualizerType.Options<O>>
        extends IVisualizerType<TFCChunkGenerator, C, GridScale, O>, Comparable<IRegionVisualizerType<?, ?>> {

    int sort();

    @Override
    default int compareTo(@NotNull IRegionVisualizerType<?, ?> o) {
        return Integer.compare(sort(), o.sort());
    }
}
