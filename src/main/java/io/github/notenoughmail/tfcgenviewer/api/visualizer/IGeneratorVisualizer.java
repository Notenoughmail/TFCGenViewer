package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Stream;

public interface IGeneratorVisualizer<
        G extends ChunkGeneratorExtension,
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<G, ?, S, ?>
        > {

    /**
     * The id of the generator visualizer
     */
    ResourceLocation id();

    /**
     * All {@link IVisualizerType}s this generator visualizer possesses
     */
    Stream<V> visualzierStream();

    /**
     * All {@link IVisualizerType}s this generator visualizer possesses
     */
    default List<V> allVisualizers() {
        return visualzierStream().toList();
    };

    /**
     * The {@link IScale scale} of the generator visualizer
     */
    S scale();

    /**
     * The maximum offset, in pixels, the preview can have
     */
    int maximumPreviewOffset();

    /**
     * The formatted name of the generator visualizer
     */
    Component name();

    /**
     * The {@link ChunkGeneratorExtension} class this generator visualizer is for
     */
    Class<? extends G> generatorType();

    /**
     * If the generator and visualizer support rock editing
     */
    boolean supportsRockEditing();

    /**
     * Synchronize server-only registry information so that is available to all {@link IVisualizerType}s handled by this generator visualizer
     */
    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {}

    /**
     * Recreate the generator, will error if simply {@code return generator;}
     */
    G recreateGenerator(G generator);

    /**
     * A codec to serialize the generator over-the-network to recreate it on the client
     */
    StreamCodec<RegistryFriendlyByteBuf, G> generatorNetworkCodec();
}
