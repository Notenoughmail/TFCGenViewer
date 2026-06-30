package io.github.notenoughmail.tfcgenviewer.api.cache;

import io.github.notenoughmail.tfcgenviewer.impl.behavior.MaybeConcurrentArea;
import io.github.notenoughmail.tfcgenviewer.impl.behavior.TableBasedRegionCache;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeBlendType;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataGenerator;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.region.RegionGenerator;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A pseudo-cache for {@link ChunkData}. The default implementation(s) do not provide
 * any true caching functionality of their own, expect where 'inherited' from the underlying
 * {@link ChunkDataGenerator}
 */
public class ChunkDataProvider {

    public static Region tfcRegion(long worldSeed, TFCChunkGenerator generator, boolean parallel) {
        return tfcRegion(Seed.of(worldSeed), generator, parallel);
    }

    public static Region tfcRegion(Seed seed, TFCChunkGenerator generator, boolean parallel) {
        return new Region(generator, seed, parallel);
    }

    /**
     * An action to be applied on generating a {@link ChunkData} so that it
     * can be promoted to {@link ChunkData.Status#FULL}
     */
    public void withPromotionToFull(Consumer<ChunkData> onGenerate) {
        fullPromotion = onGenerate;
    }

    protected final ChunkDataGenerator generator;
    @Nullable
    protected Consumer<ChunkData> fullPromotion;

    public ChunkDataProvider(ChunkDataGenerator generator) {
        this.generator = generator;
    }

    public ChunkDataGenerator generator() {
        return generator;
    }

    public ChunkData create(int chunkX, int chunkZ) {
        final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        final ChunkData data = generator.createAndGenerate(pos);
        if (fullPromotion != null) fullPromotion.accept(data);
        return data;
    }

    public static class Region extends ChunkDataProvider {

        private final BiomeSourceExtension biomeSource;
        private final RegionGenerator regionGenerator;

        // There's a nasty issue with the current region cache that sometimes causes neighboring
        // regions to share a cache position, making some chunks to recreate both regions twice
        // This bypasses that by using a different cache type which doesn't suffer the same problem
        protected Region(TFCChunkGenerator chunkGenerator, Seed seed, boolean parallel) {
            this(TableBasedRegionCache.regionGeneratorWithThisCache(chunkGenerator.settings(), seed, parallel), chunkGenerator, seed, parallel);
        }

        protected Region(RegionGenerator generator, TFCChunkGenerator chunkGenerator, Seed seed, boolean parallel) {
            super(generator.chunkDataGenerator());
            biomeSource = ((BiomeSourceExtension) chunkGenerator.getBiomeSource());
            biomeSource.initRandomState(generator, MaybeConcurrentArea.create(parallel, TFCLayers.createRegionBiomeLayer(generator, seed), TFCLayers::getFromLayerId));
            regionGenerator = generator;
        }

        public RegionGenerator regionGenerator() {
            return regionGenerator;
        }

        public boolean isWaterBiome(BiomeExtension ext) {
            return ext.biomeBlendType() != BiomeBlendType.LAND;
        }

        public boolean isOceanBiome(BiomeExtension ext) {
            return ext.biomeBlendType() == BiomeBlendType.OCEAN;
        }

        public boolean isWaterBiome(int chunkX, int chunkZ, boolean considerRivers) {
            return isWaterBiome(getBiome(chunkX, chunkZ, considerRivers));
        }

        public boolean isOceanBiome(int chunkX, int chunkZ, boolean considerRivers) {
            return isOceanBiome(getBiome(chunkX, chunkZ, considerRivers));
        }

        public BiomeExtension getBiome(int chunkX, int chunkZ, boolean considerRivers) {
            return considerRivers ?
                    biomeSource.getBiomeExtension(QuartPos.fromSection(chunkX) + 1, QuartPos.fromSection(chunkZ) + 1) :
                    biomeSource.getBiomeExtensionNoRiver(QuartPos.fromSection(chunkX) + 1, QuartPos.fromSection(chunkZ) + 1);
        }
    }
}
