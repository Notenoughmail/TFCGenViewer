package io.github.notenoughmail.tfcgenviewer.api.color;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.ColorTooltips;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.color.manager.ColorManager;
import io.github.notenoughmail.tfcgenviewer.api.color.manager.RegistryLinkedColorManager;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.util.data.DataManager.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;

/**
 * Basic and common colors
 */
public interface Colors {

    /**
     * The id of all 'unknown' colors
     */
    ResourceLocation UNKNOWN = TFCGenViewer.id("unknown");

    /**
     * A general data manager for miscellaneous {@link ColorDefinition}s
     */
    DataManager<ColorDefinition> MISC_COLORS = new DataManager<>(TFCGenViewer.id("misc_color"), ColorDefinition.CODEC);

    /**
     * The {@link RegistryLinkedColorManager} for biome colors
     */
    RegistryLinkedColorManager<Biome> BIOME_COLORS = new RegistryLinkedColorManager<>(TFCGenViewer.id("color/biome"), Registries.BIOME);
    /**
     * The unknown biome reference
     */
    Reference<RegistryLinkedColor<Biome>> UNKNOWN_BIOME = BIOME_COLORS.getReference(UNKNOWN);
    /**
     * The {@link ColorManager} for rocks
     */
    ColorManager ROCK_COLORS = new ColorManager(TFCGenViewer.id("color/rock"));
    /**
     * The {@link ColorManager} for Köppen colors
     */
    ColorManager KOPPEN_COLORS = new ColorManager(TFCGenViewer.id("color/koppen_classification"));

    Map<KoppenClimateClassification, Reference<ColorDefinition>> KOPPEN_CLASSIFICATIONS = Helpers.mapOf(KoppenClimateClassification.class, k -> KOPPEN_COLORS.getReference(TFCGenViewer.id(k.getSerializedName())));

    /**
     * A general data manager for miscellaneous {@link ColorGradientDefinition}s
     */
    DataManager<ColorGradientDefinition> MISC_GRADIENTS = new DataManager<>(TFCGenViewer.id("gradients"), ColorGradientDefinition.CODEC);
    /**
     * The color gradient used for filling oceans, see {@link #fillOcean(double, int, int, MutableImage, IVisualizerType.DrawInfo) #fillOcean}
     */
    Reference<ColorGradientDefinition> OCEAN = MISC_GRADIENTS.getReference(TFCGenViewer.id("ocean"));

    /**
     * Set the given pixel on the image to the {@link #OCEAN} value
     */
    static void fillOcean(double value, int x, int y, MutableImage image, ColorTooltips colorDescriptors) {
        image.setPixel(
                x, y,
                OCEAN.get().color(
                        value,
                        colorDescriptors
                )
        );
    }

    static void fillOcean(double value, int x, int y, MutableImage image, IVisualizerType.DrawInfo<?, ?, ?, ?> info) {
        fillOcean(value, x, y, image, info.colorTooltips());
    }
}
