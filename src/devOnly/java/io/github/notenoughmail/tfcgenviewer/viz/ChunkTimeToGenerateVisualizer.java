package io.github.notenoughmail.tfcgenviewer.viz;

import com.google.common.base.Suppliers;
import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorGradientDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.region.Units;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ChunkTimeToGenerateVisualizer implements ITFCChunkVisualizerType.Simple<ChunkTimeToGenerateVisualizer.Timer> {

    static final Supplier<ColorGradientDefinition> GRADIENT = Suppliers.memoize(() -> new ColorGradientDefinition(
            TFCGenViewerRegistration.GRAD_GRAYSCALE.get(),
            Component.literal("Time to generate"),
            // 2e8 / 256 is a clean number apparently
            Optional.of(IntStream.iterate(0, i -> i < 2e8, i -> i + 781250)
                    .<Component>mapToObj(i -> Component.literal("%s - %s ns".formatted(i, i + 781250)))
                    .toList())
    ));

    static final ColorKey COLOR_KEY = ColorKey.of(m -> GRADIENT.get().appendTo(m, true));

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public Timer createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options, DrawParallelism parallelism) {
        return new Timer(worldSeed, generator);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, Timer, ChunkScale, NoneOpt> info) {
        info.cache().start();
        final ChunkData data = info.cache().innerCache.create(xPos, zPos);
        final int nanos = info.cache().time();
        final double val = nanos / 2e8D;
        final double h = (info.cache().innerCache.regionGenerator().getOrCreateRegion(Units.blockToGrid(data.getPos().getMinBlockX()), Units.blockToGrid(data.getPos().getMinBlockZ())).noise() + 1) * 0.5;
        final int color = FastColor.ABGR32.fromArgb32(Mth.hsvToArgb((float) h, 1f, (float) (val * 0.9 + 0.1), 255));
        if (!info.colorTooltips().hasColor(color)) {
            info.colorTooltips().addTooltip(color, GRADIENT.get().tooltipTxt(val));
        }
        image.setPixel(imageX, imageY, color);
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, Timer cache) {
        return COLOR_KEY.colorKey();
    }

    @Override
    public Component name() {
        return Component.literal("Time to generate");
    }

    @Override
    public Component description() {
        return Component.literal("The time to generate");
    }

    @Override
    public int timeoutFillBGR() {
        return 0xFFFFFF;
    }

    public static final class Timer {

        final ChunkDataProvider.Region innerCache;
        final StopWatch stopWatch = new StopWatch();

        Timer(long worldSeed, TFCChunkGenerator generator) {
            innerCache = ChunkDataProvider.tfcRegion(worldSeed, generator, false);
        }

        void start() {
            stopWatch.start();
        }

        int time() {
            final long nanos = stopWatch.getNanoTime();
            stopWatch.stop();
            stopWatch.reset();
            return (int) nanos;
        }
    }
}
