package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import net.dries007.tfc.world.TFCChunkGenerator;

/**
 * A visualizer type specifically narrowed to TFC's {@link TFCChunkGenerator chunk generatorVisualizer} at {@link net.dries007.tfc.world.region.Units#GRID_WIDTH_IN_BLOCK grid scale}.
 * <p>
 * All {@code IRegionVisualiserType}s will be automatically included in TFCGenViewer's default grid-scale generatorVisualizer visualizer
 */
public interface IRegionVisualizerType<C, O extends IVisualizerType.Options<O>>
        extends IVisualizerType<TFCChunkGenerator, C, GridScale, O> {

    int sort();
}
