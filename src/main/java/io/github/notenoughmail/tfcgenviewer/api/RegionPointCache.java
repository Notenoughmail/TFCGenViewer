package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;

public class RegionPointCache {

    public static RegionPointCache of(TFCChunkGenerator generator, ImageSize scale, long worldSeed) {
        return new RegionPointCache(new RegionGenerator(generator.settings(), Seed.of(worldSeed)), scale);
    }

    private final RegionPoint[] pointCache;
    private final RegionGenerator generator;
    private final int size;

    public RegionPointCache(RegionGenerator generator, ImageSize size) {
        this.generator = generator;
        this.size = size.sizeInPixels();
        pointCache = new RegionPoint[this.size * this.size];
    }

    private int index(int x, int z) {
        return x * size + z;
    }

    private boolean isValid(int coordinate) {
        return coordinate >= 0 && coordinate < size;
    }

    protected void fillCache(Region region, int xOffset, int zOffset) {
        for (Region.Point point : region.points()) {
            // grid - offset = image
            final int x = point.x - xOffset, z = point.z - zOffset;
            if (isValid(x) && isValid(z)) {
                pointCache[index(x, z)] = new RegionPoint(region, point);
            }
        }
    }

    /**
     * @param x The x pixel in the image
     * @param y The y pixel in the image
     * @param gridX The in-world grid x coordinate
     * @param gridZ The in-world grid z coordinate
     * @return The point for the given position
     */
    public Region.Point getPoint(int x, int y, int gridX, int gridZ) {
        return getRegionPoint(x, y, gridX, gridZ).point();
    }

    /**
     * @param x The x pixel in the image
     * @param y The y pixel in the image
     * @param gridX The in-world grid x coordinate
     * @param gridZ The in-world grid z coordinate
     * @return The region & region point for the given position
     */
    public RegionPoint getRegionPoint(int x, int y, int gridX, int gridZ) {
        final int index = index(x, y);
        RegionPoint val = pointCache[index];
        if (val == null) {
            final Region region = generator.getOrCreateRegion(gridX, gridZ);
            // offset = grid - image
            final int xOffset = gridX - x, zOffset = gridZ - y;
            fillCache(region, xOffset, zOffset);
            val = pointCache[index];
        }
        return val;
    }

    /**
     * @param x The x pixel in the image
     * @param y The y pixel in the image
     * @param gridX The in-world grid x coordinate
     * @param gridZ The in-world grid z coordinate
     * @return The region for the given position
     */
    public Region getRegion(int x, int y, int gridX, int gridZ) {
        return getRegionPoint(x, y, gridX, gridZ).region();
    }

    public record RegionPoint(Region region, Region.Point point) {}
}
