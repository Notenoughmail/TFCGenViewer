package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorGradientDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

public class RockTypeVisualizer implements RegionVisualizer {

    public static final Component NAME = Component.translatable("tfcgenviewer.preview_world.visualizer_type.rock_types");

    public static final DataManager.Reference<ColorGradientDefinition> UPLIFT = Colors.COMMON_GRADIENTS.getReference(TFCGenViewer.id("rock_type/uplift"));
    public static final DataManager.Reference<ColorGradientDefinition> LAND = Colors.COMMON_GRADIENTS.getReference(TFCGenViewer.id("rock_type/land"));
    public static final DataManager.Reference<ColorGradientDefinition> VOLCANIC = Colors.COMMON_GRADIENTS.getReference(TFCGenViewer.id("rock_type/volcanic"));
    public static final DataManager.Reference<ColorGradientDefinition> OCEANIC = Colors.COMMON_GRADIENTS.getReference(TFCGenViewer.id("rock_type/oceanic"));

    public static final ColorKey COLOR_KEY = ColorKey.of(key -> {
        LAND.get().appendTo(key);
        OCEANIC.get().appendTo(key);
        VOLCANIC.get().appendTo(key);
        UPLIFT.get().appendTo(key, true);
    });

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache> info) {
        final Region.Point point = info.cache().getPoint(imageX, imageY, xPos, zPos);
        final double seed = new Random(point.rock >> 2).nextDouble();
        final ColorGradientDefinition gradient = (switch (point.rock & 0b11) {
            case 3 -> UPLIFT;
            case 2 -> LAND;
            case 1 -> VOLCANIC;
            default -> OCEANIC;
        }).get();
        image.setPixel(
                imageX,
                imageY,
                gradient.color(seed, info)
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
}
