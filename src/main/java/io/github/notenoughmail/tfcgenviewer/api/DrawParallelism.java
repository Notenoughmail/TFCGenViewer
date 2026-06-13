package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import org.jetbrains.annotations.ApiStatus;

/**
 * The parallel-processing information of a draw operation.
 * @param parallelism The maximum number of concurrent draw operations that will occur.
 *                    Will be negative if operations are purely serial
 * @param parallel If draw operations will actually be parallel. This value may not match
 *                 the value returned in {@link IVisualizerType#requestDrawInParallel(IVisualizerType.Options, ImageSize) #shouldDrawInParallel}
 */
public record DrawParallelism(
        int parallelism,
        boolean parallel
) {

    @ApiStatus.Internal
    public static <O extends IVisualizerType.Options<O>> DrawParallelism of(O options, IVisualizerType<?, ?, ?, O> viz, ImageSize size) {
        if (TFCGenViewer.disableParallelGeneration.getAsBoolean()) return NONE;
        if (!viz.requestDrawInParallel(options, size)) return NONE;
        final int parallelism = Math.min(
                TFCGenViewer.maximumNumberOfParallelDrawOperations.getAsInt(),
                viz.maxLevelOfParallelism(options, size)
        );
        if (parallelism <= 1) return NONE;
        return new DrawParallelism(parallelism, true);
    }

    private static final DrawParallelism NONE = new DrawParallelism(-1, false);
}
