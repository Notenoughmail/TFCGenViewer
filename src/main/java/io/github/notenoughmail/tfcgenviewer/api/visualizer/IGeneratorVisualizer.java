package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.SerializationInformation;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.registry.ISyncRegistries;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.stream.Stream;

public interface IGeneratorVisualizer<
        G extends ChunkGeneratorExtension,
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<G, ?, S, ?>
        > extends ISyncRegistries {

    StreamCodec<RegistryFriendlyByteBuf, RockSettings> ROCK_SETTINGS_CODEC = ImplAPI.ROCK_SETTINGS_CODEC;

    StreamCodec<RegistryFriendlyByteBuf, RockLayerSettings> ROCK_LAYER_SETTINGS_CODEC = ImplAPI.ROCK_LAYER_SETTINGS_CODEC;

    StreamCodec<RegistryFriendlyByteBuf, Settings> SETTINGS_CODEC = ImplAPI.SETTINGS_CODEC;

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
     * Recreate the generator, will error if simply {@code return generator;}
     */
    G recreateGenerator(G generator);

    /**
     * A codec to serialize the generator over-the-network to recreate it on the client
     */
    StreamCodec<RegistryFriendlyByteBuf, G> generatorNetworkCodec();

    // Default implementations since it's very likely for these to be needed unless the rock layer settings is completely empty
    @Override
    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {
        synchronizationRequest.request(RockSettings.KEY);
    }

    @Override
    default void elementCodecForRegistry(SerializationInformation serializationInformation) {
        serializationInformation.provide(RockSettings.KEY, ROCK_SETTINGS_CODEC);
    }

    @ApiStatus.Internal
    default void registrySync(SynchronizationRequest request) {
        additionalSynchronization(request);
        visualzierStream().forEach(v -> v.additionalSynchronization(request));
    }
}
