package io.github.notenoughmail.tfcgenviewer.viz;

import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import io.github.notenoughmail.tfcgenviewer.impl.TableBasedRegionCache;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class RegionNeighborPointCollisionVisualizer implements IRegionVisualizerType<RegionNeighborPointCollisionVisualizer.Cache, RegionPointExistsVisualizer.Option> {

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public RegionPointExistsVisualizer.Option createOptions(RegistryAccess registryAccess) {
        return new RegionPointExistsVisualizer.Option();
    }

    @Override
    public void addOptions(OptionProvider optionProvider, RegionPointExistsVisualizer.Option options) {
        optionProvider.orderBool("Use TFC Region Cache", options.tfc, b -> options.tfc = b)
                .finish();
    }

    @Override
    public Cache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, RegionPointExistsVisualizer.Option options, DrawParallelism parallelism) {
        return new Cache(generator.settings(), worldSeed, options.tfc);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, Cache, GridScale, RegionPointExistsVisualizer.Option> info) {
        final int color = info.cache().color(xPos, zPos);
        image.setPixel(imageX, imageY, color);
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, Cache cache) {
        return Component.empty();
    }

    @Override
    public Component name() {
        return Component.literal("Neighboring Region Cache Collision");
    }

    @Override
    public Component description() {
        return Component.empty();
    }

    public static class Cache {

        private final RegionGenerator generator;

        public Cache(Settings settings, long seed, boolean tfc) {
            final Seed s = Seed.of(seed);
            generator = tfc ?
                    new RegionGenerator(settings, s) :
                    TableBasedRegionCache.regionGeneratorWithThisCache(settings, s, false);
        }

        public int color(int x, int z) {
            final Region region = generator.getOrCreateRegion(x, z);
            if (region.isIn(x, z)) {
                for (int dx = -1 ; dx < 2 ; dx++) {
                    for (int dz = -1 ; dz < 2 ; dz++) {
                        if (dx != 0 && dz != 0) {
                            generator.getOrCreateRegion(x + dx, z + dz);
                            final Region r = generator.getOrCreateRegion(x, z);
                            if (r != region) {
                                return TFCGenViewerRegistration.GRAD_GRAYSCALE.get()
                                        .applyAsAbgr((region.noise() + 1) * 0.5);
                            }
                        }
                    }
                }
                return TFCGenViewerRegistration.GRAD_BLUE.get()
                        .applyAsAbgr((region.noise() + 1) * 0.5);
            } else {
                return 0xFF000000;
            }
        }
    }
}
