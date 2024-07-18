package com.notenoughmail.tfcgenviewer.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public record ColorDefinition(int color, Component name, int sort) implements Comparable<ColorDefinition> {

    private static final Gson GSON = new Gson();

    public static ColorDefinition parse(ResourceLocation resourcePath, Resource resource, String type, int fallback) {
        try (InputStream stream = resource.open()) {
            final JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            final Component name = json.has("key") ? Component.translatable(json.get("key").getAsString()) : Component.translatable("tfcgenviewer.%s.unknown".formatted(type));
            final int colorValue;
            if (json.has("color")) {
                colorValue = parseColor(json.get("color"), fallback, type, resourcePath);
            } else {
                TFCGenViewer.LOGGER.warn("The {} color at {} does not have a 'color' property!", type, resourcePath);
                colorValue = fallback;
            }
            return new ColorDefinition(colorValue, name, json.has("sort") ? json.get("sort").getAsInt() : 100);
        } catch (IOException exception) {
            TFCGenViewer.LOGGER.warn(
                    "Unable to open {} color resource at {}. Error:\n{}",
                    type,
                    resourcePath,
                    exception
            );
        }
        TFCGenViewer.LOGGER.warn("Unable to parse color definition for {} at {}", type, resourcePath);
        return null;
    }

    public static int parseColor(JsonElement color, int fallback, String type, ResourceLocation resourcePath) {
        if (color.isJsonObject()) {
            final JsonObject value = color.getAsJsonObject();
            if (value.has("r") && value.has("g") && value.has("b")) {
                return FastColor.ABGR32.color(
                        255,
                        value.get("b").getAsInt(),
                        value.get("g").getAsInt(),
                        value.get("r").getAsInt()
                );
            } else if (value.has("h") && value.has("s") && value.has("v")) {
                return ImageBuilder.rgbToBgr(Mth.hsvToRgb(
                        value.get("h").getAsFloat(),
                        value.get("s").getAsFloat(),
                        value.get("v").getAsFloat()
                ));
            } else {
                TFCGenViewer.LOGGER.warn("Unable to parse {} color value: {} at {}", type, value, resourcePath);
            }
        } else if (color.isJsonPrimitive()) {
            final JsonPrimitive primitive = color.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return ImageBuilder.rgbToBgr(primitive.getAsInt());
            } else if (primitive.isString()) {
                try {
                    return ImageBuilder.rgbToBgr(Integer.parseInt(primitive.getAsString(), 16));
                } catch (NumberFormatException exception) {
                    TFCGenViewer.LOGGER.warn(
                            "Unable to parse number for {} color at {}. Error:\n{}",
                            type,
                            resourcePath,
                            exception
                    );
                }
            }
        } else {
            TFCGenViewer.LOGGER.warn("Unable to parse {} color: {},", type, color);
        }
        return fallback;
    }

    public void appendTo(MutableComponent text, boolean end) {
        text.append(Component.translatable(
                "tfcgenviewer.preview_world.color_key_template",
                Component.literal("■").withStyle(style -> style.withColor(ImageBuilder.bgrToRgb(color))),
                name
        ));
        if (!end) text.append(CommonComponents.NEW_LINE);
    }

    @Override
    public int compareTo(@NotNull ColorDefinition other) {
        int sorted = Integer.compare(sort, other.sort);
        if (sorted == 0) {
            final String thisString = name.getString();
            final String otherString = other.name().getString();
            sorted = thisString.compareTo(otherString);
        }
        return sorted;
    }
}
