package io.github.notenoughmail.tfcgenviewer.api.color;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.util.data.DataManager.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface Colors {

    ResourceLocation UNKNOWN = TFCGenViewer.id("unknown");

    ColorManager BIOME_COLORS = new ColorManager(TFCGenViewer.id("color/biome_extension/tfc"));
    ColorManager ROCK_COLORS = new ColorManager(TFCGenViewer.id("color/rock/tfc"));

    DataManager<ColorGradientDefinition> COMMON_GRADIENTS = new DataManager<>(TFCGenViewer.id("gradient"), ColorGradientDefinition.CODEC);
    Reference<ColorGradientDefinition> OCEAN = COMMON_GRADIENTS.getReference(TFCGenViewer.id("ocean"));

    static void fillOcean(double value, int x, int y, MutableImage image, Int2ObjectOpenHashMap<Component> colorDescriptors) {
        image.setPixel(
                x, y,
                OCEAN.get().color(
                        value,
                        colorDescriptors
                )
        );
    }

    static void fillOcean(double value, int x, int y, MutableImage image, IVisualizer.DrawInfo<?, ?> info) {
        fillOcean(value, x, y, image, info.colorDescriptors());
    }
}
