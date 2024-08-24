package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import com.notenoughmail.tfcgenviewer.util.VisualizerType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.notenoughmail.tfcgenviewer.util.ImageBuilder.setPixel;

public class Colors {

    static final Gson GSON = new Gson();

    static ColorGradientDefinition
            RAINFALL = new ColorGradientDefinition(
                    ColorUtil.climate,
                    Component.translatable("tfcgenviewer.climate.rainfall")
            ),
            TEMPERATURE = new ColorGradientDefinition(
                    ColorUtil.climate,
                    Component.translatable("tfcgenviewer.climate.temperature")
            ),
            FILL_OCEAN = new ColorGradientDefinition(
                    ColorUtil.blue,
                    Component.translatable("biome.tfc.ocean")
            );

    static ColorDefinition
            SPAWN_BORDER = new ColorDefinition(
                    ColorUtil.DARK_GRAY,
                    Component.empty(),
                    0
            ),
            SPAWN_RETICULE = new ColorDefinition(
                    ColorUtil.SPAWN_RED,
                    Component.empty(),
                    0
            );

    public static final CacheableSupplier<Component>
            TEMP_KEY = new CacheableSupplier<>(() -> {
                final MutableComponent key = Component.empty();
                temperature().appendTo(key);
                fillOcean().appendTo(key, true);
                return key;
            }),
            RAIN_KEY = new CacheableSupplier<>(() -> {
                final MutableComponent key = Component.empty();
                rainfall().appendTo(key);
                fillOcean().appendTo(key, true);
                return key;
            });

    public static ColorGradientDefinition rainfall() {
        return RAINFALL;
    }

    public static ColorGradientDefinition temperature() {
        return TEMPERATURE;
    }

    public static ColorGradientDefinition fillOcean() {
        return FILL_OCEAN;
    }

    public static ColorDefinition spawnBorder() {
        return SPAWN_BORDER;
    }

    public static ColorDefinition getSpawnReticule() {
        return SPAWN_RETICULE;
    }

    public static final VisualizerType.DrawFunction fillOcean = (x, y, xOffset, yOffset, generator, region, point, image) ->
            setPixel(image, x, y, FILL_OCEAN.gradient().applyAsInt(region.noise() / 2));
    public static final VisualizerType.DrawFunction dev = (x, y, xPos, zPos, generator, region, point, image) ->
            setPixel(image, x, y, ColorUtil.grayscale.applyAsInt((double) region.hashCode() / (double) Integer.MAX_VALUE));

    public static void assign(ResourceLocation resourcePath, Resource resource) {
        try (InputStream stream = resource.open()) {
            final JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            switch (resourcePath.getPath()) {
                case "tfcgenviewer/colors/fill_ocean.json" -> {
                    final ColorGradientDefinition def = ColorGradientDefinition.parse(
                            json,
                            resourcePath,
                            "fill_ocean",
                            null
                    );
                    if (def != null) {
                        FILL_OCEAN = def;
                    }
                }
                case "tfcgenviewer/colors/rainfall.json" -> {
                    final ColorGradientDefinition def = ColorGradientDefinition.parse(
                            json,
                            resourcePath,
                            "climate.rainfall",
                            null
                    );
                    if (def != null) {
                        RAINFALL = def;
                    }
                }
                case "tfcgenviewer/colors/temperature.json" -> {
                    final ColorGradientDefinition def = ColorGradientDefinition.parse(
                            json,
                            resourcePath,
                            "climate.temperature",
                            null
                    );
                    if (def != null) {
                        TEMPERATURE = def;
                    }
                }
                case "tfcgenviewer/spawn/border.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "spawn.border",
                            resourcePath,
                            null
                    );
                    if (def.color() != 0xF0000000) {
                        SPAWN_BORDER = def;
                    }
                }
                case "tfcgenviewer/spawn/reticule.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "spawn.reticule",
                            resourcePath,
                            null
                    );
                    if (def.color() != 0xF0000000) {
                        SPAWN_RETICULE = def;
                    }
                }
            }
        } catch (IOException exception) {
            TFCGenViewer.LOGGER.warn(
                    "Could not open color file at {}, using previous value. Error:\n{}",
                    resourcePath,
                    exception
            );
        }
    }
}
