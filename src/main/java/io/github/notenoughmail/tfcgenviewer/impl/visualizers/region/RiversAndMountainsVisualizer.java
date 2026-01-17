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
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.region.Units;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class RiversAndMountainsVisualizer implements RegionVisualizerType<RiversAndMountainsVisualizer.Options> {

    public static final Component NAME = TFCGenViewerRegistration.regionVisualizerName(TFCGenViewerRegistration.VIZ_RIVERS_AND_MOUNTAINS);

    public static final DataManager.Reference<ColorDefinition> RIVER = color("river");
    public static final DataManager.Reference<ColorDefinition> INLAND_MOUNTAIN = color("inland_mountain");
    public static final DataManager.Reference<ColorDefinition> COASTAL_MOUNTAIN = color("coastal_mountain");
    public static final DataManager.Reference<ColorDefinition> HOT_SPOT_AGE_4 = color("hot_spot_age_4");
    public static final DataManager.Reference<ColorDefinition> HOT_SPOT_AGE_3 = color("hot_spot_age_3");
    public static final DataManager.Reference<ColorDefinition> HOT_SPOT_AGE_2 = color("hot_spot_age_2");
    public static final DataManager.Reference<ColorDefinition> HOT_SPOT_AGE_1 = color("hot_spot_age_1");

    public static final ColorKey COLOR_KEY = ColorKey.of(key -> {
        RIVER.get().appendTo(key);
        COASTAL_MOUNTAIN.get().appendTo(key);
        INLAND_MOUNTAIN.get().appendTo(key);
        HOT_SPOT_AGE_1.get().appendTo(key);
        HOT_SPOT_AGE_2.get().appendTo(key);
        HOT_SPOT_AGE_3.get().appendTo(key);
        HOT_SPOT_AGE_4.get().appendTo(key);
        BiomeAltitudeVisualizer.keyColors(key);
    });

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, TFCRegionVisualizer.Scale, Options> info) {
        final RegionPointCache.RegionPoint pair = info.cache().getRegionPoint(imageX, imageY, xPos, zPos);
        final Region.Point point = pair.point();
        if (point.land()) {
            final ColorDefinition color = point.hotSpotAge > 0 ?
                    hotSpot(point) :
                    point.coastalMountain() ?
                            COASTAL_MOUNTAIN.get() :
                            point.mountain() ?
                                    INLAND_MOUNTAIN.get() :
                                    BiomeAltitudeVisualizer.getColor(point.discreteBiomeAltitude());
            color.addTooltip(info);
            image.setPixel(imageX, imageY, color.abgr());

            for (RiverEdge edge : pair.region().rivers()) {
                if (riverEdgeEncapsulates(edge, xPos, zPos)) {
                    final MidpointFractal fractal = edge.fractal();
                    if (fractal.maybeIntersect(xPos, zPos, 0.1) && fractal.intersect(xPos, zPos, info.options().sensitivity)) {
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
        } else if (point.hotSpotAge > 0) {
            final ColorDefinition color = hotSpot(point);
            color.addTooltip(info);
            image.setPixel(imageX, imageY, color.abgr());
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
        return TFCGenViewerRegistration.VIZ_RIVERS_AND_MOUNTAINS.id();
    }

    @Override
    public Options createOptions(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize scale) {
        return new Options();
    }

    @Override
    public void addOptions(OptionRequest optionRequest, Options options) {
        optionRequest.orderDouble("tfcgenviewer.option.region_visualizer.rivers_and_mountains.sensitivity", 0.35D, 0.01D, 0.75D, d -> options.sensitivity = d)
                .withDisplay((c, d) -> Component.translatable(
                        "tfcgenviewer.option.region_visualizer.rivers_and_mountains.sensitivity.value",
                        Math.round(Mth.map(d, 0.01D, 0.75D, 0D, 1D) * 100)
                ))
                .finalizeOrder();
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

    private static ColorDefinition hotSpot(Region.Point point) {
        return (switch (point.hotSpotAge) {
            case 1 -> HOT_SPOT_AGE_1;
            case 2 -> HOT_SPOT_AGE_2;
            case 3 -> HOT_SPOT_AGE_3;
            case 4 -> HOT_SPOT_AGE_4;
            default -> throw new IllegalArgumentException("hot spot ages should be in range [0, 4]");
        }).get();
    }

    private static DataManager.Reference<ColorDefinition> color(String path) {
        return Colors.MISC_COLORS.getReference(TFCGenViewer.id("rivers_and_mountains/" + path));
    }

    public static class Options implements IVisualizerType.Options<Options> {

        double sensitivity = 0.35D;

        @Override
        public Options copy() {
            final Options copy = new Options();
            copy.sensitivity = sensitivity;
            return copy;
        }
    }
}
