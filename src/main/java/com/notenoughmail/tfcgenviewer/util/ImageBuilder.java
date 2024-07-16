package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.notenoughmail.tfcgenviewer.Config;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.region.Region;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

import static net.minecraft.util.FastColor.ABGR32.*;

// Unless otherwise stated, everything here uses ABGR color
public class ImageBuilder {

    private static DynamicTexture PREVIEW;
    private static final ResourceLocation PREVIEW_LOCATION = TFCGenViewer.identifier("preview");

    public static ResourceLocation getPreview() {
        if (PREVIEW == null) {
            PREVIEW = new DynamicTexture(previewSize(), previewSize(), false);
            Minecraft.getInstance().getTextureManager().register(PREVIEW_LOCATION, PREVIEW);
        }
        return PREVIEW_LOCATION;
    }

    public static DoubleToIntFunction linearGradient(int from, int to) {
        return value -> color(
                Mth.lerpInt((float) value, alpha(from), alpha(to)),
                Mth.lerpInt((float) value, blue(from), blue(to)),
                Mth.lerpInt((float) value, green(from), green(to)),
                Mth.lerpInt((float) value, red(from), red(to))
        );
    }

    public static DoubleToIntFunction multiLinearGradient(int... colors) {
        final DoubleToIntFunction[] parts = IntStream.range(0, colors.length - 1)
                .mapToObj(i -> linearGradient(colors[i], colors[i + 1]))
                .toArray(DoubleToIntFunction[]::new);
        return value -> parts[Mth.floor(value * parts.length)].applyAsInt((value * parts.length) % 1);
    }
    
    @Nullable
    private static Integer PREVIEW_SIZE;
    @Nullable
    private static Integer LINE_WIDTH;
    @Nullable
    private static String PREVIEW_SIZE_KM;

    public static int previewSize() {
        if (PREVIEW_SIZE == null) {
            PREVIEW_SIZE = Config.previewSize.get();
        }
        return PREVIEW_SIZE;
    }

    public static int lineWidth() {
        if (LINE_WIDTH == null) {
            LINE_WIDTH = previewSize() / 512;
        }
        return LINE_WIDTH;
    }

    public static String previewSizeKm() {
        if (PREVIEW_SIZE_KM == null) {
            PREVIEW_SIZE_KM = String.format("%.1f", previewSize() * 128 / 1000F);
        }
        return PREVIEW_SIZE_KM;
    }

    // Misc colors
    public static final int RIVER_BLUE = color(255, 250, 180, 100);
    public static final int CLEAR = 0x00000000;
    public static final int SHORT_MOUNTAIN = color(255, 50, 110, 240);
    public static final int GRAY = color(255, 150, 150, 150);
    public static final int DARK_GRAY = color(255, 50, 50, 50);
    public static final int SPAWN_RED = color(255, 48, 15, 198);

    // Biomes
    public static final int OCEAN = color(255, 220, 0, 0);
    public static final int OCEAN_REEF = color(255, 250, 160, 70);
    public static final int DEEP_OCEAN = color(255, 160, 0, 0);
    public static final int DEEP_OCEAN_TRENCH = color(255, 80, 0, 0);
    public static final int LAKE = color(255, 255, 30, 30);
    public static final int MOUNTAIN_ANY_OR_PLATEAU_LAKE = color(255, 255, 180, 180);
    public static final int RIVER = color(255, 255, 200, 0);
    public static final int OCEANIC_MOUNTAINS = color(255, 255, 0, 255);
    public static final int CANYONS = color(255, 255, 60, 180);
    public static final int LOW_CANYONS = color(255, 255, 110, 200);
    public static final int LOWLANDS = color(255, 230, 150, 220);
    public static final int MOUNTAINS = color(255, 50, 50, 255);
    public static final int OLD_MOUNTAINS = color(255, 100, 100, 240);
    public static final int PLATEAU = color(255, 120, 120, 210);
    public static final int BADLANDS = color(255, 0, 150, 255);
    public static final int INVERTED_BADLANDS = color(255, 0, 150, 240);
    public static final int SHORE = color(255, 130, 210, 230);
    public static final int HIGHLANDS = color(255, 30, 80, 20);
    public static final int ROLLING_HILLS = color(255, 50, 100, 50);
    public static final int HILLS = color(255, 80, 130, 80);
    public static final int PLAINS = color(255, 100, 200, 100);
    public static final int UNKNOWN_BIOME = color(255, 255, 96, 230);

    // Ocean depth colors
    public static final int SHALLOW_WATER = color(255, 255, 160, 150);
    public static final int DEEP_WATER = color(255, 240, 120, 120);
    public static final int VERY_DEEP_WATER = color(255, 200, 100, 100);

    // Rock Colors
    // TODO: Improve differences between grays
    public static final int GRANITE = color(255, 74, 70, 85);
    public static final int DIORITE = color(255, 142, 142, 142);
    public static final int GABBRO = color(255, 68, 85, 93);
    public static final int SHALE = color(255, 70, 67, 70);
    public static final int CLAYSTONE = color(255, 68, 102, 141);
    public static final int LIMESTONE = color(255, 107, 127, 136);
    public static final int CONGLOMERATE = color(255, 101, 113, 111);
    public static final int DOLOMITE = color(255, 89, 70, 60);
    public static final int CHERT = color(255, 70, 78, 122);
    public static final int CHALK = color(255, 193, 199, 199);
    public static final int RHYOLITE = color(255, 103, 98, 115);
    public static final int BASALT = color(255, 33, 32, 29);
    public static final int ANDESITE = color(255, 96, 96, 96);
    public static final int DACITE = color(255, 123, 123, 122);
    public static final int QUARTZITE = color(255, 128, 129, 140);
    public static final int SLATE = color(255, 103, 116, 125);
    public static final int PHYLLITE = color(255, 169, 157, 148);
    public static final int SCHIST = color(255, 65, 84, 77);
    public static final int GNEISS = color(255, 96, 109, 115);
    public static final int MARBLE = color(255, 235, 235, 227);
    public static final int UNKNOWN_ROCK = color(255, 227, 88, 255);

    public static final Map<Block, Integer> ROCK_BLOCK_COLORS = Util.make(new IdentityHashMap<>(20), map -> {
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.GRANITE).get(Rock.BlockType.RAW).get(), GRANITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.DIORITE).get(Rock.BlockType.RAW).get(), DIORITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.GABBRO).get(Rock.BlockType.RAW).get(), GABBRO);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.SHALE).get(Rock.BlockType.RAW).get(), SHALE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.CLAYSTONE).get(Rock.BlockType.RAW).get(), CLAYSTONE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.LIMESTONE).get(Rock.BlockType.RAW).get(), LIMESTONE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.CONGLOMERATE).get(Rock.BlockType.RAW).get(), CONGLOMERATE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.DOLOMITE).get(Rock.BlockType.RAW).get(), DOLOMITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.CHERT).get(Rock.BlockType.RAW).get(), CHERT);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.CHALK).get(Rock.BlockType.RAW).get(), CHALK);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.RHYOLITE).get(Rock.BlockType.RAW).get(), RHYOLITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.RAW).get(), BASALT);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.ANDESITE).get(Rock.BlockType.RAW).get(), ANDESITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.DACITE).get(Rock.BlockType.RAW).get(), DACITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.QUARTZITE).get(Rock.BlockType.RAW).get(), QUARTZITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.SLATE).get(Rock.BlockType.RAW).get(), SLATE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.PHYLLITE).get(Rock.BlockType.RAW).get(), PHYLLITE);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.SCHIST).get(Rock.BlockType.RAW).get(), SCHIST);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.GNEISS).get(Rock.BlockType.RAW).get(), GNEISS);
        map.put(TFCBlocks.ROCK_BLOCKS.get(Rock.MARBLE).get(Rock.BlockType.RAW).get(), MARBLE);
    });

    // Gradients
    public static final DoubleToIntFunction blue = linearGradient(color(255, 150, 50, 50), color(255, 255, 140, 100));
    public static final DoubleToIntFunction green = linearGradient(color(255, 0, 100, 0), color(255, 80, 200, 80));
    public static final DoubleToIntFunction VOLCANIC_ROCK = value -> color(255, 100, (int) (100 * value), 200);
    public static final DoubleToIntFunction UPLIFT_ROCK = value -> color(255, 200, (int) (180 * value), 180);
    public static final DoubleToIntFunction climate = multiLinearGradient(
            color(255, 240, 20, 180),
            color(255, 240, 180, 0),
            color(255, 220, 180, 180),
            color(255, 0, 210, 210),
            color(255, 60, 120, 200),
            color(255, 40, 40, 200)
    );

    public static final VisualizerType.DrawFunction fillOcean = (x, y, xOffset, yOffset, generator, region, point, image) -> setPixel(image, x, y, blue.applyAsInt(region.noise() / 2));

    public static Component build(RegionChunkDataGenerator generator, VisualizerType visualizer, int xOffset, int yOffset, boolean drawSpawn, int spawnDist, int spawnX, int spawnY) {
        final NativeImage image = new NativeImage(previewSize(), previewSize(), false);
        final Set<Region> visitedRegions = new HashSet<>();
        for (int x = 0; x < previewSize(); x++) {
            for (int y = 0; y < previewSize(); y++) {
                final int xPos = x + xOffset;
                final int yPos = y + yOffset;
                final Region region = generator.regionGenerator().getOrCreateRegion(xPos, yPos);
                visitedRegions.add(region);
                final Region.Point point = generator.regionGenerator().getOrCreateRegionPoint(xPos, yPos);
                visualizer.draw(x, y, xPos, yPos, generator, region, point, image);
            }
        }

        if (drawSpawn) {
            final int xCenter = (spawnX / (16 * 8)) - xOffset;
            final int yCenter = (spawnY / (16 * 8)) - yOffset;
            final int radius = spawnDist / (16 * 8);
            hLine(image, xCenter - radius, xCenter + radius, yCenter + radius, lineWidth(), DARK_GRAY);
            hLine(image, xCenter - radius, xCenter + radius, yCenter - radius, lineWidth(), DARK_GRAY);
            vLine(image, yCenter - radius, yCenter + radius, xCenter + radius, lineWidth(), DARK_GRAY);
            vLine(image, yCenter - radius, yCenter + radius, xCenter - radius, lineWidth(), DARK_GRAY);

            final int length = Math.min(radius / 4, previewSize() / 12);
            hLine(image, xCenter - length, xCenter + length, yCenter, lineWidth(), SPAWN_RED);
            vLine(image, yCenter - length, yCenter + length, xCenter, lineWidth(), SPAWN_RED);
        }

        PREVIEW.setPixels(image);
        PREVIEW.upload();

        if (false && !FMLLoader.isProduction()) {
            try {
                image.writeToFile(new File(FMLPaths.GAMEDIR.get().toFile(), String.format("screenshots\\preview_%s_%dx%d@%s.png", visualizer.name(), previewSize(), previewSize(), Util.getFilenameFormattedDateTime())));
            } catch (IOException exception) {
                TFCGenViewer.LOGGER.warn("Unable to write preview to disk!", exception);
            }
        }

        return Component.translatable(
                "tfcgenviewer.preview_world.preview_info",
                visitedRegions.size(),
                ImageBuilder.previewSizeKm(),
                ImageBuilder.previewSizeKm(),
                (xOffset + (previewSize() / 2)) * 128,
                (yOffset + (previewSize() / 2)) * 128,
                visualizer.getName(),
                visualizer.getColorKey()
        );
    }

    public static int biomeColor(int biome) {
        if (biome == TFCLayers.OCEAN) return OCEAN;
        if (biome == TFCLayers.OCEAN_REEF) return OCEAN_REEF;
        if (biome == TFCLayers.DEEP_OCEAN) return DEEP_OCEAN;
        if (biome == TFCLayers.DEEP_OCEAN_TRENCH) return DEEP_OCEAN_TRENCH;
        if (biome == TFCLayers.LAKE) return LAKE;
        if (biome == TFCLayers.MOUNTAIN_LAKE || biome == TFCLayers.OCEANIC_MOUNTAIN_LAKE || biome == TFCLayers.OLD_MOUNTAIN_LAKE || biome == TFCLayers.VOLCANIC_MOUNTAIN_LAKE || biome == TFCLayers.PLATEAU_LAKE) return MOUNTAIN_ANY_OR_PLATEAU_LAKE;
        if (biome == TFCLayers.RIVER) return RIVER;

        if (biome == TFCLayers.OCEANIC_MOUNTAINS || biome == TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS) return OCEANIC_MOUNTAINS;
        if (biome == TFCLayers.CANYONS) return CANYONS;
        if (biome == TFCLayers.LOW_CANYONS) return LOW_CANYONS;
        if (biome == TFCLayers.LOWLANDS) return LOWLANDS;

        if (biome == TFCLayers.MOUNTAINS || biome == TFCLayers.VOLCANIC_MOUNTAINS) return MOUNTAINS;
        if (biome == TFCLayers.OLD_MOUNTAINS) return OLD_MOUNTAINS;
        if (biome == TFCLayers.PLATEAU) return PLATEAU;

        if (biome == TFCLayers.BADLANDS) return BADLANDS;
        if (biome == TFCLayers.INVERTED_BADLANDS) return INVERTED_BADLANDS;

        if (biome == TFCLayers.SHORE) return SHORE;

        if (biome == TFCLayers.HIGHLANDS) return HIGHLANDS;
        if (biome == TFCLayers.ROLLING_HILLS) return ROLLING_HILLS;
        if (biome == TFCLayers.HILLS) return HILLS;
        if (biome == TFCLayers.PLAINS) return PLAINS;

        return UNKNOWN_BIOME;
    }

    public static int inlandHeightColor(Region.Point point) {
        if (point.land()) {
            return green.applyAsInt(point.baseLandHeight / 24F);
        }

        return point.shore() ? point.river() ? SHALLOW_WATER : DEEP_WATER : point.baseOceanDepth < 4 ? SHALLOW_WATER : point.baseOceanDepth < 8 ? DEEP_WATER : VERY_DEEP_WATER;
    }

    static void setPixel(NativeImage image, int x, int y, int color) {
        final int alpha = alpha(color);
        if (alpha != 0) {
            if (!image.isOutsideBounds(x, y)) {
                if (alpha == 255) {
                    image.setPixelRGBA(x, y, color);
                } else {
                    image.blendPixel(x, y, color);
                }
            }
        }
    }

    static void hLine(NativeImage image, int x0, int x1, int y, int width, int color) {
        final int min = Math.min(x0, x1);
        final int max = Math.max(x0, x1);
        for (int x = min ; x < max ; x++) {
            if (width <= 0) {
                setPixel(image, x, y, color);
            } else {
                for (int i = y - width ; i < y + width ; i++) {
                    setPixel(image, x, i, color);
                }
            }
        }
    }

    static void vLine(NativeImage image, int y0, int y1, int x, int width, int color) {
        final int min = Math.min(y0, y1);
        final int max = Math.max(y0, y1);
        for (int y = min ; y < max ; y++) {
            if (width <= 0) {
                setPixel(image, x, y, color);
            } else {
                for (int i = x - width ; i < x + width ; i++) {
                    setPixel(image, i, y, color);
                }
            }
        }
    }
}
