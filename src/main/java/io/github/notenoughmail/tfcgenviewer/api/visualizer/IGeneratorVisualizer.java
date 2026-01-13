package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IGeneratorVisualizer<G extends ChunkGeneratorExtension, I extends ImageSize, S extends IScale<I>, V extends IVisualizerType<G, ?, S, ?>> {

    /**
     * All visualizers this visualizer is capable of providing, drawing, and handling
     */
    List<V> allVisualizers();

    /**
     * The scale of the visualizer
     */
    S scale();

    /**
     * The maximum offset, in pixels, the preview can have
     */
    int maximumPreviewOffset();

    /**
     * Which visualizers the player may view the world with
     */
    List<? extends V> allowedVisualizers(ServerPlayer player);

    /**
     * The name of the visualizer
     */
    Component name();

    /**
     * The chunk generator class the visualizer can handle
     */
    Class<? extends G> generatorType();

    /**
     * If the generator and visualizer support rock editing
     */
    boolean supportsRockEditing();

    /**
     * Synchronize server-only registry information so that is available in the registry access when drawing
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

    /**
     * A codec to serialize the allowed visualizers over-the-network so they may be known on the client
     */
    StreamCodec<RegistryFriendlyByteBuf, List<V>> visualizerNetworkCodec();

    Codec<V> visualizerCodec();
}
