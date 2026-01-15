package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionRequest;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.region.Units;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class RiversAndMountainsVisualizer implements RegionVisualizerType<RiversAndMountainsVisualizer.Options> {

    public static final Component NAME = TFCGenViewerRegistration.regionVisualizerName(TFCGenViewerRegistration.VIZ_RIVERS_AND_MOUNTINS);

    public static final DataManager.Reference<ColorDefinition> RIVER = Colors.MISC_COLORS.getReference(TFCGenViewer.id("rivers_and_mountains/river"));
    public static final DataManager.Reference<ColorDefinition> INLAND_MOUNTAIN = Colors.MISC_COLORS.getReference(TFCGenViewer.id("rivers_and_mountains/inland_mountain"));
    public static final DataManager.Reference<ColorDefinition> COASTAL_MOUNTAIN = Colors.MISC_COLORS.getReference(TFCGenViewer.id("rivers_and_mountains/coastal_mountain"));

    public static final ColorKey COLOR_KEY = ColorKey.of(key -> {
        RIVER.get().appendTo(key);
        COASTAL_MOUNTAIN.get().appendTo(key);
        INLAND_MOUNTAIN.get().appendTo(key);
        BiomeAltitudeVisualizer.keyColors(key);
    });

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, TFCRegionVisualizer.Scale, Options> info) {
        final RegionPointCache.RegionPoint pair = info.cache().getRegionPoint(imageX, imageY, xPos, zPos);
        if (pair.point().land()) {
            final ColorDefinition color = pair.point().coastalMountain() ?
                    COASTAL_MOUNTAIN.get() :
                    pair.point().mountain() ?
                            INLAND_MOUNTAIN.get() :
                            BiomeAltitudeVisualizer.getColor(pair.point().discreteBiomeAltitude());
            color.addTooltip(info);
            image.setPixel(imageX, imageY, color.abgr());

            for (RiverEdge edge : pair.region().rivers()) {
                if (riverEdgeEncapsulates(edge, xPos, zPos)) {
                    final MidpointFractal fractal = edge.fractal();
                    if (fractal.maybeIntersect(xPos, zPos, info.options().sensitivity)) {
                        RIVER.get().addTooltip(info);
                        image.setPixel(
                                imageX,
                                imageY,
                                RIVER.get().abgr()
                        );
                        return;
                    }
                }
            }
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
    public ResourceLocation id() {
        return TFCGenViewerRegistration.VIZ_RIVERS_AND_MOUNTINS.id();
    }

    @Override
    public Options createOptions(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize scale) {
        return new Options();
    }

    @Override
    public void addOptions(OptionRequest optionRequest, Options options) {
        optionRequest.orderDouble("tfcgenviewer.option.region_visualizer.rivers_and_mountains.sensitivity", 0.1D, 0.01D, 0.75D, d -> options.sensitivity = d)
                .withDisplay(IVisualizerType.Options.genericCaption(d -> Component.literal(Math.round(Mth.map(d, 0.01D, 0.75D, 0D, 1D)) + "%")));
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return COLOR_KEY.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }

    public static boolean riverEdgeEncapsulates(RiverEdge edge, int gridX, int gridZ) {
        return  gridX >= Units.partToGrid(edge.minPartX) &&
                gridX <= Units.partToGrid(edge.maxPartX) &&
                gridZ >= Units.partToGrid(edge.minPartZ) &&
                gridZ <= Units.partToGrid(edge.maxPartZ);
    }

    public static class Options implements IVisualizerType.Options<Options> {

        double sensitivity = 0.1D;

        @Override
        public Options copy() {
            final Options copy = new Options();
            copy.sensitivity = sensitivity;
            return copy;
        }
    }
}
