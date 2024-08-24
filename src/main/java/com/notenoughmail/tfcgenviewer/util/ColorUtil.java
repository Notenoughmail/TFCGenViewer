package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

import static net.minecraft.util.FastColor.ABGR32.*;

public class ColorUtil {

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

    // Default colors
    public static final int RIVER_BLUE = color(255, 250, 180, 100);
    public static final int VOLCANIC_MOUNTAIN = color(255, 50, 110, 240);
    public static final int GRAY = color(255, 150, 150, 150);
    public static final int DARK_GRAY = color(255, 50, 50, 50);
    public static final int SPAWN_RED = color(255, 48, 15, 198);

    public static final int SHALLOW_WATER = color(255, 255, 160, 150);
    public static final int DEEP_WATER = color(255, 240, 120, 120);
    public static final int VERY_DEEP_WATER = color(255, 200, 100, 100);

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
}
