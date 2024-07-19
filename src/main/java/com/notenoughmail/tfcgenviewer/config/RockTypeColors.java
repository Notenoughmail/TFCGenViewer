package com.notenoughmail.tfcgenviewer.config;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
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
                ImageBuilder.blue,
                Component.translatable("tfcgenviewer.rock_type.oceanic")
        );
        DEFINITIONS[1] = new ColorGradientDefinition(
                ImageBuilder.VOLCANIC_ROCK,
                Component.translatable("tfcgenviewer.rock_type.volcanic")
        );
        DEFINITIONS[2] = new ColorGradientDefinition(
                ImageBuilder.green,
                Component.translatable("tfcgenviewer.rock_type.land")
        );
        DEFINITIONS[3] = new ColorGradientDefinition(
                ImageBuilder.UPLIFT_ROCK,
                Component.translatable("tfcgenviewer.rock_type.uplift")
        );
    }

    public static int apply(int type, double value) {
        return DEFINITIONS[type].gradient().applyAsInt(value);
    }

    public static void assignGradient(ResourceLocation resourcePath, Resource resource) {
        if (resourcePath.getNamespace().equals(TFCGenViewer.ID)) {
            final int type = switch (resourcePath.getPath()) {
                case "tfcgenviewer/rock_types/oceanic.json" -> 0;
                case "tfcgenviewer/rock_types/volcanic.json" -> 1;
                case "tfcgenviewer/rock_types/land.json" -> 2;
                case "tfcgenviewer/rock_types/uplift.json" -> 3;
                default -> -1;
            };
            switch (type) {
                case 0 -> DEFINITIONS[0] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.oceanic", ImageBuilder.blue);
                case 1 -> DEFINITIONS[1] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.volcanic", ImageBuilder.VOLCANIC_ROCK);
                case 2 -> DEFINITIONS[2] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.land", ImageBuilder.green);
                case 3 -> DEFINITIONS[3] = ColorGradientDefinition.parse(resourcePath, resource, "rock_type.uplift", ImageBuilder.UPLIFT_ROCK);
            }
        }
    }

    public static Supplier<Component> colorKey() {
        return () -> {
            final MutableComponent key = Component.empty();
            DEFINITIONS[2].appendTo(key, false);
            DEFINITIONS[0].appendTo(key, false);
            DEFINITIONS[1].appendTo(key, false);
            DEFINITIONS[3].appendTo(key, true);
            return key;
        };
    }
}
