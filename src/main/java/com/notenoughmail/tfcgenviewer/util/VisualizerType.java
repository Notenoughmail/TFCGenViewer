package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.notenoughmail.tfcgenviewer.config.BiomeColors;
import com.notenoughmail.tfcgenviewer.config.Colors;
import com.notenoughmail.tfcgenviewer.config.RockColors;
import com.notenoughmail.tfcgenviewer.config.RockTypeColors;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.notenoughmail.tfcgenviewer.util.ImageBuilder.*;
import static net.minecraft.util.FastColor.ABGR32.blue;
import static net.minecraft.util.FastColor.ABGR32.green;
import static net.minecraft.util.FastColor.ABGR32.*;

public enum VisualizerType {
    BIOMES(name("biomes"), (x, y, xPos, yPos, generator, region, point, image) -> setPixel(image, x, y, BiomeColors.get(point.biome)), BiomeColors.colorKey()),
    RAINFALL(name("rainfall"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, Colors.rainfall().gradient().applyAsInt(Mth.clampedMap(point.rainfall, 0F, 500F, 0, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }, () -> {
        final MutableComponent key = Component.empty();
        Colors.rainfall().appendTo(key, false);
        Colors.fillOcean().appendTo(key, true);
        return key;
    }),
    TEMPERATURE(name("temperature"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, Colors.temperature().gradient().applyAsInt(Mth.clampedMap(point.temperature, -33F, 33F, 0F, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }, () -> {
        final MutableComponent key = Component.empty();
        Colors.temperature().appendTo(key, false);
        Colors.fillOcean().appendTo(key, true);
        return key;
    }),
    BIOME_ALTITUDE(name("biome_altitude"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, Colors.biomeAltitude(point.discreteBiomeAltitude()));
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }, Colors.biomeAltitude(false)),
    INLAND_HEIGHT(name("inland_height"), (x, y, xPos, yPos, generator, region, point, image) -> setPixel(image, x, y, inlandHeightColor(point)), () -> {
        final MutableComponent key = Component.empty();
        Colors.inlandHeight().appendTo(key, false);
        Colors.shallowWater().appendTo(key, false);
        Colors.deepWater().appendTo(key, false);
        Colors.veryDeepWater().appendTo(key, true);
        return key;
    }),
    RIVERS(name("rivers"), (x, y, xPos, yPos, generator, region, point, image) -> {
        if (point.land()) {
            final int color;
            if (point.mountain()) {
                color = point.baseLandHeight <= 2 ? Colors.volcanicMountain().color() : Colors.inlandMountain().color();
            } else if (point.lake()) {
                color = Colors.lake().color();
            } else {
                color = Colors.biomeAltitude(point.discreteBiomeAltitude());
            }
            setPixel(image, x, y, color);
            for (RiverEdge edge : generator.regionGenerator().getOrCreatePartitionPoint(xPos, yPos).rivers()) {
                final MidpointFractal fractal = edge.fractal();
                if (fractal.maybeIntersect(xPos, yPos, 0.1F) && fractal.intersect(xPos, yPos, 0.35F)) {
                    setPixel(image, x, y, Colors.river().color());
                }
            }
        } else {
            fillOcean.draw(x, y, xPos, yPos, generator, region, point, image);
        }
    }, Colors.biomeAltitude(true)),
    ROCK_TYPES(name("rock_types"), (x, y, xPos, yPos, generator, region, point, image) -> {
        final double value = new Random(point.rock >> 2).nextDouble();
        setPixel(image, x, y, RockTypeColors.apply(point.rock & 0b11, value));
    }, RockTypeColors.colorKey()),
    ROCKS(name("rocks"), (x, y, xPos, yPos, generator, region, point, image) -> {
        final Block raw = generator.generateRock(xPos * 128 - 64, 0, yPos * 128 - 64, 0, null).raw();
        setPixel(image, x, y, RockColors.get(raw));
    }, RockColors.colorKey());

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

    private final Component name;
    private final DrawFunction drawer;
    private final Supplier<Component> colorKey;

    VisualizerType(Component name, DrawFunction drawer, Supplier<Component> colorKey) {
        this.name = name;
        this.drawer = drawer;
        this.colorKey = colorKey;
    }

    VisualizerType(Component name, DrawFunction drawer, Component colorKey) {
        this(name, drawer, Lazy.of(() -> colorKey));
    }

    public Component getName() {
        return name;
    }

    public Component getColorKey() {
        return colorKey.get();
    }

    public void draw(int x, int y, int xPos, int yPos, RegionChunkDataGenerator generator, Region region, Region.Point point, NativeImage image) {
        drawer.draw(x, y, xPos, yPos, generator, region, point, image);
    }

    @FunctionalInterface
    public interface DrawFunction {
        void draw(int x, int y, int xPos, int yPos, RegionChunkDataGenerator generator, Region region, Region.Point point, NativeImage image);
    }
}
