package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface IGeneratorVisualizer<G extends ChunkGeneratorExtension, S extends IScale<?>, V extends IVisualizer<G, ?>> {

    List<? extends V> allVisualizers();

    S scaleGroup();

    List<? extends V> allowedVisualizers(ServerPlayer player);

    Component name();

    Class<? extends G> generatorType();

    boolean supportsRockEditing();

    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {}

    G recreateGenerator(G generator);

    StreamCodec<RegistryFriendlyByteBuf, G> generatorCodec();

    StreamCodec<RegistryFriendlyByteBuf, List<V>> visualizerCodec();
}
