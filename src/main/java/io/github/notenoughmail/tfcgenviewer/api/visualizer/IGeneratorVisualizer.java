package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.stream.Stream;

public interface IGeneratorVisualizer<
        G extends ChunkGeneratorExtension,
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<G, ?, S, ?>
        > {

    /**
     * The id of the generatorVisualizer visualizer
     */
    ResourceLocation id();

    /**
     * All {@link IVisualizerType}s this generatorVisualizer visualizer possesses
     */
    Stream<V> visualzierStream();

    /**
     * All {@link IVisualizerType}s this generatorVisualizer visualizer possesses
     */
    default List<V> allVisualizers() {
        return visualzierStream().toList();
    };

    /**
     * The {@link IScale scale} of the generatorVisualizer visualizer
     */
    S scale();

    /**
     * The maximum offset, in pixels, the preview can have
     */
    int maximumPreviewOffset();

    /**
     * Which visualizers the player may view the world with
     */
    default Stream<V> allowedVisualizers(ServerPlayer player) {
        return visualzierStream().filter(v -> v.isPermitted(player));
    }

    /**
     * The formatted name of the generatorVisualizer visualizer
     */
    Component name();

    /**
     * The {@link ChunkGeneratorExtension} class this generatorVisualizer visualizer is for
     */
    Class<? extends G> generatorType();

    /**
     * If the generatorVisualizer and visualizer support rock editing
     */
    boolean supportsRockEditing();

    /**
     * Synchronize server-only registry information so that is available to all {@link IVisualizerType}s handled by this generatorVisualizer visualizer
     */
    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {}

    /**
     * Recreate the generatorVisualizer, will error if simply {@code return generatorVisualizer;}
     */
    G recreateGenerator(G generator);

    /**
     * A codec to serialize the generatorVisualizer over-the-network to recreate it on the client
     */
    StreamCodec<RegistryFriendlyByteBuf, G> generatorNetworkCodec();
}
