package com.notenoughmail.tfcgenviewer.config.color;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.util.FastColor.ABGR32.color;

public class RockColors {

    private static final Map<Block, ColorDefinition> COLORS = new IdentityHashMap<>();
    private static final List<ColorDefinition> SORTED_COLORS = new ArrayList<>();
    private static ColorDefinition UNKNOWN = new ColorDefinition(
            color(255, 227, 88, 255),
            Component.translatable("rock.tfcgenviewer.unknown"),
            100
    );

    public static void clear() {
        COLORS.clear();
        SORTED_COLORS.clear();
    }

    public static void assignColor(ResourceLocation resourcePath, Resource resource) {
        final ResourceLocation id = resourcePath.withPath(p -> p.substring(19, p.length() - 5));
        final ColorDefinition def = ColorDefinition.parse(resourcePath, resource, "rock", UNKNOWN.color(), id.withPath(p -> p.replace('/', '.')).toLanguageKey("rock"));
        if (def != null) {
            if (resourcePath.getNamespace().equals(TFCGenViewer.ID) && resourcePath.getPath().equals("tfcgenviewer/rocks/unknown.json")) {
                UNKNOWN = def;
            } else {
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
            SORTED_COLORS.forEach(def -> def.appendTo(key));
            UNKNOWN.appendTo(key, true);
            return key;
        };
    }
}
