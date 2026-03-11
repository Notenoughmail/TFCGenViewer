package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.cache.RockCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor.TFCChunkGeneratorAccessor;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RockVisualizer;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import static io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RockVisualizer.*;

public class ChunkRockVisualizer implements ITFCChunkVisualizerType<RockCache<ChunkDataProvider.Region>, RockVisualizer.Options> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_ROCK);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_ROCK);

    @Override
    public int sort() {
        return 50;
    }

    @Override
    public RockVisualizer.Options createOptions(RegistryAccess registryAccess) {
        return new RockVisualizer.Options();
    }

    @Override
    public void addOptions(OptionProvider optionProvider, RockVisualizer.Options options) {
        optionProvider.orderBool("tfcgenviewer.option.region_visualizer.rock.mode", options.surface, b -> options.surface = b)
                .withDisplay(optionProvider.genericDisplay(b -> b ? MODE_SURFACE : MODE_ELEVATION))
                .withConstantTooltip(MODE_EXP)
                .finish();
        optionProvider.orderInt("tfcgenviewer.option.region_visualizer.rock.elevation", options.elevation, -64, 320, i -> options.elevation = i)
                .withConstantTooltip(ELEVATION_EXP)
                .finish(() -> !options.surface);
    }

    @Override
    public RockCache<ChunkDataProvider.Region> createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, RockVisualizer.Options options) {
        final Seed seed = Seed.of(worldSeed);
        final RockCache<ChunkDataProvider.Region> region = new RockCache<>(ChunkDataProvider.tfcRegion(seed, generator));
        ((TFCChunkGeneratorAccessor) generator).tfcgenviewer$SetSeed(seed);
        ((TFCChunkGeneratorAccessor) generator).tfcgenviewer$SetTideHeightNoise(BiomeNoise.shoreTideLevelNoise(seed));
        return region;
    }

    // TODO: 2.1.0 | This is slow for the same reasons as the elevation viz
    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RockCache<ChunkDataProvider.Region>, ChunkScale, RockVisualizer.Options> info) {
        final ChunkData data = info.cache().innerCache.create(xPos, zPos);
        final Block raw;
        if (info.options().surface) {
            raw = info.scale()
                    .evaluateAtPosition(
                            true,
                            xPos,
                            zPos,
                            data.getRockData()::getSurfaceRock
                    )
                    .raw();
        } else {
            raw = info.cache()
                    .innerCache
                    .generator()
                    .generateRock(
                            info.scale().pixelResolutionToBlock(xPos, true),
                            info.options().elevation,
                            info.scale().pixelResolutionToBlock(zPos, true),
                            info.scale().evaluateAtPosition(true, xPos, zPos, info.generator().createHeightFillerForChunk(data.getPos())::sampleHeight).intValue(),
                            null
                    )
                    .raw();
        }
        final ColorDefinition color = info.cache().getColor(raw);
        info.addTooltip(color);
        image.setPixel(imageX, imageY, color);
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RockCache<ChunkDataProvider.Region> cache) {
        return cache.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Override
    public Component description() {
        return DESC;
    }

    @Nullable
    @Override
    public Component additionalPreviewInfo(DrawInfo<TFCChunkGenerator, RockCache<ChunkDataProvider.Region>, ChunkScale, RockVisualizer.Options> info) {
        if (info.options().surface) {
            return null;
        } else {
            return Component.translatable("tfcgenviewer.preview_info.generated_rock_chunk", info.options().elevation);
        }
    }

    @Override
    public boolean shouldDrawInParallel(RockVisualizer.Options options, ImageSize size) {
        return !options.surface;
    }
}
