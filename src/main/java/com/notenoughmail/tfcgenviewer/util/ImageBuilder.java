package com.notenoughmail.tfcgenviewer.util;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.NativeImage;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.config.color.Colors;
import com.notenoughmail.tfcgenviewer.util.custom.GeneratorPreviewException;
import net.dries007.tfc.util.Helpers;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

    private static final AtomicReference<BuilderState> BUILDER_STATE = new AtomicReference<>(BuilderState.OFF);

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

    public static BuilderState getState() {
        return BUILDER_STATE.get();
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

    // TODO: Sometimes, very rarely, the first created image will fail (?) or at least somehow break and cause a GFLW error to be printed to the console and show up completely empty | Fix that
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
            Consumer<PreviewInfo> infoReturn,
            Consumer<Integer> progressReturn,
            long seed // For error reports
    ) {
        if (BUILDER_STATE.get() == BuilderState.FINALIZE) {
            TFCGenViewer.LOGGER.warn("Apply was called while a previous builder was finalizing. In very special cases this can cause a crash, thus the previous builder will continue and the request for a new builder will be discarded");
            return;
        }
        BUILDER_STATE.set(BuilderState.SETUP);
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
            BUILDER_STATE.set(BuilderState.RUNNING);
            final Stopwatch timer = Stopwatch.createStarted();
            final int previewSizeGrids = previewSize(scale);

            final NativeImage image = new NativeImage(previewSizeGrids, previewSizeGrids, false);
            transientImage = image;
            final int halfPreviewGrids = previewSizeGrids >> 1;
            final int xDrawOffsetGrids = xCenterGrids - halfPreviewGrids;
            final int zDrawOffsetGrids = zCenterGrids - halfPreviewGrids;

            final Set<Region> visitedRegions = new HashSet<>();
            final Region[] cache = new Region[previewSizeGrids * previewSizeGrids];

            for (int x = 0; x < previewSizeGrids; x++) {
                progressReturn.accept(100 * x / previewSizeGrids);
                for (int y = 0; y < previewSizeGrids; y++) {
                    // Shift the generation by the offsets and
                    // subtract half preview to center the image
                    // relative to 0,0
                    final int xPos = x + xDrawOffsetGrids;
                    final int zPos = y + zDrawOffsetGrids;
                    final int cachePos = x * previewSizeGrids + y;
                    if (cache[cachePos] == null) {
                        final Region region = generator.regionGenerator().getOrCreateRegion(xPos, zPos);
                        if (!visitedRegions.contains(region)) {
                            addRegionToCache(cache, region, xDrawOffsetGrids, zDrawOffsetGrids, previewSizeGrids);
                            visitedRegions.add(region);
                        }
                        // Account for a rare edge case, see: https://discord.com/channels/432522930610765835/646085141847998484/1277429511608074302
                        // TODO: This can explode too, 8369517796180450123 @ 262.1km
                        // #isIn can't be trusted (for now?) and properly verifying #index would be minimally tedius
                        try {
                            if (cache[cachePos] == null && region.requireAt(xPos, zPos) != null) {
                                cache[cachePos] = region;
                            }
                        } catch (Throwable ignored) {
                            TFCGenViewer.LOGGER.warn("Ow x: {}, y: {}, {}", x, y, GeneratorPreviewException.buildMessage(
                                    seed,
                                    visualizer,
                                    scale,
                                    xCenterGrids,
                                    zCenterGrids,
                                    generator,
                                    xPos,
                                    zPos
                            ));
                        }
                    }
                    try {
                        visualizer.draw(
                                x, y,
                                xPos,
                                zPos,
                                generator,
                                cache[cachePos],
                                cache[cachePos].requireAt(xPos, zPos),
                                image
                        );
                    } catch (Throwable error) {
                        if (error instanceof IllegalStateException ise && "Image is not allocated.".equals(ise.getMessage())) {
                            throw error; // This specific error is known and harmless (in this case) and can be ignored
                        } else {
                            final String errorMsg = GeneratorPreviewException.buildMessage(
                                    seed,
                                    visualizer,
                                    scale,
                                    xCenterGrids,
                                    zCenterGrids,
                                    generator,
                                    xPos,
                                    zPos
                            );
                            if (Config.cancelPreviewOnError.get()) {
                                Helpers.throwAsUnchecked(new GeneratorPreviewException(
                                        errorMsg,
                                        error
                                ));
                            } else {
                                TFCGenViewer.LOGGER.warn("Encountered error while generating preview info pixel {},{}:\n{}{}", x, y, errorMsg, error.getMessage());
                            }
                        }
                    }
                }
            }

            progressReturn.accept(100);

            if (drawSpawn) {
                final int xSpawnCenterGrids = (spawnXBlocks / (16 * 8)) - xDrawOffsetGrids;
                final int zSpawnCenterGrids = (spawnZBlocks / (16 * 8)) - zDrawOffsetGrids;
                final int radiusGrids = spawnDistBlocks / (16 * 8);

                final int lineWidthPixels = lineWidth(scale);

                int color = Colors.SPAWN_BORDER.get().color();

                hLine(image, xSpawnCenterGrids - radiusGrids, xSpawnCenterGrids + radiusGrids, zSpawnCenterGrids + radiusGrids, lineWidthPixels, color);
                hLine(image, xSpawnCenterGrids - radiusGrids, xSpawnCenterGrids + radiusGrids, zSpawnCenterGrids - radiusGrids, lineWidthPixels, color);
                vLine(image, zSpawnCenterGrids - radiusGrids, zSpawnCenterGrids + radiusGrids, xSpawnCenterGrids + radiusGrids, lineWidthPixels, color);
                vLine(image, zSpawnCenterGrids - radiusGrids, zSpawnCenterGrids + radiusGrids, xSpawnCenterGrids - radiusGrids, lineWidthPixels, color);

                color = Colors.SPAWN_RETICULE.get().color();

                final int length = Math.min(radiusGrids / 4, previewSizeGrids / 12);
                hLine(image, xSpawnCenterGrids - length, xSpawnCenterGrids + length, zSpawnCenterGrids, lineWidthPixels, color);
                vLine(image, zSpawnCenterGrids - length, zSpawnCenterGrids + length, xSpawnCenterGrids, lineWidthPixels, color);
            }

            if (!FMLEnvironment.production && false) {
                for (Region region : visitedRegions) {
                    final int color = color(255, region.hashCode());

                    hLine(image, region.minX() - xDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, 0, color);
                    hLine(image, region.minX() - xDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, region.minZ() - zDrawOffsetGrids, 0, color);

                    vLine(image, region.minZ() - zDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, 0, color);
                    vLine(image, region.minZ() - zDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, region.minX() - xDrawOffsetGrids, 0, color);
                }
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
            BUILDER_STATE.set(BuilderState.FINALIZE);
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
            progressReturn.accept(-1);
            BUILDER_STATE.set(BuilderState.OFF);
        });
    }

    private static void addRegionToCache(Region[] cache, Region region, int xOffset, int zOffset, int size) {
        for (int x = region.minX() ; x <= region.maxX() ; x++) {
            final int xImagePos = x - xOffset;
            if (xImagePos >= 0 && xImagePos < size) {
                for (int z = region.minZ() ; z <= region.maxZ() ; z++) {
                    final int zImagePos = z - zOffset;
                    if (zImagePos >= 0 && zImagePos < size && region.at(x, z) != null) {
                        cache[xImagePos * size + zImagePos] = region;
                    }
                }
            }
        }
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
            currentImage.close();
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

    public enum BuilderState {
        OFF,
        SETUP,
        RUNNING,
        FINALIZE
    }
}
