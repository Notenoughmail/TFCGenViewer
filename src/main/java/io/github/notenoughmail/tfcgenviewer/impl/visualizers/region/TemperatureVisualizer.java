package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorGradientDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class TemperatureVisualizer implements RegionVisualizer {

    public static final Component NAME = Component.translatable("tfcgenviewer.preview_world.visualizer_type.temperature");

    public static final DataManager.Reference<ColorGradientDefinition> TEMPERATURE = Colors.COMMON_GRADIENTS.getReference(TFCGenViewer.id("temperature"));

    public static final ColorKey COLOR_KEY = ColorKey.of(m -> {
        TEMPERATURE.get().appendTo(m);
        Colors.OCEAN.get().appendTo(m, true);
    });

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache> info) {
        final RegionPointCache.RegionPoint pair = info.cache().getRegionPoint(imageX, imageY, xPos, zPos);
        if (pair.point().land()) {
            final int color = TEMPERATURE.get().color(
                    Mth.clampedMap(
                            pair.point().temperature,
                            -23F,
                            33F,
                            0F,
                            1F
                    ), info
            );
            image.setPixel(imageX, imageY, color);
        } else {
            Colors.fillOcean(
                    pair.region().noise() / 2,
                    imageX,
                    imageY,
                    image,
                    info
            );
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return COLOR_KEY.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }
}
