package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public interface IVisualizer<G extends ChunkGeneratorExtension, C> {

    default void additionalSynchronization(SynchronizationRequest synchronizationRequest) {}

    C createCache(RegistryAccess registryAccess, G generator, IScale scale, long worldSeed);

    void draw(
            int imageX,
            int imageY,
            MutableImage image,
            int xPos,
            int zPos,
            G generator,
            RegistryAccess registryAccess,
            Int2ObjectOpenHashMap<Component> colorDescriptors,
            C cache,
            IScale scale
    );

    default void afterComplete(MutableImage image, G generator, RegistryAccess registryAccess, C cache, Int2ObjectOpenHashMap<Component> colorDescriptors) {}

    Component colorKey(RegistryAccess registryAccess, C cache);

    Component name();
}
