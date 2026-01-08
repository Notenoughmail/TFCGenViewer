package com.notenoughmail.tfcgenviewer.util;

import com.mojang.serialization.Codec;
import com.notenoughmail.tfcgenviewer.color.ColorDefinition;
import com.notenoughmail.tfcgenviewer.color.Colors;
import com.notenoughmail.tfcgenviewer.color.FeatureColors;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.client.OptionInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.notenoughmail.tfcgenviewer.color.BiomeColors.Biomes;
import static com.notenoughmail.tfcgenviewer.color.Colors.*;
import static com.notenoughmail.tfcgenviewer.color.FeatureColors.Features;
import static com.notenoughmail.tfcgenviewer.color.RockColors.Rocks;
import static com.notenoughmail.tfcgenviewer.util.ColorUtil.*;
import static com.notenoughmail.tfcgenviewer.util.Permissions.*;

public enum VisualizerType implements IExtensibleEnum {
    CLIMATE_FEATURES(CLIMATE_CHARACTERISTICS, "climate_features", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors, registryAccess) -> {
        Features.prime(registryAccess);
        final List<ColorDefinition> colors = Features.search(point.biome, point.temperature, point.rainfall);
        final int i = colors.size();
        switch (i) {
            case 0 -> {
                if (point.land()) {
                    image.setPixel(
                            x, y,
                            RT_LAND.get().getColor(
                                    region != null ?
                                            region.noise() :
                                            0,
                                    colorDescriptors
                            )
                    );
                } else {
                    fillOcean.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors, registryAccess);
                }
            }
            case 1 -> {
                final ColorDefinition color = colors.get(0);
                image.setPixel(x, y, color.color(colorDescriptors));
            }
            default -> {
                final int alpha = 0xFF / i;
                final MutableComponent tooltip = Component.empty().append(FeatureColors.MULTIPLE_FEATURES).append(CommonComponents.SPACE);

                final Iterator<ColorDefinition> iter = colors.iterator();
                final ColorDefinition first = iter.next();
                image.setPixel(x, y, first.color(0xFF));
                tooltip.append(first.tooltip());

                while (iter.hasNext()) {
                    final ColorDefinition color = iter.next();
                    image.setPixel(x, y, color.color(alpha));
                    tooltip.append(", ");
                    tooltip.append(color.tooltip());
                }

                colorDescriptors.putIfAbsent(image.getABGRColor(x, y), tooltip);
            }
        }
    }, Features),
    BIOME_ALTITUDE(ALTITUDE_CHARACTERISTICS, "biome_altitude", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors, registryAccess) -> {
        if (point.land()) {
            image.setPixel(x, y, biomeAltitude(point.discreteBiomeAltitude(), colorDescriptors));
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors, registryAccess);
        }
    }, BiomeAltKey),
    INLAND_HEIGHT(BIOME_CHARACTERISTICS, "inland_height", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors, registryAccess) -> image.setPixel(x, y, inlandHeight(point, colorDescriptors)), InlandHeightKey),
    RIVERS(ALTITUDE_CHARACTERISTICS, "rivers_and_mountains", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors, registryAccess) -> {
        if (point.land()) {
            final int color;
            if (point.mountain()) {
                color = (point.baseLandHeight <= 2 ? RM_OCEANIC_VOLCANIC_MOUNTAINS : RM_INLAND_MOUNTAIN).get().color(colorDescriptors);
            } else if (point.lake()) {
                color = RM_LAKE.get().color(colorDescriptors);
            } else {
                color = biomeAltitude(point.discreteBiomeAltitude(), colorDescriptors);
            }
            image.setPixel(x, y, color);

            for (RiverEdge edge : region.rivers()) {
                if (riverEdgeEncapsulates(edge, xPos, zPos)) {
                    final MidpointFractal fractal = edge.fractal();
                    if (fractal.maybeIntersect(xPos, zPos, 0.1F) && fractal.intersect(xPos, zPos, 0.35F)) {
                        image.setPixel(x, y, RM_RIVER.get().color(colorDescriptors));
                        return; // Stop looking for rivers, we already found one
                    }
                }
            }
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors, registryAccess);
        }
    }, RiverKey);

    static {
        if (!FMLEnvironment.production) {
            create("DEV", 0, "dev", dev, r -> Component.empty());
            create("BORDER", 0, "border", dev, r -> Component.empty());
            create("RIVER_EDGES", 0, "river_edges", riverEdgesDev, r -> Component.empty());
        }
    }

    public static final VisualizerType[] VALUES = values();
    public static final Codec<VisualizerType> CODEC = Codec.intRange(0, VALUES.length - 1).xmap(b -> VALUES[b], Enum::ordinal);

    private final byte permission;
    private final Component name;
    private final DrawFunction drawer;
    private final Function<RegistryAccess, Component> colorKey;

    VisualizerType(int permission, String name, DrawFunction drawer, Function<RegistryAccess, Component> colorKey) {
        this.permission = (byte) permission;
        this.name = Component.translatable("tfcgenviewer.preview_world.visualizer_type." + name);
        this.drawer = drawer;
        this.colorKey = colorKey;
    }

    public static OptionInstance<VisualizerType> option(List<VisualizerType> visualizers) {
        return new OptionInstance<>(
                "tfcgenviewer.preview_world.visualizer_type",
                OptionInstance.noTooltip(),
                (caption, task) -> task.getName(),
                new OptionInstance.Enum<>(visualizers, VisualizerType.CODEC),
                visualizers.contains(VisualizerType.RIVERS) ? VisualizerType.RIVERS : visualizers.get(0),
                task -> {}
        );
    }

    public static List<VisualizerType> getVisualizers(byte permission) {
        final List<VisualizerType> visualizers = new ArrayList<>();
        for (VisualizerType type : VALUES) {
            if ((type.permission & permission) != 0) visualizers.add(type);
        }
        if (!FMLEnvironment.production) {
            visualizers.add(valueOf("DEV"));
            visualizers.add(valueOf("BORDER"));
            visualizers.add(valueOf("RIVER_EDGES"));
        }
        return visualizers;
    }

    public Component getName() {
        return name;
    }

    public Component getColorKey(RegistryAccess registryAccess) {
        return colorKey.apply(registryAccess);
    }

    public void draw(
            int x,
            int y,
            int xPos,
            int zPos,
            RegionChunkDataGenerator generator,
            Region region,
            Region.Point point,
            MutableImage image,
            Int2ObjectOpenHashMap<Component> colorDescriptors,
            RegistryAccess registryAccess
    ) {
        drawer.draw(
                x,
                y,
                xPos,
                zPos,
                generator,
                region,
                point,
                image,
                colorDescriptors,
                registryAccess
        );
    }

    static VisualizerType create(String title, int permission, String name, DrawFunction drawer, Function<RegistryAccess, Component> colorKey) {
        throw new IllegalStateException("VisualizerType not extended");
    }

    @FunctionalInterface
    public interface DrawFunction {
        void draw(
                int x,
                int y,
                int xPos,
                int zPos,
                RegionChunkDataGenerator generator,
                Region region,
                Region.Point point,
                MutableImage image,
                Int2ObjectOpenHashMap<Component> colorDescriptors,
                RegistryAccess registryAccess
        );
    }
}
