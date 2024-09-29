package com.notenoughmail.tfcgenviewer.util;

import net.dries007.tfc.world.region.Region;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.Random;
import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

import static com.notenoughmail.tfcgenviewer.config.color.Colors.*;
import static com.notenoughmail.tfcgenviewer.util.ImageBuilder.setPixel;
import static net.minecraft.util.FastColor.ABGR32.*;

public class ColorUtil {

    private static final Random COLOR_GENERATOR = new Random(System.nanoTime() ^ System.currentTimeMillis());

    // Actual utils
    public static DoubleToIntFunction linearGradient(int from, int to) {
        return value -> color(
                Mth.lerpInt((float) value, alpha(from), alpha(to)),
                Mth.lerpInt((float) value, blue(from), blue(to)),
                Mth.lerpInt((float) value, green(from), green(to)),
                Mth.lerpInt((float) value, red(from), red(to))
        );
    }

    public static DoubleToIntFunction multiLinearGradient(int... colors) {
        final DoubleToIntFunction[] parts = IntStream.range(0, colors.length - 1)
                .mapToObj(i -> linearGradient(colors[i], colors[i + 1]))
                .toArray(DoubleToIntFunction[]::new);
        return value -> parts[Mth.floor(value * parts.length)].applyAsInt((value * parts.length) % 1);
    }

    public static int rgbToBgr(int rgb) {
        return color(
                255,
                FastColor.ARGB32.blue(rgb),
                FastColor.ARGB32.green(rgb),
                FastColor.ARGB32.red(rgb)
        );
    }

    public static int bgrToRgb(int bgr) {
        return FastColor.ARGB32.color(
                255,
                red(bgr),
                green(bgr),
                blue(bgr)
        );
    }

    private static final Random RESETTING_RANDOM = new Random(System.nanoTime());
    private static double nextWithSeed(long seed) {
        RESETTING_RANDOM.setSeed(seed);
        return RESETTING_RANDOM.nextDouble();
    }

    public static int randomColor() {
        return FastColor.ABGR32.color(
                255,
                COLOR_GENERATOR.nextInt()
        );
    }

    // Drawers
    static final VisualizerType.DrawFunction fillOcean = (x, y, xOffset, yOffset, generator, region, point, image) ->
            setPixel(image, x, y, FILL_OCEAN.get().gradient().applyAsInt(region.noise() / 2));
    static final VisualizerType.DrawFunction dev = (x, y, xPos, zPos, generator, region, point, image) ->
            setPixel(image, x, y, ColorUtil.grayscale.applyAsInt((double) region.hashCode() / (double) Integer.MAX_VALUE));

    // Color getters that are not complex but also not easily (or cleanly) made single line
    static int inlandHeight(Region.Point point) {
        if (point.land()) {
            return IH_LAND.get().gradient().applyAsInt(point.baseLandHeight / 24F);
        }

        // Deal with it
        return (
                point.shore() ?
                point.river() ?
                        IH_SHALLOW :
                        IH_DEEP :
                point.baseOceanDepth < 4 ?
                        IH_SHALLOW :
                        point.baseOceanDepth < 8 ?
                                IH_DEEP :
                                IH_VERY_DEEP
        ).get().color();
    }

    static int biomeAltitude(int discreteAlt) {
        return (switch (discreteAlt) {
            default -> BA_LOW;
            case 1 -> BA_MEDIUM;
            case 2 -> BA_HIGH;
            case 3 -> BA_MOUNTAIN;
        }).get().color();
    }

    static int rockType(int rock) {
        return (switch (rock & 0b11) {
            default -> RT_OCEANIC;
            case 1 -> RT_VOLCANIC;
            case 2 -> RT_LAND;
            case 3 -> RT_UPLIFT;
        }).get().gradient().applyAsInt(nextWithSeed(rock >> 2));
    }

    // Default/reference gradients
    public static final DoubleToIntFunction blue = linearGradient(color(255, 150, 50, 50), color(255, 255, 140, 100));
    public static final DoubleToIntFunction green = linearGradient(color(255, 0, 100, 0), color(255, 80, 200, 80));
    public static final DoubleToIntFunction volcanic = value -> color(255, 100, (int) (100 * value), 200);
    public static final DoubleToIntFunction uplift = value -> color(255, 200, (int) (180 * value), 180);
    public static final DoubleToIntFunction climate = multiLinearGradient(
            color(255, 240, 20, 180),
            color(255, 240, 180, 0),
            color(255, 220, 180, 180),
            color(255, 0, 210, 210),
            color(255, 60, 120, 200),
            color(255, 40, 40, 200)
    );
    public static final DoubleToIntFunction grayscale = linearGradient(color(255, 255, 255, 255), color(255, 0, 0, 0));

    // Color keys
    public static final CacheableSupplier<Component> RainKey = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        RAINFALL.get().appendTo(key);
        FILL_OCEAN.get().appendTo(key, true);
        return key;
    });
    public static final CacheableSupplier<Component> TempKey = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        TEMPERATURE.get().appendTo(key);
        FILL_OCEAN.get().appendTo(key, true);
        return key;
    });
    public static final CacheableSupplier<Component> BiomeAltKey = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        baKey(key);
        return key;
    });
    public static final CacheableSupplier<Component> InlandHeightKey = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        IH_LAND.get().appendTo(key);
        IH_SHALLOW.get().appendTo(key);
        IH_DEEP.get().appendTo(key);
        IH_VERY_DEEP.get().appendTo(key, true);
        return key;
    });
    public static final CacheableSupplier<Component> RiverKey = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        RM_RIVER.get().appendTo(key);
        RM_OCEANIC_VOLCANIC_MOUNTAINS.get().appendTo(key);
        RM_INLAND_MOUNTAIN.get().appendTo(key);
        RM_LAKE.get().appendTo(key);
        baKey(key);
        return key;
    });
    public static final CacheableSupplier<Component> RockTypeKey = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        RT_LAND.get().appendTo(key);
        RT_OCEANIC.get().appendTo(key);
        RT_VOLCANIC.get().appendTo(key);
        RT_UPLIFT.get().appendTo(key, true);
        return key;
    });

    private static void baKey(MutableComponent key) {
        BA_LOW.get().appendTo(key);
        BA_MEDIUM.get().appendTo(key);
        BA_HIGH.get().appendTo(key);
        BA_MOUNTAIN.get().appendTo(key);
        FILL_OCEAN.get().appendTo(key, true);
    }
}
