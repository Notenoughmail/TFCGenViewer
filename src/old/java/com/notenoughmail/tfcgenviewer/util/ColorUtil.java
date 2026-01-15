package com.notenoughmail.tfcgenviewer.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.region.Region;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.Random;
import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

import static com.notenoughmail.tfcgenviewer.color.Colors.*;
import static net.minecraft.util.FastColor.ABGR32.*;

public class ColorUtil {

    public static final Random COLOR_GENERATOR = new Random(System.nanoTime() ^ System.currentTimeMillis());

    // A blank region for use when the region generator produces nonsense, which happens on occasion with 262km
    // Attempting to travel to a location represented by the failure state will result in a JVM crash
    public static final Region.Point FAILURE_STATE = new Region.Point();

    // Actual utils
    /**
     * Converts an 8-bit RGB channel into its equivalent linear sRGB value using an approximate gamma value of {@code 2.2}
     * @param channel The RGB component, in the range [{@code 0x00}, {@code 0xFF}]
     * @return The linear sRGB value, in the range [{@code 0}, {@code 1}]
     */
    public static double linearize(int channel) {
        return Math.pow((double) (channel & 0xFF) / 0xFF, 2.2D);
    }

    /**
     * Converts a linear sRGB value into its equivalent 8-bit RGB value using an approximate gamma value of {@code 2.2}
     * @param channel The RGB component, in the range [{@code 0}, {@code 1}]
     * @return The 8-bit RGB value, in the range [{@code 0x00}, {@code 0xFF}]
     */
    public static int delinearize(double channel) {
        return 0xFF & (int) (0xFF * Math.pow(channel, 1D / 2.2));
    }

    /**
     * Creates an interpolation between the given colors in the linear sRGB color space, returned colors are in ABGR form and always have an alpha value of {@code 0xFF}
     * @param bgr0 The first BGR color, the start of the lerp
     * @param bgr1 The second BGR color, the end of the lerp
     * @return A {@link DoubleToIntFunction} that lerps between the two colors
     */
    public static DoubleToIntFunction linearGradient(int bgr0, int bgr1) {
        final double
                r0 = linearize(red(bgr0)),
                r1 = linearize(red(bgr1)),
                g0 = linearize(green(bgr0)),
                g1 = linearize(green(bgr1)),
                b0 = linearize(blue(bgr0)),
                b1 = linearize(blue(bgr1));
        return value -> color(
                255,
                delinearize(Mth.lerp(value, b0, b1)),
                delinearize(Mth.lerp(value, g0, g1)),
                delinearize(Mth.lerp(value, r0, r1))
        );
    }

    // TODO: 1.21.1 | Rework this to make the below comment obsolete
    /*
    * This does not work if the input value is exactly 1 (due to the flooring of the value * parts.length)
    * this is dealt with by clamping things 0.999 and ignoring it
    * Something could be done to fix that, but doing so in a way that doesn't change the semantics of the
    * returned gradient isn't worth the time
    */
    public static DoubleToIntFunction multiLinearGradient(int... colors) {
        final DoubleToIntFunction[] parts = IntStream.range(0, colors.length - 1)
                .mapToObj(i -> linearGradient(colors[i], colors[i + 1]))
                .toArray(DoubleToIntFunction[]::new);
        return value -> parts[Mth.floor(value * parts.length)].applyAsInt((value * parts.length) % 1);
    }

    public static int rgb2bgr(int rgb) {
        return color(
                255,
                FastColor.ARGB32.blue(rgb),
                FastColor.ARGB32.green(rgb),
                FastColor.ARGB32.red(rgb)
        );
    }

    public static int bgr2rgb(int bgr) {
        return FastColor.ARGB32.color(
                255,
                red(bgr),
                green(bgr),
                blue(bgr)
        );
    }

    public static int randomColor() {
        return FastColor.ABGR32.color(
                255,
                COLOR_GENERATOR.nextInt()
        );
    }

    // Drawers
    static final VisualizerType.DrawFunction fillOcean = (x, y, xOffset, yOffset, generator, region, point, image, colorDescriptors, registryAccess) ->
            image.setPixel(
                    x, y,
                    FILL_OCEAN.get().getColor(
                            region != null ?
                                    region.noise() / 2 :
                                    0,
                            colorDescriptors
                    )
            );

    // Color getters that are not complex but also not easily (or cleanly) made single line
    static int inlandHeight(Region.Point point, Int2ObjectOpenHashMap<Component> colorDescriptors) {
        if (point.land()) {
            return IH_LAND.get().getColor(point.baseLandHeight / 24F, colorDescriptors);
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
        ).get().color(colorDescriptors);
    }

    // Default/reference gradients
    public static final DoubleToIntFunction blue = linearGradient(0xFF963232, 0xFFFF8C64);
    public static final DoubleToIntFunction green = linearGradient(0xFF006400, 0xFF50C850);
    public static final DoubleToIntFunction volcanic = value -> color(
            0xFF,
            0x64,
            delinearize(value * 0.1264363868D), // 0x64 linearized
            0xC8
    );
    public static final DoubleToIntFunction uplift = value -> color(
            0xFF,
            0xC8,
            delinearize(value * 0.4607566240D), // 0xB4 linearized
            0xB4
    );
    public static final DoubleToIntFunction legacy_climate = multiLinearGradient(
            0xFFF014B4,
            0xFFF0B400,
            0xFFDCB4B4,
            0xFF00D2D2,
            0xFF3C78C8,
            0xFF2828C8
    );
    public static final DoubleToIntFunction rainfall = multiLinearGradient(
            0xFF000287,
            0xFF0032FF,
            0xFF00A0FF,
            0xFF78E8FF,
            0xFF0FA00F,
            0xFFD26414,
            0xFFFAB978
    );
    public static final DoubleToIntFunction temperature = multiLinearGradient(
            0xFFFF1D00,
            0xFFFFBB00,
            0xFF94FF63,
            0xFF13FFE4,
            0xFF0079FF,
            0xFF0000D1
    );
    public static final DoubleToIntFunction grayscale = linearGradient(0xFFFFFFFF, 0xFF000000);

    // Color keys
    public static final CacheableSupplier<Component> RainKey = CacheableSupplier.of(() -> {
        final MutableComponent key = Component.empty();
        RAINFALL.get().appendTo(key);
        FILL_OCEAN.get().appendTo(key, true);
        return key;
    });
    public static final CacheableSupplier<Component> TempKey = CacheableSupplier.of(() -> {
        final MutableComponent key = Component.empty();
        TEMPERATURE.get().appendTo(key);
        FILL_OCEAN.get().appendTo(key, true);
        return key;
    });
    public static final CacheableSupplier<Component> BiomeAltKey = CacheableSupplier.of(() -> {
        final MutableComponent key = Component.empty();
        baKey(key);
        return key;
    });
    public static final CacheableSupplier<Component> InlandHeightKey = CacheableSupplier.of(() -> {
        final MutableComponent key = Component.empty();
        IH_LAND.get().appendTo(key);
        IH_SHALLOW.get().appendTo(key);
        IH_DEEP.get().appendTo(key);
        IH_VERY_DEEP.get().appendTo(key, true);
        return key;
    });
    public static final CacheableSupplier<Component> RiverKey = CacheableSupplier.of(() -> {
        final MutableComponent key = Component.empty();
        RM_RIVER.get().appendTo(key);
        RM_OCEANIC_VOLCANIC_MOUNTAINS.get().appendTo(key);
        RM_INLAND_MOUNTAIN.get().appendTo(key);
        RM_LAKE.get().appendTo(key);
        baKey(key);
        return key;
    });
    public static final CacheableSupplier<Component> RockTypeKey = CacheableSupplier.of(() -> {
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
