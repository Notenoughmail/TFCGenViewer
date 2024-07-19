package com.notenoughmail.tfcgenviewer.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class Colors {

    private static final Gson GSON = new Gson();

    private static ColorGradientDefinition
            RAINFALL = new ColorGradientDefinition(
                    ImageBuilder.climate,
                    Component.translatable("tfcgenviewer.climate.rainfall")
            ),
            TEMPERATURE = new ColorGradientDefinition(
                    ImageBuilder.climate,
                    Component.translatable("tfcgenviewer.climate.temperature")
            ),
            FILL_OCEAN = new ColorGradientDefinition(
                    ImageBuilder.blue,
                    Component.translatable("tfcgenviewer.biome.ocean")
            ),
            INLAND_HEIGHT = new ColorGradientDefinition(
                    ImageBuilder.green,
                    Component.translatable("tfcgenviewer.inland_height.land")
            );

    private static ColorDefinition
            SHALLOW_WATER = new ColorDefinition(
                    ImageBuilder.SHALLOW_WATER,
                    Component.translatable("tfcgenviewer.inland_height.shallow_water"),
                    0
            ),
            DEEP_WATER = new ColorDefinition(
                    ImageBuilder.DEEP_WATER,
                    Component.translatable("tfcgenviewer.inland_height.deep_water"),
                    0
            ),
            VERY_DEEP_WATER = new ColorDefinition(
                    ImageBuilder.VERY_DEEP_WATER,
                    Component.translatable("tfcgenviewer.inland_height.very_deep_water"),
                    0
            ),
            RIVER = new ColorDefinition(
                    ImageBuilder.RIVER_BLUE,
                    Component.translatable("tfcgenviewer.biome.river"),
                    0
            ),
            LOW = new ColorDefinition(
                    ImageBuilder.green.applyAsInt(0),
                    Component.translatable("tfcgenviewer.biome_altitude.low"),
                    0
            ),
            MEDIUM = new ColorDefinition(
                    ImageBuilder.green.applyAsInt(0.333),
                    Component.translatable("tfcgenviewer.biome_altitude.medium"),
                    0
            ),
            HIGH = new ColorDefinition(
                    ImageBuilder.green.applyAsInt(0.666),
                    Component.translatable("tfcgenviewer.biome_altitude.high"),
                    0
            ),
            MOUNTAIN = new ColorDefinition(
                    ImageBuilder.green.applyAsInt(0.999),
                    Component.translatable("tfcgenviewer.biome_altitude.mountain"),
                    0
            ),
            LAKE = new ColorDefinition(
                    ImageBuilder.SHALLOW_WATER,
                    Component.translatable("tfcgenviewer.biome.lake"),
                    0
            ),
            VOLCANIC_MOUNTAIN = new ColorDefinition(
                    ImageBuilder.VOLCANIC_MOUNTAIN,
                    Component.translatable("tfcgenviewer.rivers.oceanic_volcanic_mountain"),
                    0
            ),
            INLAND_MOUNTAIN = new ColorDefinition(
                    ImageBuilder.GRAY,
                    Component.translatable("tfcgenviewer.rivers.inland_mountain"),
                    0
            );

    public static ColorGradientDefinition rainfall() {
        return RAINFALL;
    }

    public static ColorGradientDefinition temperature() {
        return TEMPERATURE;
    }

    public static ColorGradientDefinition fillOcean() {
        return FILL_OCEAN;
    }

    public static ColorGradientDefinition inlandHeight() {
        return INLAND_HEIGHT;
    }

    public static ColorDefinition shallowWater() {
        return SHALLOW_WATER;
    }

    public static ColorDefinition deepWater() {
        return DEEP_WATER;
    }

    public static ColorDefinition veryDeepWater() {
        return VERY_DEEP_WATER;
    }

    public static ColorDefinition river() {
        return RIVER;
    }

    public static ColorDefinition lake() {
        return LAKE;
    }

    public static ColorDefinition volcanicMountain() {
        return VOLCANIC_MOUNTAIN;
    }

    public static ColorDefinition inlandMountain() {
        return INLAND_MOUNTAIN;
    }

    public static int biomeAltitude(int altitude) {
        return switch (altitude) {
            case 1 -> MEDIUM.color();
            case 2 -> HIGH.color();
            case 3 -> MOUNTAIN.color();
            default -> LOW.color();
        };
    }

    public static Supplier<Component> biomeAltitude(boolean river) {
        if (river) {
            return () -> {
                final MutableComponent key = Component.empty();
                RIVER.appendTo(key, false);
                VOLCANIC_MOUNTAIN.appendTo(key, false);
                INLAND_MOUNTAIN.appendTo(key, false);
                LAKE.appendTo(key, false);
                LOW.appendTo(key, false);
                MEDIUM.appendTo(key, false);
                HIGH.appendTo(key, false);
                FILL_OCEAN.appendTo(key, true);
                return key;
            };
        }
        return () -> {
            final MutableComponent key = Component.empty();
            LOW.appendTo(key, false);
            MEDIUM.appendTo(key, false);
            HIGH.appendTo(key, false);
            MOUNTAIN.appendTo(key, false);
            FILL_OCEAN.appendTo(key, true);
            return key;
        };
    }

    public static void assign(ResourceLocation resourcePath, Resource resource) {
        try (InputStream stream = resource.open()) {
            final JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            if (json.has("ocean_fill")) {
                final ColorGradientDefinition def = ColorGradientDefinition.parse(json.get("ocean_fill"), resourcePath, "ocean_fill", null);
                if (def != null) {
                    FILL_OCEAN = def;
                }
            }
            if (json.has("rainfall")) {
                final ColorGradientDefinition def = ColorGradientDefinition.parse(json.get("climate.rainfall"), resourcePath, "rainfall", null);
                if (def != null) {
                    RAINFALL = def;
                }
            }
            if (json.has("temperature")) {
                final ColorGradientDefinition def = ColorGradientDefinition.parse(json.get("climate.temperature"), resourcePath, "temperature", null);
                if (def != null) {
                    TEMPERATURE = def;
                }
            }
            if (json.has("inland_height")) {
                final JsonObject inlandHeight = json.getAsJsonObject("inland_height");
                if (inlandHeight.has("land")) {
                    final ColorGradientDefinition def = ColorGradientDefinition.parse(inlandHeight.get("land"), resourcePath, "inland_height.land", null);
                    if (def != null) {
                        INLAND_HEIGHT = def;
                    }
                }
                if (inlandHeight.has("shallow_water")) {
                    final ColorDefinition def = ColorDefinition.parse(inlandHeight.getAsJsonObject("shallow_water"), 0xF0000000, "inland_height.shallow_water", resourcePath);
                    if (def.color() != 0xF0000000) {
                        SHALLOW_WATER = def;
                    }
                }
                if (inlandHeight.has("deep_water")) {
                    final ColorDefinition def = ColorDefinition.parse(inlandHeight.getAsJsonObject("deep_water"), 0xF0000000, "inland_height.deep_water", resourcePath);
                    if (def.color() != 0xF0000000) {
                        DEEP_WATER = def;
                    }
                }
                if (inlandHeight.has("very_deep_water")) {
                    final ColorDefinition def = ColorDefinition.parse(inlandHeight.getAsJsonObject("very_deep_water"), 0xF0000000, "inland_height_very_deep_water", resourcePath);
                    if (def.color() != 0xF0000000) {
                        VERY_DEEP_WATER = def;
                    }
                }
            }
            if (json.has("biome_altitude")) {
                final JsonObject biomeAltitude = json.getAsJsonObject("biome_altitude");
                if (biomeAltitude.has("low")) {
                    final ColorDefinition def = ColorDefinition.parse(biomeAltitude.getAsJsonObject("low"), 0xF0000000, "biome_altitude.low", resourcePath);
                    if (def.color() != 0xF0000000) {
                        LOW = def;
                    }
                }
                if (biomeAltitude.has("medium")) {
                    final ColorDefinition def = ColorDefinition.parse(biomeAltitude.getAsJsonObject("medium"), 0xF0000000, "biome_altitude.medium", resourcePath);
                    if (def.color() != 0xF0000000) {
                        MEDIUM = def;
                    }
                }
                if (biomeAltitude.has("high")) {
                    final ColorDefinition def = ColorDefinition.parse(biomeAltitude.getAsJsonObject("high"), 0xF0000000, "biome_altitude.high", resourcePath);
                    if (def.color() != 0xF0000000) {
                        HIGH = def;
                    }
                }
                if (biomeAltitude.has("mountain")) {
                    final ColorDefinition def = ColorDefinition.parse(biomeAltitude.getAsJsonObject("mountain"), 0xF0000000, "biome_altitude.mountain", resourcePath);
                    if (def.color() != 0xF0000000) {
                        MOUNTAIN = def;
                    }
                }
            }
            if (json.has("rivers")) {
                final JsonObject rivers = json.getAsJsonObject("rivers");
                if (rivers.has("river")) {
                    final ColorDefinition def = ColorDefinition.parse(rivers.getAsJsonObject("river"), 0xF0000000, "rivers.river", resourcePath);
                    if (def.color() != 0xF0000000) {
                        RIVER = def;
                    }
                }
                if (rivers.has("oceanic_volcanic_mountain")) {
                    final ColorDefinition def = ColorDefinition.parse(rivers.getAsJsonObject("oceanic_volcanic_mountain"), 0xF0000000, "rivers.oceanic_volcanic_mountains", resourcePath);
                    if (def.color() != 0xF0000000) {
                        VOLCANIC_MOUNTAIN = def;
                    }
                }
                if (rivers.has("inland_mountain")) {
                    final ColorDefinition def = ColorDefinition.parse(rivers.getAsJsonObject("inland_mountain"), 0xF0000000, "rivers.inland_mountain", resourcePath);
                    if (def.color() != 0xF0000000) {
                        INLAND_MOUNTAIN = def;
                    }
                }
                if (rivers.has("lake")) {
                    final ColorDefinition def = ColorDefinition.parse(rivers.getAsJsonObject("lake"), 0xF0000000, "rivers.lake", resourcePath);
                    if (def.color() != 0xF0000000) {
                        LAKE = def;
                    }
                }
            }
        } catch (IOException exception) {
            TFCGenViewer.LOGGER.warn(
                    "Could not open colors file at {}, using previous values. Error:\n{}",
                    resourcePath,
                    exception
            );
        }
    }
}
