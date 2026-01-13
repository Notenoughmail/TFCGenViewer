package io.github.notenoughmail.tfcgenviewer.impl.preview;

import com.google.common.base.Stopwatch;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Preview {

    public static final Component ON_ERROR = Component.translatable("tfcgenviewer.preview_info.error");

    private static final AtomicInteger THREAD_POOL_COUNTER = new AtomicInteger(0);

    private static final ForkJoinPool GEN_THREAD_POOL = Util.make(() -> {
        final ClassLoader classLoader = TFCGenViewer.class.getClassLoader();
        return new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors()) - 2, fjp -> {
            final ForkJoinWorkerThread thread = new ForkJoinWorkerThread(fjp) {};
            thread.setContextClassLoader(classLoader);
            thread.setName("TFCGenViewer Draw Thread #%s".formatted(THREAD_POOL_COUNTER.getAndIncrement()));
            return thread;
        }, null, true, 0, 0x7FFF, 1, null, 5L, TimeUnit.SECONDS);
    });

    public static <
            G extends ChunkGeneratorExtension,
            I extends ImageSize,
            S extends IScale<I>,
            V extends IVisualizerType<G, C, S, O>,
            C,
            O extends IVisualizerType.Options<O>
            >
    CompletableFuture<ImageReturn> draw(
            Image image,
            I imageSize,
            IVisualizerType.DrawInfo<G, C, S, O> drawParams,
            V viz,
            int xCenterBlocks,
            int zCenterBlocks
    ) {
        return CompletableFuture.supplyAsync(() -> {
            final Stopwatch timer = Stopwatch.createStarted();

            final int previewPixels = imageSize.sizeInPixels();
            final int halfPreviewPixels = previewPixels >> 1;
            final int xDrawOffsetPixels = (xCenterBlocks / drawParams.scale().blocksPerPixel()) - halfPreviewPixels;
            final int zDrawOffsetPixels = (zCenterBlocks / drawParams.scale().blocksPerPixel()) - halfPreviewPixels;

            for (int x = 0 ; x < previewPixels ; x++) {
                if (!image.isAllocated()) break;
                for (int y = 0 ; y < previewPixels ; y++) {
                    if (!image.isAllocated()) break;
                    final int xPos = x + xDrawOffsetPixels;
                    final int zPos = y + zDrawOffsetPixels;
                    viz.draw(x, y, image, xPos, zPos, drawParams);
                }
            }

            viz.afterComplete(image, drawParams);

            timer.stop();
            return new ImageReturn(
                    image,
                    timer.elapsed(TimeUnit.MILLISECONDS)
            );
        }, GEN_THREAD_POOL).exceptionally(thr -> {
            TFCGenViewer.LOGGER.error("Error encountered during generation preview!", thr);
            return new ImageReturn(image, -1L);
        });
    }

    public record ImageReturn(
            Image image,
            long millis
    ) {}
}
