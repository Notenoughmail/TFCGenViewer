package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class TestChunkVisualizer implements IRegionVisualizerType<ChunkDataProvider.Region, IVisualizerType.NoneOpt> {

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public NoneOpt createOptions(RegistryAccess registryAccess) {
        return NoneOpt.INSTANCE;
    }

    @Override
    public ChunkDataProvider.Region createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options) {
        return ChunkDataProvider.tfcRegion(worldSeed, generator, false);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int chunkX, int chunkZ, DrawInfo<TFCChunkGenerator, ChunkDataProvider.Region, GridScale, NoneOpt> info) {
        final ChunkData data = info.cache().create(chunkX, chunkZ);
        if (info.cache().isWaterBiome(chunkX, chunkZ, true)) {
            final int color = Colors.OCEAN.get().color((data.getRainVariance(7, 7) + 1) / 2, info);
            image.setPixel(imageX, imageY, color);
        } else {
            final ForestType forestType = data.getForestType();
            final double val = forestType.ordinal() / 28.0;
            final Gradient gradient = TFCGenViewerRegistration.GRAD_GRAYSCALE.get();
            final int color = gradient.applyAsAbgr(val);
            image.setPixel(imageX, imageY, color);
            info.colorTooltips().addTooltip(color, Component.literal(forestType.name()));
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ChunkDataProvider.Region cache) {
        return Component.literal("All the sizes on the left are a lie! This is a chunk scale view, so every pixel = 16 blocks");
    }

    @Override
    public Component name() {
        return Component.literal("Woah");
    }

    @Override
    public Component description() {
        return Component.literal("Woah!");
    }
}
