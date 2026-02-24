package io.github.notenoughmail.tfcgenviewer.impl.preview;

import com.google.common.base.Stopwatch;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class Preview {

    public static final DataManager.Reference<ColorDefinition> SPAWN_BORDER = Colors.MISC_COLORS.getReference(TFCGenViewer.id("spawn/border"));
    public static final DataManager.Reference<ColorDefinition> SPAWN_RETICULE = Colors.MISC_COLORS.getReference(TFCGenViewer.id("spawn/reticule"));

    public static final Component ON_ERROR = Component.translatable("tfcgenviewer.preview_info.error");

    private static final ForkJoinPool GEN_THREAD_POOL = Util.make(() -> {
        final AtomicInteger counter = new AtomicInteger(0);
        final ClassLoader classLoader = TFCGenViewer.class.getClassLoader();
        return new ForkJoinPool(Math.min(4, Runtime.getRuntime().availableProcessors()), fjp -> {
            final ForkJoinWorkerThread thread = new ForkJoinWorkerThread(fjp) {};
            thread.setContextClassLoader(classLoader);
            thread.setName("TFCGenViewer Draw Thread #%s".formatted(counter.getAndIncrement()));
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

            for (int x = 0 ; x < previewPixels ; x++) {
                if (!image.isAllocated()) break;
                if (TFCGenViewerClient.displayGenerationProgress.getAsBoolean()) {
                    previewPane.updateProgress(x, imageSize);
                }
                for (int y = 0 ; y < previewPixels ; y++) {
                    if (!image.isAllocated()) break;
                    final int xPos = x + xDrawOffsetPixels;
                    final int zPos = y + zDrawOffsetPixels;
                    viz.draw(x, y, image, xPos, zPos, drawParams);
                }
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
}
