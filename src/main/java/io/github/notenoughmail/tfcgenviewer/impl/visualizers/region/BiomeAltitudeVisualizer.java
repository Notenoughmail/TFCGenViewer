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
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BiomeAltitudeVisualizer implements RegionVisualizerType.Simple {

    public static final DataManager.Reference<ColorDefinition> MOUNTAIN = color("mountain");
    public static final DataManager.Reference<ColorDefinition> HIGH = color("high");
    public static final DataManager.Reference<ColorDefinition> MID = color("mid");
    public static final DataManager.Reference<ColorDefinition> LOW = color("low");
    public static final DataManager.Reference<ColorDefinition> SHALLOW = color("shallow");
    public static final DataManager.Reference<ColorDefinition> DEEP = color("deep");
    public static final DataManager.Reference<ColorDefinition> VERY_DEEP = color("very_deep");

    public static final Component NAME = TFCGenViewerRegistration.regionVisualizerName(TFCGenViewerRegistration.VIZ_BIOME_ALT);

    public static final ColorKey COLOR_KEY = ColorKey.of(key -> {
        VERY_DEEP.get().appendTo(key);
        DEEP.get().appendTo(key);
        SHALLOW.get().appendTo(key);
        LOW.get().appendTo(key);
        MID.get().appendTo(key);
        HIGH.get().appendTo(key);
        MOUNTAIN.get().appendTo(key, true);
    });

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
        final Region.Point point = info.cache().getPoint(imageX, imageY, xPos, zPos);
        final ColorDefinition color = (point.land() ?
                switch (point.discreteBiomeAltitude()) {
                    case 0, -1 -> LOW; // *Supposedly* there's a secret 'near islands' height of -1, but it never showed up for me
                    case 1 -> MID;
                    case 2 -> HIGH;
                    case 3 -> MOUNTAIN;
                    default -> throw new IllegalStateException("Region point should have a discrete altitude in range [-1, 3], was %s".formatted(point.discreteBiomeAltitude()));
                } :
                point.baseOceanDepth < 4 ?
                        SHALLOW :
                        point.baseOceanDepth < 8 ?
                                DEEP :
                                VERY_DEEP).get();
        color.addTooltip(info);
        image.setPixel(
                imageX,
                imageY,
                color.abgr()
        );
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
