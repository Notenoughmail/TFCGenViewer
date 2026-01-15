package com.notenoughmail.tfcgenviewer.util.preview;

import net.minecraft.network.chat.MutableComponent;

public interface IWillAppendTo {

    void appendTo(MutableComponent text, boolean end);

    default void appendTo(MutableComponent text) {
        appendTo(text, false);
    }
}
