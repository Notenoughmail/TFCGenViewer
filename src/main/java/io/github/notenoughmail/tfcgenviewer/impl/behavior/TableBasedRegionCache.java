package io.github.notenoughmail.tfcgenviewer.impl.behavior;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class TableBasedRegionCache {

    public static RegionGenerator regionGeneratorWithThisCache(Settings settings, Seed seed, boolean parallel) {
        final TableBasedRegionCache regionCache = new TableBasedRegionCache(parallel);
        return new RegionGenerator(settings, seed) {
            @Override
            public Region getOrCreateRegion(Cellular2D.Cell cell) {
                return regionCache.computeIfAbsent(cell, c -> createRegion(c, ($, $_) -> {}));
            }
        };
    }

    private static final SerialLock SERIAL_LOCK_INSTANCE = new SerialLock();

    // A very sparse table-like structure implemented via maps
    final Double2ObjectMap<Column> table;
    final Lock lock;

    public TableBasedRegionCache(boolean parallel) {
        table = new Double2ObjectOpenHashMap<>();
        lock = parallel ? new ReentrantLock() : SERIAL_LOCK_INSTANCE;
    }

    public Region computeIfAbsent(Cellular2D.Cell cell, Function<Cellular2D.Cell, Region> function) {
        lock.lock();
        try {
            Region r = getColumn(cell.x()).get(cell.y());
            if (r == null) {
                r = function.apply(cell);
                getColumn(cell.x()).set(cell.y(), r);
            }
            return r;
        } finally {
            lock.unlock();
        }
    }

    private boolean parallel() {
        return lock != SERIAL_LOCK_INSTANCE;
    }

    private Column getColumn(double x) {
        return table.computeIfAbsent(x, $ -> new Column());
    }

    @Override
    public String toString() {
        return "TableBasedRegionCache{parallel=%s table=%s}".formatted(parallel(), table);
    }

    private static class SerialLock implements Lock {
        @Override
        public void lock() {}

        @Override
        public void lockInterruptibly() {}

        @Override
        public boolean tryLock() { return false; }

        @Override
        public boolean tryLock(long time, @NotNull TimeUnit unit) { return false; }

        @Override
        public void unlock() {}

        @Override
        public Condition newCondition() { throw new UnsupportedOperationException("SerialLock does not support any actual locking"); }
    }

    private class Column {

        private double firstZ;
        private Region firstRegion;
        private Double2ObjectMap<Region> ifMultiple;

        Column() {
            firstZ = Double.NaN;
        }

        @Nullable
        public Region get(double z) {
            if (Double.isNaN(firstZ)) {
                return null;
            } else if (firstRegion == null) {
                return ifMultiple.get(z);
            }
            return z == firstZ ? firstRegion : null;
        }

        public void set(double z, Region region) {
            if (Double.isNaN(firstZ)) {
                firstZ = z;
                firstRegion = region;
            } else if (firstZ != z) {
                ifMultiple = new Double2ObjectOpenHashMap<>();
                ifMultiple.put(firstZ, firstRegion);
                ifMultiple.put(z, region);
                firstRegion = null;
                if (!FMLLoader.isProduction()) TFCGenViewer.LOGGER.info("Region shares an x with another: {}", region);
            } else {
                throw new IllegalArgumentException("Tried to add %s to table (%s) multiple times?".formatted(region, TableBasedRegionCache.this));
            }
        }

        @Override
        public String toString() {
            return Double.isNaN(firstZ) ?
                    "Column{Empty}" :
                    firstRegion != null ?
                            "Column{%f = %s}".formatted(firstZ, firstRegion) :
                            "Column{%s}".formatted(ifMultiple);
        }
    }
}
