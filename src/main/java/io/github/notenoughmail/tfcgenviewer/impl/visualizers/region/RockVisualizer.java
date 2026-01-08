package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public class RockVisualizer implements RegionVisualizer {

    public static final Component NAME = Component.translatable("tfcgenviewer.preview_world.visualizer_type.rocks");

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache> info) {
        final Region.Point point = info.cache().getPoint(imageX, imageY, xPos, zPos);
        final Block raw = info.generator().rockLayerSettings().sampleAtLayer(point.rock, 0).raw();
        final ColorDefinition color = Colors.ROCK_COLORS.getOrUnknown(BuiltInRegistries.BLOCK.getKey(raw));
        color.addTooltip(info);
        image.setPixel(imageX, imageY, color.abgr());
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return Colors.ROCK_COLORS.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }
}
