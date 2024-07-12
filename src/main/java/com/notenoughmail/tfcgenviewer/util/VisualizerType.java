package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Random;

import static com.notenoughmail.tfcgenviewer.util.ImageBuilder.*;

public enum VisualizerType {
    BIOMES(name("biomes"), (x, y, xPos, yPos, generator, region, point, image) -> setPixel(image, x, y, biomeColor(point.biome))),
    RAINFALL(name("rainfall"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, temperature.applyAsInt(Mth.clampedMap(point.rainfall, 0F, 500F, 0F, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }),
    TEMPERATURE(name("temperature"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, temperature.applyAsInt(Mth.clampedMap(point.temperature, -33F, 33F, 0F, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }),
    BIOME_ALTITUDE(name("biome_altitude"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, green.applyAsInt(Mth.clampedMap(point.discreteBiomeAltitude(), 0, 3, 0, 1)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }),
    INLAND_HEIGHT(name("inland_height"), (x, y, xPos, yPos, generator, region, point, image) -> setPixel(image, x, y, inlandHeightColor(point))),
    ROCKS(name("rocks"), (x, y, xPos, yPos, generator, region, point, image) -> {
        final double value = new Random(point.rock >> 2).nextDouble();
        setPixel(image, x, y, switch (point.rock & 0b11) {
            case 0 -> blue.applyAsInt(value);
            case 1 -> VOLCANIC_ROCK.applyAsInt(value);
            case 2 -> green.applyAsInt(value);
            case 3 -> UPLIFT_ROCK.applyAsInt(value);
            default -> CLEAR;
        });
    }),
    RIVERS(name("rivers"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            final int color;
            if (point.mountain()) {
                color = point.baseLandHeight <= 2 ? SHORT_MOUNTAIN : GRAY;
            } else if (point.lake()) {
                color = SHALLOW_WATER;
            } else {
                color = green.applyAsInt(Mth.clampedMap(point.discreteBiomeAltitude(), 0, 3, 0, 1));
            }
            setPixel(image, x, y, color);
            for (RiverEdge edge : generator.getOrCreatePartitionPoint(xPos, yPos).rivers()) {
                final MidpointFractal fractal = edge.fractal();
                if (fractal.maybeIntersect(xPos, yPos, 0.1F) && fractal.intersect(xPos, yPos, 0.35F)) {
                    setPixel(image, x, y, RIVER_BLUE);
                }
            }
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    });

    public static final VisualizerType[] VALUES = values();
    public static final Codec<VisualizerType> CODEC = Codec.intRange(0, VALUES.length - 1).xmap(b -> VALUES[b], Enum::ordinal);

    private static Component name(String name) {
        return Component.translatable("tfcgenviewer.preview_world.visualizer_type." + name);
    }

    private final Component name;
    private final DrawFunction drawer;

    VisualizerType(Component name, DrawFunction drawer) {
        this.name = name;
        this.drawer = drawer;
    }

    public Component getName() {
        return name;
    }
    
    public void draw(int x, int y, int xPos, int yPos, RegionGenerator generator, Region region, Region.Point point, NativeImage image) {
        drawer.draw(x, y, xPos, yPos, generator, region, point, image);
    }

    @FunctionalInterface
    public interface DrawFunction {
        void draw(int x, int y, int xPos, int yPos, RegionGenerator generator, Region region, Region.Point point, NativeImage image);
    }
}
