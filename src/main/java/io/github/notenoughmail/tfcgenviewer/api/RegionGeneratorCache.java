package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;

public class RegionGeneratorCache {

    private final Region.Point[] regionCache;
    private final RegionGenerator generator;
    private final int size;

    public RegionGeneratorCache(RegionGenerator generator, IScale size) {
        this.generator = generator;
        this.size = size.sizeInPixels();
        regionCache = new Region.Point[this.size * this.size];
    }

    private int index(int x, int z) {
        return x * size + z;
    }

    private boolean isValid(int coordinate) {
        return coordinate >= 0 && coordinate < size;
    }

    private void fillCache(Region region, int xOffset, int zOffset) {
        for (Region.Point point : region.points()) {
            // grid - offset = image
            final int x = point.x - xOffset, z = point.z - zOffset;
            if (isValid(x) && isValid(z)) {
                regionCache[index(x, z)] = point;
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
        final int index = index(x, y);
        Region.Point val = regionCache[index];
        if (val == null) {
            final Region region = generator.getOrCreateRegion(gridX, gridZ);
            // offset = grid - image
            final int xOffset = gridX - x, zOffset = gridZ - y;
            fillCache(region, xOffset, zOffset);
            val = regionCache[index];
        }
        return val;
    }
}
