package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A group of {@link ImageSize}s
 */
public interface IScale<S extends ImageSize> {

    /**
     * The number of blocks a pixel in a drawn image represents
     */
    int blocksPerPixel();

    /**
     * Format the size
     */
    Component formatSize(S size);

    /**
     * A list of all sizes this scale possesses
     */
    List<? extends S> sizes();

    /**
     * A codec for the sizes, used when creating the slider in the preview screen
     */
    Codec<S> codec();
}
