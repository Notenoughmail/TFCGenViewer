package io.github.notenoughmail.tfcgenviewer.api.color;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.color.manager.ColorManager;
import io.github.notenoughmail.tfcgenviewer.api.color.manager.RegistryLinkedColorManager;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.util.data.DataManager.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;

public interface Colors {

    ResourceLocation UNKNOWN = TFCGenViewer.id("unknown");

    DataManager<ColorDefinition> MISC_COLORS = new DataManager<>(TFCGenViewer.id("misc_color"), ColorDefinition.CODEC);

    RegistryLinkedColorManager<Biome> BIOME_COLORS = new RegistryLinkedColorManager<>(TFCGenViewer.id("color/biome"), Registries.BIOME);
    Reference<RegistryLinkedColor<Biome>> UNKNOWN_BIOME = BIOME_COLORS.getReference(UNKNOWN);
    ColorManager ROCK_COLORS = new ColorManager(TFCGenViewer.id("color/rock"));
    ColorManager KOPPEN_COLORS = new ColorManager(TFCGenViewer.id("color/koppen_classification"));

    Map<KoppenClimateClassification, Reference<ColorDefinition>> KOPPEN_CLASSIFICATIONS = Helpers.mapOf(KoppenClimateClassification.class, k -> KOPPEN_COLORS.getReference(TFCGenViewer.id(k.getSerializedName())));

    DataManager<ColorGradientDefinition> MISC_GRADIENTS = new DataManager<>(TFCGenViewer.id("gradients"), ColorGradientDefinition.CODEC);
    Reference<ColorGradientDefinition> OCEAN = MISC_GRADIENTS.getReference(TFCGenViewer.id("ocean"));

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
        fillOcean(value, x, y, image, info.colorTooltips());
    }
}
