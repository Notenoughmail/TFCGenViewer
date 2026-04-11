package io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import io.github.notenoughmail.tfcgenviewer.api.cache.ChunkDataProvider;
import io.github.notenoughmail.tfcgenviewer.api.cache.ClimateFeatureCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class ChunkClimateRestrictedVisualizer implements ITFCChunkVisualizerType.Simple<ClimateFeatureCache<ChunkDataProvider.Region>> {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_CHUNK_CLIMATE_FEATURE);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_CHUNK_CLIMATE_FEATURE);

    @Override
    public int sort() {
        return 70;
    }

    @Override
    public ClimateFeatureCache<ChunkDataProvider.Region> createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options, DrawParallelism parallelism) {
        return new ClimateFeatureCache<>(registryAccess, ChunkDataProvider.tfcRegion(worldSeed, generator, parallelism.parallel()));
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ClimateFeatureCache<ChunkDataProvider.Region>, ChunkScale, NoneOpt> info) {
        final ChunkData data = info.cache().innerCache.create(xPos, zPos);
        final BiomeExtension biome = info.cache().innerCache.getBiome(xPos, zPos, false);
        final float rainVariance = data.getRainVariance(7, 7);
        final List<ColorDefinition> colors = info.cache().search(
                biome.key(),
                data.getAverageSeaLevelTemp(7, 7),
                data.getAverageRainfall(7, 7),
                rainVariance
        );
        final int i = colors.size();
        switch (i) {
            case 0 -> {
                if (info.cache().innerCache.isOceanBiome(biome)) {
                    Colors.fillOcean(
                            (rainVariance + 1) / 2,
                            imageX,
                            imageY,
                            image,
                            info
                    );
                } else {
                    final ColorDefinition color = ClimateFeatureCache.LAND.get();
                    info.addTooltip(color);
                    image.setPixel(
                            imageX,
                            imageY,
                            color
                    );
                }
            }
            case 1 -> {
                final ColorDefinition color = colors.getFirst();
                info.addTooltip(color);
                image.setPixel(
                        imageX,
                        imageY,
                        color
                );
            }
            default -> {
                final int alpha = 0xFF / i;
                final MutableComponent tooltip = Component.translatable("tfcgenviewer.climate_features.multiple_present");

                final Iterator<ColorDefinition> iter = colors.iterator();
                final ColorDefinition first = iter.next();
                image.setPixel(imageX, imageY, first);
                tooltip.append(Component.translatable("tfcgenviewer.climate_features.list_entry", first.getTooltip()));

                while (iter.hasNext()) {
                    final ColorDefinition color = iter.next();
                    image.setPixel(imageX, imageY, color.abgr(alpha));
                    tooltip.append(Component.translatable("tfcgenviewer.climate_features.list_entry", color.getTooltip()));
                }

                info.colorTooltips().addTooltip(
                        image.getABGRColor(imageX, imageY),
                        tooltip
                );
            }
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ClimateFeatureCache<ChunkDataProvider.Region> cache) {
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

    @Override
    public void additionalSynchronization(SynchronizationRequest synchronizationRequest) {
        ClimateFeatureCache.syncRequest(synchronizationRequest, null);
    }

    @Nullable
    @Override
    public <T> Codec<T> elementCodecForRegistry(ResourceKey<? extends Registry<T>> registry) {
        return ClimateFeatureCache.codecForRegistry(registry);
    }

    @Override
    public boolean shouldDrawInParallel(NoneOpt options, ImageSize size) {
        return true;
    }
}
