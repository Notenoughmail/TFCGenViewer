package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.IWillAppendTo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.DoubleToIntFunction;

public record ColorGradientDefinition(DoubleToIntFunction gradient, Component name, Component[] tooltips) implements IWillAppendTo {

    public ColorGradientDefinition(DoubleToIntFunction gradient, Component name) {
        this(gradient, name, new Component[]{ name });
    }

    private static final double[] keyValues = new double[] { 0, 0.2, 0.4, 0.6, 0.8, 0.9999999 };

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
        final Component key = Component.translatable(
                json.has("key") ?
                        json.get("key").getAsString() :
                        id.toLanguageKey("tfcgenviewer.gradient")
        );
        return new ColorGradientDefinition(
                gradient,
                key,
                json.has("tooltip_keys") ?
                        getTooltips(json.get("tooltip_keys")) :
                        new Component[] { key }
        );
    }

    private static Component[] getTooltips(JsonElement json) {
        if (json instanceof JsonArray array) {
            if (array.isEmpty()) {
                throw new JsonParseException("tooltip_keys array should have at least one value!");
            }
            final Component[] tooltips = new Component[array.size()];
            for (int i = 0 ; i < tooltips.length ; i++) {
                tooltips[i] = Component.translatable(array.get(i).getAsString());
            }
            return tooltips;
        }
        throw new JsonParseException("tooltip_keys property should be an array of strings!");
    }

    private static final String[] gradientKeys = new String[] { "from", "to" };

    public static DoubleToIntFunction parseGradient(JsonElement gradient) {
        if (gradient.isJsonPrimitive()) {
            final int color = ColorDefinition.parseColor(gradient);
            return value -> color;
        } else if (gradient instanceof JsonObject json) {
            if (ColorDefinition.hasAll(json, gradientKeys)) {
                final int from = ColorDefinition.parseColor(json.get("from"));
                final int to = ColorDefinition.parseColor(json.get("to"));
                return ColorUtil.linearGradient(from, to);
            }
            throw new JsonParseException("A color gradient of an object type needs a 'from' and a 'to' field");
        } else if (gradient instanceof JsonArray array) {
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

    @Override
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

    public int getColor(double d, Int2ObjectOpenHashMap<Component> colorDescriptors) {
        d = Mth.clamp(d, 0D, 0.9999999D);
        final int color = gradient.applyAsInt(d);
        if (!colorDescriptors.containsKey(color)) {
            colorDescriptors.put(color, tooltips[
                    tooltips.length == 1 ?
                            0 :
                            (int) Mth.map(d, 0D, 1D, 0, tooltips.length)
                    ]
            );
        }
        return color;
    }
}
