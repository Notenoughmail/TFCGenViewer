package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;

public sealed class ColorTooltips permits ColorTooltips.Synchronous {

    public static ColorTooltips of(boolean parallel) {
        return parallel ? new Synchronous() : new ColorTooltips();
    }

    public static final Component NO_TOOLTIP = Component.translatable("tfcgenviewer.widget.preview_pane.no_tooltip");

    protected final Int2ObjectOpenHashMap<Component> mapping;

    protected ColorTooltips() {
        mapping = new Int2ObjectOpenHashMap<>();
        mapping.defaultReturnValue(NO_TOOLTIP);
    }

    public void addColorTooltip(ColorDefinition color) {
        addTooltip(color.abgr(), color.getTooltip());
    }

    /**
     * Assign the tooltip to the color if the color does not already have a tooltip
     */
    public void addTooltip(int abgrColor, Component tooltip) {
        mapping.putIfAbsent(abgrColor, tooltip);
    }

    /**
     * If the given color already has a tooltip associated with it
     */
    public boolean hasColor(int abgrColor) {
        return mapping.containsKey(abgrColor);
    }

    public Component get(int abgrColor) {
        return mapping.get(abgrColor);
    }

    public static final class Synchronous extends ColorTooltips {

        @Override
        public void addTooltip(int abgrColor, Component tooltip) {
            synchronized (mapping) {
                super.addTooltip(abgrColor, tooltip);
            }
        }

        @Override
        public boolean hasColor(int abgrColor) {
            synchronized (mapping) {
                return super.hasColor(abgrColor);
            }
        }

        @Override
        public Component get(int abgrColor) {
            synchronized (mapping) {
                return super.get(abgrColor);
            }
        }
    }
}
