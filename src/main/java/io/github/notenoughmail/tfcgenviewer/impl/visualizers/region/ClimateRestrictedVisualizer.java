package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.ClimateFeatureCache;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class ClimateRestrictedVisualizer implements IRegionVisualizerType<ClimateFeatureCache<RegionPointCache>, IVisualizerType.NoneOpt> {

    public static final Component NAME = Component.translatable("tfcgenviewer.visualizers.region.climate_restricted");

    @Override
    public ResourceLocation id() {
        return TFCGenViewerRegistration.VIZ_CLIMATE_FEATURE.id();
    }

    @Override
    public NoneOpt createOptions(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize scale) {
        return NoneOpt.INSTANCE;
    }

    @Override
    public ClimateFeatureCache<RegionPointCache> createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed) {
        return new ClimateFeatureCache<>(registryAccess, RegionPointCache.of(generator, size, worldSeed));
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, ClimateFeatureCache<RegionPointCache>, TFCRegionVisualizer.Scale, NoneOpt> info) {
        final RegionPointCache.RegionPoint regionPoint = info.cache().regionCache.getRegionPoint(imageX, imageY, xPos, zPos);
        final Region.Point point = regionPoint.point();
        final List<ColorDefinition> colors = info.cache().search(
                TFCLayers.getFromLayerId(point.biome).key(),
                point.temperature,
                point.rainfall,
                point.rainfallVariance
        );
        final int i = colors.size();
        switch (i) {
            case 0 -> {
                if (point.land()) {
                    final ColorDefinition color = ClimateFeatureCache.LAND.get();
                    color.addTooltip(info);
                    image.setPixel(
                            imageX,
                            imageY,
                            color.abgr()
                    );
                } else {
                    Colors.fillOcean(
                            regionPoint.region().noise() / 2,
                            imageX,
                            imageY,
                            image,
                            info
                    );
                }
            }
            case 1 -> {
                final ColorDefinition color = colors.getFirst();
                color.addTooltip(info);
                image.setPixel(
                        imageX,
                        imageY,
                        color.abgr()
                );
            }
            default -> {
                final int alpha = 0xFF / i;
                final MutableComponent tooltip = Component.translatable("tfcgenviewer.climate_features.multiple_present");

                final Iterator<ColorDefinition> iter = colors.iterator();
                final ColorDefinition first = iter.next();
                image.setPixel(imageX, imageY, first.abgr());
                tooltip.append(Component.translatable("tfcgenviewer.climate_features.list_entry", first.getTooltip()));

                while (iter.hasNext()) {
                    final ColorDefinition color = iter.next();
                    image.setPixel(imageX, imageY, color.abgr(alpha));
                    tooltip.append(Component.translatable("tfcgenviewer.climate_features.list_entry", color.getTooltip()));
                }

                info.colorDescriptors().putIfAbsent(
                        image.getABGRColor(imageX, imageY),
                        tooltip
                );
            }
        }
    }

    @Nullable
    @Override
    public Component additionalPreviewInfo(DrawInfo<TFCChunkGenerator, ClimateFeatureCache<RegionPointCache>, TFCRegionVisualizer.Scale, NoneOpt> info) {
        return Component.translatable("tfcgenviewer.preview_info.generated_regions", info.cache().regionCache.visitedRegions());
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, ClimateFeatureCache<RegionPointCache> cache) {
        return cache.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }
}
