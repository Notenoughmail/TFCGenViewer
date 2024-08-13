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
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraft.util.FastColor.ABGR32.alpha;

public class ImageBuilder {

    public static final ResourceLocation THROBBER = TFCGenViewer.identifier("textures/gui/throbber.png");

    private static final ResourceLocation[] PREVIEW_LOCATIONS = Util.make(new ResourceLocation[7], array -> {
        for (int i = 0 ; i < 7 ; i++) {
            array[i] = TFCGenViewer.identifier("preview/" + i);
        }
    });

    private static final DynamicTexture[] PREVIEWS = Util.make(new DynamicTexture[7], array -> {
        for (int i = 0 ; i < 7 ; i++) {
            final int size = previewSize(i);
            try {
                // Do not try-with-resources or #close() these
                DynamicTexture texture = new DynamicTexture(size, size, false);
                array[i] = texture;
                Minecraft.getInstance().getTextureManager().register(PREVIEW_LOCATIONS[i], texture);
            } catch (Exception exception) {
                TFCGenViewer.LOGGER.error("Could not make dynamic texture for size {} (scale {})! Error:\n{}", size, i, exception);
            }
        }
    });

    private static void upload(int scale, NativeImage image) {
        final DynamicTexture preview = PREVIEWS[scale];
        preview.setPixels(image);
        preview.upload();
    }

    private static void clearPreviews() {
        for (int i = 0 ; i < 7 ; i++) {
            // Free the previously used images from memory
            PREVIEWS[i].setPixels(null);
        }
    }

    /**
     * Gets the preview size in grids from the scale option (0-6)
     */
    public static int previewSize(int scale) {
        return 2 << (scale + 4); // == Math.pow(2, scale + 5)
    }

    public static int lineWidth(int scale) {
        return previewSize(scale) >> 9; // == previewSize(scale) / 128
    }

    public static String previewSizeKm(int scale) {
        return "%.1f".formatted(previewSize(scale) * 128 / 1000F);
    }


    private static NativeImage currentImage, transientImage;
    private static String imageName;
    private static CompletableFuture<Void> builderProcess;

    public static void build(
            RegionChunkDataGenerator generator,
            VisualizerType visualizer,
            int xOffsetGrids,
            int yOffsetGrids,
            boolean drawSpawn,
            int spawnDistBlocks,
            int spawnXBlocks,
            int spawnYBlocks,
            int scale,
            Consumer<PreviewInfo> infoReturn
    ) {
        if (Config.useThrobber.get()) {
            infoReturn.accept(PreviewInfo.EMPTY);
            clearPreviews();
        }
        if (currentImage != null) {
            currentImage.close();
            currentImage = null;
        }
        cancelRunning();
        builderProcess = CompletableFuture.supplyAsync(() -> {
            final long start = System.currentTimeMillis();
            final int previewSizeGrids = previewSize(scale);

            final NativeImage image = new NativeImage(previewSizeGrids, previewSizeGrids, false);
            transientImage = image;
            final Set<Region> visitedRegions = new HashSet<>();
            final int halfPreviewGrids = previewSizeGrids >> 1;
            final int xDrawOffsetGrids = -xOffsetGrids - halfPreviewGrids;
            final int yDrawOffsetGrids = -yOffsetGrids - halfPreviewGrids;

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
                final int xCenterGrids = (spawnXBlocks / (16 * 8)) - xDrawOffsetGrids;
                final int yCenterGrids = (spawnYBlocks / (16 * 8)) - yDrawOffsetGrids;
                final int radiusGrids = spawnDistBlocks / (16 * 8);

                final int lineWidthPixels = lineWidth(scale);

                hLine(image, xCenterGrids - radiusGrids, xCenterGrids + radiusGrids, yCenterGrids + radiusGrids, lineWidthPixels, Colors.spawnBorder().color());
                hLine(image, xCenterGrids - radiusGrids, xCenterGrids + radiusGrids, yCenterGrids - radiusGrids, lineWidthPixels, Colors.spawnBorder().color());
                vLine(image, yCenterGrids - radiusGrids, yCenterGrids + radiusGrids, xCenterGrids + radiusGrids, lineWidthPixels, Colors.spawnBorder().color());
                vLine(image, yCenterGrids - radiusGrids, yCenterGrids + radiusGrids, xCenterGrids - radiusGrids, lineWidthPixels, Colors.spawnBorder().color());

                final int length = Math.min(radiusGrids / 4, previewSizeGrids / 12);
                hLine(image, xCenterGrids - length, xCenterGrids + length, yCenterGrids, lineWidthPixels, Colors.getSpawnReticule().color());
                vLine(image, yCenterGrids - length, yCenterGrids + length, xCenterGrids, lineWidthPixels, Colors.getSpawnReticule().color());
            }

            final String previewKm = previewSizeKm(scale);
            return new ProcessReturn(
                    new PreviewInfo(
                        Component.translatable(
                                "tfcgenviewer.preview_world.preview_info",
                                visitedRegions.size(),
                                "%.1f".formatted((System.currentTimeMillis() - start) / 1000F),
                                previewKm,
                                previewKm,
                                xOffsetGrids * 128,
                                yOffsetGrids * 128,
                                visualizer.getName(),
                                visualizer.getColorKey()
                        ),
                        PREVIEW_LOCATIONS[scale],
                        previewSizeGrids * 128,
                        xDrawOffsetGrids * 128,
                        yDrawOffsetGrids * 128
                    ),
                    image,
                    "%s_%dx%d_%d_%s.png".formatted(Util.getFilenameFormattedDateTime(), previewSizeGrids, previewSizeGrids, visitedRegions.size(), visualizer.name())
            );
        }).thenAccept(pr -> {
            clearPreviews();
            transientImage = null;
            infoReturn.accept(pr.previewInfo());
            currentImage = pr.currentImage();
            imageName = pr.imageName();
            upload(scale, currentImage);
            // TODO: Play ding sound on completion?
        });
    }

    private static void cancelRunning() {
        if (builderProcess != null) {
            builderProcess.cancel(true);
        }
        if (transientImage != null) {
            transientImage.close();
        }
    }

    public static void cancelAndClearPreviews() {
        cancelRunning();
        clearPreviews();
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

    public static void exportImage() {
        if (currentImage != null) {
            try {
                currentImage.writeToFile(new File(FMLPaths.getOrCreateGameRelativePath(Path.of("screenshots", "tfcgenviewer")).toFile(), imageName));
            } catch (Exception exception) {
                TFCGenViewer.LOGGER.error("Unable to write preview %s to disk!".formatted(imageName), exception);
            }
        }
    }

    private record ProcessReturn(PreviewInfo previewInfo, NativeImage currentImage, String imageName) {}
}
