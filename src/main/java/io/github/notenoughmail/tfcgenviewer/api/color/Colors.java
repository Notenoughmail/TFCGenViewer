package io.github.notenoughmail.tfcgenviewer.api.color;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.util.data.DataManager.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface Colors {

    ResourceLocation UNKNOWN = TFCGenViewer.id("unknown");

    ColorManager BIOME_COLORS = new ColorManager(TFCGenViewer.id("color/biome"));
    ColorManager ROCK_COLORS = new ColorManager(TFCGenViewer.id("color/rock"));
    ColorManager KOPPEN_COLORS = new ColorManager(TFCGenViewer.id("color/koppen_classification"));

    Map<KoppenClimateClassification, Reference<ColorDefinition>> KOPPENS = Helpers.mapOf(KoppenClimateClassification.class, k -> KOPPEN_COLORS.getReference(TFCGenViewer.id(k.getSerializedName())));

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

    static void fillOcean(double value, int x, int y, MutableImage image, IVisualizerType.DrawInfo<?, ?, ?, ?> info) {
        fillOcean(value, x, y, image, info.colorDescriptors());
    }
}
