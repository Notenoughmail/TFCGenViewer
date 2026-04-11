package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class ChunkBiomeVisualizer implements ITFCChunkVisualizerType.Simple<ChunkBiomeVisualizer.Cache> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_BIOME);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_BIOME);

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public ChunkBiomeVisualizer.Cache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options, DrawParallelism parallelism) {
        return new Cache(worldSeed, generator);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ChunkBiomeVisualizer.Cache, ChunkScale, NoneOpt> info) {
        final ColorDefinition color = info.cache().getColor(xPos, zPos);
        info.addTooltip(color);
        image.setPixel(imageX, imageY, color);
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ChunkBiomeVisualizer.Cache cache) {
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

    public static class Cache {

        private final ChunkDataProvider.Region biomeSource;
        private boolean unknownEncountered = false;
        private final Set<ColorDefinition> colorsEncountered;

        Cache(long worldSeed, TFCChunkGenerator generator) {
            biomeSource = ChunkDataProvider.tfcRegion(worldSeed, generator, false);
            colorsEncountered = new HashSet<>();
        }

        public ColorDefinition getColor(int xPos, int zPos) {
            final BiomeExtension ext = biomeSource.getBiome(xPos, zPos, true);
            final ColorDefinition color = Colors.BIOME_COLORS.getInstanceColor(ext.key(), Colors.UNKNOWN_BIOME);
            colorsEncountered.add(color);
            return color;
        }

        public Component colorKey() {
            final MutableComponent key = Component.empty();
            final ColorDefinition unknown = Colors.UNKNOWN_BIOME.get().color();
            final Iterator<ColorDefinition> iter = colorsEncountered.stream()
                    .filter(Objects::nonNull)
                    .filter(c -> {
                        if (c == unknown) {
                            unknownEncountered = true;
                            return false;
                        }
                        return true;
                    })
                    .distinct()
                    .sorted()
                    .iterator();
            while (iter.hasNext()) {
                iter.next()
                        .appendTo(key, !unknownEncountered && !iter.hasNext());
            }
            if (unknownEncountered) {
                unknown.appendTo(key, true);
            }
            return key;
        }
    }
}
