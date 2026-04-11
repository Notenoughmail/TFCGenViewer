package io.github.notenoughmail.tfcgenviewer.viz;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RegionVisualizerType;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class RegionPositionVisualizer implements RegionVisualizerType.Simple {

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, GridScale, NoneOpt> info) {
        final Region region = info.cache().getRegion(imageX, imageY, xPos, zPos);
        if (region.isIn(xPos, zPos)) {
            final int color = TFCGenViewerRegistration.GRAD_GREEN.get()
                    .applyAsAbgr((region.noise() + 1) * 0.5);
            image.setPixel(imageX, imageY, color);
            if (!info.colorTooltips().hasColor(color)) {
                info.colorTooltips().addTooltip(color, Component.literal("%s".formatted(region)));
            }
        } else {
            image.setPixel(imageX, imageY, 0xFFFFFFFF);
        }
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return Component.empty();
    }

    @Override
    public Component name() {
        return Component.literal("Region Position");
    }

    @Override
    public Component description() {
        return Component.literal("Region position");
    }
}
