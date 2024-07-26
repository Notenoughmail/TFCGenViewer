package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

import static net.minecraft.util.FastColor.ABGR32.*;

// Unless otherwise stated, everything here uses ABGR color
public class ImageBuilder {

    private static final ResourceLocation[] PREVIEW_LOCATIONS = Util.make(new ResourceLocation[7], array -> {
        for (int i = 0 ; i < 7 ; i++) {
            array[i] = TFCGenViewer.identifier("preview/" + i);
        }
    });

    // TODO: Fix the images being broken now for some reason
    private static DynamicTexture[] PREVIEWS;
    private static DynamicTexture PREVIEW;

    public static ResourceLocation getPreview(int scale) {
        if (PREVIEW == null) {
            // REMOVE THIS
            PREVIEW = new DynamicTexture(256, 256, false);
            Minecraft.getInstance().getTextureManager().register(PREVIEW_LOCATIONS[0], PREVIEW);
        }
        return PREVIEW_LOCATIONS[0];
    }

    public static void initPreviews() {
        getPreview(3);
        if (PREVIEWS == null) {
            PREVIEWS = Util.make(new DynamicTexture[7], array -> {
                for (int i = 0 ; i < 7 ; i++) {
                    final int scale = previewSize(i);
                    try (DynamicTexture texture = new DynamicTexture(scale, scale, false)) {
                        array[i] = texture;
                        Minecraft.getInstance().getTextureManager().register(PREVIEW_LOCATIONS[i], array[i]);
                    } catch (Exception exception) {
                        TFCGenViewer.LOGGER.error("Could not make dynamic texture for scale {}! Error:\n{}", scale, exception);
                    }
                }
            });
        }
    }

    private static void upload(int scale, NativeImage image) {
        if (scale == 3) {
            // REMOVE THIS
            PREVIEW.setPixels(image);
            PREVIEW.upload();
        } else {
            for (int i = 0; i < 7; i++) {
                PREVIEWS[i].close();
            }
            PREVIEWS[scale].setPixels(image);
            PREVIEWS[scale].upload();
        }
    }

    /**
     * Gets the preview size in grids from the scale option (0-6)
     */
    public static int previewSize(int scale) {
        return (int) Math.pow(2, scale + 5);
    }

    public static int lineWidth(int scale) {
        return previewSize(scale) / 512;
    }

    public static String previewSizeKm(int scale) {
        return "%.1f".formatted(previewSize(scale) * 128 / 1000F);
    }


    private static final boolean exportImages = true;

    public static PreviewInfo build(
            RegionChunkDataGenerator generator,
            VisualizerType visualizer,
            int xOffsetGrids,
            int yOffsetGrids,
            boolean drawSpawn,
            int spawnDistBlocks,
            int spawnXBlocks,
            int spawnYBlocks,
            int scale
    ) {
        final int previewSizeGrids = previewSize(scale);

        final NativeImage image = new NativeImage(previewSizeGrids, previewSizeGrids, false);
        final Set<Region> visitedRegions = new HashSet<>();
        final int halfPreviewGrids = previewSizeGrids / 2;
        final int xDrawOffsetGrids = xOffsetGrids - halfPreviewGrids;
        final int yDrawOffsetGrids = yOffsetGrids - halfPreviewGrids;

        for (int x = 0; x < previewSizeGrids; x++) {
            for (int y = 0; y < previewSizeGrids; y++) {
                // Shift the generation by the offsets and
                // subtract half preview to center the image
                // relative to 0,0
                final int xPos = x + xDrawOffsetGrids;
                final int yPos = y + yDrawOffsetGrids;
                final Region region = generator.regionGenerator().getOrCreateRegion(xPos, yPos);
                visitedRegions.add(region);
                final Region.Point point = generator.regionGenerator().getOrCreateRegionPoint(xPos, yPos);
                visualizer.draw(x, y, xPos, yPos, generator, region, point, image);
            }
        }

        if (drawSpawn) {
            final int xCenterGrids = (spawnXBlocks / (16 * 8)) - xOffsetGrids;
            final int yCenterGrids = (spawnYBlocks / (16 * 8)) - yOffsetGrids;
            final int radiusGrids = spawnDistBlocks / (16 * 8);

            final int lineWidthPixels = lineWidth(scale);

            // TODO: Make spawn overlay colors resourcepackable
            hLine(image, xCenterGrids - radiusGrids, xCenterGrids + radiusGrids, yCenterGrids + radiusGrids, lineWidthPixels, ColorUtil.DARK_GRAY);
            hLine(image, xCenterGrids - radiusGrids, xCenterGrids + radiusGrids, yCenterGrids - radiusGrids, lineWidthPixels, ColorUtil.DARK_GRAY);
            vLine(image, yCenterGrids - radiusGrids, yCenterGrids + radiusGrids, xCenterGrids + radiusGrids, lineWidthPixels, ColorUtil.DARK_GRAY);
            vLine(image, yCenterGrids - radiusGrids, yCenterGrids + radiusGrids, xCenterGrids - radiusGrids, lineWidthPixels, ColorUtil.DARK_GRAY);

            final int length = Math.min(radiusGrids / 4, previewSizeGrids / 12);
            hLine(image, xCenterGrids - length, xCenterGrids + length, yCenterGrids, lineWidthPixels, ColorUtil.SPAWN_RED);
            vLine(image, yCenterGrids - length, yCenterGrids + length, xCenterGrids, lineWidthPixels, ColorUtil.SPAWN_RED);
        }

        upload(scale, image);

        if (exportImages && !FMLLoader.isProduction()) {
            try {
                image.writeToFile(new File(FMLPaths.GAMEDIR.get().toFile(), String.format("screenshots\\preview_%s_%dx%d_%d@%s.png", visualizer.name(), previewSizeGrids, previewSizeGrids, visitedRegions.size(), Util.getFilenameFormattedDateTime())));
            } catch (IOException exception) {
                TFCGenViewer.LOGGER.warn("Unable to write preview to disk!", exception);
            }
        }

        final String previewKm = previewSizeKm(scale);
        // In blocks
        final int xCenter = xOffsetGrids * 128;
        final int yCenter = yOffsetGrids * 128;
        final int halfPreviewBlocks = halfPreviewGrids * 128;
        return new PreviewInfo(
                Component.translatable(
                        "tfcgenviewer.preview_world.preview_info",
                        visitedRegions.size(),
                        previewKm,
                        previewKm,
                        xCenter,
                        yCenter,
                        visualizer.getName(),
                        visualizer.getColorKey()
                ),
                scale,
                previewSizeGrids * 128,
                xCenter - halfPreviewBlocks,
                yCenter - halfPreviewBlocks
        );
    }

    public static void setPixel(NativeImage image, int x, int y, int color) {
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
