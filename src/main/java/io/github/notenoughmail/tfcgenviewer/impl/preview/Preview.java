package io.github.notenoughmail.tfcgenviewer.impl.preview;

import com.google.common.base.Stopwatch;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.client.TFCGenViewerClient;
import io.github.notenoughmail.tfcgenviewer.client.widget.InfoPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.PreviewPane;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class Preview {

    public static final DataManager.Reference<ColorDefinition> SPAWN_BORDER = Colors.MISC_COLORS.getReference(TFCGenViewer.id("spawn/border"));
    public static final DataManager.Reference<ColorDefinition> SPAWN_RETICULE = Colors.MISC_COLORS.getReference(TFCGenViewer.id("spawn/reticule"));

    public static final Component ON_ERROR = Component.translatable("tfcgenviewer.preview_info.error");

    private static final ClassLoader CLASS_LOADER = TFCGenViewer.class.getClassLoader();

    private static final ForkJoinPool GEN_THREAD_POOL = Util.make(() -> {
        final AtomicInteger counter = new AtomicInteger(0);
        return new ForkJoinPool(2, fjp -> {
            final ForkJoinWorkerThread thread = new ForkJoinWorkerThread(fjp) {};
            thread.setContextClassLoader(CLASS_LOADER);
            thread.setName("TFCGenViewer Draw Thread #%s".formatted(counter.getAndIncrement()));
            return thread;
        }, null, true, 0, 0x7FFF, 1, null, 5L, TimeUnit.SECONDS);
    });

    public static <
            G extends ChunkGeneratorExtension,
            I extends ImageSize,
            S extends IScale<I>,
            C,
            O extends IVisualizerType.Options<O>
            >
    CompletableFuture<ImageReturn> draw(
            Image image,
            I imageSize,
            IVisualizerType.DrawInfo<G, C, S, O> drawParams,
            IVisualizerType<G, C, S, O> viz,
            int xCenterBlocks,
            int zCenterBlocks,
            ResourceLocation visualizerId,
            PreviewPane previewPane,
            InfoPane infoPane,
            SpawnInfo spawnInfo,
            RegistryAccess registryAccess,
            boolean showCenterCoords
    ) {
        return CompletableFuture.supplyAsync(() -> {
            infoPane.setGenerating(viz);
            final Stopwatch timer = Stopwatch.createStarted();

            final int previewPixels = imageSize.sizeInPixels();
            final int halfPreviewPixels = previewPixels >> 1;
            final int blocksPerPixel = drawParams.scale().blocksPerPixel();
            final int xDrawOffsetPixels = (xCenterBlocks / blocksPerPixel) - halfPreviewPixels;
            final int zDrawOffsetPixels = (zCenterBlocks / blocksPerPixel) - halfPreviewPixels;

            final IntConsumer progressReturn = TFCGenViewerClient.displayGenerationProgress.getAsBoolean() ?
                    i -> previewPane.updateProgress(i, imageSize) :
                    i -> {};

            if (viz.supportsParallelProcessing()) {
                handleParallelDraw(
                        image,
                        viz,
                        drawParams,
                        previewPixels,
                        progressReturn,
                        xDrawOffsetPixels,
                        zDrawOffsetPixels
                );
            } else {
                handleDraw(
                        image,
                        viz,
                        drawParams,
                        previewPixels,
                        progressReturn,
                        xDrawOffsetPixels,
                        zDrawOffsetPixels
                );
            }

            viz.afterComplete(image, drawParams);

            if (spawnInfo.drawSpawn()) {
                final int xSpawnCenterPixels = (spawnInfo.xCenterBlocks() / blocksPerPixel) - xDrawOffsetPixels;
                final int zSpawnCenterPixels = (spawnInfo.zCenterBlocks() / blocksPerPixel) - zDrawOffsetPixels;
                final int spawnRadiusPixels = spawnInfo.radiusBlocks() / blocksPerPixel;

                final ColorDefinition border = SPAWN_BORDER.get();
                final ColorDefinition reticule = SPAWN_RETICULE.get();
                drawParams.addTooltip(border);
                drawParams.addTooltip(reticule);

                final int lineWidth = drawParams.size().lineWidth();

                image.hLine(xSpawnCenterPixels - spawnRadiusPixels, xSpawnCenterPixels + spawnRadiusPixels, zSpawnCenterPixels + spawnRadiusPixels, lineWidth, border.abgr());
                image.hLine(xSpawnCenterPixels - spawnRadiusPixels, xSpawnCenterPixels + spawnRadiusPixels, zSpawnCenterPixels - spawnRadiusPixels, lineWidth, border.abgr());
                image.vLine(zSpawnCenterPixels - spawnRadiusPixels, zSpawnCenterPixels + spawnRadiusPixels, xSpawnCenterPixels + spawnRadiusPixels, lineWidth, border.abgr());
                image.vLine(zSpawnCenterPixels - spawnRadiusPixels, zSpawnCenterPixels + spawnRadiusPixels, xSpawnCenterPixels - spawnRadiusPixels, lineWidth, border.abgr());

                final int reticuleLength = Math.min(spawnRadiusPixels / 4, previewPixels /12);
                image.hLine(xSpawnCenterPixels - reticuleLength, xSpawnCenterPixels + reticuleLength, zSpawnCenterPixels, lineWidth, reticule.abgr());
                image.vLine(zSpawnCenterPixels - reticuleLength, zSpawnCenterPixels + reticuleLength, xSpawnCenterPixels, lineWidth, reticule.abgr());
            }

            timer.stop();
            final long millis = timer.elapsed(TimeUnit.MILLISECONDS);
            if (drawParams.cache() instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    TFCGenViewer.LOGGER.error("Could not close cache!", e);
                    return Helpers.throwAsUnchecked(e);
                }
            }
            return new ImageReturn(
                    image,
                    millis,
                    Util.make(new StringBuilder(), builder -> {
                        builder.append(Util.getFilenameFormattedDateTime())
                                .append("-")
                                .append(visualizerId.toDebugFileName())
                                .append("+")
                                .append(Objects.requireNonNull(GenViewerAPI.VISUALIZER_REGISTRY.getKey(viz)).toDebugFileName());
                        viz.appendToFileName(s -> builder.append("-").append(s), drawParams.options());
                        builder.append(".png");
                    }).toString(),
                    Util.make(
                            Component.translatable("tfcgenviewer.preview_info.base", viz.name(), drawParams.scale().formatSize(imageSize), formatMillis(millis)),
                            c -> {
                                if (showCenterCoords) {
                                    c.append(CommonComponents.NEW_LINE)
                                            .append(Component.translatable("tfcgenviewer.preview_info.centered_on", xCenterBlocks, zCenterBlocks));
                                }
                                c.append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE);
                                final Component additional = viz.additionalPreviewInfo(drawParams);
                                if (additional != null) {
                                    c.append(Component.translatable("tfcgenviewer.preview_info.additional_from_visualizer", additional))
                                            .append(CommonComponents.NEW_LINE)
                                            .append(CommonComponents.NEW_LINE);
                                }
                                c.append(Component.translatable("tfcgenviewer.preview_info.color_key", viz.colorKey(registryAccess, drawParams.cache())));
                            }
                    )
            );
        }, GEN_THREAD_POOL).exceptionally(thr -> {
            TFCGenViewer.LOGGER.error("Error encountered during generation preview!", thr);
            previewPane.alterState(true);
            infoPane.setError();
            return new ImageReturn(image, -1L, null, ON_ERROR);
        }).thenApply(ret -> {
            if (ret.millis() != -1) {
                final int halfImageBlocks = drawParams.scale().blocksPerPixel() * imageSize.sizeInPixels() / 2;
                previewPane.updateImage(
                        ret.image(),
                        drawParams.colorTooltips(),
                        drawParams.scale(),
                        xCenterBlocks - halfImageBlocks,
                        zCenterBlocks - halfImageBlocks
                );
                infoPane.setMessage(ret.infoPaneMessage());
            }
            if (TFCGenViewerClient.dingWhenGenerated.getAsBoolean()) {
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.ARROW_HIT_PLAYER, 1F));
            }
            return ret;
        });
    }

    private static <
            G extends ChunkGeneratorExtension,
            C,
            S extends IScale<?>,
            O extends IVisualizerType.Options<O>
            > void handleParallelDraw(
                    Image image,
                    IVisualizerType<G, C, S, O> viz,
                    IVisualizerType.DrawInfo<G, C, S, O> drawParams,
                    int previewPixels,
                    IntConsumer progressReturn,
                    int xDrawOffsetPixels,
                    int zDrawOffsetPixels
    ) {
        final PropertyDispatch.QuadFunction<Integer, Integer, Integer, Integer, ThrowingRunnable> callerFactory =
                (x, y, xPos, zPos) ->
                        () -> {
                            final FutureTask<?> task = drawTask(viz, x, y, image, xPos, zPos, drawParams);
                            task.run();
                            task.get(viz.timeoutMillis(), TimeUnit.MILLISECONDS);
                        };

        try (final FutureBlockingQueue queue = new FutureBlockingQueue()) {
            for (int x = 0 ; x < previewPixels ; x++) {
                if (!image.isAllocated()) return;
                progressReturn.accept(x);
                final int xPos = x + xDrawOffsetPixels;
                for (int y = 0 ; y < previewPixels ; y++) {
                    if (!image.isAllocated()) return;
                    final int zPos = y + zDrawOffsetPixels;
                    final int fx = x, fy = y;
                    queue.add(
                            callerFactory.apply(x, y, xPos, zPos),
                            () -> "Visualizer type %s timed out while drawing %d %d (%d %d)".formatted(
                                    GenViewerAPI.VISUALIZER_REGISTRY.getKey(viz),
                                    fx,
                                    fy,
                                    xPos,
                                    zPos
                            )
                    );
                }
            }
        } catch (Throwable e) {
            Helpers.throwAsUnchecked(e);
        }
    }

    private static <
            G extends ChunkGeneratorExtension,
            C,
            S extends IScale<?>,
            O extends IVisualizerType.Options<O>
            > void handleDraw(
                    Image image,
                    IVisualizerType<G, C, S, O> viz,
                    IVisualizerType.DrawInfo<G, C, S, O> drawParams,
                    int previewPixels,
                    IntConsumer progressReturn,
                    int xDrawOffsetPixels,
                    int zDrawOffsetPixels
    ) {
        for (int x = 0 ; x < previewPixels ; x++) {
            if (!image.isAllocated()) return;
            progressReturn.accept(x);
            final int xPos = x + xDrawOffsetPixels;
            for (int y = 0 ; y < previewPixels ; y++) {
                if (!image.isAllocated()) return;
                final int zPos = y + zDrawOffsetPixels;
                final FutureTask<?> task = drawTask(viz, x, y, image, xPos, zPos, drawParams);
                try {
                    task.run();
                    task.get(viz.timeoutMillis(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    TFCGenViewer.LOGGER.error(
                            "Visualizer type {} timed out while drawing {} {} ({} {})",
                            GenViewerAPI.VISUALIZER_REGISTRY.getKey(viz),
                            x,
                            y,
                            xPos,
                            zPos
                    );
                    image.setPixel(x, y, 0xFF000000);
                    if (!FMLEnvironment.production) Helpers.throwAsUnchecked(e);
                } catch (Exception e) {
                    Helpers.throwAsUnchecked(e);
                }
            }
        }
    }

    private static <
            G extends ChunkGeneratorExtension,
            C,
            S extends IScale<?>,
            O extends IVisualizerType.Options<O>
            > FutureTask<?> drawTask(
                    IVisualizerType<G, C, S, O> viz,
                    int imageX,
                    int imageY,
                    Image image,
                    int xPos,
                    int zPos,
                    IVisualizerType.DrawInfo<G, C, S, O> drawParams
    ) {
        return new FutureTask<>(() -> viz.draw(imageX, imageY, image, xPos, zPos, drawParams), null);
    }

    private static String formatMillis(long millis) {
        final double seconds = (double) millis / 1000L;
        if (millis < 1000) {
            return "%.2f".formatted(seconds);
        } else {
            return "%.1f".formatted(seconds);
        }
    }

    public record ImageReturn(
            Image image,
            long millis,
            String name,
            Component infoPaneMessage
    ) {
        public void export() {
            if (millis != -1) {
                image.export(name);
            } else {
                TFCGenViewer.LOGGER.warn("Unable to export image due to above errors");
            }
        }
    }

    public record SpawnInfo(boolean drawSpawn, int xCenterBlocks, int zCenterBlocks, int radiusBlocks) {

        public static SpawnInfo of(OptionInstance<Boolean> drawSpawn, OptionInstance<Integer> xCenterBlocks, OptionInstance<Integer> zCenterBlocks, OptionInstance<Integer> radiusBlocks) {
            if (drawSpawn.get()) {
                return new SpawnInfo(true, xCenterBlocks.get(), zCenterBlocks.get(), radiusBlocks.get());
            }
            return NO_SPAWN;
        }

        public static SpawnInfo of(OptionInstance<Boolean> drawSpawn, Settings settings) {
            if (drawSpawn.get()) {
                return new SpawnInfo(true, settings.spawnCenterX(), settings.spawnCenterZ(), settings.spawnDistance());
            }
            return NO_SPAWN;
        }

        public static final SpawnInfo NO_SPAWN = new SpawnInfo(false, 0, 0, 0);
    }

    public static <I extends ImageSize> OptionInstance<I> imageSizeOption(IScale<I> scale) {
        return new OptionInstance<>(
                "tfcgenviewer.option.preview_size",
                OptionInstance.noTooltip(),
                (caption, i) -> scale.formatSize(i),
                new OptionInstance.Enum<>(scale.sizes(), scale.codec()),
                scale.getDefault(),
                i -> {}
        );
    }

    private static final Codec<IVisualizerType<?, ?, ?, ?>> VIZ_CODEC = GenViewerAPI.VISUALIZER_REGISTRY.byNameCodec();

    public static <V extends IVisualizerType<?, ?, ?, ?>> OptionInstance<V> visualizerTypeOption(List<V> visualziers, Consumer<V> onChange) {
        return new OptionInstance<>(
                "tfcgenviewer.option.visualizer_type",
                viz -> Tooltip.create(viz.description()),
                (caption, viz) -> viz.name(),
                new OptionInstance.Enum<>(visualziers, VIZ_CODEC.xmap(TFCGenViewer::<V>cast, Function.identity())),
                visualziers.getFirst(),
                onChange
        );
    }

    // Taken from CreateTFCWorldScreen
    public static OptionInstance<Integer> kmOption(String caption, int min, int max, int defaultValue) {
        return new OptionInstance<>(
                caption,
                OptionInstance.cachedConstantTooltip(Component.translatable(caption + ".tooltip")),
                (text, value) -> Options.genericValueLabel(
                        text,
                        Component.translatable("tfc.settings.km", String.format("%.1f", value / 1000.0))
                ),
                new OptionInstance.IntRange(min, max),
                defaultValue,
                i -> {}
        );
    }

    private static class FutureBlockingQueue implements AutoCloseable {

        private final Future<?>[] values;
        private final ExecutorService service;

        private final ReentrantLock lock;
        private final Condition notFull;

        int count;
        int index;

        private volatile Throwable exception;

        FutureBlockingQueue() {
            lock = new ReentrantLock(false);
            notFull = lock.newCondition();

            final int parallelism = Math.min(5, Runtime.getRuntime().availableProcessors() - 4);
            final String threadName = Thread.currentThread().getName();
            final AtomicInteger count = new AtomicInteger();

            values = new Future[parallelism];
            service = new ForkJoinPool(parallelism, fjp -> {
                final ForkJoinWorkerThread thread = new ForkJoinWorkerThread(fjp) {};
                thread.setContextClassLoader(CLASS_LOADER);
                thread.setName(threadName + "-" + count.getAndIncrement());
                return thread;
            }, null, true, 0, 0x7FFF, 1, null, 1L, TimeUnit.SECONDS);
        }

        public void add(ThrowingRunnable drawTask, Supplier<String> timeOutString) throws Throwable {
            lock.lockInterruptibly();
            try {
                while (count == values.length) notFull.await();
                if (exception != null) throw exception;
                queue(drawTask, timeOutString);
            } finally {
                lock.unlock();
            }
        }

        private void queue(ThrowingRunnable drawTask, Supplier<String> timeOutString) {
            final int i = index;
            values[i] = CompletableFuture.<Throwable>supplyAsync(() -> {
                Helpers.uncheck(drawTask);
                return null;
            }, service)
                    .exceptionally(t -> {
                        if (t instanceof TimeoutException e) {
                            TFCGenViewer.LOGGER.error(timeOutString.get());
                            return FMLEnvironment.production ? null : e;
                        }
                        return t;
                    })
                    .thenAccept(t -> {
                        Helpers.uncheck(lock::lockInterruptibly);
                        try {
                            remove(i);
                        } finally {
                            lock.unlock();
                        }
                        if (t != null) exception = t;
                    });
            count++;
        }

        private void remove(int i) {
            values[i] = null;
            count--;
            index = i;
            notFull.signal();
        }

        public void close() {
            for (Future<?> task : values) {
                if (task != null) task.cancel(true);
            }
            service.shutdownNow();
        }
    }
}
