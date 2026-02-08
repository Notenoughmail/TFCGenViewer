package io.github.notenoughmail.tfcgenviewer.api.cache;

import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RockCache<T> {

    public final T innerCache;
    private boolean unknownEncountered;
    private final Set<ColorDefinition> encounteredRocks;

    public RockCache(T innerCache) {
        this.innerCache = innerCache;
        encounteredRocks = new HashSet<>();
    }

    public ColorDefinition getColor(Block block) {
        final ColorDefinition color = Colors.ROCK_COLORS.get(BuiltInRegistries.BLOCK.getKey(block));
        if (color == null) {
            unknownEncountered = true;
            return Colors.ROCK_COLORS.unknown();
        }
        encounteredRocks.add(color);
        return color;
    }

    public Component colorKey() {
        final MutableComponent key = Component.empty();
        final Iterator<ColorDefinition> iter = encounteredRocks.stream()
                .sorted()
                .iterator();
        while (iter.hasNext()) {
            iter.next().appendTo(key, !unknownEncountered && !iter.hasNext());
        }
        if (unknownEncountered) {
            Colors.ROCK_COLORS.unknown().appendTo(key, true);
        }
        return key;
    }
}
