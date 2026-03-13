package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.cache.RockCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor.TFCChunkGeneratorAccessor;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RockVisualizer;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import static io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RockVisualizer.*;

public class ChunkRockVisualizer implements ITFCChunkVisualizerType<RockCache<ChunkRockVisualizer.Cache>, RockVisualizer.Options> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_ROCK);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_ROCK);

    @Override
    public int sort() {
        return 50;
    }

    @Override
    public RockVisualizer.Options createOptions(RegistryAccess registryAccess) {
        return new RockVisualizer.Options();
    }

    @Override
    public void addOptions(OptionProvider optionProvider, RockVisualizer.Options options) {
        optionProvider.orderBool("tfcgenviewer.option.region_visualizer.rock.mode", options.surface, b -> options.surface = b)
                .withDisplay(optionProvider.genericDisplay(b -> b ? MODE_SURFACE : MODE_ELEVATION))
                .withConstantTooltip(MODE_EXP)
                .finish();
        optionProvider.orderInt("tfcgenviewer.option.region_visualizer.rock.elevation", options.elevation, -64, 320, i -> options.elevation = i)
                .withConstantTooltip(ELEVATION_EXP)
                .finish(() -> !options.surface);
    }

    @Override
    public RockCache<Cache> createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, RockVisualizer.Options options) {
        final Seed seed = Seed.of(worldSeed);
        ((TFCChunkGeneratorAccessor) generator).tfcgenviewer$SetSeed(seed);
        return new RockCache<>(new Cache(ChunkDataProvider.tfcRegion(seed, generator), options.surface ? null : new ChunkElevationVisualizer.ElevationCache(generator, seed)));
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RockCache<Cache>, ChunkScale, RockVisualizer.Options> info) {
        final ChunkData data = info.cache().innerCache.chunkDataProvider().create(xPos, zPos);
        final Block raw;
        if (info.options().surface) {
            raw = info.evaluateAtBlockPosition(
                    true,
                    xPos,
                    zPos,
                    data.getRockData()::getSurfaceRock
            ).raw();
        } else {
            final ChunkElevationVisualizer.ElevationCache elevationCache = info.cache().innerCache.elevationCache();
            assert elevationCache != null;
            elevationCache.primePos(xPos, zPos);
            raw = info.cache()
                    .innerCache
                    .chunkDataProvider()
                    .generator()
                    .generateRock(
                            info.pixelResolutionToBlock(xPos, true),
                            info.options().elevation,
                            info.pixelResolutionToBlock(zPos, true),
                            info.evaluateAtBlockPosition(true, xPos, zPos, elevationCache::sample),
                            null
                    )
                    .raw();
        }
        final ColorDefinition color = info.cache().getColor(raw);
        info.addTooltip(color);
        image.setPixel(imageX, imageY, color);
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RockCache<Cache> cache) {
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
    public Component additionalPreviewInfo(DrawInfo<TFCChunkGenerator, RockCache<Cache>, ChunkScale, RockVisualizer.Options> info) {
        if (info.options().surface) {
            return null;
        } else {
            return Component.translatable("tfcgenviewer.preview_info.generated_rock_chunk", info.options().elevation);
        }
    }

    @Override
    public boolean shouldDrawInParallel(RockVisualizer.Options options, ImageSize size) {
        return !options.surface;
    }

    public record Cache(ChunkDataProvider.Region chunkDataProvider, @Nullable ChunkElevationVisualizer.ElevationCache elevationCache) {}
}
