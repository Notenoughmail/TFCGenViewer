package com.notenoughmail.tfcgenviewer.util.preview;

import com.google.common.base.Stopwatch;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.color.Colors;
import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.VisualizerType;
import com.notenoughmail.tfcgenviewer.util.custom.GeneratorPreviewException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.minecraft.util.FastColor.ABGR32.color;

public class ImageBuilder {

    private static final Component NO_TOOLTIP = Component.translatable("tfcgenviewer.preview_world.no_tooltip_available");

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

    @Nullable
    private static Image currentImage, transientImage;
    private static String imageName;
    private static CompletableFuture<Void> builderProcess;

    // TODO: [Future] Sometimes, very rarely, the first created image will fail (?) or at least somehow break and cause a GL error to be printed to the console and show up completely empty | Find out how & why that ever happened the fix it
    // OpenGL debug message: id=1281, source=API, type=ERROR, severity=HIGH, message='GL_INVALID_VALUE error generated. Invalid texture format.'
    // OpenGL debug message: id=1000, source=API, type=ERROR, severity=HIGH, message='glTexSubImage2D has generated an error (GL_INVALID_OPERATION)'
    public static void build(
            RegionChunkDataGenerator generator,
            VisualizerType visualizer,
            int xCenterGrids,
            int zCenterGrids,
            boolean drawSpawn,
            int spawnDistBlocks,
            int spawnXBlocks,
            int spawnZBlocks,
            PreviewScale scale,
            Consumer<PreviewInfo> infoReturn,
            Consumer<Integer> progressReturn,
            boolean showCoords,
            long seed // For error reports
    ) {
        if (BUILDER_STATE.get() == BuilderState.FINALIZE) {
            TFCGenViewer.LOGGER.warn("Apply was called while a previous builder was finalizing. In very special cases this can cause a crash, thus the previous builder will continue and the request for a new builder will be discarded");
            return;
        }
        BUILDER_STATE.set(BuilderState.SETUP);
        if (Config.useThrobber.get()) {
            infoReturn.accept(PreviewInfo.EMPTY);
            PreviewScale.clearPreviews(currentImage);
        }
        if (currentImage != null) {
            currentImage.close();
            currentImage = null;
        }
        cancelRunning();
        builderProcess = CompletableFuture.supplyAsync(() -> {
            BUILDER_STATE.set(BuilderState.RUNNING);
            final Stopwatch timer = Stopwatch.createStarted();
            final int previewSizeGrids = scale.previewSize;

            final Image image = new Image(previewSizeGrids);
            transientImage = image;
            final int halfPreviewGrids = previewSizeGrids >> 1;
            final int xDrawOffsetGrids = xCenterGrids - halfPreviewGrids;
            final int zDrawOffsetGrids = zCenterGrids - halfPreviewGrids;

            final Set<Region> visitedRegions = new HashSet<>();
            final Region[] cache = new Region[previewSizeGrids * previewSizeGrids];
            final Int2ObjectOpenHashMap<Component> colorDescriptors = new Int2ObjectOpenHashMap<>();
            colorDescriptors.defaultReturnValue(NO_TOOLTIP);

            for (int x = 0; x < previewSizeGrids ; x++) {
                progressReturn.accept(102 * x / previewSizeGrids);
                for (int y = 0; y < previewSizeGrids ; y++) {
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
                        // There are further, more insidious edge cases, for those we use FAILURE_STATE and deal with the issue case by case
                        if (cache[cachePos] == null) {
                            final int index = region.index(xPos, zPos);
                            if (index >= 0 && index < region.data().length) {
                                cache[cachePos] = region;
                            }
                        }
                    }
                    if (image.isAllocated()) {
                        try {
                            visualizer.draw(
                                    x, y,
                                    xPos,
                                    zPos,
                                    generator,
                                    cache[cachePos],
                                    cache[cachePos] != null ? cache[cachePos].requireAt(xPos, zPos) : ColorUtil.FAILURE_STATE,
                                    image,
                                    colorDescriptors
                            );
                        } catch (Throwable error) {
                            // This specific error is known and harmless (in this case) and can be ignored
                            // It is more-or-less unavoidable due to writing to and de-allocating potentially happening on different threads
                            // Though attempts are made to prevent writing to the image before modify it
                            if (!(error instanceof IllegalStateException ise && "Image is not allocated.".equals(ise.getMessage()))) {
                                final String errorMsg = GeneratorPreviewException.buildMessage(
                                        seed,
                                        visualizer,
                                        scale.ordinal(),
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
                                    TFCGenViewer.LOGGER.warn("Encountered error while generating preview info pixel %d,%d:\n%s".formatted(x, y, errorMsg), error);
                                }
                            }
                        }
                    } else {
                        return ProcessReturn.EMPTY;
                    }
                }
            }

            if (drawSpawn) {
                final int xSpawnCenterGrids = (spawnXBlocks / (16 * 8)) - xDrawOffsetGrids;
                final int zSpawnCenterGrids = (spawnZBlocks / (16 * 8)) - zDrawOffsetGrids;
                final int radiusGrids = spawnDistBlocks / (16 * 8);

                int color = Colors.SPAWN_BORDER.get().color(colorDescriptors);

                image.hLine(xSpawnCenterGrids - radiusGrids, xSpawnCenterGrids + radiusGrids, zSpawnCenterGrids + radiusGrids, scale.lineWidth, color);
                image.hLine(xSpawnCenterGrids - radiusGrids, xSpawnCenterGrids + radiusGrids, zSpawnCenterGrids - radiusGrids, scale.lineWidth, color);
                image.vLine(zSpawnCenterGrids - radiusGrids, zSpawnCenterGrids + radiusGrids, xSpawnCenterGrids + radiusGrids, scale.lineWidth, color);
                image.vLine(zSpawnCenterGrids - radiusGrids, zSpawnCenterGrids + radiusGrids, xSpawnCenterGrids - radiusGrids, scale.lineWidth, color);

                color = Colors.SPAWN_RETICULE.get().color(colorDescriptors);

                final int length = Math.min(radiusGrids / 4, previewSizeGrids / 12);
                image.hLine(xSpawnCenterGrids - length, xSpawnCenterGrids + length, zSpawnCenterGrids, scale.lineWidth, color);
                image.vLine(zSpawnCenterGrids - length, zSpawnCenterGrids + length, xSpawnCenterGrids, scale.lineWidth, color);
            }

            if (!FMLEnvironment.production && visualizer.name().equals("BORDER")) {
                for (Region region : visitedRegions) {
                    final int color = color(255, region.hashCode());
                    colorDescriptors.putIfAbsent(color, Component.literal(Integer.toHexString(region.hashCode()) + " Border"));

                    image.hLine(region.minX() - xDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, scale.lineWidth, color);
                    image.hLine(region.minX() - xDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, region.minZ() - zDrawOffsetGrids, scale.lineWidth, color);

                    image.vLine(region.minZ() - zDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, region.maxX() - xDrawOffsetGrids, scale.lineWidth, color);
                    image.vLine(region.minZ() - zDrawOffsetGrids, region.maxZ() - zDrawOffsetGrids, region.minX() - xDrawOffsetGrids, scale.lineWidth, color);
                }
            }

            final String previewKm = scale.previewSizeKm;
            timer.stop();
            final String time = "%.1f".formatted(timer.elapsed(TimeUnit.MILLISECONDS) / 1000F);
            return new ProcessReturn(
                    new PreviewInfo(
                        showCoords ?
                                Component.translatable(
                                        "tfcgenviewer.preview_world.preview_info",
                                        visitedRegions.size(),
                                        time,
                                        previewKm,
                                        previewKm,
                                        xCenterGrids * 128,
                                        zCenterGrids * 128,
                                        visualizer.getName(),
                                        visualizer.getColorKey()
                                ) :
                                Component.translatable(
                                    "tfcgenviewer.preview_world.preview_info.no_coords",
                                    visitedRegions.size(),
                                    time,
                                    previewKm,
                                    previewKm,
                                    visualizer.getName(),
                                    visualizer.getColorKey()
                                ),
                        scale.textureId,
                        previewSizeGrids,
                        xDrawOffsetGrids * 128,
                        zDrawOffsetGrids * 128,
                        PreviewInfo.Mode.PREVIEW,
                        colorDescriptors
                    ),
                    image,
                    "%s_%dx%d_%d_%s.png".formatted(Util.getFilenameFormattedDateTime(), previewSizeGrids, previewSizeGrids, visitedRegions.size(), visualizer.name()),
                    true
            );
        }, GENERATOR_THREAD_POOL).exceptionally(thr -> {
            TFCGenViewer.LOGGER.error("Error encountered during generation!", thr);
            return ProcessReturn.ERROR;
        }).thenAccept(pr -> {
            BUILDER_STATE.set(BuilderState.FINALIZE);
            transientImage = null;
            if (pr != null) {
                currentImage = pr.currentImage();
                imageName = pr.imageName();
                scale.upload(currentImage);
                infoReturn.accept(pr.previewInfo());
                if (pr.ding() && Config.dingWhenGenerated.get()) {
                    Minecraft
                            .getInstance()
                            .getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.ARROW_HIT_PLAYER, 1.0F));
                }
            } else {
                currentImage = null;
                infoReturn.accept(PreviewInfo.ERROR);
                PreviewScale.clearPreviews(currentImage);
                if (Config.dingWhenGenerated.get()) {
                    Minecraft
                            .getInstance()
                            .getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.ARROW_HIT_PLAYER, 1.0F));
                }
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
            PreviewScale.clearPreviews(currentImage);
        }
        cancelRunning();
    }

    public static void exportImage() {
        if (currentImage != null) {
            currentImage.export(imageName);
        }
    }

    private record ProcessReturn(PreviewInfo previewInfo, Image currentImage, String imageName, boolean ding) {

        static ProcessReturn EMPTY = new ProcessReturn(PreviewInfo.EMPTY, null, null, false);
        static ProcessReturn ERROR = new ProcessReturn(PreviewInfo.ERROR, null, null, true);
    }

    public enum BuilderState {
        OFF,
        SETUP,
        RUNNING,
        FINALIZE
    }
}
