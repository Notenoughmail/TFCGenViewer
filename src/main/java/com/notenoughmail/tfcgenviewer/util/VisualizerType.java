package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.notenoughmail.tfcgenviewer.config.color.Colors;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.river.MidpointFractal;
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
    BIOMES(0b00100000, "biomes", (x, y, xPos, zPos, generator, region, point, image) -> setPixel(image, x, y, Biomes.color(point.biome)), Biomes.key()),
    RAINFALL(0b10000000, "rainfall", (x, y, xPos, zPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, Colors.RAINFALL.get().gradient().applyAsInt(Mth.clampedMap(point.rainfall, 0F, 500F, 0, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image);
        }
    }, RainKey),
    TEMPERATURE(0b10000000, "temperature", (x, y, xPos, zPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, Colors.TEMPERATURE.get().gradient().applyAsInt(Mth.clampedMap(point.temperature, -33F, 33F, 0F, 0.999F)));
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image);
        }
    }, TempKey),
    BIOME_ALTITUDE(0b00010000, "biome_altitude", (x, y, xPos, zPos, generator, region, point, image) -> {
        if (point.land()) {
            setPixel(image, x, y, biomeAltitude(point.discreteBiomeAltitude()));
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image);
        }
    }, BiomeAltKey),
    INLAND_HEIGHT(0b00100000, "inland_height", (x, y, xPos, zPos, generator, region, point, image) -> setPixel(image, x, y, inlandHeight(point)), InlandHeightKey),
    RIVERS(0b00010000, "rivers_and_mountains", (x, y, xPos, zPos, generator, region, point, image) -> {
        if (point.land()) {
            final int color;
            if (point.mountain()) {
                color = (point.baseLandHeight <= 2 ? RM_OCEANIC_VOLCANIC_MOUNTAINS : RM_INLAND_MOUNTAIN).get().color();
            } else if (point.lake()) {
                color = RM_LAKE.get().color();
            } else {
                color = biomeAltitude(point.discreteBiomeAltitude());
            }
            setPixel(image, x, y, color);
            for (RiverEdge edge : generator.regionGenerator().getOrCreatePartitionPoint(xPos, zPos).rivers()) {
                final MidpointFractal fractal = edge.fractal();
                if (fractal.maybeIntersect(xPos, zPos, 0.1F) && fractal.intersect(xPos, zPos, 0.35F)) {
                    setPixel(image, x, y, RM_RIVER.get().color());
                    return; // Stop looking for rivers, we already found one
                }
            }
        } else {
            fillOcean.draw(x, y, xPos, zPos, generator, region, point, image);
        }
    }, RiverKey),
    ROCK_TYPES(0b01000000, "rock_types", (x, y, xPos, zPos, generator, region, point, image) -> setPixel(image, x, y, rockType(point.rock)), RockTypeKey),
    ROCKS(0b01000000, "rocks", (x, y, xPos, zPos, generator, region, point, image) -> {
        final Block raw = generator.generateRock(xPos * 128 - 64, 90, zPos * 128 - 64, 100, null).raw();
        setPixel(image, x, y, Rocks.color(raw));
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

    public void draw(int x, int y, int xPos, int zPos, RegionChunkDataGenerator generator, Region region, Region.Point point, NativeImage image) {
        drawer.draw(x, y, xPos, zPos, generator, region, point, image);
    }

    static VisualizerType create(String title, int permission, String name, DrawFunction drawer, Supplier<Component> colorKey) {
        throw new IllegalStateException("VisualizerYpe not extended");
    }

    @FunctionalInterface
    public interface DrawFunction {
        void draw(int x, int y, int xPos, int zPos, RegionChunkDataGenerator generator, Region region, Region.Point point, NativeImage image);
    }
}
