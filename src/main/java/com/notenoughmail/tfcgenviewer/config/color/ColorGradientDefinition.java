package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.IWillAppendTo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.DoubleToIntFunction;

public record ColorGradientDefinition(DoubleToIntFunction gradient, Component name) implements IWillAppendTo {

    private static final double[] keyValues = new double[] { 0, 0.2, 0.4, 0.6, 0.8, 0.999 };

    public static ColorGradientDefinition parse(ResourceLocation id, JsonObject json) {
        final DoubleToIntFunction gradient;
        if (json.has("gradient")) {
            gradient = parseGradient(json.get("gradient"));
        } else if (json.has("reference")) {
            final DoubleToIntFunction ref = reference(json.get("reference").getAsString());
            if (ref == null) {
                throw new JsonParseException("Unknown reference: %s".formatted(json.get("reference")));
            }
            gradient = ref;
        } else {
            throw new JsonParseException("Color gradient definition requires a 'gradient' or 'reference' field");
        }
        return new ColorGradientDefinition(
                gradient,
                json.has("key") ?
                        Component.translatable(json.get("key").getAsString()) :
                        Component.translatable(id.toLanguageKey("tfcgenviewer.gradient"))

        );
    }

    private static final String[] gradientKeys = new String[] { "from", "to" };

    public static DoubleToIntFunction parseGradient(JsonElement gradient) {
        if (gradient.isJsonPrimitive()) {
            final int color = ColorDefinition.parseColor(gradient);
            return value -> color;
        } else if (gradient.isJsonObject()) {
            final JsonObject json = gradient.getAsJsonObject();
            if (ColorDefinition.hasAll(json, gradientKeys)) {
                final int from = ColorDefinition.parseColor(json.get("from"));
                final int to = ColorDefinition.parseColor(json.get("to"));
                return ColorUtil.linearGradient(from, to);
            }
            throw new JsonParseException("A color gradient of an object type needs a 'from' and a 'to' field");
        } else if (gradient.isJsonArray()) {
            final JsonArray array = gradient.getAsJsonArray();
            return switch (array.size()) {
                case 0 -> throw new JsonParseException("A color gradient array cannot be empty!");
                case 1 -> {
                    final int color = ColorDefinition.parseColor(array.get(0));
                    yield value -> color;
                }
                case 2 -> {
                    final int from = ColorDefinition.parseColor(array.get(0));
                    final int to = ColorDefinition.parseColor(array.get(1));
                    yield ColorUtil.linearGradient(from, to);
                }
                default -> {
                    final int[] colors = new int[array.size()];
                    for (int i = 0 ; i < colors.length ; i++) {
                        colors[i] = ColorDefinition.parseColor(array.get(i));
                    }
                    yield ColorUtil.multiLinearGradient(colors);
                }
            };
        }
        throw new JsonParseException("A color gradient must either be an object, an array, or a singular color");
    }

    @Nullable
    public static DoubleToIntFunction reference(String ref) {
        return switch (ref.toLowerCase(Locale.ROOT)) {
            case "blue", "ocean" -> ColorUtil.blue;
            case "green", "land" -> ColorUtil.green;
            case "climate", "temp", "temperature", "rain", "rainfall" -> ColorUtil.climate;
            case "volcanic", "volcanic_rock" -> ColorUtil.volcanic;
            case "uplift", "uplift_rock" -> ColorUtil.uplift;
            case "gray", "grey", "grayscale", "greyscale" -> ColorUtil.grayscale;
            case "random" -> ColorUtil.linearGradient(ColorUtil.randomColor(), ColorUtil.randomColor());
            default -> null;
        };
    }

    public void appendTo(MutableComponent text, boolean end) {
        final MutableComponent colors = Component.empty();
        for (double i : keyValues) {
            colors.append(Component.literal("■").withStyle(style -> style.withColor(ColorUtil.bgrToRgb(gradient.applyAsInt(i)))));
        }
        text.append(Component.translatable(
                "tfcgenviewer.preview_world.color_key_template",
                colors,
                name
        ));
        if (!end) text.append(CommonComponents.NEW_LINE);
    }
}
