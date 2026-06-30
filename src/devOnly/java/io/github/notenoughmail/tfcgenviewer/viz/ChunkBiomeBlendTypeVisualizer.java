package io.github.notenoughmail.tfcgenviewer.viz;

import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class ChunkBiomeBlendTypeVisualizer implements ITFCChunkVisualizerType.Simple<ChunkDataProvider.Region> {

    private static final DataManager.Reference<ColorDefinition>
            LAND = Colors.KOPPEN_CLASSIFICATIONS.get(KoppenClimateClassification.DFC),
            OCEAN = Colors.KOPPEN_CLASSIFICATIONS.get(KoppenClimateClassification.AF),
            LAKE = Colors.KOPPEN_CLASSIFICATIONS.get(KoppenClimateClassification.CSA);

    private static final ColorKey COLOR_KEY = ColorKey.of(m -> {
        m.append("Land: ");
        LAND.get().appendTo(m);
        m.append("Ocean: ");
        OCEAN.get().appendTo(m);
        m.append("Lake: ");
        LAKE.get().appendTo(m, true);
    });

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public ChunkDataProvider.Region createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options, DrawParallelism parallelism) {
        return ChunkDataProvider.tfcRegion(worldSeed, generator, false);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ChunkDataProvider.Region, ChunkScale, NoneOpt> info) {
        final BiomeExtension ext = info.cache().getBiome(xPos, zPos, false);
        final ColorDefinition color = (switch (ext.biomeBlendType()) {
            case LAND -> LAND;
            case OCEAN -> OCEAN;
            case LAKE -> LAKE;
        }).get();
        image.setPixel(imageX, imageY, color.abgr());
        info.addTooltip(color);
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ChunkDataProvider.Region cache) {
        return COLOR_KEY.colorKey();
    }

    @Override
    public Component name() {
        return Component.literal("Biome Blend Type");
    }

    @Override
    public Component description() {
        return Component.literal("The biome blend type at a position");
    }
}
