package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public interface IUncachedVisualizer<G extends ChunkGeneratorExtension> extends IVisualizer<G, IUncachedVisualizer.NoopCache> {

    @Override
    default NoopCache createCache(RegistryAccess registryAccess, G generator, IScale scale, long seed) {
        return NoopCache.INSTANCE;
    }

    @Override
    default void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, G generator, RegistryAccess registryAccess, Int2ObjectOpenHashMap<Component> colorDescriptors, NoopCache cache, IScale scale) {
        draw(imageX, imageY, image, xPos, zPos, generator, registryAccess, colorDescriptors, scale);
    }

    void draw(
            int imageX,
            int imageY,
            MutableImage image,
            int xPos,
            int zPos,
            G generator,
            RegistryAccess registryAccess,
            Int2ObjectOpenHashMap<Component> colorDescriptors,
            IScale scale
    );

    enum NoopCache { INSTANCE }
}
