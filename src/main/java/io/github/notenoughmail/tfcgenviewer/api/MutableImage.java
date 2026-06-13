package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;

/**
 * An abstract wrapper around a {@link com.mojang.blaze3d.platform.NativeImage NativeIamge} that can be included in common code
 */
public interface MutableImage {

    /**
     * If the given coordinate is valid
     */
    boolean inRange(int z);

    /**
     * If the image is allocated, and thus can be edited
     */
    boolean isAllocated();

    /**
     * Sets the pixel at the given coordinates to the given color,
     * blending non {@code 0xFF} alpha values and ensuring the pixel
     * is within the bounds of the image
     */
    void setPixel(int x, int y, int abgrColor);

    /**
     * Sets the pixel at the given coordinates to the given color, ensuring the pixel is within the bounds of the image
     */
    default void setPixel(int x, int y, ColorDefinition color) {
        setPixel(x, y, color.abgr());
    }

    /**
     * Draws a vertical line from {@code x0} to {@code x1} at the given {@code y} in the given color. Non-{@code 0xFF}
     * alphas will be blended. Coordinates beyond the bounds of the image will be skipped
     * @param width How many additional pixels, <i>above and below</i> of {@code y}, to draw. Values ≤ 0 are treated as 0
     */
    void hLine(int x0, int x1, int y, int width, int abgrColor);

    /**
     * Draws a horizontal line from {@code y0} to {@code y1} at the given {@code x} in the given color. Non-{@code 0xFF}
     * alphas will be blended. Coordinates beyond the bounds of the image will be skipped
     * @param width How many additional pixels, <i>left and right</i> of {@code x}, to draw. Values ≤ 0 are treated as 0
     */
    void vLine(int y0, int y1, int x, int width, int abgrColor);

    /**
     * Gets the ABGR color at the given pixel. Will be {@code 0} if outside the image bounds or the image is unallocated
     */
    int getABGRColor(int x, int y);
}
