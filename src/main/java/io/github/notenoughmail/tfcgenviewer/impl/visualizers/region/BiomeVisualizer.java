package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BiomeVisualizer implements RegionVisualizerType.Simple {

    public static final Component NAME = TFCGenViewerRegistration.regionVisualizerName(TFCGenViewerRegistration.VIZ_BIOME);

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public ResourceLocation id() {
        return TFCGenViewerRegistration.VIZ_BIOME.id();
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, TFCRegionVisualizer.Scale, NoneOpt> info) {
        final Region.Point point = info.cache().getPoint(imageX, imageY, xPos, zPos);
        final ResourceLocation biome = TFCBiomes.REGISTRY.getKey(TFCLayers.getFromLayerId(point.biome));
        assert biome != null;
        final ColorDefinition color = Colors.BIOME_COLORS.getOrUnknown(biome);
        color.addTooltip(info);
        image.setPixel(imageX, imageY, color.abgr());
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return Colors.BIOME_COLORS.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }
}
