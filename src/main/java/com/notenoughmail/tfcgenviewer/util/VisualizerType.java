package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.notenoughmail.tfcgenviewer.config.color.Colors;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.notenoughmail.tfcgenviewer.config.color.BiomeColors.Biomes;
import static com.notenoughmail.tfcgenviewer.config.color.Colors.*;
import static com.notenoughmail.tfcgenviewer.config.color.RockColors.Rocks;
import static com.notenoughmail.tfcgenviewer.util.ColorUtil.*;
import static com.notenoughmail.tfcgenviewer.util.ImageBuilder.setPixel;

public enum VisualizerType implements IExtensibleEnum {
    BIOMES(0b00100000, "biomes", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> setPixel(image, x, y, Biomes.color(point.biome, colorDescriptors)), Biomes.key()),
    RAINFALL(0b10000000, "rainfall", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> {
        if (point.land()) {
            final int color = Colors.RAINFALL.get().getColor(
                    Mth.clampedMap(
                            point.rainfall,
                            0F,
                            500F,
                            0,
                            1F
                    ), colorDescriptors
            );
            setPixel(image, x, y, color);
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors);
        }
    }, RainKey),
    TEMPERATURE(0b10000000, "temperature", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> {
        if (point.land()) {
            final int color = Colors.TEMPERATURE.get().getColor(
                    Mth.clampedMap(
                            point.temperature,
                            -20F,
                            30F,
                            0F,
                            1F
                    ),
                    colorDescriptors
            );
            setPixel(image, x, y, color);
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors);
        }
    }, TempKey),
    BIOME_ALTITUDE(0b00010000, "biome_altitude", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> {
        if (point.land()) {
            setPixel(image, x, y, biomeAltitude(point.discreteBiomeAltitude(), colorDescriptors));
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors);
        }
    }, BiomeAltKey),
    INLAND_HEIGHT(0b00100000, "inland_height", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> setPixel(image, x, y, inlandHeight(point, colorDescriptors)), InlandHeightKey),
    RIVERS(0b00010000, "rivers_and_mountains", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> {
        if (point.land()) {
            final int color;
            if (point.mountain()) {
                color = (point.baseLandHeight <= 2 ? RM_OCEANIC_VOLCANIC_MOUNTAINS : RM_INLAND_MOUNTAIN).get().color(colorDescriptors);
            } else if (point.lake()) {
                color = RM_LAKE.get().color(colorDescriptors);
            } else {
                color = biomeAltitude(point.discreteBiomeAltitude(), colorDescriptors);
            }
            setPixel(image, x, y, color);
            for (RiverEdge edge : generator.regionGenerator().getOrCreatePartitionPoint(xPos, zPos).rivers()) {
                final MidpointFractal fractal = edge.fractal();
                if (fractal.maybeIntersect(xPos, zPos, 0.1F) && fractal.intersect(xPos, zPos, 0.35F)) {
                    setPixel(image, x, y, RM_RIVER.get().color(colorDescriptors));
                    return; // Stop looking for rivers, we already found one
                }
            }
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors);
        }
    }, RiverKey),
    ROCK_TYPES(0b01000000, "rock_types", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> setPixel(image, x, y, rockType(point.rock, colorDescriptors)), RockTypeKey),
    ROCKS(0b01000000, "rocks", (x, y, xPos, zPos, generator, region, point, image, colorDescriptors) -> {
        final Block raw = generator.generateRock(xPos * 128 - 64, 90, zPos * 128 - 64, 100, null).raw();
        setPixel(image, x, y, Rocks.color(raw).color(colorDescriptors));
    }, Rocks.key());

    static {
        if (!FMLEnvironment.production) {
            create("DEV", 0, "dev", ColorUtil.dev, Component::empty);
        }
    }


    public static final VisualizerType[] VALUES = values();
    public static final Codec<VisualizerType> CODEC = Codec.intRange(0, VALUES.length - 1).xmap(b -> VALUES[b], Enum::ordinal);

    private final byte permission;
    private final Component name;
    private final DrawFunction drawer;
    private final Supplier<Component> colorKey;

    VisualizerType(int permission, String name, DrawFunction drawer, Supplier<Component> colorKey) {
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
        }
        return visualizers;
    }

    public Component getName() {
        return name;
    }

    public Component getColorKey() {
        return colorKey.get();
    }

    public void draw(int x, int y, int xPos, int zPos, RegionChunkDataGenerator generator, Region region, Region.Point point, NativeImage image, Int2ObjectOpenHashMap<Component> colorDescriptors) {
        drawer.draw(x, y, xPos, zPos, generator, region, point, image, colorDescriptors);
    }

    static VisualizerType create(String title, int permission, String name, DrawFunction drawer, Supplier<Component> colorKey) {
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
                NativeImage image,
                Int2ObjectOpenHashMap<Component> colorDescriptors
        );
    }
}
