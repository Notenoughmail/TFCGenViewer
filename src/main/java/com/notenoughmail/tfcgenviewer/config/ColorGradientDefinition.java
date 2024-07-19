package com.notenoughmail.tfcgenviewer.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.DoubleToIntFunction;

public record ColorGradientDefinition(DoubleToIntFunction gradient, Component name) {

    private static final double[] keyValues = new double[] { 0, 0.2, 0.4, 0.6, 0.8, 0.999 };
    private static final Gson GSON = new Gson();

    @Nullable
    public static ColorGradientDefinition parse(ResourceLocation resourcePath, Resource resource, String type, @Nullable DoubleToIntFunction fallback) {
        try (InputStream stream = resource.open()) {
            return parse(
                    GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class),
                    resourcePath,
                    type,
                    fallback
            );
        } catch (IOException exception) {
            TFCGenViewer.LOGGER.warn(
                    "Unable to open {} color gradient resource at {}. Error:\n{}",
                    type,
                    resourcePath,
                    exception
            );
        }
        TFCGenViewer.LOGGER.warn("Unable to parse color gradient definition for {} at {}", type, resourcePath);
        return null;
    }

    @Nullable
    public static ColorGradientDefinition parse(JsonElement json, ResourceLocation resourcePath, String type, @Nullable DoubleToIntFunction fallback) {
        if (json.isJsonObject()) {
            final JsonObject obj = json.getAsJsonObject();
            final DoubleToIntFunction gradientValue;
            if (obj.has("gradient")) {
                gradientValue = parseGradient(obj.get("gradient"), fallback, type, resourcePath);
            } else if (obj.has("reference")) {
                final DoubleToIntFunction ref = reference(obj.get("reference").getAsString());
                if (ref != null) {
                    gradientValue = ref;
                } else {
                    TFCGenViewer.LOGGER.warn("Unknown color gradient reference: {} at {}", obj.get("reference").getAsString(), resourcePath);
                    gradientValue = fallback;
                }
            } else {
                TFCGenViewer.LOGGER.warn("The {} color gradient at {} does not have a 'gradient' property!", type, resourcePath);
                gradientValue = fallback;
            }
            return gradientValue == null ? null : new ColorGradientDefinition(
                    gradientValue,
                    obj.has("key") ? Component.translatable(obj.get("key").getAsString()) : Component.translatable("tfcgenviewer.%s".formatted(type))
            );
        }
        TFCGenViewer.LOGGER.warn(
                "Unable to parse {} color gradient at {}, is not an object!",
                json,
                resourcePath
        );
        return null;
    }

    @Nullable
    public static DoubleToIntFunction parseGradient(JsonElement gradient, @Nullable DoubleToIntFunction fallback, String type, ResourceLocation resourcePath) {
        if (gradient.isJsonPrimitive()) {
            final int color = ColorDefinition.parseColor(gradient, 0xF0000000, type, resourcePath);
            return color == 0xF0000000 ? fallback : value -> color;
        } else if (gradient.isJsonArray()) {
            final JsonArray array = gradient.getAsJsonArray();
            if (array.size() < 3) {
                if (array.isEmpty()) {
                    TFCGenViewer.LOGGER.warn(
                            "Attempted to parse empty {} color gradient array {} at {}",
                            type,
                            gradient,
                            resourcePath
                    );
                    return fallback;
                } else if (array.size() == 1) {
                    final int color = ColorDefinition.parseColor(array.get(0), 0xF0000000, type, resourcePath);
                    return color == 0xF0000000 ? fallback : value -> color;
                } else {
                    final int from = ColorDefinition.parseColor(array.get(0), 0xF0000000, type, resourcePath);
                    final int to = ColorDefinition.parseColor(array.get(1), 0xF0000000, type, resourcePath);
                    if (from == 0xF0000000 || to == 0xF0000000) {
                        TFCGenViewer.LOGGER.warn(
                                "Unable to parse {} color gradient {} at {}",
                                type,
                                array,
                                resourcePath
                        );
                        return fallback;
                    } else {
                        return ImageBuilder.linearGradient(from, to);
                    }
                }
            }
            final int[] colors = new int[array.size()];
            for (int i = 0 ; i < colors.length ; i++) {
                colors[i] = ColorDefinition.parseColor(array.get(i), 0, type, resourcePath);
            }
            return ImageBuilder.multiLinearGradient(colors);
        } else if (gradient.isJsonObject()) {
            final JsonObject json = gradient.getAsJsonObject();
            if (json.has("from") && json.has("to")) {
                final int from = ColorDefinition.parseColor(json.get("from"), 0xF0000000, type, resourcePath);
                final int to = ColorDefinition.parseColor(json.get("to"), 0xF0000000, type, resourcePath);
                if (from == 0xF0000000 || to == 0xF0000000) {
                    TFCGenViewer.LOGGER.warn(
                            "Unable to parse {} color gradient {} at {}",
                            type,
                            json,
                            resourcePath
                    );
                    return fallback;
                } else {
                    return ImageBuilder.linearGradient(from, to);
                }
            }
        }
        TFCGenViewer.LOGGER.warn("Unable to parse color gradient definition for {} at {}", type, resourcePath);
        return fallback;
    }

    @Nullable
    public static DoubleToIntFunction reference(String ref) {
        if (ref.equalsIgnoreCase("blue") || ref.equalsIgnoreCase("ocean")) {
            return ImageBuilder.blue;
        } else if (ref.equalsIgnoreCase("green") || ref.equalsIgnoreCase("land")) {
            return ImageBuilder.green;
        } else if (
                ref.equalsIgnoreCase("climate") ||
                ref.equalsIgnoreCase("temp") ||
                ref.equalsIgnoreCase("temperature") ||
                ref.equalsIgnoreCase("rain") ||
                ref.equalsIgnoreCase("rainfall")
        ) {
            return ImageBuilder.climate;
        } else if (ref.equalsIgnoreCase("volcanic") || ref.equalsIgnoreCase("volcanic_rock")) {
            return ImageBuilder.VOLCANIC_ROCK;
        } else if (ref.equalsIgnoreCase("uplift") || ref.equalsIgnoreCase("uplift_rock") || ref.equalsIgnoreCase("pink")) {
            return ImageBuilder.UPLIFT_ROCK;
        }
        return null;
    }

    public void appendTo(MutableComponent text, boolean end) {
        final MutableComponent colors = Component.empty();
        for (double i : keyValues) {
            colors.append(Component.literal("■").withStyle(style -> style.withColor(ImageBuilder.bgrToRgb(gradient.applyAsInt(i)))));
        }
        text.append(Component.translatable(
                "tfcgenviewer.preview_world.color_key_template",
                colors,
                name
        ));
        if (!end) text.append(CommonComponents.NEW_LINE);
    }
}
