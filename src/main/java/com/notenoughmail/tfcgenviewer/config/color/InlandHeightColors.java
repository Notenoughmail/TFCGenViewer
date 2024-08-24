package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import net.dries007.tfc.world.region.Region;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.notenoughmail.tfcgenviewer.config.color.Colors.GSON;

public class InlandHeightColors {

    private static ColorGradientDefinition HEIGHT = new ColorGradientDefinition(
            ColorUtil.green,
            Component.translatable("tfcgenviewer.inland_height.land")
    );

    private static ColorDefinition
            SHALLOW_WATER = new ColorDefinition(
                ColorUtil.SHALLOW_WATER,
                Component.translatable("tfcgenviewer.inland_height.shallow_water"),
                0
            ),
            DEEP_WATER = new ColorDefinition(
                    ColorUtil.DEEP_WATER,
                    Component.translatable("tfcgenviewer.inland_height.deep_water"),
                    0
            ),
            VERY_DEEP_WATER = new ColorDefinition(
                    ColorUtil.VERY_DEEP_WATER,
                    Component.translatable("tfcgenviewer.inland_height.very_deep_water"),
                    0
            );
    public static final CacheableSupplier<Component> KEY = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        HEIGHT.appendTo(key);
        SHALLOW_WATER.appendTo(key);
        DEEP_WATER.appendTo(key);
        VERY_DEEP_WATER.appendTo(key);
        return key;
    });

    public static void assign(ResourceLocation resourcePath, Resource resource) {
        try (InputStream stream = resource.open()) {
            final JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            switch (resourcePath.getPath()) {
                case "tfcgenviewer/inland_height/land.json" -> {
                    final ColorGradientDefinition def = ColorGradientDefinition.parse(
                            json,
                            resourcePath,
                            "inland_height.land",
                            null
                    );
                    if (def != null) {
                        HEIGHT = def;
                    }
                }
                case "tfcgenviewer/inland_height/shallow_water.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "inland_height.shallow_water",
                            resourcePath,
                            "tfcgenviewer.inland_height.shallow_water");
                    if (def.color() != 0xF0000000) {
                        SHALLOW_WATER = def;
                    }
                }
                case "tfcgenviewer/inland_height/deep_water.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "inland_height.deep_water",
                            resourcePath,
                            "tfcgenviewer.inland_height.deep_water"
                    );
                    if (def.color() != 0xF0000000) {
                        DEEP_WATER = def;
                    }
                }
                case "tfcgenviewer/inland_height/very_deep_water.json" -> {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "inland_height.very_deep_water",
                            resourcePath,
                            "tfcgenviewer.inland_height.very_deep_water"
                    );
                    if (def.color() != 0xF0000000) {
                        VERY_DEEP_WATER = def;
                    }
                }
            }
        } catch (IOException exception) {
            TFCGenViewer.LOGGER.warn(
                    "Could not open inland height color file at {}, using previous value. Error:\n{}",
                    resourcePath,
                    exception
            );
        }
    }

    public static int color(Region.Point point) {
        if (point.land()) {
            return HEIGHT.gradient().applyAsInt(point.baseLandHeight / 24F);
        }

        return point.shore() ?
                point.river() ?
                        SHALLOW_WATER.color() :
                        DEEP_WATER.color() :
                point.baseOceanDepth < 4 ?
                        SHALLOW_WATER.color() :
                        point.baseOceanDepth < 8 ?
                                DEEP_WATER.color() :
                                VERY_DEEP_WATER.color();
    }
}
