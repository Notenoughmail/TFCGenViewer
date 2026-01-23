package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.cache.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class KoppenVisualizer implements RegionVisualizerType.Simple {

    public static final Component NAME = TFCGenViewerRegistration.visualizerName(TFCGenViewerRegistration.VIZ_KOPPEN);
    public static final Component DESC = TFCGenViewerRegistration.visualizerDescription(TFCGenViewerRegistration.VIZ_KOPPEN);

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, GridScale, NoneOpt> info) {
        final Region.Point point = info.cache().getPoint(imageX, imageY, xPos, zPos);
        final ColorDefinition color;
        if (point.land()) {
            color = Colors.KOPPEN_CLASSIFICATIONS.get(KoppenClimateClassification.classify(
                    point.temperature,
                    point.rainfall,
                    point.rainfallVariance,
                    info.cache().isNorthernHemisphere(zPos, info.scale())
            )).get();
        } else {
            color = Colors.KOPPEN_COLORS.unknown();
        }
        color.addTooltip(info);
        image.setPixel(imageX, imageY, color.abgr());
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return Colors.KOPPEN_COLORS.colorKey();
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
    public int sort() {
        return 60;
    }
}
