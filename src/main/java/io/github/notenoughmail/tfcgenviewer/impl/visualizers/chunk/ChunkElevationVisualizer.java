package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorGradientDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor.TFCChunkGeneratorAccessor;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.*;
import net.dries007.tfc.world.biome.BiomeBlendType;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.river.RiverBlendType;
import net.dries007.tfc.world.river.RiverNoiseSampler;
import net.dries007.tfc.world.shore.ShoreBlendType;
import net.dries007.tfc.world.shore.ShoreNoiseSampler;
import net.dries007.tfc.world.volcano.CenteredFeatureBlendType;
import net.dries007.tfc.world.volcano.CenteredFeatureNoiseSampler;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChunkElevationVisualizer implements ITFCChunkVisualizerType.Simple<ChunkElevationVisualizer.ElevationCache> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_ELEVATION);
    public static final Component DESCRIPTION = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_ELEVATION);

    public static final DataManager.Reference<ColorGradientDefinition> LOW = Colors.MISC_GRADIENTS.getReference(TFCGenViewer.id("low_elevation"));
    public static final DataManager.Reference<ColorGradientDefinition> MID = Colors.MISC_GRADIENTS.getReference(TFCGenViewer.id("middle_elevation"));
    public static final DataManager.Reference<ColorGradientDefinition> HIGH = Colors.MISC_GRADIENTS.getReference(TFCGenViewer.id("high_elevation"));

    public static final ColorKey COLOR_KEY = ColorKey.of(m -> {
        LOW.get().appendTo(m);
        MID.get().appendTo(m);
        HIGH.get().appendTo(m, true);
    });

    @Override
    public int sort() {
        return 10;
    }

    @Nullable
    @Override
    public ElevationCache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options, DrawParallelism parallelism) {
        final Seed seed = Seed.of(worldSeed);
        ChunkDataProvider.tfcRegion(seed, generator, parallelism.parallel()); // init the biome layer for the height filler
        ((TFCChunkGeneratorAccessor) generator).tfcgenviewer$SetSeed(seed);
        return new ElevationCache(generator, seed);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ElevationCache, ChunkScale, NoneOpt> info) {
        info.cache().primePos(xPos, zPos);
        final int elevation = info.evaluateAtBlockPosition(true, xPos, zPos, info.cache()::sample);
        final int color = elevation < TFCChunkGenerator.SEA_LEVEL_Y ?
                LOW.get().color(Mth.clampedMap(elevation, 23, TFCChunkGenerator.SEA_LEVEL_Y, 0, 1), info) :
                elevation > 103 ?
                        HIGH.get().color(Mth.clampedMap(elevation, 103, 203, 0, 1), info) :
                        MID.get().color(Mth.map(elevation, TFCChunkGenerator.SEA_LEVEL_Y, 105, 0, 1), info);
        image.setPixel(imageX, imageY, color);
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ElevationCache cache) {
        return COLOR_KEY.colorKey();
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
    public boolean requestDrawInParallel(NoneOpt options, ImageSize size) {
        return true;
    }

    public static class ElevationCache {

        // Trivially cacheable (chunk-agnostic) lookups
        private final Map<BiomeExtension, BiomeNoiseSampler> biomeSamplers;
        private final Map<RiverBlendType, RiverNoiseSampler> riverSamplers;
        private final Map<ShoreBlendType, ShoreNoiseSampler> shoreSamplers;
        private final Map<CenteredFeatureBlendType, CenteredFeatureNoiseSampler> volcanoSamplers;

        private final Noise2D tideHeightNoise;
        private final BiomeSourceExtension biomeSourceExt;
        private final Sampler<BiomeExtension> biomeSampler;
        private ChunkPos pos;

        public ElevationCache(TFCChunkGenerator generator, Seed seed) {
            biomeSamplers = generator.createBiomeSamplersForChunk(null);
            riverSamplers = generator.createRiverSamplersForChunk();
            shoreSamplers = generator.createShoreSamplersForChunk();
            volcanoSamplers = generator.createVolcanoSamplersForChunk();
            tideHeightNoise = BiomeNoise.shoreTideLevelNoise(seed);
            biomeSourceExt = generator.customBiomeSource;
            biomeSampler = (x, z) -> biomeSourceExt.getBiomeExtensionNoRiver(QuartPos.fromBlock(x), QuartPos.fromBlock(z));
            pos = ChunkPos.ZERO;
        }

        public void primePos(int chunkX, int chunkZ) {
            pos = new ChunkPos(chunkX, chunkZ);
        }

        // (blockX & 15) == (blockZ & 15) == 8 will always be true when using DrawInfo#evaluateAtBlockPosition
        public int sample(int blockX, int blockZ) {
            return (int) new ChunkHeightFiller(
                    sampleBiomesForRelevantPositions(),
                    biomeSourceExt,
                    biomeSamplers,
                    riverSamplers,
                    shoreSamplers,
                    volcanoSamplers,
                    TFCChunkGenerator.SEA_LEVEL_Y,
                    tideHeightNoise
            ).sampleHeight(blockX, blockZ);
        }

        // A 'specialized' version of ChunkBiomeSampler#sampleBiomes which only calculates the biome weights for positions we care about/will use
        // Takes around a quarter of the time and only introduces minute error in 0.4% of pixels
        private Object2DoubleMap<BiomeExtension>[] sampleBiomesForRelevantPositions() {
            final Object2DoubleMap<BiomeExtension>[] chunkBiomeWeightArray = ChunkBiomeSampler.newWeightArray(4 * 4);
            final int chunkX = pos.getMinBlockX(), chunkZ = pos.getMinBlockZ();

            // 1 | (1 << 2) = 5
            // 2 | (1 << 2) = 6
            // 1 | (2 << 2) = 9
            // 2 | (2 << 2) = 10
            for (int x = 1 ; x < 3 ; x++) {
                for (int z = 1 ; z < 3 ; z++) {
                    final Object2DoubleMap<BiomeExtension> weight = new Object2DoubleOpenHashMap<>();
                    chunkBiomeWeightArray[x | (z << 2)] = weight;
                    ChunkBiomeSampler.sampleBiomesAtPositionWithKernel(weight, biomeSampler, ChunkBiomeSampler.KERNEL_9x9, 4, chunkX, chunkZ, x - 1, z - 1);
                }
            }

            final Object2DoubleMap<BiomeExtension>[] quartBiomeWeightArray = ChunkBiomeSampler.newWeightArray(7 * 7);
            final Object2DoubleMap<BiomeExtension> transientWeights = new Object2DoubleOpenHashMap<>();

            // 3 + 7 * 3 = 24
            // 4 + 7 * 3 = 25
            // 3 + 7 * 4 = 31
            // 4 + 7 * 4 = 32
            for (int x = 3 ; x < 5 ; x++) {
                for (int z = 3 ; z < 5 ; z++) {
                    final Object2DoubleMap<BiomeExtension> quartBiomeWeight = new Object2DoubleOpenHashMap<>();
                    transientWeights.clear();

                    ChunkBiomeSampler.sampleBiomesAtPositionWithKernel(quartBiomeWeight, biomeSampler, ChunkBiomeSampler.KERNEL_9x9, 2, chunkX, chunkZ, x - 1, z - 1);

                    final int x1 = chunkX + ((x - 1) << 2); // Block coordinates
                    final int z1 = chunkZ + ((z - 1) << 2);

                    final int coordX = x1 >> 4; // Chunk coordinates
                    final int coordZ = z1 >> 4;

                    final double lerpX = (x1 - (coordX << 4)) * (1 / 16d); // Deltas, in the range [0, 1)
                    final double lerpZ = (z1 - (coordZ << 4)) * (1 / 16d);

                    // At the x and z values care about, index16X and index16Z will always be 1, so no need calculate the indexes into the chunk biome array
                    ChunkBiomeSampler.sampleBiomesCornerContribution(transientWeights, chunkBiomeWeightArray[5], (1 - lerpX) * (1 - lerpZ));
                    ChunkBiomeSampler.sampleBiomesCornerContribution(transientWeights, chunkBiomeWeightArray[6], lerpX * (1 - lerpZ));
                    ChunkBiomeSampler.sampleBiomesCornerContribution(transientWeights, chunkBiomeWeightArray[9], (1 - lerpX) * lerpZ);
                    ChunkBiomeSampler.sampleBiomesCornerContribution(transientWeights, chunkBiomeWeightArray[10], lerpX * lerpZ);

                    ChunkBiomeSampler.composeSampleWeights(quartBiomeWeight, transientWeights, b -> b.biomeBlendType().ordinal(), BiomeBlendType.SIZE);

                    quartBiomeWeightArray[x + 7 * z] = quartBiomeWeight;
                }
            }
            return quartBiomeWeightArray;
        }
    }
}
