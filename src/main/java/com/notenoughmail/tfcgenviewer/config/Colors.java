package com.notenoughmail.tfcgenviewer.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Colors {

    static final Gson GSON = new Gson();

    static ColorGradientDefinition
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
                    Component.translatable("biome.tfc.ocean")
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
