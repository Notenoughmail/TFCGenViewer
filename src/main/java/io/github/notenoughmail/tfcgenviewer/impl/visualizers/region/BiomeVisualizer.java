package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.color.RegistryLinkedColor;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.layer.TFCLayers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BiomeVisualizer implements IRegionVisualizerType<BiomeVisualizer.Cache, IVisualizerType.NoneOpt> {

    public static final Component NAME = TFCGenViewerRegistration.regionVisualizerName(TFCGenViewerRegistration.VIZ_BIOME);

    public static final ColorKey COLOR_KEY = ColorKey.of(key -> {
        Colors.BIOME_COLORS.getValues()
                .stream()
                .map(RegistryLinkedColor::color)
                .filter(c -> c != Colors.UNKNOWN_BIOME.get().color())
                .sorted()
                .forEach(c -> c.appendTo(key));
        Colors.UNKNOWN_BIOME.get().color().appendTo(key, true);
    });

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public ResourceLocation id() {
        return TFCGenViewerRegistration.VIZ_BIOME.id();
    }

    @Override
    public NoneOpt createOptions(RegistryAccess registryAccess) {
        return NoneOpt.INSTANCE;
    }

    @Override
    public Cache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed) {
        return new Cache(RegionPointCache.of(generator, size, worldSeed));
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, Cache, TFCRegionVisualizer.Scale, NoneOpt> info) {
        final ColorDefinition color = info.cache().getColor(imageX, imageY, xPos, zPos);
        color.addTooltip(info);
        image.setPixel(imageX, imageY, color.abgr());
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, Cache cache) {
        return COLOR_KEY.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }

    public static class Cache {

        private final RegionPointCache pointCache;
        private final ColorDefinition[] biomeColors;

        Cache(RegionPointCache pointCache) {
            this.pointCache = pointCache;
            biomeColors = new ColorDefinition[128]; // This will need to be updated if TFC ever expands
        }

        public ColorDefinition getColor(int imageX, int imageY, int xPos, int zPos) {
            final int biome = pointCache.getPoint(imageX, imageY, xPos, zPos).biome;
            if (biomeColors[biome] == null) {
                final BiomeExtension ext = TFCLayers.getFromLayerId(biome);
                final ColorDefinition color = Colors.BIOME_COLORS.getInstanceColor(ext.key(), Colors.UNKNOWN_BIOME);
                biomeColors[biome] = color;
            }
            return biomeColors[biome];
        }

    }
}
