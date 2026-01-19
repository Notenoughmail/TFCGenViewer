package io.github.notenoughmail.tfcgenviewer.api.color;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;

public sealed interface DescribableColor permits ColorDefinition, ColorGradientDefinition {

    /**
     * Appends this color key to a text component for use in color keys
     * @param end If this color is the final object being added to the color key
     */
    void appendTo(MutableComponent text, boolean end);

    /**
     * Appends this color to a text component for use in color keys
     */
    default void appendTo(MutableComponent text) {
        appendTo(text, false);
    }

    @ApiStatus.Internal
    default Component colorBlock(int argbColor) {
        return Component.literal("■").withColor(argbColor);
    }
}
