package io.github.notenoughmail.tfcgenviewer.api.color;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface IDescribeColor {

    void appendTo(MutableComponent text, boolean end);

    default void appendTo(MutableComponent text) {
        appendTo(text, false);
    }

    default Component colorBlock(int argbColor) {
        return Component.literal("■").withColor(argbColor);
    }
}
