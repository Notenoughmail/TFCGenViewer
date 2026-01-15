package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BiomeAltitudeVisualizer implements RegionVisualizerType.Simple {

    public static final DataManager.Reference<ColorDefinition> MOUNTAIN = color("mountain");
    public static final DataManager.Reference<ColorDefinition> HIGH = color("high");
    public static final DataManager.Reference<ColorDefinition> MID = color("mid");
    public static final DataManager.Reference<ColorDefinition> LOW = color("low");
    public static final DataManager.Reference<ColorDefinition> NEAR_ISLAND = color("near_island");

    public static final Component NAME = TFCGenViewerRegistration.regionVisualizerName(TFCGenViewerRegistration.VIZ_BIOME_ALT);

    public static final ColorKey COLOR_KEY = ColorKey.of(BiomeAltitudeVisualizer::keyColors);

    public static void keyColors(MutableComponent key) {
        NEAR_ISLAND.get().appendTo(key);
        LOW.get().appendTo(key);
        MID.get().appendTo(key);
        HIGH.get().appendTo(key);
        MOUNTAIN.get().appendTo(key);
        Colors.OCEAN.get().appendTo(key, true);
    }

    public static ColorDefinition getColor(int discreteHeight) {
        return (switch (discreteHeight) {
            case -1 -> NEAR_ISLAND;
            case 0 -> LOW;
            case 1 -> MID;
            case 2 -> HIGH;
            case 3 -> MOUNTAIN;
            default -> throw new IllegalStateException("Region point should have a discrete altitude in range [-1, 3], was %s".formatted(discreteHeight));
        }).get();
    }

    @Override
    public ResourceLocation id() {
        return TFCGenViewerRegistration.VIZ_BIOME_ALT.id();
    }

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, TFCRegionVisualizer.Scale, NoneOpt> info) {
        final RegionPointCache.RegionPoint pair = info.cache().getRegionPoint(imageX, imageY, xPos, zPos);
        if (pair.point().land()) {
            final ColorDefinition color = getColor(pair.point().discreteBiomeAltitude());
            color.addTooltip(info);
            image.setPixel(
                    imageX,
                    imageY,
                    color.abgr()
            );
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

    private static DataManager.Reference<ColorDefinition> color(String name) {
        return Colors.MISC_COLORS.getReference(TFCGenViewer.id("biome_altitude/" + name));
    }
}
