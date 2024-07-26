package com.notenoughmail.tfcgenviewer.config;

import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
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

import static com.notenoughmail.tfcgenviewer.config.Colors.FILL_OCEAN;
import static com.notenoughmail.tfcgenviewer.config.Colors.GSON;

public class BiomeAltitudeColors {

    private static final ColorDefinition[] COLORS = new ColorDefinition[4];

    static {
        COLORS[0] = new ColorDefinition(
                ColorUtil.green.applyAsInt(0),
                Component.translatable("tfcgenviewer.biome_altitude.low"),
                0
        );
        COLORS[1] = new ColorDefinition(
                ColorUtil.green.applyAsInt(0.333),
                Component.translatable("tfcgenviewer.biome_altitude.medium"),
                0
        );
        COLORS[2] = new ColorDefinition(
                ColorUtil.green.applyAsInt(0.666),
                Component.translatable("tfcgenviewer.biome_altitude.high"),
                0
        );
        COLORS[3] = new ColorDefinition(
                ColorUtil.green.applyAsInt(0.999),
                Component.translatable("tfcgenviewer.biome_altitude.mountain"),
                0
        );
    }

    public static void assign(ResourceLocation resourcePath, Resource resource) {
        try (InputStream stream = resource.open()) {
            final JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            switch (resourcePath.getPath()) {
                case "tfcgenviewer/biome_altitude/low.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "biome_altitude.low",
                            resourcePath,
                            "tfcgenviewer.biome_altitude.low"
                    );
                    if (def.color() != 0xF0000000) {
                        COLORS[0] = def;
                    }
                }
                case "tfcgenviewer/biome_altitude/medium.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "biome_altitude.medium",
                            resourcePath,
                            "tfcgenviewer.biome_altitude.medium"
                    );
                    if (def.color() != 0xF0000000) {
                        COLORS[1] = def;
                    }
                }
                case "tfcgenviewer/biome_altitude/high.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "biome_altitude.high",
                            resourcePath,
                            "tfcgenviewer.biome_altitude.high"
                    );
                    if (def.color() != 0xF0000000) {
                        COLORS[2] = def;
                    }
                }
                case "tfcgenviewer/biome_altitude/mountain.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "biome_altitude.mountain",
                            resourcePath,
                            "tfcgenviewer.biome_altitude.mountain"
                    );
                    if (def.color() != 0xF0000000) {
                        COLORS[3] = def;
                    }
                }
            }
        } catch (IOException exception) {
            TFCGenViewer.LOGGER.warn(
                    "Could not open biome altitude color file at {}, using previous value. Error:\n{}",
                    resourcePath,
                    exception
            );
        }
    }

    public static int color(int altitude) {
        return COLORS[altitude].color();
    }

    public static Supplier<Component> colorKey() {
        return () -> {
            final MutableComponent key = Component.empty();
            append(key);
            return key;
        };
    }

    public static void append(MutableComponent key) {
        for (ColorDefinition def : COLORS) {
            def.appendTo(key);
        }
        FILL_OCEAN.appendTo(key, true);
    }
}
