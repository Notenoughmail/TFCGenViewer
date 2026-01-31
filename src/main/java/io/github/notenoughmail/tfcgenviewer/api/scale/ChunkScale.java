package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A scale of 1 pixel = 1 chunk (16 blocks)
 */
public enum ChunkScale implements IScale<ChunkSize> {
    INSTANCE;

    @Override
    public int blocksPerPixel() {
        return 16;
    }

    @Override
    public Component formatSize(ChunkSize size) {
        return size.display();
    }

    @Override
    public ChunkSize getDefault() {
        return ChunkSize._3;
    }

    @Override
    public List<ChunkSize> sizes() {
        return ChunkSize.SIZES;
    }

    @Override
    public Codec<ChunkSize> codec() {
        return ChunkSize.CODEC;
    }
}
