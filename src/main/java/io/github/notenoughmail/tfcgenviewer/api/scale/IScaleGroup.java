package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A group of {@link IScale}s
 */
public interface IScaleGroup<S extends IScale> {

    /**
     * The number of blocks a pixel in a drawn image represents
     */
    int blocksPerPixel();

    /**
     * Format the scale
     */
    Component formatScale(S scale);

    /**
     * A list of all scales this scale group possesses
     */
    List<? extends S> scales();

    // Why did I write this down???
    Codec<S> codec();
}
