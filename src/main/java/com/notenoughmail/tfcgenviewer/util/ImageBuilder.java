package com.notenoughmail.tfcgenviewer.util;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.NativeImage;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.config.color.Colors;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.minecraft.util.FastColor.ABGR32.alpha;
import static net.minecraft.util.FastColor.ABGR32.color;

public class ImageBuilder {

    private static final AtomicInteger POOL_THREAD_COUNTER = new AtomicInteger(0);

    private static final ForkJoinPool GENERATOR_THREAD_POOL = Util.make(() -> {
        // Ensure the processBuilder is run on forge's class loader so TFC classes can be found
        final ClassLoader classLoader = TFCGenViewer.class.getClassLoader();
        return new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors()) - 2, fjp -> {
            final ForkJoinWorkerThread thread = new ForkJoinWorkerThread(fjp) {};
            thread.setContextClassLoader(classLoader);
            thread.setName("TFCGenViewer Generation Thread #%s".formatted(POOL_THREAD_COUNTER.getAndIncrement()));
            return thread; // Kill thread after 5 seconds, else default values
        }, null, true, 0, 0x7FFF, 1, null, 5L, TimeUnit.SECONDS);
    });

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
        clearPreviews();
        final DynamicTexture preview = PREVIEWS[scale];
        preview.setPixels(image);
        preview.upload();
    }

    private static void clearPreviews() {
        for (int i = 0 ; i < 7 ; i++) {
            final DynamicTexture preview = PREVIEWS[i];
            // If someone is really "lucky" the game will attempt to render an image which has been closed via this
            // If they decide to change this config mid-generation and it explodes, that's on them tbh
            if (!Config.useThrobber.get() && preview.getPixels() == currentImage) continue;
            // Free the previously used images from memory
            preview.setPixels(null);
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
            int xCenterGrids,
            int zCenterGrids,
            boolean drawSpawn,
            int spawnDistBlocks,
            int spawnXBlocks,
            int spawnZBlocks,
            int scale,
            Consumer<PreviewInfo> infoReturn
    ) {
        if (Config.useThrobber.get()) {
            infoReturn.accept(PreviewInfo.EMPTY);
            clearPreviews();
        }
        // TODO: How did this ever work when the throbber was turned off?
        if (currentImage != null) {
            currentImage.close();
            currentImage = null;
        }
        cancelRunning();
        builderProcess = CompletableFuture.supplyAsync(() -> {
            final Stopwatch timer = Stopwatch.createStarted();
            final int previewSizeGrids = previewSize(scale);

            final NativeImage image = new NativeImage(previewSizeGrids, previewSizeGrids, false);
            transientImage = image;
            final Set<Region> visitedRegions = new HashSet<>();
            final int halfPreviewGrids = previewSizeGrids >> 1;
            final int xDrawOffsetGrids = xCenterGrids - halfPreviewGrids;
            final int zDrawOffsetGrids = zCenterGrids - halfPreviewGrids;

            // TODO: [Important] Eliminate region duplication
            for (int x = 0; x < previewSizeGrids; x++) {
                for (int y = 0; y < previewSizeGrids; y++) {
                    // Shift the generation by the offsets and
                    // subtract half preview to center the image
                    // relative to 0,0
                    final int xPos = x + xDrawOffsetGrids;
                    final int zPos = y + zDrawOffsetGrids;
                    final Region region = generator.regionGenerator().getOrCreateRegion(xPos, zPos);
                    visitedRegions.add(region);
                    final Region.Point point = generator.regionGenerator().getOrCreateRegionPoint(xPos, zPos);
                    visualizer.draw(x, y, xPos, zPos, generator, region, point, image);
                }
            }

            if (drawSpawn) {
                final int xSpawnCenterGrids = (spawnXBlocks / (16 * 8)) - xDrawOffsetGrids;
                final int zSpawnCenterGrids = (spawnZBlocks / (16 * 8)) - zDrawOffsetGrids;
                final int radiusGrids = spawnDistBlocks / (16 * 8);

                final int lineWidthPixels = lineWidth(scale);

                hLine(image, xSpawnCenterGrids - radiusGrids, xSpawnCenterGrids + radiusGrids, zSpawnCenterGrids + radiusGrids, lineWidthPixels, Colors.spawnBorder().color());
                hLine(image, xSpawnCenterGrids - radiusGrids, xSpawnCenterGrids + radiusGrids, zSpawnCenterGrids - radiusGrids, lineWidthPixels, Colors.spawnBorder().color());
                vLine(image, zSpawnCenterGrids - radiusGrids, zSpawnCenterGrids + radiusGrids, xSpawnCenterGrids + radiusGrids, lineWidthPixels, Colors.spawnBorder().color());
                vLine(image, zSpawnCenterGrids - radiusGrids, zSpawnCenterGrids + radiusGrids, xSpawnCenterGrids - radiusGrids, lineWidthPixels, Colors.spawnBorder().color());

                final int length = Math.min(radiusGrids / 4, previewSizeGrids / 12);
                hLine(image, xSpawnCenterGrids - length, xSpawnCenterGrids + length, zSpawnCenterGrids, lineWidthPixels, Colors.getSpawnReticule().color());
                vLine(image, zSpawnCenterGrids - length, zSpawnCenterGrids + length, xSpawnCenterGrids, lineWidthPixels, Colors.getSpawnReticule().color());
            }

            if (!FMLEnvironment.production) {
                final List<Integer> regions = new ArrayList<>();
                int duplicatesCount = 0;
                for (Region region : visitedRegions) {
                    final int color = color(255, region.hashCode());
                    final int regionNum = region.minX() + (region.minZ() * region.sizeX()); // Should be relatively unique, not that false positives are all that common
                    if (regions.contains(regionNum)) {
                        TFCGenViewer.LOGGER.warn("Found duplicate region at ({},{})", region.minX() << 7, region.minZ() << 7); // Convert to blocks
                        duplicatesCount++;
                    }
                    regions.add(regionNum);

                    hLine(image, region.minX() - xDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, 0, color);
                    hLine(image, region.minX() - xDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, region.minZ() - zDrawOffsetGrids, 0, color);

                    vLine(image, region.minZ() - zDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, 0, color);
                    vLine(image, region.minZ() - zDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, region.minX() - xDrawOffsetGrids, 0, color);
                }
                TFCGenViewer.LOGGER.info("Found {} duplicate regions, there are only {} actual regions present", duplicatesCount, visitedRegions.size() - duplicatesCount);
            }

            final String previewKm = previewSizeKm(scale);
            timer.stop();
            return new ProcessReturn(
                    new PreviewInfo(
                        Component.translatable(
                                "tfcgenviewer.preview_world.preview_info",
                                visitedRegions.size(),
                                "%.1f".formatted(timer.elapsed(TimeUnit.MILLISECONDS) / 1000F),
                                previewKm,
                                previewKm,
                                xCenterGrids * 128,
                                zCenterGrids * 128,
                                visualizer.getName(),
                                visualizer.getColorKey()
                        ),
                        PREVIEW_LOCATIONS[scale],
                        previewSizeGrids * 128,
                        xDrawOffsetGrids * 128,
                        zDrawOffsetGrids * 128
                    ),
                    image,
                    "%s_%dx%d_%d_%s.png".formatted(Util.getFilenameFormattedDateTime(), previewSizeGrids, previewSizeGrids, visitedRegions.size(), visualizer.name())
            );
        }, GENERATOR_THREAD_POOL).exceptionally(thr -> {
            if (!(thr instanceof CompletionException compExc && compExc.getCause() instanceof IllegalStateException ise && "Image is not allocated.".equals(ise.getMessage()))) {
                // #cancelRunning() closes the transient image, this may happen before the builderProcess is fully finished
                // Thus, this specific case the error can be ignored, as it is known and wanted, even if a bit ugly
                TFCGenViewer.LOGGER.error("Error encountered during generation!", thr);
            }
            return null;
        }).thenAccept(pr -> {
            transientImage = null;
            if (pr != null) {
                currentImage = pr.currentImage();
                imageName = pr.imageName();
                upload(scale, currentImage);
                infoReturn.accept(pr.previewInfo());
            } else {
                currentImage = null;
                infoReturn.accept(PreviewInfo.ERROR);
                clearPreviews();
            }
            if (Config.dingWhenGenerated.get()) {
                Minecraft
                        .getInstance()
                        .getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.ARROW_HIT_PLAYER, 1.0F));
            }
            builderProcess = null;
        });
    }

    private static void cancelRunning() {
        if (builderProcess != null) {
            builderProcess.cancel(true);
            builderProcess = null;
        }
        if (transientImage != null) {
            transientImage.close();
            transientImage = null;
        }
        if (currentImage != null) {
            currentImage.close();;
            currentImage = null;
        }
    }

    public static void cancelAndClearPreviews() {
        if (builderProcess != null && !builderProcess.isDone()) {
            // A very rare error can happen when a build process finishes and the previews are cleared
            // at just the right time where the screen will attempt to display a freed image
            // The builder already clears the previews when it finishes so this should be fine
            clearPreviews();
        }
        cancelRunning();
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
