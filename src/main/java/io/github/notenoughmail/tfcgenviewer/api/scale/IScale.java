package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.BlockEvaluationFunction;
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
     * Convert the given pixel resolution position to block scale
     * @param pixelResolutionPosition The coordinate in pixel resolution
     * @param center If the returned block coordinate should be shifted to be in the mid-point of the pixel
     */
    default int pixelResolutionToBlock(int pixelResolutionPosition, boolean center) {
        int blockRes = pixelResolutionPosition * blocksPerPixel();
        if (center) blockRes += blocksPerPixel() / 2;
        return blockRes;
    }

    /**
     * Perform an action at block scale
     */
    default <T> T evaluateAtBlockPosition(boolean center, int pixelResolutionX, int pixelResolutionZ, BlockEvaluationFunction<T> function) {
        return function.evaluate(pixelResolutionToBlock(pixelResolutionX, center), pixelResolutionToBlock(pixelResolutionZ, center));
    }

    /**
     * Format the size as text
     */
    Component formatSize(S size);

    S getDefault();

    /**
     * A list of all sizes this scale possesses
     */
    List<S> sizes();

    /**
     * A codec for the sizes, used when creating the slider in the preview screen
     */
    Codec<S> codec();
}
