package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.config.Colors;
import com.notenoughmail.tfcgenviewer.config.Config;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

import static net.minecraft.util.FastColor.ABGR32.*;

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

    @Nullable
    private static Integer PREVIEW_SIZE;
    @Nullable
    private static Integer LINE_WIDTH;
    @Nullable
    private static String PREVIEW_SIZE_KM;

    public static int previewSize() {
        if (PREVIEW_SIZE == null) {
            PREVIEW_SIZE = Config.previewSize.get();
        }
        return PREVIEW_SIZE;
    }

    public static int lineWidth() {
        if (LINE_WIDTH == null) {
            LINE_WIDTH = previewSize() / 512;
        }
        return LINE_WIDTH;
    }

    public static String previewSizeKm() {
        if (PREVIEW_SIZE_KM == null) {
            PREVIEW_SIZE_KM = String.format("%.1f", previewSize() * 128 / 1000F);
        }
        return PREVIEW_SIZE_KM;
    }

    // Misc colors
    public static final int RIVER_BLUE = color(255, 250, 180, 100);
    public static final int VOLCANIC_MOUNTAIN = color(255, 50, 110, 240);
    public static final int GRAY = color(255, 150, 150, 150);
    public static final int DARK_GRAY = color(255, 50, 50, 50);
    public static final int SPAWN_RED = color(255, 48, 15, 198);

    // Ocean depth colors
    public static final int SHALLOW_WATER = color(255, 255, 160, 150);
    public static final int DEEP_WATER = color(255, 240, 120, 120);
    public static final int VERY_DEEP_WATER = color(255, 200, 100, 100);

    // Gradients
    public static final DoubleToIntFunction blue = linearGradient(color(255, 150, 50, 50), color(255, 255, 140, 100));
    public static final DoubleToIntFunction green = linearGradient(color(255, 0, 100, 0), color(255, 80, 200, 80));
    public static final DoubleToIntFunction VOLCANIC_ROCK = value -> color(255, 100, (int) (100 * value), 200);
    public static final DoubleToIntFunction UPLIFT_ROCK = value -> color(255, 200, (int) (180 * value), 180);
    public static final DoubleToIntFunction climate = multiLinearGradient(
            color(255, 240, 20, 180),
            color(255, 240, 180, 0),
            color(255, 220, 180, 180),
            color(255, 0, 210, 210),
            color(255, 60, 120, 200),
            color(255, 40, 40, 200)
    );

    public static final VisualizerType.DrawFunction fillOcean = (x, y, xOffset, yOffset, generator, region, point, image) -> setPixel(image, x, y, Colors.fillOcean().gradient().applyAsInt(region.noise() / 2));

    public static Component build(RegionChunkDataGenerator generator, VisualizerType visualizer, int xOffset, int yOffset, boolean drawSpawn, int spawnDist, int spawnX, int spawnY) {
        final NativeImage image = new NativeImage(previewSize(), previewSize(), false);
        final Set<Region> visitedRegions = new HashSet<>();
        for (int x = 0; x < previewSize(); x++) {
            for (int y = 0; y < previewSize(); y++) {
                final int xPos = x + xOffset;
                final int yPos = y + yOffset;
                final Region region = generator.regionGenerator().getOrCreateRegion(xPos, yPos);
                visitedRegions.add(region);
                final Region.Point point = generator.regionGenerator().getOrCreateRegionPoint(xPos, yPos);
                visualizer.draw(x, y, xPos, yPos, generator, region, point, image);
            }
        }

        if (drawSpawn) {
            final int xCenter = (spawnX / (16 * 8)) - xOffset;
            final int yCenter = (spawnY / (16 * 8)) - yOffset;
            final int radius = spawnDist / (16 * 8);
            hLine(image, xCenter - radius, xCenter + radius, yCenter + radius, lineWidth(), DARK_GRAY);
            hLine(image, xCenter - radius, xCenter + radius, yCenter - radius, lineWidth(), DARK_GRAY);
            vLine(image, yCenter - radius, yCenter + radius, xCenter + radius, lineWidth(), DARK_GRAY);
            vLine(image, yCenter - radius, yCenter + radius, xCenter - radius, lineWidth(), DARK_GRAY);

            final int length = Math.min(radius / 4, previewSize() / 12);
            hLine(image, xCenter - length, xCenter + length, yCenter, lineWidth(), SPAWN_RED);
            vLine(image, yCenter - length, yCenter + length, xCenter, lineWidth(), SPAWN_RED);
        }

        PREVIEW.setPixels(image);
        PREVIEW.upload();

        if (false && !FMLLoader.isProduction()) {
            try {
                image.writeToFile(new File(FMLPaths.GAMEDIR.get().toFile(), String.format("screenshots\\preview_%s_%dx%d@%s.png", visualizer.name(), previewSize(), previewSize(), Util.getFilenameFormattedDateTime())));
            } catch (IOException exception) {
                TFCGenViewer.LOGGER.warn("Unable to write preview to disk!", exception);
            }
        }

        return Component.translatable(
                "tfcgenviewer.preview_world.preview_info",
                visitedRegions.size(),
                ImageBuilder.previewSizeKm(),
                ImageBuilder.previewSizeKm(),
                (xOffset + (previewSize() / 2)) * 128,
                (yOffset + (previewSize() / 2)) * 128,
                visualizer.getName(),
                visualizer.getColorKey()
        );
    }

    static void setPixel(NativeImage image, int x, int y, int color) {
        final int alpha = alpha(color);
        if (alpha != 0) {
            if (!image.isOutsideBounds(x, y)) {
                if (alpha == 255) {
                    image.setPixelRGBA(x, y, color);
                } else {
                    image.blendPixel(x, y, color);
                }
            }
        }
    }

    static void hLine(NativeImage image, int x0, int x1, int y, int width, int color) {
        final int min = Math.min(x0, x1);
        final int max = Math.max(x0, x1);
        for (int x = min ; x < max ; x++) {
            if (width <= 0) {
                setPixel(image, x, y, color);
            } else {
                for (int i = y - width ; i < y + width ; i++) {
                    setPixel(image, x, i, color);
                }
            }
        }
    }

    static void vLine(NativeImage image, int y0, int y1, int x, int width, int color) {
        final int min = Math.min(y0, y1);
        final int max = Math.max(y0, y1);
        for (int y = min ; y < max ; y++) {
            if (width <= 0) {
                setPixel(image, x, y, color);
            } else {
                for (int i = x - width ; i < x + width ; i++) {
                    setPixel(image, i, y, color);
                }
            }
        }
    }
}
