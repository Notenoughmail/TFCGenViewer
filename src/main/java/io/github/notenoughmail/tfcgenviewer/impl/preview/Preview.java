package io.github.notenoughmail.tfcgenviewer.impl.preview;

import com.google.common.base.Stopwatch;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.client.widget.InfoPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.PreviewPane;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
            int zCenterBlocks,
            ResourceLocation visualizerId,
            PreviewPane previewPane,
            InfoPane infoPane,
            SpawnInfo spawnInfo,
            RegistryAccess registryAccess
    ) {
        return CompletableFuture.supplyAsync(() -> {
            infoPane.setGenerating(viz);
            final Stopwatch timer = Stopwatch.createStarted();

            final int previewPixels = imageSize.sizeInPixels();
            final int halfPreviewPixels = previewPixels >> 1;
            final int xDrawOffsetPixels = (xCenterBlocks / drawParams.scale().blocksPerPixel()) - halfPreviewPixels;
            final int zDrawOffsetPixels = (zCenterBlocks / drawParams.scale().blocksPerPixel()) - halfPreviewPixels;

            for (int x = 0 ; x < previewPixels ; x++) {
                if (!image.isAllocated()) break;
                previewPane.updateProgress(x, imageSize);
                for (int y = 0 ; y < previewPixels ; y++) {
                    if (!image.isAllocated()) break;
                    final int xPos = x + xDrawOffsetPixels;
                    final int zPos = y + zDrawOffsetPixels;
                    viz.draw(x, y, image, xPos, zPos, drawParams);
                }
            }

            viz.afterComplete(image, drawParams);

            timer.stop();
            final long millis = timer.elapsed(TimeUnit.MILLISECONDS);
            return new ImageReturn(
                    image,
                    millis,
                    Util.make(new StringBuilder(), builder -> {
                        builder.append(Util.getFilenameFormattedDateTime())
                                .append("-")
                                .append(visualizerId.toDebugFileName())
                                .append("+")
                                .append(viz.id().toDebugFileName());
                        viz.appendToFileName(builder, drawParams.options());
                        builder.append(".png");
                    }).toString(),
                    Util.make(
                            Component.translatable("tfcgenviewer.preview_info.base", viz.name(), drawParams.scale().formatSize(imageSize), formatMillis(millis)),
                            c -> {
                                c.append(CommonComponents.NEW_LINE)
                                        .append(Component.translatable("tfcgenviewer.preview_info.centered_on", xCenterBlocks, zCenterBlocks))
                                        .append(CommonComponents.NEW_LINE)
                                        .append(CommonComponents.NEW_LINE);
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
                        drawParams.colorDescriptors(),
                        drawParams.scale(),
                        xCenterBlocks - halfImageBlocks,
                        zCenterBlocks - halfImageBlocks
                );
                infoPane.setMessage(ret.infoPaneMessage());
            }
            return ret;
        });
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

    public record SpawnInfo(boolean drawSpawn, int xCenterBlocks, int zCenterBlocks, int radiusBlocks) {}
}
