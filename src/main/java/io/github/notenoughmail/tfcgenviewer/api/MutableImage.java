package io.github.notenoughmail.tfcgenviewer.api;

/**
 * An abstract wrapper around a {@link com.mojang.blaze3d.platform.NativeImage NativeIamge}
 */
public interface MutableImage {

    /**
     * If the given coordinate is valid
     */
    boolean inRange(int z);

    /**
     * If the image is allocated
     */
    boolean isAllocated();

    /**
     * Closes the image
     */
    void close();

    /**
     * Exports the image to the given file within the {@code screenshots/tfcgenviewer/} folder
     */
    void export(String name);

    /**
     * Sets the pixel at the given coordinates to the given color,
     * blending non {@code 0xFF} alpha values and ensuring the pixel
     * is within the bounds of the image
     */
    void setPixel(int x, int y, int abgrColor);

    /**
     * Draws a vertical line from {@code x0} to {@code x1} at the given {@code y}
     * in the given color. Non-{@code 0xFF} alphas will be blended
     * @param width The width to draw the line as, values ≤ 0 are treated as 0.
     *              How many additional pixels, <i>above and below</i>, to draw
     */
    void hLine(int x0, int x1, int y, int width, int abgrColor);

    /**
     * Draws a horizontal line from {@code y0} to {@code y1} at the given {@code x}
     * in the given color. Non-{@code 0xFF} alphas will be blended
     * @param width The width to draw the line as, values ≤ 0 are treated as 0.
     *              How many additional pixels, <i>left and right</i>, to draw
     */
    void vLine(int y0, int y1, int x, int width, int abgrColor);

    /**
     * Gets the ABGR color at the given pixel. Will be transparent if outside the image bounds or unallocated
     */
    int getABGRColor(int x, int y);
}
