package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.cache.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface RegionVisualizerType<O extends IVisualizerType.Options<O>> extends IRegionVisualizerType<RegionPointCache, O> {

    @Override
    default RegionPointCache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, O options) {
        return RegionPointCache.of(generator, size, worldSeed);
    }

    @Nullable
    @Override
    default Component additionalPreviewInfo(DrawInfo<TFCChunkGenerator, RegionPointCache, GridScale, O> info) {
        return Component.translatable("tfcgenviewer.preview_info.generated_regions", info.cache().visitedRegions());
    }

    interface Simple extends RegionVisualizerType<NoneOpt> {

        @Override
        default NoneOpt createOptions(RegistryAccess registryAccess) {
            return NoneOpt.INSTANCE;
        }
    }
}
