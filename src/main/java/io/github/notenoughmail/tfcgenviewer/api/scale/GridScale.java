package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

public enum GridScale implements IScale {
    _0,
    _1,
    _2,
    _3,
    _4,
    _5,
    _6
    ;

    public static final List<GridScale> SCALES = List.of(values());
    public static final Codec<GridScale> CODEC = Codec.intRange(0, SCALES.size() - 1).xmap(SCALES::get, Enum::ordinal);

    private final int size, lineWidth;
    private final Component display;

    GridScale() {
        size = 2 << (ordinal() + 4); // == Math.pow(x, scale + 5)
        lineWidth = size >> 9;
        display = Component.translatable("tfcgenviewer.preview_world.km", "%.1f".formatted(size * 128 / 1000F));
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
