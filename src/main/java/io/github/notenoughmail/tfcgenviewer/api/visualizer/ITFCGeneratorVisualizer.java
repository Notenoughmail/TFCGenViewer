package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.SerializationInformation;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.settings.RockSettings;

public interface ITFCGeneratorVisualizer<
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<TFCChunkGenerator, ?, S, ?>
        > extends IGeneratorVisualizer<TFCChunkGenerator, I, S, V> {

    @Override
    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {
        synchronizationRequest.request(RockSettings.KEY);
    }

    @Override
    default void elementCodecForRegistry(SerializationInformation serializationInformation) {
        serializationInformation.provide(RockSettings.KEY, RockSettings.CODEC);
    }
}
