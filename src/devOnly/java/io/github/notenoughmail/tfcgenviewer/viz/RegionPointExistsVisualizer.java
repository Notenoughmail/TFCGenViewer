package io.github.notenoughmail.tfcgenviewer.viz;

import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import io.github.notenoughmail.tfcgenviewer.impl.NoiseBasedRegionCache;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RegionVisualizerType;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.RegionGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class RegionPointExistsVisualizer implements RegionVisualizerType<RegionPointExistsVisualizer.Option> {

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public RegionPointCache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, Option options, DrawParallelism parallelism) {
        final Seed seed = Seed.of(worldSeed);
        return new RegionPointCache(
                options.tfc ?
                        new RegionGenerator(generator.settings(), seed) :
                        NoiseBasedRegionCache.regionGeneratorWithThisCache(generator.settings(), seed, parallelism.parallel()),
                size.sizeInPixels(),
                0
        );
    }

    @Override
    public Option createOptions(RegistryAccess registryAccess) {
        return new Option();
    }

    @Override
    public void addOptions(OptionProvider optionProvider, Option options) {
        optionProvider.orderBool("Use TFC Region Cache", options.tfc, b -> options.tfc = b)
                .finish();
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, GridScale, Option> info) {
        final var region = info.cache().getRegionPoint(imageX, imageY, xPos, zPos);
        if (region != null) {
            if (region.region().isIn(xPos, zPos)) {
                image.setPixel(
                        imageX,
                        imageY,
                        TFCGenViewerRegistration.GRAD_BLUE.get()
                                .applyAsAbgr((region.region().noise() + 1) * 0.5)
                );
            } else {
                image.setPixel(
                        imageX,
                        imageY,
                        0xFF000000
                );
            }
        } else {
            image.setPixel(imageX, imageY, 0xFFFFFFFF);
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return Component.empty();
    }

    @Override
    public Component name() {
        return Component.literal("Point Exists");
    }

    @Override
    public Component description() {
        return Component.empty();
    }

    public static class Option implements Options<Option> {

        public boolean tfc = false;

        @Override
        public Option copy() {
            final Option o = new Option();
            o.tfc = tfc;
            return o;
        }
    }
}
