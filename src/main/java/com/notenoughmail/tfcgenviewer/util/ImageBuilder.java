package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.notenoughmail.tfcgenviewer.Config;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.dries007.tfc.world.region.RegionGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

// Unless otherwise stated, everything here uses ABGR color
public class ImageBuilder {

    private static DynamicTexture PREVIEW;
    private static final ResourceLocation PREVIEW_LOCATION = TFCGenViewer.identifier("preview");

    public static ResourceLocation getPreview() {
        if (PREVIEW == null) {
            PREVIEW = new DynamicTexture(previewSize(), previewSize(), false);
            Minecraft.getInstance().getTextureManager().register(PREVIEW_LOCATION, PREVIEW);
        }
        return PREVIEW_LOCATION;
    }

    public static DoubleToIntFunction linearGradientRGB(int from, int to) {
        return value -> ARGB32.lerp((float) value, from, to);
    }

    public static DoubleToIntFunction linearGradient(int from, int to) {
        return value -> ABGR32.color(
                Mth.lerpInt((float) value, ABGR32.alpha(from), ABGR32.alpha(to)),
                Mth.lerpInt((float) value, ABGR32.blue(from), ABGR32.blue(to)),
                Mth.lerpInt((float) value, ABGR32.green(from), ABGR32.green(to)),
                Mth.lerpInt((float) value, ABGR32.red(from), ABGR32.red(to))
        );
    }

    public static DoubleToIntFunction multiLinearGradientRGB(int... colors) {
        final DoubleToIntFunction[] parts = IntStream.range(0, colors.length - 1)
                .mapToObj(i -> linearGradientRGB(colors[i], colors[i + 1]))
                .toArray(DoubleToIntFunction[]::new);
        return value -> parts[Mth.floor(value * parts.length)].applyAsInt((value * parts.length) % 1);
    }

    public static DoubleToIntFunction multiLinearGradient(int... colors) {
        final DoubleToIntFunction[] parts = IntStream.range(0, colors.length - 1)
                .mapToObj(i -> linearGradient(colors[i], colors[i + 1]))
                .toArray(DoubleToIntFunction[]::new);
        return value -> parts[Mth.floor(value * parts.length)].applyAsInt((value * parts.length) % 1);
    }

    public static int rgbTobgr(int rgb) {
        return ABGR32.color(ARGB32.alpha(rgb), ARGB32.blue(rgb), ARGB32.green(rgb), ARGB32.red(rgb));
    }

    public static int bgrTorgb(int bgr) {
        return ARGB32.color(ABGR32.alpha(bgr), ABGR32.red(bgr), ABGR32.green(bgr), ABGR32.blue(bgr));
    }
    
    @Nullable
    private static Integer PREVIEW_SIZE;

    public static int previewSize() {
        if (PREVIEW_SIZE == null) {
            PREVIEW_SIZE = Config.visualizeSize.get();
        }
        return PREVIEW_SIZE;
    }

    public static final int RIVER_BLUE = ABGR32.color(255, 250, 210, 100);
    public static final int NULL_POINT = ABGR32.color(255, 160, 160, 160);
    public static final int OUTSIDE_REGION = ABGR32.color(255, 100, 100, 100);
    public static final int CLEAR = 0x00000000;
    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;
    public static final DoubleToIntFunction blue = linearGradient(ABGR32.color(255, 150, 50, 50), ABGR32.color(255, 255, 140, 100));
    public static final DoubleToIntFunction green = linearGradient(ABGR32.color(255, 0, 100, 0), ABGR32.color(255, 80, 200, 80));
    public static final DoubleToIntFunction VOLCANIC_ROCK = value -> ABGR32.color(255, 100, (int) (100 * value), 200);
    public static final DoubleToIntFunction UPLIFT_ROCK = value -> ABGR32.color(255, 200, (int) (180 * value), 180);

    public static void build(RegionGenerator generator, VisualizeTask visualizer) {
        final NativeImage image = new NativeImage(previewSize(), previewSize(), false);
        for (RegionGenerator.Task task : RegionGenerator.Task.values()) {
            if (visualizer.taskApplies(task)) {
                for (int x = 0; x < previewSize(); x++) {
                    for (int y = 0; y < previewSize(); y++) {
                        visualizer.draw(x, y, task, generator, image);
                    }
                }
            }
        }
        drawLine(image, 0, 0, previewSize(), previewSize(), WHITE);
        PREVIEW.setPixels(image);
        PREVIEW.upload();
    }

    static void setPixel(NativeImage image, int x, int y, int color) {
        if (!image.isOutsideBounds(x, y)) {
            final int alpha = ABGR32.alpha(color);
            if (alpha == 255) {
                image.setPixelRGBA(x, y, color);
            } else if (alpha != 0) {
                image.blendPixel(x, y, color);
            }
        }
    }

    // TODO: Fix
    static void fillRect(NativeImage image, int x0, int y0, int x1, int y1, int color) {
        final int minX = Math.min(x0, x1);
        final int dx = Math.abs(x0 - x1);
        final int minY = Math.min(y0, y1);
        final int dy = Math.abs(y0 - y1);
        for (int x = minX ; x < dx + minX ; x++) {
            for (int y = minY ; y < dy + minY ; y++) {
                setPixel(image, x, y, color);
            }
        }
    }

    static void drawLine(NativeImage image, int x0, int y0, int x1, int y1, int color) {
        drawLine(image, x0, y0, x1, y1, 1, color);
    }

    // TODO: Fix
    static void drawLine(NativeImage image, int x0, int y0, int x1, int y1, int width, int color) {
        final int minX = Math.min(x0, x1);
        final int dx = Math.abs(x0 - x1);
        final int minY = Math.min(y0, y1);
        final int dy = Math.abs(y0 - y1);
        final int maxY = minY + dy;
        for (int x = minX ; x < minX + dx ; x++) {
            final int yCenter = Mth.lerpInt((float) (x - minX) / dx, minY, maxY);
            if (width == 0 || width == 1) {
                setPixel(image, x, yCenter, color);
            } else {
                final int hWidth = width / 2;
                for (int y = yCenter - hWidth ; y < yCenter + hWidth ; y++) {
                    setPixel(image, x, y, color);
                }
            }
        }
    }
}
