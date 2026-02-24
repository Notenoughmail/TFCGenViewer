package io.github.notenoughmail.tfcgenviewer.api.cache;

import com.google.common.collect.ImmutableSet;
import io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor.TFCChunkGeneratorAccessor;
import net.dries007.tfc.world.ChunkHeightFiller;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataGenerator;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.ConcurrentArea;
import net.dries007.tfc.world.region.RegionGenerator;
import net.minecraft.Util;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

// TODO: Is there a good reason AND way to cache this?
public class ChunkDataProvider {

    public static Region tfcRegion(long worldSeed, TFCChunkGenerator generator, boolean full) {
        final Region provider = new Region(generator, Seed.of(worldSeed), full);
        if (full) {
            provider.withPromotionToFull(c -> {
                final ChunkPos pos = c.getPos();
                final int minX = pos.getMinBlockX(), minZ = pos.getMinBlockZ();
                final int[] surfaceElevation = new int[16 * 16];
                final ChunkHeightFiller filler = generator.createHeightFillerForChunk(pos);
                for (int x = 0 ; x < 16 ; x++) {
                    for (int z = 0 ; z < 16 ; z++) {
                        surfaceElevation[x + 16 * z] = (int) filler.sampleHeight(minX + x, minZ + z);
                    }
                }
                c.generateFull(surfaceElevation, new int[0]);
            });
        }
        return provider;
    }

    public void withPromotionToFull(Consumer<ChunkData> onGenerate) {
        fullPromotion = onGenerate;
    }

    protected final ChunkDataGenerator generator;
    @Nullable
    protected Consumer<ChunkData> fullPromotion;

    public ChunkDataProvider(ChunkDataGenerator generator) {
        this.generator = generator;
    }

    public ChunkData create(int chunkX, int chunkZ) {
        final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        final ChunkData data = generator.createAndGenerate(pos);
        if (fullPromotion != null) fullPromotion.accept(data);
        return data;
    }

    public static class Region extends ChunkDataProvider {

        private static final Set<BiomeExtension> WATER_BIOMES = Util.make(new ImmutableSet.Builder<BiomeExtension>(), b -> b.add(
                TFCBiomes.OCEAN,
                TFCBiomes.DEEP_OCEAN,
                TFCBiomes.DEEP_OCEAN_TRENCH,
                TFCBiomes.OCEAN_REEF,
                TFCBiomes.LAKE,
                TFCBiomes.MELTWATER_LAKE,
                TFCBiomes.SUBGLACIAL_LAKE,
                TFCBiomes.PLATEAU_LAKE,
                TFCBiomes.VOLCANIC_OCEANIC_MOUNTAIN_LAKE,
                TFCBiomes.MOUNTAIN_LAKE,
                TFCBiomes.OLD_MOUNTAIN_LAKE,
                TFCBiomes.TOWER_KARST_LAKE,
                TFCBiomes.OCEANIC_MOUNTAIN_LAKE,
                TFCBiomes.VOLCANIC_MOUNTAIN_LAKE,
                TFCBiomes.RIVER
        )).build();

        private final BiomeSourceExtension biomeSource;

        Region(TFCChunkGenerator chunkGenerator, Seed seed, boolean full) {
            this(new RegionGenerator(chunkGenerator.settings(), seed), chunkGenerator, seed, full);
        }

        Region(RegionGenerator generator, TFCChunkGenerator chunkGenerator, Seed seed, boolean full) {
            super(generator.chunkDataGenerator());
            biomeSource = ((BiomeSourceExtension) chunkGenerator.getBiomeSource());
            biomeSource.initRandomState(generator, new ConcurrentArea<>(TFCLayers.createRegionBiomeLayer(generator, seed), TFCLayers::getFromLayerId));
            if (full) {
                ((TFCChunkGeneratorAccessor) chunkGenerator).tfcgenviewer$SetSeed(seed);
                ((TFCChunkGeneratorAccessor) chunkGenerator).tfcgenviewer$SetTideHeightNoise(BiomeNoise.shoreTideLevelNoise(seed));
            }
        }

        public boolean isWaterBiome(BiomeExtension biomeExtension) {
            return WATER_BIOMES.contains(biomeExtension);
        }

        public boolean isWaterBiome(int chunkX, int chunkZ, boolean considerRivers) {
            return isWaterBiome(getBiome(chunkX, chunkZ, considerRivers));
        }

        public BiomeExtension getBiome(int chunkX, int chunkZ, boolean considerRivers) {
            return considerRivers ?
                    biomeSource.getBiomeExtension(QuartPos.fromSection(chunkX), QuartPos.fromSection(chunkZ)) :
                    biomeSource.getBiomeExtensionNoRiver(QuartPos.fromSection(chunkX), QuartPos.fromSection(chunkZ));
        }
    }
}
