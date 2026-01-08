package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BiomeVisualizer implements RegionVisualizer {

    public static final Component NAME = Component.translatable("tfcgenviewer.preview_world.visualizer_type.biomes");

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache> info) {
        final Region.Point point = info.cache().getPoint(imageX, imageY, xPos, zPos);
        final ResourceLocation biome = TFCBiomes.REGISTRY.getKey(TFCLayers.getFromLayerId(point.biome));
        assert biome != null;
        final ColorDefinition color = Colors.BIOME_COLORS.getOrUnknown(biome);
        image.setPixel(imageX, imageY, color.abgr());
        color.addTooltip(info);
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
