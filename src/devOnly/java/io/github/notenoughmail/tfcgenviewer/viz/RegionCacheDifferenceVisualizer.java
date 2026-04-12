package io.github.notenoughmail.tfcgenviewer.viz;

import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TableBasedRegionCache;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class RegionCacheDifferenceVisualizer implements IRegionVisualizerType<RegionCacheDifferenceVisualizer.Cache, IVisualizerType.NoneOpt> {

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public NoneOpt createOptions(RegistryAccess registryAccess) {
        return NoneOpt.INSTANCE;
    }

    @Override
    public Cache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options, DrawParallelism parallelism) {
        return new Cache(generator.settings(), worldSeed);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, Cache, GridScale, NoneOpt> info) {
        final Region tfc = info.cache().getTFC(xPos, zPos), alt = info.cache().getAlternative(xPos, zPos);
        final double diff = tfc.noise() - alt.noise();
        if (diff == 0.0) {
            image.setPixel(
                    imageX,
                    imageY,
                    TFCGenViewerRegistration.GRAD_GRAYSCALE.get()
                            .applyAsAbgr((tfc.noise() + 1) * 0.5)
            );
        } else {
            image.setPixel(
                    imageX,
                    imageY,
                    TFCGenViewerRegistration.GRAD_GREEN.get()
                            .applyAsAbgr((diff + 2) * 0.25)
            );
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, Cache cache) {
        return Component.empty();
    }

    @Override
    public Component name() {
        return Component.literal("Region Cache Difference");
    }

    @Override
    public Component description() {
        return Component.empty();
    }

    public static class Cache {

        private final RegionGenerator tfc, alternative;

        public Cache(Settings settings, long seed) {
            tfc = new RegionGenerator(settings, Seed.of(seed));
            alternative = TableBasedRegionCache.regionGeneratorWithThisCache(settings, Seed.of(seed), false);
        }

        public Region getTFC(int x, int z) {
            return tfc.getOrCreateRegion(x, z);
        }

        public Region getAlternative(int x, int z) {
            return alternative.getOrCreateRegion(x, z);
        }
    }
}
