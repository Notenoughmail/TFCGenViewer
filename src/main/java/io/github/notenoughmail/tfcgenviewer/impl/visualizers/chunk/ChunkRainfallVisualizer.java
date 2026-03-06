package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RainfallVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ChunkRainfallVisualizer implements ITFCChunkVisualizerType.Simple<ChunkDataProvider.Region> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_RAINFALL);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_RAINFALL);

    @Override
    public int sort() {
        return 20;
    }

    @Override
    public ChunkDataProvider.Region createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options) {
        return ChunkDataProvider.tfcRegion(worldSeed, generator);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ChunkDataProvider.Region, ChunkScale, NoneOpt> info) {
        final ChunkData data = info.cache().create(xPos, zPos);
        if (info.cache().isOceanBiome(xPos, zPos, false)) {
            Colors.fillOcean(
                    (data.getRainVariance(7, 7) + 1) / 2,
                    imageX,
                    imageY,
                    image,
                    info
            );
        } else {
            final int color = RainfallVisualizer.RAINFALL.get().color(
                    Mth.clampedMap(
                            data.getAverageRainfall(7, 7),
                            0,
                            500,
                            0,
                            1
                    ),
                    info
            );
            image.setPixel(imageX, imageY, color);
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ChunkDataProvider.Region cache) {
        return RainfallVisualizer.COLOR_KEY.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Override
    public Component description() {
        return DESC;
    }
}
