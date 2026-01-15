package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public enum GridSize implements ImageSize {
    _0,
    _1,
    _2,
    _3,
    _4,
    _5,
    _6
    ;

    public static final List<GridSize> SIZES = List.of(values());
    public static final Codec<GridSize> CODEC = Codec.intRange(0, SIZES.size() - 1).xmap(SIZES::get, Enum::ordinal);

    private final int size, lineWidth;
    private final Component display;

    GridSize() {
        size = 2 << (ordinal() + 4); // == Math.pow(x, scale + 5)
        lineWidth = size >> 9;
        display = Component.translatable("tfcgenviewer.unit.kilometer", "%.1f".formatted(size * 128 / 1000F));
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
