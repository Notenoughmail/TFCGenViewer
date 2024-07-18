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
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.util.FastColor.ABGR32.color;

public class RockColors {

    private static final Gson GSON = new Gson();
    private static final Map<Block, Definition> COLORS = new IdentityHashMap<>();
    private static final List<Definition> SORTED_COLORS = new ArrayList<>();
    private static Definition UNKNOWN = new Definition(
            color(255, 227, 88, 255),
            Component.translatable("tfcgenviewer.rock.unknown")
    );

    public static Definition getUnknown() {
        return UNKNOWN;
    }

    public static void clear() {
        COLORS.clear();
        SORTED_COLORS.clear();
    }

    public static void assignColor(ResourceLocation resourcePath, Resource resource) {
        final Definition def = Definition.parse(resourcePath, resource);
        if (def != null) {
            if (resourcePath.getNamespace().equals(TFCGenViewer.ID) && resourcePath.getPath().equals("tfcgenviewer/rocks/unknown.json")) {
                UNKNOWN = def;
            } else {
                final ResourceLocation id = resourcePath.withPath(p -> p.substring(19, p.length() - 5));
                final Block block = ForgeRegistries.BLOCKS.getValue(id);
                if (block != null) {
                    COLORS.put(block, def);
                    SORTED_COLORS.add(def);
                } else {
                    TFCGenViewer.LOGGER.warn("Unable to assign rock color to unknown block: {}", id);
                }
            }
        }
    }

    public static int get(Block raw) {
        final Definition def = COLORS.get(raw);
        if (def != null) {
            return def.color();
        }
        return UNKNOWN.color();
    }

    public static void forEach(Consumer<Definition> def) {
        SORTED_COLORS.sort(Definition::compareTo);
        SORTED_COLORS.forEach(def);
    }

    public record Definition(int color, Component name) implements Comparable<Definition> {

        public static Definition parse(ResourceLocation resourcePath, Resource resource) {
            try (InputStream stream = resource.open()) {
                final JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
                final Component name = json.has("key") ? Component.translatable(json.get("key").getAsString()) : Component.translatable("tfcgenviewer.rock.unknown");
                int colorValue = UNKNOWN.color();
                if (json.has("color")) {
                    final JsonElement color = json.get("color");
                    if (color.isJsonObject()) {
                        final JsonObject value = color.getAsJsonObject();
                        if (value.has("r") && value.has("g") && value.has("b")) {
                            colorValue = FastColor.ABGR32.color(
                                    255,
                                    value.get("b").getAsInt(),
                                    value.get("g").getAsInt(),
                                    value.get("r").getAsInt()
                            );
                        } else if (value.has("h") && value.has("s") && value.has("v")) {
                            colorValue = ImageBuilder.rgbToBgr(Mth.hsvToRgb(
                                    value.get("h").getAsFloat(),
                                    value.get("s").getAsFloat(),
                                    value.get("v").getAsFloat()
                            ));
                        } else {
                            TFCGenViewer.LOGGER.warn("Unable to parse rock color value: {}", value);
                        }
                    } else if (color.isJsonPrimitive()) {
                        final JsonPrimitive primitive = color.getAsJsonPrimitive();
                        if (primitive.isNumber()) {
                            colorValue = ImageBuilder.rgbToBgr(primitive.getAsInt());
                        } else if (primitive.isString()) {
                            colorValue = ImageBuilder.rgbToBgr(Integer.parseInt(primitive.getAsString(), 16));
                        }
                    } else {
                        TFCGenViewer.LOGGER.warn("Unable to parse rock color: {},", color);
                    }
                } else {
                    TFCGenViewer.LOGGER.warn("Rock color at {} does not have a 'color' property!", resourcePath);
                }
                return new Definition(colorValue, name);
            } catch (IOException exception) {
                TFCGenViewer.LOGGER.warn(
                        "Unable to open rock color resource at {}. Error:\n{}",
                        resourcePath,
                        exception
                );
            } catch (NumberFormatException exception) {
                TFCGenViewer.LOGGER.warn(
                        "Unable to parse number for rock color at {}. Error:\n{}",
                        resourcePath,
                        exception
                );
            }
            return null;
        }

        public void appendTo(MutableComponent text, boolean end) {
            text.append(Component.translatable(
                    "tfcgenviewer.preview_world.visualizer_type.rocks.color_key_template",
                    Component.literal("■").withStyle(style -> style.withColor(ImageBuilder.bgrToRgb(color))),
                    name
            ));
            if (!end) text.append(CommonComponents.NEW_LINE);
        }

        @Override
        public int compareTo(@NotNull RockColors.Definition other) {
            final String thisString = name.getString();
            final String otherString = other.name().getString();
            return thisString.compareTo(otherString);
        }
    }
}
