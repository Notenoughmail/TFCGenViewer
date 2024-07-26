package com.notenoughmail.tfcgenviewer.config;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.util.function.Supplier;

public class RockTypeColors {
    private static final ColorGradientDefinition[] DEFINITIONS = new ColorGradientDefinition[4];

    static {
        DEFINITIONS[0] = new ColorGradientDefinition(
                ColorUtil.blue,
                Component.translatable("tfcgenviewer.rock_type.oceanic")
        );
        DEFINITIONS[1] = new ColorGradientDefinition(
                ColorUtil.volcanic,
                Component.translatable("tfcgenviewer.rock_type.volcanic")
        );
        DEFINITIONS[2] = new ColorGradientDefinition(
                ColorUtil.green,
                Component.translatable("tfcgenviewer.rock_type.land")
        );
        DEFINITIONS[3] = new ColorGradientDefinition(
                ColorUtil.uplift,
                Component.translatable("tfcgenviewer.rock_type.uplift")
        );
    }

    public static int apply(int type, double value) {
        return DEFINITIONS[type].gradient().applyAsInt(value);
    }

    public static void assignGradient(ResourceLocation resourcePath, Resource resource) {
        if (resourcePath.getNamespace().equals(TFCGenViewer.ID)) {
            switch (resourcePath.getPath()) {
                case "tfcgenviewer/rock_types/oceanic.json" ->
                        DEFINITIONS[0] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.oceanic", ColorUtil.blue);
                case "tfcgenviewer/rock_types/volcanic.json" ->
                        DEFINITIONS[1] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.volcanic", ColorUtil.volcanic);
                case "tfcgenviewer/rock_types/land.json" ->
                        DEFINITIONS[2] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.land", ColorUtil.green);
                case "tfcgenviewer/rock_types/uplift.json" ->
                        DEFINITIONS[3] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.uplift", ColorUtil.uplift);
            }
        }
    }

    public static Supplier<Component> colorKey() {
        return () -> {
            final MutableComponent key = Component.empty();
            DEFINITIONS[2].appendTo(key);
            DEFINITIONS[0].appendTo(key);
            DEFINITIONS[1].appendTo(key);
            DEFINITIONS[3].appendTo(key, true);
            return key;
        };
    }
}
