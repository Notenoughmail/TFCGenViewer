package io.github.notenoughmail.tfcgenviewer.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;

public class ColorTooltips extends Int2ObjectOpenHashMap<Component> {

    public static final Component NO_TOOLTIP = Component.translatable("tfcgenviewer.widget.preview_pane.no_tooltip");

    public ColorTooltips() {
        defaultReturnValue(NO_TOOLTIP);
    }
}
