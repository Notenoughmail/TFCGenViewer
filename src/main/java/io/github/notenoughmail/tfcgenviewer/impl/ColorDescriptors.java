package io.github.notenoughmail.tfcgenviewer.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;

public class ColorDescriptors extends Int2ObjectOpenHashMap<Component> {

    public static final Component NO_TOOLTIP = Component.translatable("tfcgenviewer.widget.preview_pane.no_tooltip");

    public static final ColorDescriptors EMPTY = new ColorDescriptors(false);

    private final boolean available;

    public ColorDescriptors(boolean available) {
        this.available = available;
        defaultReturnValue(NO_TOOLTIP);
    }

    public ColorDescriptors() {
        this(true);
    }

    public boolean available() {
        return available;
    }
}
