package io.github.notenoughmail.tfcgenviewer.api.scale;

/**
 * The size of image
 */
public interface ImageSize {

    /**
     * The edge length, in pixels, of an image of this size
     */
    int sizeInPixels();

    /**
     * The value to pass as the {@code width} param in {@link io.github.notenoughmail.tfcgenviewer.api.MutableImage#hLine(int, int, int, int, int) MutableImage#hLine}
     * and {@link io.github.notenoughmail.tfcgenviewer.api.MutableImage#vLine(int, int, int, int, int) MutableImage#vLine}
     */
    int lineWidth();
}
