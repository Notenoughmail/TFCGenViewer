package com.notenoughmail.tfcgenviewer.util.custom;

import com.notenoughmail.tfcgenviewer.mixin.RockLayerSettingsAccessor;
import com.notenoughmail.tfcgenviewer.util.VisualizerType;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;

public class GeneratorPreviewException extends Exception {

    public GeneratorPreviewException(String message, Throwable baseCause) {
        super(message, baseCause);
    }

    public static String buildMessage(
            long seed,
            VisualizerType visualizer,
            int scale,
            int xCenterGrids,
            int zCenterGrids,
            RegionChunkDataGenerator generator,
            int xPos,
            int zPos
    ) {
        return """
                
                Generation failed! Relevant information:
                seed: %s
                visualizer: %s
                scale: %s
                xCenterGrids: %s (%.2f km)
                zCenterGrids: %s (%.2f km)
                rockLayerSettings: %s
                xPos: %s (%s)
                zPos: %s (%s)
                Error follows
                """.formatted(
                        seed,
                        visualizer.name(),
                        scale,
                        xCenterGrids,
                        (float) xCenterGrids * 128 / 1000,
                        zCenterGrids,
                        (float) zCenterGrids * 128 / 1000,
                        ((RockLayerSettingsAccessor) (Object) generator.rockLayerSettings()).tfcgenviewer$GetData(),
                        xPos,
                        xPos * 128,
                        zPos,
                        zPos * 128
                );
    }
}
