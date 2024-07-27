package com.notenoughmail.tfcgenviewer.config;

import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static com.notenoughmail.tfcgenviewer.config.Colors.GSON;

public class RiversColors {

    static ColorDefinition
            RIVER = new ColorDefinition(
                ColorUtil.RIVER_BLUE,
                Component.translatable("biome.tfc.river"),
                0
            ),
            LAKE = new ColorDefinition(
                    ColorUtil.SHALLOW_WATER,
                    Component.translatable("biome.tfc.lake"),
                    0
            ),
            VOLCANIC_MOUNTAIN = new ColorDefinition(
                    ColorUtil.VOLCANIC_MOUNTAIN,
                    Component.translatable("tfcgenviewer.rivers.oceanic_volcanic_mountain"),
                    0
            ),
            INLAND_MOUNTAIN = new ColorDefinition(
                    ColorUtil.GRAY,
                    Component.translatable("tfcgenviewer.rivers.inland_mountain"),
                    0
            );

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

    public static void assign(ResourceLocation resourcePath, Resource resource) {
        try (InputStream stream = resource.open()) {
            final JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            switch (resourcePath.getPath()) {
                case "tfcgenviewer/rivers_and_mountains/river.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "rivers_and_mountains.river",
                            resourcePath,
                            "biome.tfc.river"
                    );
                    if (def.color() != 0xF0000000) {
                        RIVER = def;
                    }
                }
                case "tfcgenviewer/rivers_and_mountains/oceanic_volcanic_mountain.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "rivers_and_mountains.oceanic_volcanic_mountains",
                            resourcePath,
                            "tfcgenviewer.rivers.oceanic_volcanic_mountain"
                    );
                    if (def.color() != 0xF0000000) {
                        VOLCANIC_MOUNTAIN = def;
                    }
                }
                case "tfcgenviewer/rivers_and_mountains/inland_mountain.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "rivers_and_mountains.inland_mountain",
                            resourcePath,
                            "tfcgenviewer.rivers.inland_mountain"
                    );
                    if (def.color() != 0xF0000000) {
                        INLAND_MOUNTAIN = def;
                    }
                }
                case "tfcgenviewer/rivers_and_mountains/lake.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "rivers_and_mountains.lake",
                            resourcePath,
                            "biome.tfc.lake"
                    );
                    if (def.color() != 0xF0000000) {
                        LAKE = def;
                    }
                }
            }
        } catch (IOException exception) {
            TFCGenViewer.LOGGER.warn(
                    "Unable to open rivers and mountains color file at {}, using previous value. Error:\n{}",
                    resourcePath,
                    exception
            );
        }
    }

    public static Supplier<Component> colorKey() {
        return () -> {
            final MutableComponent key = Component.empty();
            RIVER.appendTo(key);
            VOLCANIC_MOUNTAIN.appendTo(key);
            INLAND_MOUNTAIN.appendTo(key);
            LAKE.appendTo(key);
            BiomeAltitudeColors.append(key);
            return key;
        };
    }
}
