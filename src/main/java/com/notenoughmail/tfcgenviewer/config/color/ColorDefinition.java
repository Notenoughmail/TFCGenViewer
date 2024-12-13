package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.IWillAppendTo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public record ColorDefinition(int color, Component name, int sort, Component tooltip) implements Comparable<ColorDefinition>, IWillAppendTo {

    public ColorDefinition(int color, Component name, int sort) {
        this(color, name, sort, name);
    }

    public static ColorDefinition parse(JsonObject json, String fallbackKey) {
        if (json.has("color")) {
            final Component key = Component.translatable(
                    json.has("key") ?
                            json.get("key").getAsString() :
                            fallbackKey
            );
            return new ColorDefinition(
                    parseColor(json.get("color")),
                    key,
                    json.has("sort") ?
                            json.get("sort").getAsInt() :
                            100,
                    json.has("tooltip_key") ?
                            Component.translatable(json.get("tooltip_key").getAsString()) :
                            key
            );
        }
        throw new JsonParseException("Color definition requires a 'color' field to be present");
    }

    public static ColorDefinition parse(ResourceLocation id, JsonObject json) {
        return parse(json, id.toLanguageKey("tfcgenviewer.color"));
    }

    private static final String[][] objColorKeys = new String[][] {
            { "r", "g", "b" },
            { "h", "s", "v" }
    };

    public static int parseColor(JsonElement color) {
        if (color instanceof JsonObject value) {
            if (hasAll(value, objColorKeys[0])) {
                return FastColor.ABGR32.color(
                        255,
                        value.get("b").getAsInt(),
                        value.get("g").getAsInt(),
                        value.get("r").getAsInt()
                );
            } else if (hasAll(value, objColorKeys[1])) {
                return ColorUtil.rgbToBgr(Mth.hsvToRgb(
                        value.get("h").getAsFloat(),
                        value.get("s").getAsFloat(),
                        value.get("v").getAsFloat()
                ));
            }
            throw new JsonParseException("A color of an object type should have fields of either [r, g, and b] or [h, s, and v]");
        } else if (color instanceof JsonPrimitive prim) {
            if (prim.isNumber()) {
                return ColorUtil.rgbToBgr(prim.getAsInt());
            } else if (prim.isString()) {
                return ColorUtil.rgbToBgr(Integer.parseInt(prim.getAsString(), 16));
            }
            throw new JsonParseException("Color should be an object, a string, or an integer");
        } else if (color.isJsonNull()) {
            return ColorUtil.randomColor();
        }
        throw new JsonParseException("Color should be an object, a string, or an integer");
    }

    public static boolean hasAll(JsonObject value, String[] fields) {
        for (String field : fields) {
            if (!value.has(field)) {
                return false;
            }
        }
        return true;
    }

    public void appendTo(MutableComponent text, boolean end) {
        text.append(Component.translatable(
                "tfcgenviewer.preview_world.color_key_template",
                Component.literal("■").withStyle(style -> style.withColor(ColorUtil.bgrToRgb(color))),
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

    public int color(Int2ObjectOpenHashMap<Component> colorDescriptors) {
        colorDescriptors.putIfAbsent(color, tooltip);
        return color;
    }
}
