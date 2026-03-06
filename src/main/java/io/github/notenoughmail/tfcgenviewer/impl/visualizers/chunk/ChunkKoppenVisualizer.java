package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class ChunkKoppenVisualizer implements ITFCChunkVisualizerType.Simple<ChunkDataProvider.Region> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_KOPPEN);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_KOPPEN);

    @Override
    public int sort() {
        return 60;
    }

    @Override
    public ChunkDataProvider.Region createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options) {
        return ChunkDataProvider.tfcRegion(worldSeed, generator);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ChunkDataProvider.Region, ChunkScale, NoneOpt> info) {
        final ColorDefinition color;
        if (info.cache().isOceanBiome(xPos, zPos, false)) {
            color = Colors.KOPPEN_COLORS.unknown();
        } else {
            final ChunkData data = info.cache().create(xPos, zPos);
            color = Colors.KOPPEN_CLASSIFICATIONS.get(KoppenClimateClassification.classify(
                    data.getAverageSeaLevelTemp(7, 7),
                    data.getAverageRainfall(7, 7),
                    data.getRainVariance(7, 7),
                    info.isNorthernHemisphere(zPos)
            )).get();
        }
        info.addTooltip(color);
        image.setPixel(imageX, imageY, color.abgr());
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ChunkDataProvider.Region cache) {
        return Colors.KOPPEN_COLORS.colorKey();
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
