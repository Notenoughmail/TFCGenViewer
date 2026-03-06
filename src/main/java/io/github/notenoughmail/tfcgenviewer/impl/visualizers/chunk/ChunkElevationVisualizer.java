package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor.TFCChunkGeneratorAccessor;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RockTypeVisualizer;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

// TODO: 2.1.0 | Unique gradients
public class ChunkElevationVisualizer implements ITFCChunkVisualizerType<ChunkDataProvider.Region, IVisualizerType.NoneOpt> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_ELEVATION);
    public static final Component DESCRIPTION = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_ELEVATION);

    @Override
    public int sort() {
        return 10;
    }

    @Override
    public NoneOpt createOptions(RegistryAccess registryAccess) {
        return NoneOpt.INSTANCE;
    }

    @Override
    public ChunkDataProvider.Region createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options) {
        final Seed seed = Seed.of(worldSeed);
        final ChunkDataProvider.Region region = ChunkDataProvider.tfcRegion(seed, generator);
        ((TFCChunkGeneratorAccessor) generator).tfcgenviewer$SetSeed(seed);
        ((TFCChunkGeneratorAccessor) generator).tfcgenviewer$SetTideHeightNoise(BiomeNoise.shoreTideLevelNoise(seed));
        return region;
    }

    // TODO: 2.1.0 | The elevation calculation is slooooow (200000-300000ns per chunk), is there any way to make it less so?
    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ChunkDataProvider.Region, ChunkScale, NoneOpt> info) {
        final ChunkData data = info.cache().create(xPos, zPos);
        if (info.cache().isWaterBiome(xPos, zPos, true)) {
            Colors.fillOcean(
                    (data.getRainVariance(7, 7) + 1) / 2,
                    imageX,
                    imageY,
                    image,
                    info
            );
        } else {
            final int elevation = info.scale().evaluateAtPosition(true, xPos, zPos, info.generator().createHeightFillerForChunk(data.getPos())::sampleHeight).intValue();
            final int color = (elevation > 100 ? RockTypeVisualizer.UPLIFT : RockTypeVisualizer.LAND).get()
                    .color(elevation > 100 ?
                                    Mth.clampedMap(elevation, 100, 175, 0, 1) :
                                    Mth.clampedMap(elevation, TFCChunkGenerator.SEA_LEVEL_Y, 100, 0, 1),
                            info
                    );
            image.setPixel(imageX, imageY, color);
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ChunkDataProvider.Region cache) {
        return Component.empty();
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Override
    public Component description() {
        return DESCRIPTION;
    }

    @Override
    public boolean supportsParallelProcessing() {
        return true;
    }
}
