package io.github.notenoughmail.tfcgenviewer.impl;

import it.unimi.dsi.fastutil.HashCommon;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.noise.FastNoiseLite;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

public class NoiseBasedRegionCache {

    public static RegionGenerator regionGeneratorWithThisCache(Settings settings, Seed seed, boolean parallel) {
        final NoiseBasedRegionCache regionCache = new NoiseBasedRegionCache(256, seed.seed(), parallel);
        return new RegionGenerator(settings, seed) {
            @Override
            public Region getOrCreateRegion(Cellular2D.Cell cell) {
                Region r = regionCache.getIfPresent(cell);
                if (r == null) {
                    r = createRegion(cell, (t, re) -> {});
                    regionCache.set(cell, r);
                }
                return r;
            }
        };
    }

    private static final SerialLock SERIAL_LOCK_INSTANCE = new SerialLock();

    final long[] keys;
    final Region[] values;
    final int mask;
    final FastNoiseLite keyGenerator;
    final StampedLock lock;

    public NoiseBasedRegionCache(int size, long seed, boolean parallel) {
        size = Mth.smallestEncompassingPowerOfTwo(size);
        mask = size - 1;
        keys = new long[size];
        values = new Region[size];
        Arrays.fill(keys, Long.MIN_VALUE);
        keyGenerator = new FastNoiseLite((int) (seed ^ (seed >> 32)));
        keyGenerator.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        keyGenerator.SetFractalOctaves(2);
        keyGenerator.SetFrequency(100_000D);
        lock = parallel ? new StampedLock() : SERIAL_LOCK_INSTANCE;
    }

    @Nullable
    public Region getIfPresent(Cellular2D.Cell cell) {
        final long key = key(cell);
        final int index = (int) HashCommon.mix(key) & mask;
        final long stamp = lock.readLock();
        Region r = null;
        if (keys[index] == key) r = values[index];
        lock.unlockRead(stamp);
        return r;
    }

    public void set(Cellular2D.Cell cell, Region value) {
        final long key = key(cell);
        final int index = (int) HashCommon.mix(key) & mask;
        final long stamp = lock.writeLock();
        keys[index] = key;
        values[index] = value;
        lock.unlockWrite(stamp);
    }

    // TODO: 2.1.0 | This does not work as well as I initially thought...
    private long key(Cellular2D.Cell cell) {
        final double noise = keyGenerator.GetNoise(cell.x(), cell.y());
        return Double.doubleToRawLongBits(noise);
    }

    private static class SerialLock extends StampedLock {

        @Override
        public long writeLock() {
            return 0L;
        }

        @Override
        public long readLock() {
            return 0L;
        }

        @Override
        public void unlockWrite(long stamp) {
        }

        @Override
        public void unlockRead(long stamp) {
        }
    }
}
