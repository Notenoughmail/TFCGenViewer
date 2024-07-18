package com.notenoughmail.tfcgenviewer.config;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.util.FastColor.ABGR32.color;

public class RockColors {

    private static final Map<Block, ColorDefinition> COLORS = new IdentityHashMap<>();
    private static final List<ColorDefinition> SORTED_COLORS = new ArrayList<>();
    private static ColorDefinition UNKNOWN = new ColorDefinition(
            color(255, 227, 88, 255),
            Component.translatable("tfcgenviewer.rock.unknown"),
            100
    );

    public static void clear() {
        COLORS.clear();
        SORTED_COLORS.clear();
    }

    public static void assignColor(ResourceLocation resourcePath, Resource resource) {
        final ColorDefinition def = ColorDefinition.parse(resourcePath, resource, "rock", UNKNOWN.color());
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
        final ColorDefinition def = COLORS.get(raw);
        if (def != null) {
            return def.color();
        }
        return UNKNOWN.color();
    }

    public static Supplier<Component> colorKey() {
        return () -> {
            final MutableComponent key = Component.empty();
            SORTED_COLORS.sort(ColorDefinition::compareTo);
            SORTED_COLORS.forEach(def -> def.appendTo(key, false));
            UNKNOWN.appendTo(key, true);
            return key;
        };
    }
}
