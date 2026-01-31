package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * {@link ImageSize}s at a chunk scale ranging from 32 to 1024 chunks in powers of 2
 */
public enum ChunkSize implements ImageSize {
    _0,
    _1,
    _2,
    _3,
    _4,
    _5
    ;

    public static final List<ChunkSize> SIZES = List.of(values());
    public static final Codec<ChunkSize> CODEC = Codec.intRange(0, SIZES.size() - 1).xmap(SIZES::get, Enum::ordinal);

    private final int size, lineWidth;
    private final Component display;

    ChunkSize() {
        size = 2 << (ordinal() + 4);
        lineWidth = size >> 8;
        display = Component.translatable("tfcgenviewer.unit.chunk", size);
    }

    @Override
    public int sizeInPixels() {
        return size;
    }

    @Override
    public int lineWidth() {
        return lineWidth;
    }

    public Component display() {
        return display;
    }
}
