package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.core.RegistryAccess;

public interface RegionVisualizer extends ITFCVisualizer<RegionPointCache> {

    @Override
    default RegionPointCache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize scale, long worldSeed) {
        return RegionPointCache.of(generator, scale, worldSeed);
    }
}
