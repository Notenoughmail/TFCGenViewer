package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.Random;
import java.util.stream.IntStream;

import static com.notenoughmail.tfcgenviewer.util.ImageBuilder.*;
import static net.minecraft.util.FastColor.ABGR32.*;

public enum VisualizerType {
    BIOMES(name("biomes"), (x, y, xPos, yPos, generator, region, point, image) -> setPixel(image, x, y, biomeColor(point.biome)), colors("biomes", OCEAN, OCEAN_REEF, DEEP_OCEAN, DEEP_OCEAN_TRENCH, LAKE, MOUNTAIN_ANY_OR_PLATEAU_LAKE, RIVER, OCEANIC_MOUNTAINS, CANYONS, LOW_CANYONS, LOWLANDS, MOUNTAINS, OLD_MOUNTAINS, PLATEAU, BADLANDS, INVERTED_BADLANDS, SHORE, HIGHLANDS, ROLLING_HILLS, HILLS, PLAINS)),
    RAINFALL(name("rainfall"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, climate.applyAsInt(Mth.clampedMap(point.rainfall, 0F, 500F, 0, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }, colors("rainfall", climate.applyAsInt(0), climate.applyAsInt(0.2), climate.applyAsInt(0.4), climate.applyAsInt(0.6), climate.applyAsInt(0.8), climate.applyAsInt(0.999), blue.applyAsInt(0), blue.applyAsInt(0.2), blue.applyAsInt(0.4), blue.applyAsInt(0.6), blue.applyAsInt(0.8), blue.applyAsInt(0.999))),
    TEMPERATURE(name("temperature"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, climate.applyAsInt(Mth.clampedMap(point.temperature, -33F, 33F, 0F, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }, colors("temperature", climate.applyAsInt(0), climate.applyAsInt(0.2), climate.applyAsInt(0.4), climate.applyAsInt(0.6), climate.applyAsInt(0.8), climate.applyAsInt(0.999), blue.applyAsInt(0), blue.applyAsInt(0.2), blue.applyAsInt(0.4), blue.applyAsInt(0.6), blue.applyAsInt(0.8), blue.applyAsInt(0.999))),
    BIOME_ALTITUDE(name("biome_altitude"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, green.applyAsInt(Mth.clampedMap(point.discreteBiomeAltitude(), 0, 3, 0, 1)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }, colors("biome_altitude", green.applyAsInt(0), green.applyAsInt(0.333), green.applyAsInt(0.666), green.applyAsInt(1), blue.applyAsInt(0), blue.applyAsInt(0.2), blue.applyAsInt(0.4), blue.applyAsInt(0.6), blue.applyAsInt(0.8), blue.applyAsInt(0.999))),
    INLAND_HEIGHT(name("inland_height"), (x, y, xPos, yPos, generator, region, point, image) -> setPixel(image, x, y, inlandHeightColor(point)), colors("inland_height", green.applyAsInt(0), green.applyAsInt(0.2), green.applyAsInt(0.4), green.applyAsInt(0.6), green.applyAsInt(0.8), green.applyAsInt(0.999), SHALLOW_WATER, DEEP_WATER, VERY_DEEP_WATER)),
    ROCK_TYPES(name("rock_types"), (x, y, xPos, yPos, generator, region, point, image) -> {
        final double value = new Random(point.rock >> 2).nextDouble();
        setPixel(image, x, y, switch (point.rock & 0b11) {
            case 0 -> blue.applyAsInt(value);
            case 1 -> VOLCANIC_ROCK.applyAsInt(value);
            case 2 -> green.applyAsInt(value);
            case 3 -> UPLIFT_ROCK.applyAsInt(value);
            default -> CLEAR;
        });
    }, colors("rock_types", green.applyAsInt(0), green.applyAsInt(0.2), green.applyAsInt(0.4), green.applyAsInt(0.6), green.applyAsInt(0.8), green.applyAsInt(0.999), blue.applyAsInt(0), blue.applyAsInt(0.2), blue.applyAsInt(0.4), blue.applyAsInt(0.6), blue.applyAsInt(0.8), blue.applyAsInt(0.999), VOLCANIC_ROCK.applyAsInt(0), VOLCANIC_ROCK.applyAsInt(0.2), VOLCANIC_ROCK.applyAsInt(0.4), VOLCANIC_ROCK.applyAsInt(0.6), VOLCANIC_ROCK.applyAsInt(0.8), VOLCANIC_ROCK.applyAsInt(0.999), UPLIFT_ROCK.applyAsInt(0), UPLIFT_ROCK.applyAsInt(0.2), UPLIFT_ROCK.applyAsInt(0.4), UPLIFT_ROCK.applyAsInt(0.6), UPLIFT_ROCK.applyAsInt(0.8), UPLIFT_ROCK.applyAsInt(0.999))),
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
    }, colors("rivers", RIVER_BLUE, SHORT_MOUNTAIN, GRAY, SHALLOW_WATER, green.applyAsInt(0), green.applyAsInt(0.2), green.applyAsInt(0.4), green.applyAsInt(0.6), green.applyAsInt(0.8), green.applyAsInt(0.999), blue.applyAsInt(0), blue.applyAsInt(0.2), blue.applyAsInt(0.4), blue.applyAsInt(0.6), blue.applyAsInt(0.8), blue.applyAsInt(0.999))),
    ROCKS(name("rocks"), (x, y, xPos, yPos, generator, region, point, image) -> {
        // TODO: Implement
    }, colors("rocks", GRANITE, DIORITE, GABBRO, SHALE, CLAYSTONE, LIMESTONE, CONGLOMERATE, DOLOMITE, CHERT, CHALK, RHYOLITE, BASALT, ANDESITE, DACITE, QUARTZITE, SLATE, PHYLLITE, SCHIST, GNEISS, MARBLE, UNKNOWN_ROCK));

    public static final VisualizerType[] VALUES = values();
    public static final Codec<VisualizerType> CODEC = Codec.intRange(0, VALUES.length - 1).xmap(b -> VALUES[b], Enum::ordinal);

    private static Component name(String name) {
        return Component.translatable("tfcgenviewer.preview_world.visualizer_type." + name);
    }

    private static MutableComponent colors(String name, int... colors) {
        return Component.translatable(
                "tfcgenviewer.preview_world.visualizer_type." + name + ".color_key",
                IntStream.of(colors).mapToObj(i ->
                        Component.literal("■").withStyle(style ->
                                style.withColor(FastColor.ARGB32.color(
                                        alpha(i),
                                        red(i),
                                        green(i),
                                        blue(i)
                                )))).toArray()
        );
    }

    private final Component name, colorKey;
    private final DrawFunction drawer;

    VisualizerType(Component name, DrawFunction drawer, Component colorKey) {
        this.name = name;
        this.drawer = drawer;
        this.colorKey = colorKey;
    }

    public Component getName() {
        return name;
    }

    public Component getColorKey() {
        return colorKey;
    }

    public void draw(int x, int y, int xPos, int yPos, RegionGenerator generator, Region region, Region.Point point, NativeImage image) {
        drawer.draw(x, y, xPos, yPos, generator, region, point, image);
    }

    @FunctionalInterface
    public interface DrawFunction {
        void draw(int x, int y, int xPos, int yPos, RegionGenerator generator, Region region, Region.Point point, NativeImage image);
    }
}
