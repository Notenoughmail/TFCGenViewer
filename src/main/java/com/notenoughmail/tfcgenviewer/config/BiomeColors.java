package com.notenoughmail.tfcgenviewer.config;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.layer.TFCLayers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.util.FastColor.ABGR32.color;

public class BiomeColors {

    private static final Map<BiomeExtension, ColorDefinition> COLORS = new IdentityHashMap<>();
    private static final List<ColorDefinition> SORTED_COLORS = new ArrayList<>();

    private static ColorDefinition UNKNOWN = new ColorDefinition(
            color(255, 170, 170, 170),
            Component.translatable("tfcgenviewer.biome.unknown"),
            100
    );

    public static void clear() {
        COLORS.clear();
        SORTED_COLORS.clear();
    }

    public static void assignColor(ResourceLocation resourcePath, Resource resource) {
        final ColorDefinition def = ColorDefinition.parse(resourcePath, resource, "biome", UNKNOWN.color());
        if (def != null) {
            if (resourcePath.getNamespace().equals(TFCGenViewer.ID) && resourcePath.getPath().equals("tfcgenviewer/biomes/unknown.json")) {
                UNKNOWN = def;
            } else {
                final ResourceLocation id = resourcePath.withPath(p -> p.substring(20, p.length() - 5));
                final BiomeExtension ext = TFCBiomes.getById(id);
                if (ext != null) {
                    COLORS.put(ext, def);
                    SORTED_COLORS.add(def);
                } else {
                    TFCGenViewer.LOGGER.warn("Unable to assign rock color to unknown biome extension: {}", id);
                }
            }
        }
    }

    public static int get(int biome) {
        final ColorDefinition def = COLORS.get(TFCLayers.getFromLayerId(biome));
        if (def != null) {
            return def.color();
        }
        return UNKNOWN.color();
    }

    public static Supplier<Component> colorKey() {
        return () -> {
            final MutableComponent key = Component.empty();
            SORTED_COLORS.stream().distinct().sorted().forEach(def -> def.appendTo(key, false));
            UNKNOWN.appendTo(key, true);
            return key;
        };
    }
}
