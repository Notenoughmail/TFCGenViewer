package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.cache.RockCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class RockVisualizer implements IRegionVisualizerType<RockCache<RegionPointCache>, RockVisualizer.Options> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_ROCK);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_ROCK);
    public static final Component MODE_EXP = Component.translatable("tfcgenviewer.option.region_visualizer.rock.mode.tooltip");
    public static final Component MODE_SURFACE = Component.translatable("tfcgenviewer.option.region_visualizer.rock.mode.surface");
    public static final Component MODE_ELEVATION = Component.translatable("tfcgenviewer.option.region_visualizer.rock.mode.elevation");
    public static final Component ELEVATION_EXP = Component.translatable("tfcgenviewer.option.region_visualizer.rock.elevation.tooltip");

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RockCache<RegionPointCache>, GridScale, Options> info) {
        final Region.Point point = info.cache().innerCache.getPoint(imageX, imageY, xPos, zPos);
        final Block raw;
        if (info.options().surface) {
            raw = info.rockLayerSettings()
                    .sampleAtLayer(point.rock, 0)
                    .raw();
        } else {
            final int surfaceElevation = point.land() ?
                    point.mountain() || point.coastalMountain() ?
                            100 :
                            75 :
                    60;
            raw = info.cache()
                    .innerCache
                    .getGenerator()
                    .chunkDataGenerator()
                    .generateRock(
                            info.pixelResolutionToBlock(xPos, true),
                            info.options().elevation,
                            info.pixelResolutionToBlock(zPos, true),
                            surfaceElevation,
                            null
                    )
                    .raw();
        }
        final ColorDefinition color = info.cache().getColor(raw);
        info.addTooltip(color);
        image.setPixel(imageX, imageY, color);
    }

    @Override
    public Options createOptions(RegistryAccess registryAccess) {
        return new Options();
    }

    @Override
    public void addOptions(OptionProvider optionProvider, Options options) {
        optionProvider.orderBool("tfcgenviewer.option.region_visualizer.rock.mode", options.surface, b -> options.surface = b)
                .withDisplay(optionProvider.genericDisplay(b -> b ? MODE_SURFACE : MODE_ELEVATION))
                .withConstantTooltip(MODE_EXP)
                .finish();
        optionProvider.orderInt("tfcgenviewer.option.region_visualizer.rock.elevation", options.elevation, -64, 320, i -> options.elevation = i)
                .withConstantTooltip(ELEVATION_EXP)
                .finish(() -> !options.surface);
    }

    @Override
    public RockCache<RegionPointCache> createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, Options options, DrawParallelism parallelism) {
        return new RockCache<>(RegionPointCache.of(generator, size, worldSeed));
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RockCache<RegionPointCache> cache) {
        return cache.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Override
    public Component description() {
        return DESC;
    }

    @Nullable
    @Override
    public Component additionalPreviewInfo(DrawInfo<TFCChunkGenerator, RockCache<RegionPointCache>, GridScale, Options> info) {
        return info.options().surface ?
                Component.translatable("tfcgenviewer.preview_info.generated_regions", info.cache().innerCache.visitedRegions()) :
                Component.translatable(
                        "tfcgenviewer.preview_info.generated_rock",
                        info.cache().innerCache.visitedRegions(),
                        info.options().elevation
                );
    }

    @Override
    public int sort() {
        return 50;
    }

    public static final class Options implements IVisualizerType.Options<Options> {

        public boolean surface = true;
        public int elevation = 75; // Random guess for 'surface' y-level

        @Override
        public Options copy() {
            final Options ret = new Options();
            ret.surface = surface;
            ret.elevation = elevation;
            return ret;
        }
    }
}
