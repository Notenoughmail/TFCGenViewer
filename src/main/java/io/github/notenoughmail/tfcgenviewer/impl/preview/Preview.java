package io.github.notenoughmail.tfcgenviewer.impl.preview;

import com.google.common.base.Stopwatch;
import com.machinezoo.noexception.throwing.ThrowingRunnable;
import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.client.options.EnhancedEnumValueSet;
import io.github.notenoughmail.tfcgenviewer.client.options.EnhancedSliderValueSet;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

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
            IVisualizerType.DrawInfo<G, C, S, O> drawParams,
            IVisualizerType<G, C, S, O> viz,
            int xCenterBlocks,
            int zCenterBlocks,
            ResourceLocation visualizerId,
            PreviewPane previewPane,
            InfoPane infoPane,
            SpawnInfo spawnInfo,
            DrawParallelism parallelism,
            RegistryAccess registryAccess,
            boolean showCenterCoords
    ) {
        return CompletableFuture.supplyAsync(() -> {
            infoPane.setGenerating(viz);
            final Stopwatch timer = Stopwatch.createStarted();

            final int previewPixels = drawParams.size().sizeInPixels();
            final int halfPreviewPixels = previewPixels >> 1;
            final int blocksPerPixel = drawParams.scale().blocksPerPixel();
            final int xDrawOffsetPixels = (xCenterBlocks / blocksPerPixel) - halfPreviewPixels;
            final int zDrawOffsetPixels = (zCenterBlocks / blocksPerPixel) - halfPreviewPixels;

            final IntConsumer progressReturn = TFCGenViewer.displayGenerationProgress.getAsBoolean() ?
                    i -> previewPane.updateProgress(i, drawParams.size()) :
                    i -> {};

            if (parallelism.parallel()) {
                handleParallelDraw(image, viz, drawParams, previewPixels, progressReturn, xDrawOffsetPixels, zDrawOffsetPixels, parallelism.parallelism());
            } else {
                handleSerialDraw(image, viz, drawParams, previewPixels, progressReturn, xDrawOffsetPixels, zDrawOffsetPixels);
            }

            viz.afterComplete(image, drawParams, xDrawOffsetPixels, zDrawOffsetPixels);

            if (spawnInfo.drawSpawn()) {
                handleSpawnDraw(image, spawnInfo, drawParams, blocksPerPixel, xDrawOffsetPixels, zDrawOffsetPixels, previewPixels);
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
                            Component.translatable(
                                    "tfcgenviewer.preview_info.base",
                                    viz.name(),
                                    drawParams.scale().formatSize(TFCGenViewer.cast(drawParams.size())),
                                    formatMillis(millis)
                            ),
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
                final int halfImageBlocks = drawParams.scale().blocksPerPixel() * drawParams.size().sizeInPixels() / 2;
                previewPane.updateImage(
                        ret.image(),
                        drawParams.colorTooltips(),
                        drawParams.scale(),
                        xCenterBlocks - halfImageBlocks,
                        zCenterBlocks - halfImageBlocks
                );
                infoPane.setMessage(ret.infoPaneMessage());
            }
            if (TFCGenViewer.dingWhenGenerated.getAsBoolean()) {
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.ARROW_HIT_PLAYER, 1F));
            }
            return ret;
        });
    }

    private static void handleSpawnDraw(
            Image image,
            SpawnInfo spawnInfo,
            IVisualizerType.DrawInfo<?, ?, ?, ?> drawParams,
            int blocksPerPixel,
            int xDrawOffsetPixels,
            int zDrawOffsetPixels,
            int previewPixels
    ) {
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

        final int reticuleLength = Math.min(spawnRadiusPixels / 4, previewPixels / 12);
        image.hLine(xSpawnCenterPixels - reticuleLength, xSpawnCenterPixels + reticuleLength, zSpawnCenterPixels, lineWidth, reticule.abgr());
        image.vLine(zSpawnCenterPixels - reticuleLength, zSpawnCenterPixels + reticuleLength, xSpawnCenterPixels, lineWidth, reticule.abgr());
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
                    int zDrawOffsetPixels,
                    int parallelism
    ) {
        try (final FutureBlockingQueue queue = new FutureBlockingQueue(parallelism)) {
            for (int x = 0 ; x < previewPixels ; x++) {
                if (!image.isAllocated()) return;
                progressReturn.accept(x);
                final int xPos = x + xDrawOffsetPixels;
                for (int y = 0 ; y < previewPixels ; y++) {
                    if (!image.isAllocated()) return;
                    final int zPos = y + zDrawOffsetPixels;
                    queue.queueBlocking(new DrawTask<>(viz, x, y, image, xPos, zPos, drawParams)::executeDraw);
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
            > void handleSerialDraw(
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
                Helpers.uncheck(new DrawTask<>(viz, x, y, image, xPos, zPos, drawParams)::executeDraw);
            }
        }
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
                new EnhancedEnumValueSet<>(scale.codec(), scale.sizes(), false),
                scale.getDefault(),
                i -> {}
        );
    }

    private static final Codec<IVisualizerType<?, ?, ?, ?>> VIZ_CODEC = GenViewerAPI.VISUALIZER_REGISTRY.byNameCodec();

    public static <V extends IVisualizerType<?, ?, ?, ?>> OptionInstance<V> visualizerTypeOption(List<V> visualizers, Consumer<V> onChange) {
        return new OptionInstance<>(
                "tfcgenviewer.option.visualizer_type",
                viz -> Tooltip.create(viz.description()),
                (caption, viz) -> viz.name(),
                new EnhancedEnumValueSet<>(VIZ_CODEC.xmap(TFCGenViewer::<V>cast, Function.identity()), visualizers, false),
                visualizers.getFirst(),
                onChange
        );
    }

    public static OptionInstance<Boolean> boolOption(String caption, boolean initialValue, Consumer<Boolean> onChange) {
        return new OptionInstance<>(
                caption,
                OptionInstance.noTooltip(),
                OptionInstance.BOOLEAN_TO_STRING,
                EnhancedEnumValueSet.BOOL,
                initialValue,
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
                EnhancedSliderValueSet.integer(min, max),
                defaultValue,
                i -> {}
        );
    }

    private static class FutureBlockingQueue implements AutoCloseable {

        private final ExecutorService service;

        private volatile Throwable exception;

        FutureBlockingQueue(int parallelism) {

            final ThreadFactory baseFactory = Thread.ofVirtual()
                    .name(Thread.currentThread().getName() + "-", 0L)
                    .factory();

            service = new ThreadPoolExecutor(
                    parallelism,
                    parallelism,
                    0L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(parallelism + 5) {
                        // Force the damned executor to be blocking
                        @Override
                        public boolean offer(@NotNull Runnable runnable) {
                            try {
                                put(runnable);
                            } catch (InterruptedException e) {
                                Helpers.throwAsUnchecked(e);
                            }
                            return true;
                        }
                    },
                    r -> {
                        final Thread t = baseFactory.newThread(r);
                        t.setContextClassLoader(CLASS_LOADER);
                        return t;
                    }
            );
        }

        public void queueBlocking(ThrowingRunnable drawTask) throws Throwable {
            // Never gets overwritten to null and which one is ultimately throw shouldn't matter too much so this
            // doesn't need any threading safeguards
            if (exception != null) throw exception;
            CompletableFuture.<Throwable>supplyAsync(() -> {
                Helpers.uncheck(drawTask);
                return null;
            }, service)
                    .exceptionally(Function.identity())
                    .thenAccept(t -> {
                        if (t != null) exception = t;
                    });
        }

        public void close() {
            service.shutdownNow();
        }
    }

    private static class DrawTask<
            G extends ChunkGeneratorExtension,
            C,
            S extends IScale<?>,
            O extends IVisualizerType.Options<O>
            > extends FutureTask<@Nullable Object> {

        private final IVisualizerType<G, C, S, O> viz;
        private final Image image;
        private final int imageX, imageY, xPos, zPos;

        public DrawTask(
                IVisualizerType<G, C, S, O> viz,
                int imageX,
                int imageY,
                Image image,
                int xPos,
                int zPos,
                IVisualizerType.DrawInfo<G, C, S, O> drawParams
        ) {
            // TODO: Java 25 | Would before-super operations allow this to be non-capturing?
            super(() -> viz.draw(imageX, imageY, image, xPos, zPos, drawParams), null);
            this.viz = viz;
            this.image = image;
            this.imageX = imageX;
            this.imageY = imageY;
            this.xPos = xPos;
            this.zPos = zPos;
        }

        public void executeDraw() throws Throwable {
            try {
                run();
                get(TFCGenViewer.absoluteMaximumMicrosToDrawPixel.getAsInt(), TimeUnit.MICROSECONDS);
            } catch (TimeoutException e) {
                TFCGenViewer.LOGGER.error(
                        "Visualizer type {} timed out while drawing {} {} ({} {})",
                        GenViewerAPI.VISUALIZER_REGISTRY.getKey(viz),
                        imageX,
                        imageY,
                        xPos,
                        zPos
                );
                image.setPixel(imageX, imageY, 0xFF000000 | viz.timeoutFillBGR());
                if (!FMLEnvironment.production) throw e;
            }
        }
    }
}
