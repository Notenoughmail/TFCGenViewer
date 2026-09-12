package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import net.dries007.tfc.world.TFCChunkGenerator;

/**
 * A {@link IGeneratorVisualizer} narrowed to only handle TFC's default chunk generator
 */
public interface ITFCGeneratorVisualizer<
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<TFCChunkGenerator, ?, S, ?>
        > extends IGeneratorVisualizer<TFCChunkGenerator, I, S, V> {}
